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
	mResultFrameType(RESULT_FRAME_TYPE_DST_LINE)
{
	// H(色相)は制限なし, S(彩度)は0-約5%, 2:V(明度)は約80-100%
	mExtractColorHSV[0] = 0;	// H下限
	mExtractColorHSV[1] = 0;	// S下限
	mExtractColorHSV[2] = 200;	// V下限
	mExtractColorHSV[3] = 180;	// H下限
	mExtractColorHSV[4] = 10;	// S上限
	mExtractColorHSV[5] = 255;	// V上限
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

int ImageProcessor::setExtractionColor(const int lower[], const int upper[]) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	memcpy(&mExtractColorHSV[0], lower, sizeof(float) * 3);
	memcpy(&mExtractColorHSV[3], upper, sizeof(float) * 3);

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

#define TYPE_NON -1
#define TYPE_LINE 0
#define TYPE_CURVE 1
#define TYPE_CORNER 2

// 検出したオブジェクトの優先度の判定
// 第1引数が第2引数よりも小さい(前にある=優先度が高い)時に真(正)を返す
static bool comp_line_priority(const DetectRec &left, const DetectRec &right ) {
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

#define RESULT_NUM 20
// 直線検知
#define DETECT_LINES 0

int ImageProcessor::detect_line(
	std::vector<struct DetectRec> &contours,	// 近似輪郭
	const bool needs_result, const bool show_detects, const cv::Mat result_frame,
	struct DetectRec &possible) {			// 結果

	ENTER();

	std::vector<struct DetectRec> possibles;		// 可能性のある輪郭
	double hu_moments[8];
	int ix = 0;	// 輪郭のインデックス
	float last_analogous = -1;

	// 検出した輪郭の数分ループする
	for (auto contour = contours.begin(); contour != contours.end(); contour++) {
		struct DetectRec approx = *contour;		// 近似輪郭
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = approx.area_rect;
		// アスペクト比が正方形に近いものはスキップ
		if (LIKELY((approx.aspect > 0.3f) && (approx.aspect < 3.0f))) continue;
		if (show_detects) {
			draw_rect(result_frame, area_rect, cv::Scalar(255, 127, 0));	// 橙色
		}
		// 最小矩形と元輪郭の面積比が大き過ぎる場合と面積の割に頂点が多いものもスキップ
		const float area_vertex = approx.area / approx.contour.size();
		if (((approx.area_rate < 0.67f) && (approx.area_rate > 1.5f))	// ±50%以上ずれている時はスキップ
			|| (area_vertex < 200.0f)) continue;		// 1頂点あたり200ピクセルよりも小さい
		if (show_detects) {
			draw_rect(result_frame, area_rect, cv::Scalar(0, 255, 0));	// 緑色
		}
		// 輪郭のHu momentを計算
		cv::HuMoments(cv::moments(approx.contour), hu_moments);
		// 基準値と比較, メソッド1は時々一致しない, メソッド2,3だとほとんど一致しない, 完全一致なら0が返る
		const float analogous = (float)compHuMoments(hu_moments, 1);
		// Hu momentsが基準値との差が大きい時はスキップ FIXME ここは幅を持たせた方がいいかも
		if (analogous > last_analogous) {
			last_analogous = analogous;
#if 0
			clear_stringstream(ss);
			ss 	<< std::scientific << hu_moments[0] << ','
				<< std::scientific << hu_moments[1] << ','
				<< std::scientific << hu_moments[2] << ','
				<< std::scientific << hu_moments[3] << ','
				<< std::scientific << hu_moments[4] << ','
				<< std::scientific << hu_moments[5] << ','
				<< std::scientific << hu_moments[6];
			LOGI("hu_moments%d:%s", ix, ss.str().c_str());
//					LOGI("hu_moments%d:%18le,%18le,%18le,%18le,%18le,%18le,%18le", ix,
//						hu_moments[0], hu_moments[1], hu_moments[2], hu_moments[3], hu_moments[4], hu_moments[5], hu_moments[6]);
//					LOGI("analogous%d:%f", ix, analogous);
#endif
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
			if (show_detects) {
				draw_rect(result_frame, area_rect, cv::Scalar(0, 0, 125));	// 青色
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
//				LOGI("fit elipse:(%f,%f),%f", ellipse.size.width, ellipse.size.height, ellipse.angle);
				const double a = fmax(ellipse.size.width, ellipse.size.height);
				if (a > 0) {
					const double b = fmin(ellipse.size.width, ellipse.size.height);
					possible.curvature = (float)(b / a / a);
//					LOGI("fit elipse:(%f,%f),%f,%f", ellipse.size.width, ellipse.size.height, ellipse.angle, possible.curvature);
				}
				if (show_detects) {
					cv::ellipse(result_frame, ellipse.center, ellipse.size, ellipse.angle, 0, 360, cv::Scalar(255, 0, 0));
				}
			} catch (cv::Exception e) {
				LOGE("fitEllipse failed:%s", e.msg.c_str());
			}
		}
		if (show_detects) {
			cv::polylines(result_frame, possible.contour, true, cv::Scalar(255, 0, 0), 2);	// 赤色
			// 中央から検出したオブジェクトの中心に向かって線を引く
			cv::line(result_frame, cv::Point(320, 184), possible.area_rect.center, cv::Scalar(255, 0, 0), 8, 8);
			// FIXME 解析結果を配列にセットする
			// ラインの中心座標(位置ベクトル,cv::RotatedRect#center)
			// ラインの長さ(長軸長さ=length)
			// ラインの方向(cv::RotatedRect#angle)
			// アスペクト比(もしくは幅=短軸長さ)
			// FIXME 円フィッティングの曲率/上半分と下半分の傾き
		}
	} else {
		possible.type = TYPE_NON;
	}
	RETURN(0, int);
}

int ImageProcessor::detect_circle(
	std::vector<struct DetectRec> &contours,	// 近似輪郭
	const bool needs_result, const bool show_detects, const cv::Mat result_frame,
	struct DetectRec &possible) {			// 結果

	ENTER();

	RETURN(0, int);
}

int ImageProcessor::detect_corner(
	std::vector<struct DetectRec> &contours,	// 輪郭
	const bool needs_result, const bool show_detects, const cv::Mat result_frame,
	struct DetectRec &possible) {			// 結果
	ENTER();

	RETURN(0, int);
}

/** プロセッシングスレッドの実体 */
void ImageProcessor::do_process(JNIEnv *env) {
	ENTER();

	float detected[RESULT_NUM];
	std::stringstream ss;
	double hu_moments[8];
	int extractColorHSV[6];
	struct DetectRec possible;
	std::vector<std::vector< cv::Point>> contours;	// 輪郭データ
	std::vector<struct DetectRec> approxes;	// 近似輪郭

	for ( ; mIsRunning ; ) {
		// フレームデータの取得待ち
		cv::Mat frame = getFrame();
		if (!mIsRunning) break;
		try {
//================================================================================
// 前処理
			cv::Mat src, bk_result, result;
			// RGBAのままだとHSVに変換できないので一旦BGRに変える
			cv::cvtColor(frame, src, cv::COLOR_RGBA2BGR, 1);
			cv::normalize(src, src, 0, 255, cv::NORM_MINMAX);
			// 色抽出処理, 色相は問わず彩度が低くて明度が高い領域を抽出 FIXME 抽出条件を指定できるようにしたい
			mMutex.lock();
			{
				memcpy(extractColorHSV, mExtractColorHSV, sizeof(extractColorHSV));
			}
			mMutex.unlock();
			colorExtraction(&src, &src, cv::COLOR_BGR2HSV, &extractColorHSV[0], &mExtractColorHSV[3]);
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

			const int frame_type = mResultFrameType;
			const bool needs_result = frame_type != RESULT_FRAME_TYPE_NON;
			const bool show_src = (frame_type == RESULT_FRAME_TYPE_SRC) || (frame_type == RESULT_FRAME_TYPE_SRC_LINE);
			const bool show_detects = needs_result && (frame_type == RESULT_FRAME_TYPE_SRC_LINE) || (frame_type == RESULT_FRAME_TYPE_DST_LINE);
			// 表示用にカラー画像に戻す
			if (needs_result) {
				if (show_src) {
					bk_result = frame;
				} else {
					cv::cvtColor(src, bk_result, cv::COLOR_GRAY2RGBA);
				}
			}
//--------------------------------------------------------------------------------
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
			if (show_detects) {
				for (size_t i = 0; i < lines.size(); i++ ) {
					cv::Vec4i l = lines[i];
					cv::line(bk_result, cv::Point(l[0], l[1]), cv::Point(l[2], l[3]), cv::Scalar(255, 0, 0), 3, 8);
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
					cv::line(bk_result, pt1, pt2, cv::Scalar(255, 0, 0), 3, 8 );
				}
			}
#endif
#endif // #if DETECT_LINES
//================================================================================
// 輪郭の検出処理
// 最大で直線・円弧・コーナーの3つの処理が走るので近似輪郭検出と最低限のチェック(面積とか)は1回だけ先に済ましておく
			// 外周に四角を描いておく。でないと画面の外にはみ出した部分が有る形状を閉曲線として検出出来ない
			cv:rectangle(src, cv::Rect(8, 8, src.cols - 8, src.rows - 8), cv::Scalar(255, 255, 255), 8);
			// 輪郭を求める
			contours.clear();
			cv::findContours(src, contours,
				cv::RETR_CCOMP, 		// RETR_EXTERNAL:輪郭検出方法は外形のみ, RETR_LIST:階層なし, RETR_CCOMP:2階層, RETR_TREE:階層
				cv::CHAIN_APPROX_NONE);	// 輪郭データ近似方法, CHAIN_APPROX_NONE:無し,  CHAIN_APPROX_SIMPLE:直線は頂点のみにする,  CHAIN_APPROX_TC89_L1, CHAIN_APPROX_TC89_KCOS
//			// 検出した輪郭を全て描画
//			if (show_detects) {
//				cv::drawContours(result, contours, -1, cv::Scalar(80, 80, 0));	// 薄い黄色
//			}
			approxes.clear();
			// 検出した輪郭の数分ループする
			for (auto contour = contours.begin(); contour != contours.end(); contour++) {
				std::vector< cv::Point > approx;		// 近似輪郭
				// 輪郭を近似する, 近似精度は輪郭全周の1%まで(FIXME これはもう少し大きくてもいいかも)
				cv::approxPolyDP(cv::Mat(*contour), approx,
					0.01 * cv::arcLength(*contour, true),  // epsilon: 近似精度(元の輪郭と近似曲線との最大距離)
					true);	// closed: 閉曲線にするかどうか
				const size_t num_vertex = approx.size();
				if (LIKELY(num_vertex < 4)) continue;	// 3角形はスキップ
				// 輪郭を内包する最小矩形(回転あり)を取得
				cv::RotatedRect area_rect = cv::minAreaRect(approx);
				// 常に横長として幅と高さを取得
				const float w = fmax(area_rect.size.width, area_rect.size.height);	// 最小矩形の幅=長軸長さ
				const float h = fmin(area_rect.size.width, area_rect.size.height);	// 最小矩形の高さ=短軸長さ
				// 外周線
				if ((w > 620) && (h > 350)) continue;
				// 近似輪郭の面積を計算, 面積が小さすぎるのはスキップ
				const float area = (float)cv::contourArea(approx);
				if (area < 1000.0f) continue;
				if (show_detects) {
					draw_rect(bk_result, area_rect, cv::Scalar(255, 255, 0));	// 黄色
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
//================================================================================
// 直線ラインの検出処理
			// 表示用にカラー画像に戻す
			result = bk_result;
			detect_line(approxes, needs_result, show_detects, result, possible);
			if (UNLIKELY(possible.type == TYPE_NON)) {
// 円弧の検出処理
				result = bk_result;
				detect_circle(approxes, needs_result, show_detects, result, possible);
			}
			if (UNLIKELY(possible.type == TYPE_NON)) {
// コーナーの検出処理
				result = bk_result;
				detect_circle(approxes, needs_result, show_detects, result, possible);
			}
//================================================================================
// Java側のコールバックメソッドを呼び出す
			if (LIKELY(mIsRunning && fields.callFromNative && mClazz && mWeakThiz)) {
				jfloatArray detected_array = env->NewFloatArray(RESULT_NUM);
				env->SetFloatArrayRegion(detected_array, 0, RESULT_NUM, detected);
				// 解析画像
				jobject buf_frame = needs_result ? env->NewDirectByteBuffer(result.data, result.total() * result.elemSize()) : NULL;
				// コールバックメソッドを呼び出す
				env->CallStaticVoidMethod(mClazz, fields.callFromNative, mWeakThiz, possible.type, buf_frame, detected_array);
				env->ExceptionClear();
				if (LIKELY(detected_array)) {
					env->DeleteLocalRef(detected_array);
				}
				if (buf_frame) {
					env->DeleteLocalRef(buf_frame);
				}
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
	const int lower[], const int upper[]) {

	ENTER();

	int result = 0;

    cv::Mat colorImage;

	try {
		cv::Mat lut = cv::Mat(256, 1, CV_8UC3);

		cv::cvtColor(*src, colorImage, convert_code);

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
	{ "nativeSetExtractionColor",	"(JIIIIII)I", (void *) nativeSetExtractionColor },
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
