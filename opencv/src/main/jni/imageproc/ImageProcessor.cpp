//
// Created by saki on 16/03/01.
//

#if 0	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
//	#define USE_LOGALL
	#define USE_LOGD
	#undef LOG_NDEBUG
	#undef NDEBUG
#endif

#include <jni.h>
#include <stdlib.h>
#include <algorithm>
#include <string>
#include <vector>
#include <sstream>
//#include <GLES2/gl2.h>
//#include <GLES2/gl2ext.h>
#include <GLES3/gl3.h>		// API>=18
#include <GLES3/gl3ext.h>	// API>=18

#include "utilbase.h"
#include "common_utils.h"
#include "JNIHelp.h"
#include "Errors.h"
#include "glutils.h"

#include "ImageProcessor.h"

#define USE_PBO 1

struct fields_t {
    jmethodID callFromNative;
    jmethodID arrayID;	// ByteBufferがdirectBufferでない時にJava側からbyte[]を取得するためのメソッドid
};
static fields_t fields;

// 繰り返し使うのでstaticに生成しておく
static const cv::Scalar COLOR_ORANGE = cv::Scalar(255, 127, 0);
static const cv::Scalar COLOR_RED = cv::Scalar(255, 0, 0);
static const cv::Scalar COLOR_GREEN = cv::Scalar(255, 0, 0);
static const cv::Scalar COLOR_BLUE = cv::Scalar(0, 0, 255);
static const cv::Scalar COLOR_WHITE = cv::Scalar(255, 255, 255);
static const cv::Scalar COLOR_BLACK = cv::Scalar(0, 0, 0);
static const cv::Scalar COLOR_YELLOW = cv::Scalar(255, 255, 0);

using namespace android;

ImageProcessor::ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz)
:	mWeakThiz(env->NewGlobalRef(weak_thiz_obj)),
	mClazz((jclass)env->NewGlobalRef(clazz)),
	mIsRunning(false),
	pbo_ix(0)
{
	// 結果形式
	mParam.mResultFrameType = RESULT_FRAME_TYPE_DST_LINE;
	// 色抽出するかどうか
	mParam.mEnableExtract = false;
	// 輪郭近似
	mParam.mApproxType = APPROX_RELATIVE;
	mParam.mApproxFactor = 0.01;
	// Canny
	mParam.mEnableCanny = true;
	mParam.mCannythreshold1 = 50.0;	// エッジ検出する際のしきい値
	mParam.mCannythreshold2 = 200.0;
	// 基準図形との類似性の最大値
	mParam.mMaxAnalogous = 50.0;
	// H(色相)は制限なし, S(彩度)は0-約5%, 2:V(明度)は約80-100%
	mExtractColorHSV[0] = 0;	// H下限
	mExtractColorHSV[1] = 0;	// S下限
	mExtractColorHSV[2] = 200;	// V下限
	mExtractColorHSV[3] = 180;	// H下限
	mExtractColorHSV[4] = 10;	// S上限
	mExtractColorHSV[5] = 255;	// V上限

#if USE_PBO
	pbo[0] = pbo[1] = 0;
#endif
}

ImageProcessor::~ImageProcessor() {
	clearFrames();
}

void ImageProcessor::release(JNIEnv *env) {
	ENTER();

	if (LIKELY(env)) {
		if (mWeakThiz) {
			env->DeleteGlobalRef(mWeakThiz);
			mWeakThiz = NULL;
		}
		if (mClazz) {
			env->DeleteGlobalRef(mClazz);
			mClazz = NULL;
		}
	}
	clearFrames();

	EXIT();
}

/**
 * プロセッシングスレッド開始
 * これはJava側の描画スレッド内から呼ばれる(EGLContextが有るのでOpenGL|ES関係の処理可)
 */
int ImageProcessor::start(const int &width, const int &height) {
	ENTER();
	int result = -1;

	if (!isRunning()) {
		mMutex.lock();
		{
#if USE_PBO
			// glReadPixelsに使うピンポンバッファ用PBOの準備
			const GLsizeiptr pbo_size = (GLsizeiptr)width * height * 4;
			// バッファ名を2つ生成
			glGenBuffers(2, pbo);
			GLCHECK("glGenBuffers");
			glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[0]);
			GLCHECK("glBindBuffer");
			glBufferData(GL_PIXEL_PACK_BUFFER, pbo_size, NULL, GL_DYNAMIC_READ);
			glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[1]);
			GLCHECK("glBindBuffer");
			glBufferData(GL_PIXEL_PACK_BUFFER, pbo_size, NULL, GL_DYNAMIC_READ);
			glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
			pbo_ix = 0;
#endif
			mIsRunning = true;
			result = pthread_create(&processor_thread, NULL, processor_thread_func, (void *)this);
		}
		mMutex.unlock();
	} else {
		LOGW("already running");
	}

	RETURN(result, int);
}

/**
 * プロセッシングスレッド終了
 * これはJava側の描画スレッド内から呼ばれる(EGLContextが有るのでOpenGL|ES関係の処理可)
 */
int ImageProcessor::stop() {
	ENTER();

	bool b = isRunning();
	if (LIKELY(b)) {
		mMutex.lock();
		{
			MARK("signal to processor thread");
			mIsRunning = false;
			mSync.broadcast();
		}
		mMutex.unlock();
		MARK("プロセッサスレッド終了待ち");
		if (pthread_join(processor_thread, NULL) != EXIT_SUCCESS) {
			LOGW("terminate processor thread: pthread_join failed");
		}
#if USE_PBO
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
		glDeleteBuffers(2, pbo);
		pbo[0] = pbo[1] = 0;
#endif
	}
	clearFrames();
	mPool.clear();

	RETURN(0, int);
}

int ImageProcessor::setExtractionColor(const int lower[], const int upper[]) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	memcpy(&mExtractColorHSV[0], &lower[0], sizeof(int) * 3);
	memcpy(&mExtractColorHSV[3], &upper[0], sizeof(int) * 3);

	RETURN(0, int);
}

//================================================================================
//
//================================================================================
int ImageProcessor::handleFrame(const int &width, const int &height, const int &unused) {
	ENTER();

	if (!canAddFrame()) RETURN(1, int);;	// dropped

	// OpenGLのフレームバッファをMatに読み込んでキューする
	cv::Mat mat = obtainFromPool(width, height);
#if USE_PBO
	const int read_ix = pbo_ix; // 今回読み込むPBOのインデックス
	const int next_read_ix = pbo_ix = (pbo_ix + 1) % 2;	// 読み込み要求するPBOのインデックス
	const GLsizeiptr pbo_size = (GLsizeiptr)width * height * 4;
	//　読み込み要求を行うPBOをbind
	glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[next_read_ix]);
	// 非同期でPBOへ読み込み要求
	glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
	// 実際にデータを取得するPBOをbind
	glBindBuffer(GL_PIXEL_PACK_BUFFER, pbo[read_ix]);
	// PBO内のデータにアクセスできるようにマップする
	const uint8_t *read_data = (uint8_t *)glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, pbo_size, GL_MAP_READ_BIT);
	if (LIKELY(read_data)) {
		// ここでコピーする
		memcpy(mat.data, read_data, pbo_size);
		glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
	}
	glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
#else
	glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, mat.data);
#endif
	addFrame(mat);

	RETURN(0, int);
}

//================================================================================
// フレームキュー
//================================================================================
/*protected*/
void ImageProcessor::clearFrames() {
	ENTER();

	mPoolMutex.lock();
	{
		mPool.clear();
	}
	mPoolMutex.unlock();

	mMutex.lock();
	{
		for ( ; !mFrames.empty(); ) {
			mFrames.pop();
		}
	}
	mMutex.unlock();

	EXIT();
}

/** フレームキューからフレームを取得, フレームキューが空ならブロックする */
/*protected*/
cv::Mat ImageProcessor::getFrame() {
	ENTER();

	cv::Mat result;

	Mutex::Autolock lock(mMutex);
	if (mFrames.empty()) {
		mSync.wait(mMutex);
	}
	if (mIsRunning && !mFrames.empty()) {
		result = mFrames.front();
		mFrames.pop();
	}

	RET(result);
}

/** フレームプールからフレームを取得, フレームプールが空なら新規に生成する  */
/*protected*/
cv::Mat ImageProcessor::obtainFromPool(const int &width, const int &height) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	cv::Mat frame;
	if (LIKELY(!mPool.empty())) {
		frame = mPool.back();
		mPool.pop_back();
		frame.create(height, width, CV_8UC4);	// XXX rows=height, cols=widthなので注意
	} else {
		frame = cv::Mat(height, width, CV_8UC4);	// XXX rows=height, cols=widthなので注意
	}

	RET(frame);
}

/** フレームプールにフレームを返却する */
/*protected*/
void ImageProcessor::recycle(cv::Mat &frame) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	if (mPool.size() < MAX_POOL_SIZE) {
		mPool.push_back(frame);
	}

	EXIT();
}

/** フレームキューにフレームを追加する, キュー中のフレーム数が最大数を超えると先頭を破棄する */
/*protected*/
int ImageProcessor::addFrame(cv::Mat &frame) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	if (mFrames.size() >= MAX_QUEUED_FRAMES) {
		// キュー中のフレーム数が最大数を超えたので先頭を破棄する
		mFrames.pop();
	}
	mFrames.push(frame);
	mSync.signal();

	RETURN(0, int);
}

/** プロセッシングスレッドの実行関数 */
/*private*/
void *ImageProcessor::processor_thread_func(void *vptr_args) {
	ENTER();

	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(vptr_args);
	if (LIKELY(processor)) {
		// Java側へアクセスできるようにするためにJavaVMへアタッチする
		JavaVM *vm = getVM();
		CHECK(vm);
		JNIEnv *env;
		vm->AttachCurrentThread(&env, NULL);
		CHECK(env);
		processor->do_process(env);
		LOGD("プロセッサループ終了, JavaVMからデタッチする");
		vm->DetachCurrentThread();
		LOGD("デタッチ終了");
	}

	PRE_EXIT();
	pthread_exit(NULL);
}

/** プロセッシングスレッドの実体 */
/*private*/
void ImageProcessor::do_process(JNIEnv *env) {
	ENTER();

	DetectRec_t possible;
	std::vector<std::vector< cv::Point>> contours;	// 輪郭データ
	std::vector<DetectRec_t> approxes;	// 近似輪郭
	DetectParam_t param;

	for ( ; mIsRunning ; ) {
		// フレームデータの取得待ち
		cv::Mat frame = getFrame();
		if (!mIsRunning) break;
		try {
//================================================================================
// フラグ更新
			param.set(mParam);
//			const int frame_type = mResultFrameType;
//			const bool needs_result = frame_type != RESULT_FRAME_TYPE_NON;
//			const bool show_src = (frame_type == RESULT_FRAME_TYPE_SRC) || (frame_type == RESULT_FRAME_TYPE_SRC_LINE);
//			const bool show_detects = needs_result && (frame_type == RESULT_FRAME_TYPE_SRC_LINE) || (frame_type == RESULT_FRAME_TYPE_DST_LINE);
//			const bool enable_extract = mEnableExtract;
//			const bool enable_canny = mEnableCanny;
// 前処理
			cv::Mat src, bk_result, result;
			pre_process(frame, src, bk_result, result, param);
//--------------------------------------------------------------------------------
// 輪郭の検出処理
// 最大で直線・円弧・コーナーの3つの処理が走るので近似輪郭検出と最低限のチェック(面積とか)は1回だけ先に済ましておく
			findContours(src, bk_result, contours, approxes, param);
//--------------------------------------------------------------------------------
// 直線ラインの検出処理
			// 表示用にカラー画像に戻す
			result = bk_result;
			detect_line(approxes, result, possible, param);
			if (UNLIKELY(possible.type == TYPE_NON)) {
// 円弧の検出処理
				result = bk_result;
				detect_circle(approxes, result, possible, param);
			}
			if (UNLIKELY(possible.type == TYPE_NON)) {
// コーナーの検出処理
				result = bk_result;
				detect_circle(approxes, result, possible, param);
			}
//================================================================================
			// Java側のコールバックメソッドを呼び出す
			callJavaCallback(env, possible, result, param);
		} catch (cv::Exception e) {
			LOGE("do_process failed:%s", e.msg.c_str());
			continue;
		}
		recycle(frame);
	}

	EXIT();
}

#define RESULT_NUM 20

/*private*/
int ImageProcessor::callJavaCallback(JNIEnv *env, DetectRec_t &detect_result, cv::Mat &result, const DetectParam_t &param) {
	ENTER();

	float detected[RESULT_NUM];

	if (LIKELY(mIsRunning && fields.callFromNative && mClazz && mWeakThiz)) {
		// FIXME 解析結果を配列にセットする
		// ラインの中心座標(位置ベクトル,cv::RotatedRect#center)
		// ラインの長さ(長軸長さ=length)
		// ラインの方向(cv::RotatedRect#angle)
		// アスペクト比(もしくは幅=短軸長さ)
		// FIXME 円フィッティングの曲率/上半分と下半分の傾き
		jfloatArray detected_array = env->NewFloatArray(RESULT_NUM);
		env->SetFloatArrayRegion(detected_array, 0, RESULT_NUM, detected);
		// 解析画像
		jobject buf_frame = param.needs_result ? env->NewDirectByteBuffer(result.data, result.total() * result.elemSize()) : NULL;
		// コールバックメソッドを呼び出す
		env->CallStaticVoidMethod(mClazz, fields.callFromNative, mWeakThiz, detect_result.type, buf_frame, detected_array);
		env->ExceptionClear();
		if (LIKELY(detected_array)) {
			env->DeleteLocalRef(detected_array);
		}
		if (buf_frame) {
			env->DeleteLocalRef(buf_frame);
		}
	}

	RETURN(0, int);
}

// stringstreamをクリアして再利用できるようにする
static void clear_stringstream(std::stringstream &ss) {
	static const std::string empty_string;

	ss.str(empty_string);
	ss.clear();
	ss << std::dec;     // clear()でも元に戻らないので、毎回指定する。
}

// RotatedRectを指定線色で描画する
static void draw_rect(cv::Mat img, cv::RotatedRect rect, cv::Scalar color) {
	cv::Point2f vertices[4];
	rect.points(vertices);
	for (int i = 0; i < 4; i++) {
		cv::line(img, vertices[i], vertices[(i+1)%4], color);
	}
}

static double HU_MOMENTS[] = {
//	0.383871,0.119557,0.000044,0.000022,0.000000,0.000008,0.000000
	3.673166e-01,1.071715e-01,1.763543e-04,9.209628e-05,1.173179e-08,3.007932e-05,-3.488433e-10
};

/**
 * doubleの引数の符号を返す
 * @return 0:引数がゼロの時, -1:引数が負の時, 1:引数が正の時
 */
static inline int sign(const double v) {
	return (v > 0) - (v < 0);
}

/** Hu momentsを基準値と比較
 * methodは1〜3
 * cv::matchShapesが画像を引数にしているのを計算済みのHu momentsを渡せるようにしただけ
 * @return 0: 完全一致
 */
static double compHuMoments(const double mb[], int method) {
	static const double eps = 1.e-5;

    int sma, smb;
    double mmm;
    double result = 0;

    switch (method) {
    case 1:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(HU_MOMENTS[i]);
            double amb = fabs(mb[i]);

			sma = sign(HU_MOMENTS[i]);	// (HU_MOMENTS[i] > 0) ? 1 : ((HU_MOMENTS[i] < 0) ? -1 : 0);
			smb = sign(mb[i]);			// (mb[i] > 0) ? 1 : ((mb[i] < 0) ? -1 : 0);

            if (ama > eps && amb > eps) {
                ama = 1. / (sma * log10(ama));
                amb = 1. / (smb * log10(amb));
                result += fabs(-ama + amb);
            }
        }
        break;

    case 2:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(HU_MOMENTS[i]);
            double amb = fabs(mb[i]);

			sma = sign(HU_MOMENTS[i]);	// (HU_MOMENTS[i] > 0) ? 1 : ((HU_MOMENTS[i] < 0) ? -1 : 0);
			smb = sign(mb[i]);			// (mb[i] > 0) ? 1 : ((mb[i] < 0) ? -1 : 0);

            if (ama > eps && amb > eps) {
                ama = sma * log10(ama);
                amb = smb * log10(amb);
                result += fabs(-ama + amb);
            }
        }
        break;

    case 3:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(HU_MOMENTS[i]);
            double amb = fabs(mb[i]);

			sma = sign(HU_MOMENTS[i]);	// (HU_MOMENTS[i] > 0) ? 1 : ((HU_MOMENTS[i] < 0) ? -1 : 0);
			smb = sign(mb[i]);			// (mb[i] > 0) ? 1 : ((mb[i] < 0) ? -1 : 0);

            if (ama > eps && amb > eps) {
                ama = sma * log10(ama);
                amb = smb * log10(amb);
                mmm = fabs((ama - amb) / ama);
                if( result < mmm )
                    result = mmm;
            }
        }
        break;
    default:
        LOGE("Unknown comparison method:%d", method);
    }

    return result;
}

// 検出したオブジェクトの優先度の判定
// 第1引数が第2引数よりも小さい(前にある=優先度が高い)時に真(正)を返す
static bool comp_line_priority(const DetectRec &left, const DetectRec &right) {
//	// 頂点1つあたりの面積の比較(大きい方)
//	const bool b1 = left.area_vertex > right.area_vertex;
	// 類似性(小さい方, 曲線だと大きくなってしまう)
	const bool b2 = left.analogous < right.analogous;
	// 近似輪郭と実輪郭の面積比(小さい方, 曲線だと大きくなってしまう)
	const bool b3 = left.area_rate < right.area_rate;
	// アスペクト比の比較(10%以上違う時, 大きい方)
	const float a4 = left.aspect / right.aspect;
	const bool b4 = (a4 < 0.9f || a4 > 1.1f) ? left.aspect > right.aspect : false;
	// 長さの比較(10ピクセル以上違う時, 大きい方)
	const bool b5 = fabs(left.length - right.length) > 10 ? left.length - right.length : false;
	return
		(b5 && b4 && b3 && b2)		// 長くてアスペクト比が大きくて面積比が小さくて類似性が良い
		|| (b5 && b4 && b3)			// 長くてアスペクト比が大きくて面積比が小さい
		|| (b5 && b4 && b2)			// 長くてアスペクト比が大きくて類似性が良い
		|| (b5 && b4)				// 長くてアスペクト比が大きい
		|| (b5 && b3 && b2)			// 長くて面積比が小さくて類似性が良い
		|| (b5 && b3)				// 長くて面積比が小さい
		|| (b5 && b2)				// 長くて類似性が良い
		|| (b4 && b3 && b2)			// アスペクト比が大きくて面積比が小さくて類似性が良い
		|| (b4 && b3)				// アスペクト比が大きくて面積比が小さい
		|| (b4 && b2)				// アスペクト比が大きくて類似性が良い
		|| (b3 && b2)				// 面積比が小さくて類似性良い
		|| (b5)						// 長い
		|| (b4)						// アスペクト比が大きい
		|| (b3)						// 面積比が小さくい
		|| (b2);					// 類似性が良い
}

/** 映像の前処理 */
/*protected*/
int ImageProcessor::pre_process(cv::Mat &frame, cv::Mat &src, cv::Mat &bk_result, cv::Mat &result,
	const DetectParam_t &param) {

	ENTER();

	// RGBAのままだとHSVに変換できないので一旦BGRに変える
	cv::cvtColor(frame, src, cv::COLOR_RGBA2BGR, 1);
//	cv::normalize(src, src, 0, 255, cv::NORM_MINMAX);
	// 色抽出処理, 色相は問わず彩度が低くて明度が高い領域を抽出
	if (param.mEnableExtract) {
		int extractColorHSV[6];
		mMutex.lock();
		{
			memcpy(extractColorHSV, mExtractColorHSV, sizeof(int) * 6);
		}
		mMutex.unlock();
		colorExtraction(src, &src, cv::COLOR_BGR2HSV, 0, &extractColorHSV[0], &extractColorHSV[3]);
	}
	// グレースケールに変換(RGBA->Y)
	cv::cvtColor(src, src, cv::COLOR_BGR2GRAY, 1);
	// 平滑化
//	cv::Sobel(src, src, CV_32F, 1, 1);
//	cv::convertScaleAbs(src, src, 1, 0);
	if (param.mEnableCanny) {
		// エッジ検出(Cannyの結果は2値化されてる)
		cv::Canny(src, src, param.mCannythreshold1, param.mCannythreshold2);
	}
	// 2値化
//	cv::threshold(src, src, 125, 255, cv::THRESH_BINARY);
//	cv::threshold(src, src, 200, 255, cv::THRESH_BINARY_INV);

	// 表示用にカラー画像に戻す
	if (param.needs_result) {
		if (param.show_src) {
			bk_result = frame;
		} else {
			cv::cvtColor(src, bk_result, cv::COLOR_GRAY2RGBA);
//			cv::cvtColor(src, bk_result, cv::COLOR_BGR2RGBA);
		}
	}
//	cv::cvtColor(src, src, cv::COLOR_BGR2GRAY, 1);

	RETURN(0, int);
}

/**
 * 指定したHSV色範囲に収まる領域を抽出する
 * @param src
 * @param dst
 * @param convert_code srcの画像をHSVに変換するためのcv:cvtColorの第3引数
 * @param method 抽出方法 0:LUT, 1:inRange
 * @param lower HSV下限
 * @param upper HSV上限
 */
/*protected*/
int ImageProcessor::colorExtraction(const cv::Mat &src, cv::Mat *dst,
	int convert_code,			// cv:cvtColorの第3引数, カラー変換方法
	int method,
	const int lower[], const int upper[]) {

	ENTER();

	int result = 0;

    cv::Mat hsv;

	try {
		// HSVに変換
		cv::cvtColor(src, hsv, convert_code);

		if (method == 1) {
			cv::Mat mask;
			cv::inRange(hsv, cv::Scalar(lower[0], lower[1], lower[2]) , cv::Scalar(upper[0], upper[1], upper[2]), mask);
			cv::Mat output;
			src.copyTo(output, mask);	// copyToの出力先はデータが入ってちゃだめらしい
			*dst = output;
		} else {
			cv::Mat lut = cv::Mat(256, 1, CV_8UC3);
			// 指定したHSV範囲からLUT(Look Up Table)を作成
			for (int i = 0; i < 256; i++) {
				for (int k = 0; k < 3; k++) {
					if (lower[k] <= upper[k]) {
						if ((lower[k] <= i) && (i <= upper[k])) {
							lut.data[i * lut.step + k] = 255;
						} else{
							lut.data[i * lut.step + k] = 0;
						}
					} else {
						if ((i <= upper[k]) || (lower[k] <= i)) {
							lut.data[i * lut.step + k] = 255;
						} else {
							lut.data[i * lut.step + k] = 0;
						}
					}
				}
			}

			// LUTを使用して二値化
			cv::LUT(hsv, lut, hsv);

			// Channel毎に分解
			std::vector<cv::Mat> planes;
			cv::split(hsv, planes);

			// マスクを作成・・・HSVのどれかが0になってれば除外される
			cv::Mat mask;
			cv::bitwise_and(planes[0], planes[1], mask);
			cv::bitwise_and(mask, planes[2], mask);

			// 出力
			cv::Mat output;
			src.copyTo(output, mask);	// copyToの出力先はデータが入ってちゃだめらしい
			*dst = output;
//			*dst = mask;	// マスクを返せば勝手に２値画像になる
		}
	} catch (cv::Exception e) {
		LOGE("colorExtraction failed:%s", e.msg.c_str());
		result = -1;
	}
    RETURN(result, int);
}

/** 輪郭線を検出 */
/*protected*/
int ImageProcessor::findContours(cv::Mat &src, cv::Mat &result,
	std::vector<std::vector< cv::Point>> &contours,	// 輪郭データ
	std::vector<DetectRec_t> &approxes,	// 近似輪郭
	const DetectParam_t &param) {
	ENTER();

	DetectRec_t possible;

	contours.clear();
	approxes.clear();

	// 外周に四角を描いておく。でないと画面の外にはみ出した部分が有る形状を閉曲線として検出出来ない
	cv:rectangle(src, cv::Rect(8, 8, src.cols - 8, src.rows - 8), COLOR_WHITE, 8);
	// 輪郭を求める
	cv::findContours(src, contours,
		cv::RETR_CCOMP, 		// RETR_EXTERNAL:輪郭検出方法は外形のみ, RETR_LIST:階層なし, RETR_CCOMP:2階層, RETR_TREE:階層
		cv::CHAIN_APPROX_NONE);	// 輪郭データ近似方法, CHAIN_APPROX_NONE:無し,  CHAIN_APPROX_SIMPLE:直線は頂点のみにする,  CHAIN_APPROX_TC89_L1, CHAIN_APPROX_TC89_KCOS
//	// 検出した輪郭を全て描画
//	if (show_detects) {
//		cv::drawContours(result, contours, -1, COLOR_YELLOW);
//	}
	// 検出した輪郭の数分ループする
	for (auto contour = contours.begin(); contour != contours.end(); contour++) {
		std::vector< cv::Point > approx;		// 近似輪郭
		// 輪郭近似精度(元の輪郭と近似曲線との最大距離)を計算
		const double epsilon = param.mApproxType == APPROX_RELATIVE
			? mParam.mApproxFactor * cv::arcLength(*contour, true)
			: mParam.mApproxFactor;
		// 輪郭を近似する
		cv::approxPolyDP(*contour, approx, epsilon, true);	// 閉曲線にする
		const size_t num_vertex = approx.size();
		if (LIKELY(num_vertex < 4)) continue;	// 3角形はスキップ
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = cv::minAreaRect(approx);
		// 常に横長として幅と高さを取得
		const float w = fmax(area_rect.size.width, area_rect.size.height);	// 最小矩形の幅=長軸長さ
		const float h = fmin(area_rect.size.width, area_rect.size.height);	// 最小矩形の高さ=短軸長さ
		if ((w > 620) && (h > 345)) continue;	// 外周線
		// 近似輪郭の面積を計算, 面積が小さすぎるのはスキップ
		const float area = (float)cv::contourArea(approx);
		if (area < 1000.0f) continue;
		if (param.show_detects) {
			draw_rect(result, area_rect, COLOR_YELLOW);
		}
		possible.type = TYPE_NON;
		possible.contour.assign(approx.begin(), approx.end());
		possible.area_rect = area_rect;
		possible.area = area;
		possible.area_rate = w * h / area;
		possible.aspect = w / h;
		possible.length = w;	// 長軸長さ
		possible.width = h;		// 短軸長さ
		approxes.push_back(possible);
	}

	RETURN(0, int);
}

// 直線検知
#define DETECT_LINES 0

/*protected*/
int ImageProcessor::detect_line(
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	cv::Mat &result_frame, DetectRec_t &possible,
	const DetectParam_t &param) {			// 結果

	ENTER();

#if DETECT_LINES
// 線分の検出処理(単なるテスト)
	std::vector<cv::Vec4i> lines;
#if 1
	// 確率的ハフ変換による直線検出
	cv::HoughLinesP(src, lines,
		2,			// 距離分解能[ピクセル]
		CV_PI/180,	// 角度分解能[ラジアン]
		70,			// Accumulatorのしきい値
		20,			// 最小長さ[ピクセル]
		20			// 2点が同一線上にあるとみなす最大距離[ピクセル]
	);
	// 検出結果をresultに書き込み
	if (param.show_detects) {
		for (size_t i = 0; i < lines.size(); i++ ) {
			cv::Vec4i l = lines[i];
			cv::line(bk_result, cv::Point(l[0], l[1]), cv::Point(l[2], l[3]), COLOR_RED, 3, 8);
		}
	}
#else
	// ハフ変換による直線検出
	std::vector<cv::Vec2i> lines;
	cv::HoughLines(src, lines, 1, CV_PI/180, 100);
	// 検出結果をresultに書き込み
	if (param.show_detects) {
		for (size_t i = 0; i < lines.size(); i++ ) {
			float rho = lines[i][0];
			float theta = lines[i][1];
			double a = cos(theta), b = sin(theta);
			double x0 = a*rho, y0 = b*rho;
			cv::Point pt1(cvRound(x0 + 1000*(-b)), cvRound(y0 + 1000*(a)));
			cv::Point pt2(cvRound(x0 - 1000*(-b)), cvRound(y0 - 1000*(a)));
			cv::line(bk_result, pt1, pt2, COLOR_RED, 3, 8 );
		}
	}
#endif
#endif // #if DETECT_LINES
	std::vector<DetectRec_t> possibles;		// 可能性のある輪郭
	double hu_moments[8];
	int ix = 0;	// 輪郭のインデックス

	// 検出した輪郭の数分ループする
	for (auto contour = contours.begin(); contour != contours.end(); contour++) {
		DetectRec_t approx = *contour;		// 近似輪郭
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = approx.area_rect;
		// アスペクト比が正方形に近いものはスキップ
		if (LIKELY((approx.aspect > 0.3f) && (approx.aspect < 3.0f))) continue;
		if (param.show_detects) {
			draw_rect(result_frame, area_rect, COLOR_ORANGE);
		}
		// 最小矩形と元輪郭の面積比が大き過ぎる場合スキップ
		if ((approx.area_rate < 0.67f) && (approx.area_rate > 1.5f)) continue;	// ±50%以上ずれている時はスキップ
		const float area_vertex = approx.area / approx.contour.size();
		// 面積の割に頂点が多いものもスキップ これを入れるとエッジがギザギザの時に検出できなくなる
//		if (area_vertex < 200.0f) continue;		// 1頂点あたり200ピクセルよりも小さい
		if (param.show_detects) {
			draw_rect(result_frame, area_rect, COLOR_GREEN);
		}
		// 輪郭のHu momentを計算
		cv::HuMoments(cv::moments(approx.contour), hu_moments);
		// 基準値と比較, メソッド1は時々一致しない, メソッド2,3だとほとんど一致しない, 完全一致なら0が返る
		const float analogous = (float)compHuMoments(hu_moments, 1);
		// Hu momentsが基準値との差が大きい時はスキップ
		if (analogous < param.mMaxAnalogous) {
			// ラインの可能性が高い輪郭を追加
			possible.type = TYPE_LINE;
			possible.contour.assign(approx.contour.begin(), approx.contour.end());
			possible.area_rect = approx.area_rect;
			possible.area = approx.area;
			possible.area_rate = approx.area_rate;
			possible.aspect = approx.aspect;
			possible.length = approx.length;	// 長軸長さ
			possible.width = approx.width;		// 短軸長さ
			possible.analogous = analogous;
			possibles.push_back(possible);
			if (param.show_detects) {
				draw_rect(result_frame, area_rect, COLOR_BLUE);
			}
		}
		ix++;
	}
	// 優先度の最も高いものを選択する
	if (possibles.size() > 0) {
		// 優先度の降順にソートする
		std::sort(possibles.begin(), possibles.end(), comp_line_priority);
		possible = *possibles.begin();
		possible.curvature = 0;
		// 近似輪郭の面積と最小矩形の面積の比が大きい時は曲がっているかもしれないので楕円フィッティングして曲率を計算してみる
		if ((possible.area_rate > 1.2f) && (possible.contour.size() > 6)) {	// 5点以上あれば楕円フィッティング出来る
			try {
				cv::RotatedRect ellipse = cv::fitEllipse(possible.contour);
//				LOGI("fit ellipse:(%f,%f),%f", ellipse.size.width, ellipse.size.height, ellipse.angle);
				const double a = fmax(ellipse.size.width, ellipse.size.height);
				if (a > 0) {
					const double b = fmin(ellipse.size.width, ellipse.size.height);
					possible.curvature = (float)(b / a / a);
//					LOGI("fit ellipse:(%f,%f),%f,%f", ellipse.size.width, ellipse.size.height, ellipse.angle, possible.curvature);
				}
				if (param.show_detects) {
					cv::ellipse(result_frame, ellipse.center, ellipse.size, ellipse.angle, 0, 360, COLOR_RED);
				}
			} catch (cv::Exception e) {
				LOGE("fitEllipse failed:%s", e.msg.c_str());
			}
		}
		if (param.show_detects) {
			// ラインとして検出した輪郭線を赤で描画する
			cv::polylines(result_frame, possible.contour, true, COLOR_RED, 2);	// 赤色
			// 中央から検出したオブジェクトの中心に向かって線を引く
			cv::line(result_frame, cv::Point(320, 184), possible.area_rect.center, COLOR_RED, 8, 8);
		}
	} else {
		possible.type = TYPE_NON;
	}
	RETURN(0, int);
}

/** 円状のラインを検出する処理 */
/*protected*/
int ImageProcessor::detect_circle(
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	cv::Mat &result_frame, DetectRec_t &possible,
	const DetectParam_t &param) {			// 結果

	ENTER();

	RETURN(0, int);
}

// ラインのコーナーを検出する処理
/*protected*/
int ImageProcessor::detect_corner(
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	cv::Mat &result_frame, DetectRec_t &possible,
	const DetectParam_t &param) {			// 結果

	ENTER();

	RETURN(0, int);
}

//********************************************************************************
//********************************************************************************
static void nativeClassInit(JNIEnv* env, jclass clazz) {
	ENTER();

	fields.callFromNative = env->GetStaticMethodID(clazz, "callFromNative",
         "(Ljava/lang/ref/WeakReference;ILjava/nio/ByteBuffer;[F)V");
	if (UNLIKELY(!fields.callFromNative)) {
		LOGW("can't find com.serenegiant.ImageProcessor#callFromNative");
	}
	env->ExceptionClear();
	// ByteBufferがdirectBufferでない時にJava側からbyte[]を取得するためのメソッドidを取得
    jclass byteBufClass = env->FindClass("java/nio/ByteBuffer");

	if (LIKELY(byteBufClass)) {
		fields.arrayID = env->GetMethodID(byteBufClass, "array", "()[B");
		if (!fields.arrayID) {
			LOGE("Can't find java/nio/ByteBuffer#array");
		}
	} else {
		LOGE("Can't find java/nio/ByteBuffer");
	}
	env->ExceptionClear();

	EXIT();
}

static ID_TYPE nativeCreate(JNIEnv *env, jobject thiz,
	jobject weak_thiz_obj) {

	ImageProcessor *processor = NULL;

	jclass clazz = env->GetObjectClass(thiz);
	if (LIKELY(clazz)) {
		processor = new ImageProcessor(env, weak_thiz_obj, clazz);
		setField_long(env, thiz, "mNativePtr", reinterpret_cast<ID_TYPE>(processor));
	} else {
		jniThrowRuntimeException(env, "can't find com.serenegiant.ImageProcessor");
	}

	RETURN(reinterpret_cast<ID_TYPE>(processor), ID_TYPE);
}

static void nativeRelease(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	setField_long(env, thiz, "mNativePtr", 0);
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		// 終了処理
		processor->release(env);
		SAFE_DELETE(processor);
	}

	EXIT();
}

static jint nativeStart(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint width, jint height) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->start(width, height);
	}

	RETURN(result, jint);
}

static jint nativeStop(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->stop();
	}

	RETURN(result, jint);
}

static int nativeHandleFrame(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint width, jint height, jint tex_name) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		// フレーム処理
		// こっちはNative側でglReadPixelsを呼んで画像を読み込んで処理する時
		result = processor->handleFrame(width, height, tex_name);
	}

	RETURN(result, jint);
}

static jint nativeSetResultFrameType(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint result_frame_type) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		processor->setResultFrameType(result_frame_type);
		result = 0;
	}

	RETURN(result, jint);
}

static jint nativeGetResultFrameType(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = 0;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->getResultFrameType();
	}

	RETURN(result, jint);
}

static jint nativeSetExtractionColor(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint lowerH, jint upperH, jint lowerS, jint upperS, jint lowerV, jint upperV) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		const int lower[3] = {lowerH, lowerS, lowerV};
		const int upper[3] = {upperH, upperS, upperV};
		result = processor->setExtractionColor(lower, upper);
	}

	RETURN(result, jint);
}

static jint nativeSetEnableExtract(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint enable) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		processor->setEnableExtract(enable);
		result = 0;
	}

	RETURN(result, jint);
}

static jint nativeGetEnableExtract(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->getEnableExtract();
	}

	RETURN(result, jint);
}

static jint nativeSetEnableCanny(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint enable) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		processor->setEnableCanny(enable);
		result = 0;
	}

	RETURN(result, jint);
}

static jint nativeGetEnableCanny(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->getEnableCanny();
	}

	RETURN(result, jint);
}

//================================================================================
//================================================================================
static JNINativeMethod methods[] = {
	{ "nativeClassInit",			"()V",   (void*)nativeClassInit },
	{ "nativeCreate",				"(Ljava/lang/ref/WeakReference;)J", (void *) nativeCreate },
	{ "nativeRelease",				"(J)V", (void *) nativeRelease },
	{ "nativeStart",				"(JII)I", (void *) nativeStart },
	{ "nativeStop",					"(J)I", (void *) nativeStop },
	{ "nativeHandleFrame",			"(JIII)I", (void *) nativeHandleFrame },
	{ "nativeSetResultFrameType",	"(JI)I", (void *) nativeSetResultFrameType },
	{ "nativeGetResultFrameType",	"(J)I", (void *) nativeGetResultFrameType },
	{ "nativeSetExtractionColor",	"(JIIIIII)I", (void *) nativeSetExtractionColor },
	{ "nativeSetEnableExtract",		"(JI)I", (void *) nativeSetEnableExtract },
	{ "nativeGetEnableExtract",		"(J)I", (void *) nativeGetEnableExtract },
	{ "nativeSetEnableCanny",		"(JI)I", (void *) nativeSetEnableCanny },
	{ "nativeGetEnableCanny",		"(J)I", (void *) nativeGetEnableCanny },
};


int register_ImageProcessor(JNIEnv *env) {
	// ネイティブメソッドを登録
	if (registerNativeMethods(env,
		"com/serenegiant/opencv/ImageProcessor",
		methods, NUM_ARRAY_ELEMENTS(methods)) < 0) {
		return -1;
	}
	return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
#if LOCAL_DEBUG
    LOGD("JNI_OnLoad");
#endif

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register native methods
    int result = register_ImageProcessor(env);

	setVM(vm);

#if LOCAL_DEBUG
    LOGD("JNI_OnLoad:finished:result=%d", result);
#endif
    return JNI_VERSION_1_6;
}
