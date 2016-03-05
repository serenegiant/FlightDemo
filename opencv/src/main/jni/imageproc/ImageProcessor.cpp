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

#include "utilbase.h"
#include "common_utils.h"
#include "JNIHelp.h"
#include "Errors.h"

#include "ImageProcessor.h"

// キューに入れることができる最大フレーム数
#define MAX_QUEUED_FRAMES 8

struct fields_t {
    jmethodID callFromNative;
    jmethodID arrayID;	// ByteBufferがdirectBufferでない時にJava側からbyte[]を取得するためのメソッドid
};
static fields_t fields;

using namespace android;

ImageProcessor::ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz)
:	mWeakThiz(env->NewGlobalRef(weak_thiz_obj)),
	mClazz((jclass)env->NewGlobalRef(clazz)),
	mIsRunning(false),
	mResultFrameType(RESULT_FRAME_TYPE_SRC)
{
}

ImageProcessor::~ImageProcessor() {
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

	EXIT();
}

/** プロセッシングスレッド開始 */
int ImageProcessor::start() {
	ENTER();
	int result = -1;

	if (!isRunning()) {
		mMutex.lock();
		{
			mIsRunning = true;
			result = pthread_create(&processor_thread, NULL, processor_thread_func, (void *)this);
		}
		mMutex.unlock();
	} else {
		LOGW("already running");
	}

	RETURN(result, int);
}

/** プロセッシングスレッド終了 */
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
		mMutex.lock();
		{
			for ( ; !mFrames.empty(); ) {
				mFrames.pop();
			}
		}
		mMutex.unlock();
	}
	RETURN(0, int);
}

/** プロセッシングスレッドの実行関数 */
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

struct DetectRec {
	std::vector< cv::Point > contour;
	cv::RotatedRect area_rect;
	float area_rate;
	float area;
	float aspect;
	float analogous;
	float length;	// 長い方の辺
};

static double HU_MOMENTS[] = {
	0.383871,0.119557,0.000044,0.000022,0.000000,0.000008,0.000000
};

// Hu Momentsを基準値と比較
// methodは1〜3
static float compHuMoments(const double mb[], int method) {
	static const double eps = 1.e-5;

    int sma, smb;
    double mmm;
    double result = 0;

    switch (method) {
    case 1:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(HU_MOMENTS[i]);
            double amb = fabs(mb[i]);

            if (HU_MOMENTS[i] > 0)
                sma = 1;
            else if (HU_MOMENTS[i] < 0)
                sma = -1;
            else
                sma = 0;
            if ( mb[i] > 0 )
                smb = 1;
            else if (mb[i] < 0)
                smb = -1;
            else
                smb = 0;

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

            if (HU_MOMENTS[i] > 0)
                sma = 1;
            else if (HU_MOMENTS[i] < 0)
                sma = -1;
            else
                sma = 0;
            if (mb[i] > 0)
                smb = 1;
            else if (mb[i] < 0)
                smb = -1;
            else
                smb = 0;

            if (ama > eps && amb > eps) {
                ama = sma * log10(ama);
                amb = smb * log10(amb);
                result += fabs(-ama + amb);
            }
        }
        break;

    case 3:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs( HU_MOMENTS[i]);
            double amb = fabs( mb[i] );

            if (HU_MOMENTS[i] > 0)
                sma = 1;
            else if (HU_MOMENTS[i] < 0)
                sma = -1;
            else
                sma = 0;
            if (mb[i] > 0)
                smb = 1;
            else if (mb[i] < 0)
                smb = -1;
            else
                smb = 0;

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

    return (float)result;
}

// 検出したオブジェクトの優先度の判定
static bool comp_priority(const DetectRec &left, const DetectRec &right ) {
	// 面積の比較
	const float a1 = left.area / right.area;
	const bool b1 = (a1 < 0.9f || a1 > 1.1f) ? left.area > right.area : false;
	// アスペクト比の比較
	const float a2 = left.aspect / right.aspect;
	const bool b2 = (a2 < 0.8f || a2 > 1.2f) ? left.aspect > right.aspect : false;
	// 近似輪郭と実輪郭の差
	const float a3 = left.area_rate / right.area_rate;
	const bool b3 = (a3 < 0.8f || a3 > 1.2f) ? left.area_rate < right.area_rate : false;
	// 類似率の差
	const bool b4 = left.analogous < right.analogous;
	// 長さの比較
	const bool b5 = left.length > right.length;
	return b5 || (b4 && !b5) || (b4 && !b5 && b1) || (!b4 && a1) || (b1 || b2);
}

// 直線検知
#define DETECT_LINES 0
// 輪郭抽出
#define DETECT_CONTOURS 1

/** プロセッシングスレッドの実体 */
void ImageProcessor::do_process(JNIEnv *env) {
	ENTER();

	float detected[20];
	for ( ; mIsRunning ; ) {
		// フレームデータの取得待ち
		cv::Mat frame = getFrame();
		if (!mIsRunning) break;
		// FIXME 未実装 OpenCVでの解析処理
		try {
			cv::Mat src, result;

			// RGBAのままだとHSVに変換できないので一旦BGRに変える
			cv::cvtColor(frame, src, cv::COLOR_RGBA2BGR, 1);
			cv::normalize(src, src, 0, 255, cv::NORM_MINMAX);
			// 色抽出処理, 色相は問わず彩度が低くて明度が高い領域を抽出
			colorExtraction(&src, &src, cv::COLOR_BGR2HSV,
				0, 180,		// H(色相)は制限なし
				0, 10,		// S(彩度)は0-約5%
				200, 255	// V(明度)は約80-100%
			);
			// グレースケールに変換(RGBA->Y)
			cv::cvtColor(src, src, cv::COLOR_BGR2GRAY, 1);
			// 平滑化
//			cv::Sobel(src, src, CV_32F, 1, 1);
//			cv::convertScaleAbs(src, src, 1, 0);
			// エッジ検出(Cannyの結果は2値化されてる)
			cv::Canny(src, src, 50, 200);
			// 2値化
//			cv::threshold(src, src, 200, 255, cv::THRESH_BINARY);
//			cv::threshold(src, src, 200, 255, cv::THRESH_BINARY_INV);
			// 表示用にカラー画像に戻す
			const bool show_src = (mResultFrameType == RESULT_FRAME_TYPE_SRC) || (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE);
			if (show_src) {
				result = frame;
			} else {
				cv::cvtColor(src, result, cv::COLOR_GRAY2RGBA);
			}

			const bool show_detects = (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE) || (mResultFrameType == RESULT_FRAME_TYPE_DST_LINE);
//--------------------------------------------------------------------------------
#if DETECT_LINES
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
			if (show_detects) {
				for (size_t i = 0; i < lines.size(); i++ ) {
					cv::Vec4i l = lines[i];
					cv::line(result, cv::Point(l[0], l[1]), cv::Point(l[2], l[3]), cv::Scalar(255, 0, 0), 3, 8);
				}
			}
#else
			// ハフ変換による直線検出
			std::vector<cv::Vec2i> lines;
			cv::HoughLines(src, lines, 1, CV_PI/180, 100);
			// 検出結果をresultに書き込み
			if (show_detects) {
				for (size_t i = 0; i < lines.size(); i++ ) {
					float rho = lines[i][0];
					float theta = lines[i][1];
					double a = cos(theta), b = sin(theta);
					double x0 = a*rho, y0 = b*rho;
					cv::Point pt1(cvRound(x0 + 1000*(-b)), cvRound(y0 + 1000*(a)));
					cv::Point pt2(cvRound(x0 - 1000*(-b)), cvRound(y0 - 1000*(a)));
					cv::line(result, pt1, pt2, cv::Scalar(255, 0, 0), 3, 8 );
				}
			}
#endif
#endif // #if DETECT_LINES
//--------------------------------------------------------------------------------
#if DETECT_CONTOURS
			// 外周に四角を描いておく。でないと画面の外にはみ出した部分が有る形状を閉曲線として近似出来ない
			cv:rectangle(src, cv::Rect(8, 8, src.cols - 8, src.rows - 8), cv::Scalar(255, 255, 255), 8);
			std::vector< std::vector< cv::Point > > contours;	// 輪郭データ
			std::vector< DetectRec > possibles;	// ラインの可能性のある輪郭データ
			cv::findContours(src, contours,
				cv::RETR_CCOMP, 		// RETR_EXTERNAL:輪郭検出方法は外形のみ, RETR_LIST:階層なし, RETR_CCOMP:2階層, RETR_TREE:階層
				cv::CHAIN_APPROX_NONE);	// 輪郭データ近似方法, CHAIN_APPROX_NONE:無し,  CHAIN_APPROX_SIMPLE:直線は頂点のみにする,  CHAIN_APPROX_TC89_L1, CHAIN_APPROX_TC89_KCOS
			// 検出した輪郭を全て描画
//			cv::drawContours(result, contours, -1, cv::Scalar(80, 80, 0));	// 薄い黄色
			// 輪郭のインデックス
			int ix = 0;
			float last_analogous = -1;
			std::stringstream ss;
			for (auto contour = contours.begin(); contour != contours.end(); contour++) {
				std::vector< cv::Point > approx;	// 近似図形データ
				// 輪郭を近似する
				cv::approxPolyDP(cv::Mat(*contour), approx,
					0.01 * cv::arcLength(*contour, true),  // epsilon: 近似精度(元の輪郭と近似曲線との最大距離)
					true);	// closed: 閉曲線にするかどうか
				const size_t num_vertex = approx.size();
				if (LIKELY(num_vertex < 4)) continue;	// 3角形は除外
				// 輪郭線を描画する
//				cv::drawContours(result, contours, ix, cv::Scalar(255, 255, 0));	// 黄色
				// 輪郭を内包する最小矩形(回転あり)を取得
				cv::RotatedRect area_rect = cv::minAreaRect(approx);
				const float w = area_rect.size.width;
				const float h = area_rect.size.height;
				// 外周線
				if ((w > 620) && (h > 350)) continue;
				// 近似図形の面積を計算
				const float area = (float)cv::contourArea(approx);
				// 面積が小さすぎるものはスキップ
				if (area < 2000.0f) continue;
				draw_rect(result, area_rect, cv::Scalar(255, 255, 0));	// 黄色
				// 最小矩形のアスペクト比を計算
				const float aspect = w / h;
				// アスペクト比が正方形に近いものはスキップ
				if (LIKELY((aspect > 0.3f) && (aspect < 3.0f))) continue;
				draw_rect(result, area_rect, cv::Scalar(255, 127, 0));	// 橙色
				const float area_rate = w * h / area;
				// 近似輪郭と元輪郭の面積差が大き過ぎる場合もスキップ
				if ((area_rate < 0.9f) && (area_rate > 1.8f)) continue;
				draw_rect(result, area_rect, cv::Scalar(0, 255, 255));	// 水色
//				draw_rect(result, area_rect, cv::Scalar(0, 255, 0));	// 緑色

				double hu_moments[8];
				cv::HuMoments(cv::moments(approx), hu_moments);
				// メソッド1は時々一致しない, メソッド2,3だとほとんど一致しない
				const float analogous = compHuMoments(hu_moments, 1);
				// Hu momentsが基準値との差が大きい時はスキップ
				if (analogous > last_analogous) {
					last_analogous = analogous;
//					LOGI("hu_moments%d:%f,%f,%f,%f,%f,%f,%f", ix,
//						hu_moments[0], hu_moments[1], hu_moments[2], hu_moments[3], hu_moments[4], hu_moments[5], hu_moments[6]);
//					LOGI("analogous%d:%f", ix, analogous);
					struct DetectRec possible;
					possible.contour.assign(approx.begin(), approx.end());
					possible.area_rect = area_rect;
					possible.area = area;
					possible.area_rate = area_rate;
					possible.aspect = aspect;
					possible.analogous = fabs(analogous - 100);
					possible.length = fmax(area_rect.size.width, area_rect.size.height);
					possibles.push_back(possible);
					draw_rect(result, area_rect, cv::Scalar(0, 0, 125));	// 青色
				}
				ix++;
			}
			// 優先度の最も高いものを選択する
			if (possibles.size() > 0) {
				// 優先度の降順にソートする
				std::sort(possibles.begin(), possibles.end(), comp_priority);
				struct DetectRec possible = *possibles.begin();
				cv::polylines(result, possible.contour, true, cv::Scalar(255, 0, 0), 2);	// 赤色
				// 中央から検出したオブジェクトの中心に向かって線を引く
				cv::line(result, cv::Point(320, 184), possible.area_rect.center, cv::Scalar(255, 0, 0), 8, 8);
//				int ix = 0;
//				for (auto iter = possibles.begin(); iter != possibles.end(); iter++) {
//					LOGI("possible%d:%f", ix, (*iter).analogous);
//					ix++;
//				}
			}
#endif	// #if DETECT_CONTOURS
//--------------------------------------------------------------------------------
			// Java側のコールバックメソッドを呼び出す
			if (LIKELY(mIsRunning && fields.callFromNative && mClazz && mWeakThiz)) {
				// 結果 FIXME ByteBufferで返す代わりにコピーされるけどfloat配列の方がいいかも
				jobject buf_detected = env->NewDirectByteBuffer(detected, sizeof(float) * 20);
				// 解析画像
				jobject buf_frame = env->NewDirectByteBuffer(result.data, result.total() * result.elemSize());
				// コールバックメソッドを呼び出す
				env->CallStaticVoidMethod(mClazz, fields.callFromNative, mWeakThiz, buf_frame, buf_detected);
				env->ExceptionClear();
				env->DeleteLocalRef(buf_detected);
				env->DeleteLocalRef(buf_frame);
			}
		} catch (cv::Exception e) {
			LOGE("do_process failed:%s", e.msg.c_str());
			continue;
		}
	}

	EXIT();
}

/** 指定したHSV色範囲に収まる領域を抽出する */
int ImageProcessor::colorExtraction(cv::Mat *src, cv::Mat *dst,
	int convert_code,			// cv:cvtColorの第3引数, カラー変換方法
	int HLower, int HUpper,		// 色相範囲 [0,180]
	int SLower, int SUpper,		// 彩度範囲 [0,255]
	int VLower, int VUpper) {	// 明度範囲 [0,255]

	ENTER();

	int result = 0;

    cv::Mat colorImage;
    int lower[3];
    int upper[3];

	try {
		cv::Mat lut = cv::Mat(256, 1, CV_8UC3);

		cv::cvtColor(*src, colorImage, convert_code);

		lower[0] = HLower;
		lower[1] = SLower;
		lower[2] = VLower;

		upper[0] = HUpper;
		upper[1] = SUpper;
		upper[2] = VUpper;

		for (int i = 0; i < 256; i++) {
			for (int k = 0; k < 3; k++) {
				if (lower[k] <= upper[k]) {
					if ((lower[k] <= i) && (i <= upper[k])) {
						lut.data[i*lut.step+k] = 255;
					} else{
						lut.data[i*lut.step+k] = 0;
					}
				} else {
					if ((i <= upper[k]) || (lower[k] <= i)) {
						lut.data[i*lut.step+k] = 255;
					} else {
						lut.data[i*lut.step+k] = 0;
					}
				}
			}
		}

		// LUTを使用して二値化
		cv::LUT(colorImage, lut, colorImage);

		// Channel毎に分解
		std::vector<cv::Mat> planes;
		cv::split(colorImage, planes);

		// マスクを作成
		cv::Mat maskImage;
		cv::bitwise_and(planes[0], planes[1], maskImage);
		cv::bitwise_and(maskImage, planes[2], maskImage);

		// 出力
		cv::Mat maskedImage;
		src->copyTo(maskedImage, maskImage);
		*dst = maskedImage;
	} catch (cv::Exception e) {
		LOGE("colorExtraction failed:%s", e.msg.c_str());
		result = -1;
	}
    RETURN(result, int);
}

//================================================================================
//
//================================================================================
int ImageProcessor::handleFrame(const uint8_t *frame, const int &width, const int &height) {
	ENTER();

	// 受け取ったフレームデータをMatにしてキューする
	cv::Mat mat = cv::Mat(height, width, CV_8UC4, (void *)frame);
	addFrame(mat);

	RETURN(0, int);
}

//================================================================================
// フレームキュー
//================================================================================
/** フレームキューからフレームを取得, フレームキューが空ならブロックする */
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

/** フレームキューにフレームを追加する, キュー中のフレーム数が最大数を超えると先頭を破棄する */
int ImageProcessor::addFrame(cv::Mat &frame) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	if (mFrames.size() >= MAX_QUEUED_FRAMES) {
		// キュー中のフレーム数が最大数を超えたので先頭を破棄する
		mFrames.pop();
	}
	mFrames.push(frame.clone());	// コピーを追加する
	mSync.signal();

	RETURN(0, int);
}

//********************************************************************************
//********************************************************************************
static void nativeClassInit(JNIEnv* env, jclass clazz) {
	ENTER();

	fields.callFromNative = env->GetStaticMethodID(clazz, "callFromNative",
         "(Ljava/lang/ref/WeakReference;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)V");
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
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->start();
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
	ID_TYPE id_native, jobject byteBuf_obj, jint width, jint height) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		// フレーム処理
		// 引数のByteBufferをnativeバッファに変換出来るかどうか試してみる
		void *buf = env->GetDirectBufferAddress(byteBuf_obj);
	    jlong dstSize;
		jbyteArray byteArray = NULL;
		if (LIKELY(buf)) {
			// ダイレクトバッファだった＼(^o^)／
			dstSize = env->GetDirectBufferCapacity(byteBuf_obj);
		} else {
			// 引数のByteBufferがダイレクトバッファじゃなかった(´・ω・｀)
			// ByteBuffer#arrayを呼び出して内部のbyte[]を取得できるかどうか試みる
			byteArray = (jbyteArray)env->CallObjectMethod(byteBuf_obj, fields.arrayID);
			if (UNLIKELY(byteArray == NULL)) {
				// byte[]を取得できなかった時
				LOGE("byteArray is null");
		        RETURN(BAD_VALUE, jint);
			}
	        buf = env->GetByteArrayElements(byteArray, NULL);
	        dstSize = env->GetArrayLength(byteArray);
		}
		// 配列の長さチェック
	    if (LIKELY(dstSize >= (width * height) << 2)) {	// RGBA
			result = processor->handleFrame((uint8_t *)buf, width, height);
		} else {
	        LOGE("nativeHandleFrame saw wrong dstSize %lld", dstSize);
	        result = BAD_VALUE;
	    }
		// ByteBufferのデータを開放
	    if (byteArray) {
	        env->ReleaseByteArrayElements(byteArray, (jbyte *)buf, 0);
	    }
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

//================================================================================
//================================================================================
static JNINativeMethod methods[] = {
	{ "nativeClassInit",		"()V",   (void*)nativeClassInit },
	{ "nativeCreate",			"(Ljava/lang/ref/WeakReference;)J", (void *) nativeCreate },
	{ "nativeRelease",			"(J)V", (void *) nativeRelease },
	{ "nativeStart",			"(J)I", (void *) nativeStart },
	{ "nativeStop",				"(J)I", (void *) nativeStop },
	{ "nativeHandleFrame",		"(JLjava/nio/ByteBuffer;II)I", (void *) nativeHandleFrame },
	{ "nativeSetResultFrameType",	"(JI)I", (void *) nativeSetResultFrameType },
	{ "nativeGetResultFrameType",	"(J)I", (void *) nativeGetResultFrameType },
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
