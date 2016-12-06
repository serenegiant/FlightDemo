//
// Created by saki on 16/03/31.
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
#include "IPDetector.h"

IPDetector::IPDetector()
#if CALC_COEFFS
:	mThinning(2, 2)
#endif
{
	ENTER();

	EXIT();
}

IPDetector::~IPDetector() {
	ENTER();

	EXIT();
}

void IPDetector::resize(const int &width, const int &height) {
	if ((mWidth != width) || (mHeight != height)) {
		mWidth = width;
		mHeight = height;
#if CALC_COEFFS
		mThinning.resize(width, height);
#endif
	}
}

int IPDetector::calcCoeffs(cv::Mat &work, const std::vector< cv::Point> &contour, std::vector<Coeff4_t> &coeffs) {
	ENTER();

#if CALC_COEFFS
	int result = -1;
	coeffs.clear();

	// 外接四角を取得
	cv::Rect bounds = cv::boundingRect(contour);
	// ROIを作成
	cv::Mat roi = work(bounds);
	// ROIに対して細線化
	mThinning.apply(roi, roi, 8);

	std::vector<std::vector< cv::Point>> local_contours;
	findContours(work, local_contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE);
	if (local_contours.size() >= 1) {
		std::vector< cv::Point> local;
		float area = 0.0f;
		int vertex_num = 0;
		for (auto iter2 = local_contours.begin(); iter2 != local_contours.end(); iter2++) {
			if ((*iter2).size() > 1) {
				// 面積が大きいか頂点数の多いのを選ぶ
				const float a = (float)cv::contourArea(*iter2);
				if ((a > area) || ((*iter2).size() > vertex_num)) {
					local = *iter2;
					vertex_num = (*iter2).size();
					area = a;
				}
			}
		}
		if ((area >= 0.0f) && (local.size() >= 2)) {
			result = mCubicSpline.reset(local);
		} else {
			LOGD("輪郭が見つからなかった:area=%f,size=%d", area, local.size());
		}
		if (!result) {
			coeffs = mCubicSpline.getCoeffs();
		} else {
			LOGD("3次スプライン係数の初期化に失敗した");
		}
	} else {
		LOGD("local coutors is zero");
	}
	RETURN(result, int);
#else
	RETURN(0, int);
#endif
}

void IPDetector::drawSpline(cv::Mat &dst) {
#if CALC_COEFFS
	if (mCubicSpline.getCoeffs().size() > 0) {
		const int n = mCubicSpline.getDataNum();
		if (n > 1) {
			cv::Point2f w = mCubicSpline.getValue(0, 0);
			for (int i = 0; i < n - 1; i++) {
				for (int j = 0; j < 3; ++j) {
					cv::Point2f v = mCubicSpline.getValue(i, j / 3.0f);
					cv::line(dst, w, v, COLOR_PINK);
					w = v;
				}
			}
		}
	}
#endif
}
