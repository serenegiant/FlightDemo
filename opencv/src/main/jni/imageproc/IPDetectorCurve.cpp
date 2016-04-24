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

#include "IPDetectorCurve.h"

// 検出したオブジェクトの優先度の判定
// 第1引数が第2引数よりも小さい(=前にある=優先度が高い)時に真(正)を返す
static bool comp_priority(const DetectRec_t *left, const DetectRec_t *right) {
	// 類似性(小さい方, 曲線だと大きくなってしまう)
	const bool b2 = left->analogous > right->analogous;
	// 近似輪郭と実輪郭の面積比(小さい方, 曲線だと大きくなってしまう)
	const bool b3 = left->area_rate > right->area_rate;
	// アスペクト比の比較(大きい方)
	const bool b4 = left->aspect > right->aspect;
	// 長さの比較(大きい方)
	const bool b5 = left->length > right->length;
	return
		(b5 && b4)					// 長くてアスペクト比が大きい
		|| (b5 && b4 && b3 && b2)	// 長くてアスペクト比が大きくて面積比が小さくて類似性が良い
		|| (b5 && b4 && b3)			// 長くてアスペクト比が大きくて面積比が小さい
		|| (b4 && b3 && b2)			// アスペクト比が大きくて面積比が小さくて類似性が良い
		|| (b4 && b3)				// アスペクト比が大きくて面積比が小さい
		|| (b4 && b2)				// アスペクト比が大きくて類似性が良い
		|| (b5 && b3 && b2)			// 長くて面積比が小さくて類似性が良い
		|| (b5 && b3)				// 長くて面積比が小さい
		|| (b5 && b2)				// 長くて類似性が良い
		|| (b3 && b2)				// 面積比が小さくて類似性良い
		|| (b5)						// 長い
		|| (b4)						// アスペクト比が大きい
		|| (b3)						// 面積比が小さくい
		|| (b2);					// 類似性が良い
}

//********************************************************************************
//********************************************************************************
IPDetectorCurve::IPDetectorCurve() {
	ENTER();

	EXIT();
}

IPDetectorCurve::~IPDetectorCurve() {
	ENTER();

	EXIT();
}

int IPDetectorCurve::detect(
	cv::Mat &src,						// 解析画像
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	std::vector<const DetectRec_t *> &possibles,	// ワーク用
	cv::Mat &result_frame,				// 結果書き込み用Mat
	DetectRec_t &result,				// 結果
	const DetectParam_t &param) {		// パラメータ

	ENTER();

	double hu_moments[8];
//	std::vector<const DetectRec_t *> possibles;		// 可能性のある輪郭
	possibles.clear();

#if CALC_COEFFS
	cv::Mat work = src;
	cv::threshold(work, work, 10, 255, CV_THRESH_BINARY);
#endif
	// 検出した輪郭の数分ループする
	for (auto iter = contours.begin(); iter != contours.end(); iter++) {
		DetectRec_t *rec = &(*iter);		// 輪郭レコード
//		if (LIKELY(rec->aspect > param.mMinLineAspect)) continue;
		if (param.show_detects) {
			cv::polylines(result_frame, rec->contour, true, COLOR_ORANGE, 2);
		}
		// 近似輪郭の面積と最小矩形の面積の比が大きい時は曲がっているかもしれない
		if ((rec->area_rate < 1.2f) || (rec->contour.size() < 7)) continue;
		rec->curvature = rec->ex = rec->ey = 0.0f;
		try {
			rec->ellipse = cv::fitEllipse(rec->contour);
			// 長軸/短軸長さなので1/2にして半径相当の値にする
			rec->ellipse.size.width /= 2.0f;
			rec->ellipse.size.height /= 2.0f;
			const double a = fmax(rec->ellipse.size.width, rec->ellipse.size.height);
			if (a > 0) {
				const double b = fmin(rec->ellipse.size.width, rec->ellipse.size.height);
				rec->curvature = (float)(b / a / a);
				rec->ex = rec->ellipse.center.x;
				rec->ey = rec->ellipse.center.y;
			} else {
				continue;
			}
		} catch (cv::Exception e) {
			// 楕円フィッティング出来なかった
			continue;
		}
		if (param.show_detects) {
			cv::polylines(result_frame, rec->contour, true, COLOR_ACUA, 2);
		}
#if CALC_COEFFS
		// 細線化して3次スプライン近似
		if (calcCoeffs(work, rec->contour, rec->coeffs)) continue;
		if (param.show_detects) {
//			drawSpline(result_frame);
		}
#endif
		// ラインの可能性が高い輪郭を追加
		possibles.push_back(rec);
		if (param.show_detects) {
			cv::polylines(result_frame, rec->contour, true, COLOR_BLUE, 2);
		}
	}
	// 優先度の最も高いものを選択する
	if (possibles.size() > 0) {
		if (possibles.size() > 1) {
			// 優先度の降順にソートする
			std::sort(possibles.begin(), possibles.end(), comp_priority);
		}
		result = *(*possibles.begin());	// 先頭=優先度が最高
		result.type = TYPE_CURVE;
	} else {
		result.type = TYPE_NON;
	}
	possibles.clear();

	RETURN(0, int);
}
