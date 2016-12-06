//
// Created by saki on 16/03/30.
//

#if 1	// デバッグ情報を出さない時は1
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

#include "utilbase.h"

#include "IPPreprocess.h"

IPPreprocess::IPPreprocess()
:	mThinning(2, 2)
{
	ENTER();

	EXIT();
}

IPPreprocess::~IPPreprocess() {
	ENTER();

	EXIT();
}

/** 映像の前処理 */
/*protected*/
int IPPreprocess::pre_process(cv::Mat &frame, cv::Mat &src, cv::Mat &result, const DetectParam_t &param) {

	ENTER();

	int res = 0;
	// 輪郭抽出結果(最外形輪郭)
	std::vector<std::vector< cv::Point>> outlines;	// これも上位から渡さないかんかなぁ

	try {
		// 台形補正
		if (param.mTrapeziumRate) {
			cv::warpPerspective(frame, frame, param.perspectiveTransform, cv::Size(src.cols, src.rows));
		}
		// RGBAのままだとHSVに変換できないので一旦BGRに変える
		cv::cvtColor(frame, src, cv::COLOR_RGBA2BGR, 1);
//		cv::normalize(src, src, 0, 255, cv::NORM_MINMAX);
		// 色抽出処理
		if (param.mEnableExtract) {
			colorExtraction(src, &src, cv::COLOR_BGR2HSV, 0, &param.extractColorHSV[0], &param.extractColorHSV[3]);
		}
		// グレースケールに変換(RGBA->Y)
		cv::cvtColor(src, src, cv::COLOR_BGR2GRAY, 1);
		// 輪郭内の塗りつぶし(色抽出してなければ全面塗りつぶされる)
		if (param.mFillInnerContour) {
			findContours(src, outlines, cv::RETR_EXTERNAL);
			// 見つかった輪郭を塗りつぶす
			cv::drawContours(src, outlines, -1, COLOR_WHITE, cv::FILLED);
		}
		// 平滑化
//		cv::Sobel(src, src, CV_32F, 1, 1);
//		cv::convertScaleAbs(src, src, 1, 0);
		if (param.mSmoothType) {
			static const double sigma = 3.0;	// FIXME これはパラメータにする?
			const int ksize = (int)(sigma * 5) | 1;	// カーネルサイズ, 正の奇数かゼロでないとだめ(ゼロの時はsigmaから内部計算)
			switch (param.mSmoothType) {
			case SMOOTH_GAUSSIAN:
				cv::GaussianBlur(src, src, cv::Size(ksize, ksize), sigma, sigma);
				break;
			case SMOOTH_MEDIAN:
				cv::medianBlur(src, src, ksize);
				break;
			case SMOOTH_BLUR:
				cv::blur(src, src, cv::Size(ksize, ksize));
				break;
			case SMOOTH_DILATION:
				cv::dilate(src, src, cv::Mat());
				break;
			default:
				break;
			}
		}
		// FIXME 平滑化後に2値化が必要?
//		if (param.mSmoothType) {
//		// 2値化
//			cv::adaptiveThreshold(src, src, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY, 7, 0);
//		}
		// 2値化
//		cv::threshold(src, src, 125, 255, cv::THRESH_BINARY);
//		cv::threshold(src, src, 200, 255, cv::THRESH_BINARY_INV);
		// 細線化
		if (param.mMaxThinningLoop) {
			mThinning.resize(src.cols, src.rows);
			if (outlines.empty()) {
				// 輪郭内塗りつぶしをしてない時は最外形を取得してないのでここで取得
				findContours(src, outlines, cv::RETR_EXTERNAL);
			}
			cv::threshold(src, src, 10, 255, CV_THRESH_BINARY);
			for (auto outline = outlines.begin(); outline != outlines.end(); outline++) {
				// 外接四角を取得
				cv::Rect bounds = cv::boundingRect(*outline);
				// ROIを作成
				cv::Mat roi = src(bounds);
				// ROIに対して細線化
				mThinning.apply(roi, roi, param.mMaxThinningLoop);
			}
		}
		outlines.clear();
		// エッジ検出(Cannyの結果は2値化されてる)
		if (param.mEnableCanny) {
			cv::Canny(src, src, param.mCannyThreshold1, param.mCannyThreshold2);
		}

		// 表示用にカラー画像に戻す
		if (param.needs_result) {
			if (param.show_src) {
				result = frame;
			} else {
				cv::cvtColor(src, result, cv::COLOR_GRAY2RGBA);
			}
		}

	} catch (cv::Exception e) {
		LOGE("pre_process failed:%s", e.msg.c_str());
		res = -1;
	}

    RETURN(res, int);
}

// 最大輪郭数
#define MAX_CONTOURS 100

/** 輪郭線を検出 */
/*protected*/
int IPPreprocess::findPossibleContours(cv::Mat &src, cv::Mat &result,
	std::vector<std::vector< cv::Point>> &contours,	// 輪郭データ
	std::vector<DetectRec_t> &approxes,	// 近似輪郭データ
	const DetectParam_t &param) {

	ENTER();

	std::stringstream ss;
	DetectRec_t possible;
	std::vector<cv::Vec4i> hierarchy;
	std::vector< cv::Point > convex, approx;		// 近似輪郭
	cv::Point2f vertices[4];
	const float areaErrLimit2Min = 1.0f / param.mAreaErrLimit2;

	// 輪郭を求める
	findContours(src, contours, hierarchy, cv::RETR_CCOMP, cv::CHAIN_APPROX_NONE);
//	findContours(src, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE);
	// 検出した輪郭の数分ループする
	int idx = -1, cnt = 0;
	for (auto contour = contours.begin(); contour != contours.end(); contour++) {
		idx++;
		if (hierarchy[idx][3] != -1) continue;	// 一番外側じゃない時
		// 凸包図形にする
		convex.clear();
		approx.clear();
		cv::convexHull(*contour, convex);
		const size_t num_vertex = convex.size();
		if (LIKELY(num_vertex < 4)) continue;	// 3角形はスキップ
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = cv::minAreaRect(convex);
		// 常に横長として幅と高さを取得
		const float w = fmax(area_rect.size.width, area_rect.size.height);	// 最小矩形の幅=長軸長さ
		const float h = fmin(area_rect.size.width, area_rect.size.height);	// 最小矩形の高さ=短軸長さ
		const float a = w * h;	// 最小矩形の面積
		// 外周線または最小矩形が小さすぎるか大きすぎるのはスキップ
		if (((w > 620) && (h > 350)) || (a < param.mAreaLimitMin) || (a > param.mAreaLimitMax)) continue;
		if (param.show_detects) {
			cv::drawContours(result, contours, idx, COLOR_YELLOW);	// 輪郭
		}
		// 凸包図形の面積を計算
		const float area_convex = (float)cv::contourArea(convex);
		// 面積が小さすぎるのと大きすぎるのはスキップ
		if ((area_convex < param.mAreaLimitMin) || (area_convex > param.mAreaLimitMax)) continue;
		if (param.show_detects) {
			cv::polylines(result, convex, true, COLOR_GREEN);	// 凸包
		}
		// 輪郭近似精度(元の輪郭と近似曲線との最大距離)を計算
		const double epsilon = param.mApproxType == APPROX_RELATIVE
			? param.mApproxFactor * cv::arcLength(*contour, true)	// 周長に対する比
			: param.mApproxFactor;								// 絶対値
		// 輪郭を近似する
		cv::approxPolyDP(*contour, approx, epsilon, true);	// 閉曲線にする
		// 最小矩形の面積が凸包図形面積より指定値以上大きければスキップ=凹凸が激しい
		if (a / area_convex > param.mAreaErrLimit1) {
			// 輪郭の面積を計算・・・XXX 輪郭が画面の左端または上端からはみ出していると正しく計算出来ないみたい
			const float area = (float)cv::contourArea(approx);
			if (param.show_detects) {
				clear_stringstream(ss);
				ss << std::setw(5) << (int)a << ':' << std::setw(5) << (int)area;
				cv::putText(result, ss.str(), area_rect.center, cv::FONT_HERSHEY_SIMPLEX, 0.5f, COLOR_GREEN);
			}
			const float rate = a / area;
			if ((rate < areaErrLimit2Min) || (rate > param.mAreaErrLimit2))
				continue;
		}
		if (param.show_detects) {
			cv::polylines(result, approx, true, COLOR_YELLOW, 2);
		}
		if (UNLIKELY(++cnt > MAX_CONTOURS)) break;
		if (param.show_detects) {
			cv::polylines(result, approx, true, COLOR_GREEN, 2);
		}

		possible.clear();
		possible.type = TYPE_NON;
		possible.moments = cv::moments(approx);
		if (possible.moments.m00 != 0.0f) {
			possible.center.x = possible.moments.m10 / possible.moments.m00;
			possible.center.y = possible.moments.m01 / possible.moments.m00;
		} else {
			possible.center.x = possible.center.y = 0.0f;
		}
		possible = approx; // *contour;
		possible.area_rect = area_rect;	// 最小矩形
		possible.ellipse.center.x = possible.ellipse.center.y = possible.ex = possible.ey = 0.0f;
		possible.area_rate = w * h / area_convex;		// 凸包図形面積に対する最小矩形の面積比
		possible.area = area_convex;					// 凸包図形の面積
		possible.aspect = h != 0.0f ? w / h : 0.0f;		// 最小矩形のアスペクト比
		possible.length = w;				// 最小矩形の長軸長さ
		possible.width = h;					// 最小矩形の短軸長さ
		possible.analogous = 0.0f;
		possible.curvature = possible.ex = possible.ey = 0.0f;
		approxes.push_back(possible);
	}

	RETURN(0, int);
}
