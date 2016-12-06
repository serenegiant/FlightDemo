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

#include "IPDetectorLine.h"

static double HU_MOMENTS[] = {
//	0.383871,0.119557,0.000044,0.000022,0.000000,0.000008,0.000000
	3.673166e-01,1.071715e-01,1.763543e-04,9.209628e-05,1.173179e-08,3.007932e-05,-3.488433e-10
};


// 検出したオブジェクトの優先度の判定
// 第1引数が第2引数よりも小さい(=前にある=優先度が高い)時に真(正)を返す
static bool comp_priority(const DetectRec_t *left, const DetectRec_t *right) {
	// 類似性(小さい方, 曲線だと大きくなってしまう)
	const bool b2 = left->analogous < right->analogous;
	// 近似輪郭と実輪郭の面積比(小さい方, 曲線だと大きくなってしまう)
	const bool b3 = left->area_rate < right->area_rate;
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
IPDetectorLine::IPDetectorLine() : IPDetector() {
	ENTER();

	EXIT();
}

IPDetectorLine::~IPDetectorLine() {
	ENTER();

	EXIT();
}

int IPDetectorLine::detect(
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
		// アスペクト比が正方形に近いものはスキップ
		if ((rec->aspect < param.mMinLineAspect) && (rec->area_rate > 1.2f)) continue;
		if (param.show_detects) {
			cv::polylines(result_frame, rec->contour, true, COLOR_ORANGE, 2);
		}

		// 最小矩形と元輪郭の面積比が大き過ぎる場合スキップ
		if ((rec->area_rate > 1.2f) && (rec->contour.size() > 6)) continue;
//		if (rec->area_rate > 1.5f) continue;
//		if (rec->area_rate > 1.75f) continue;
//		if (rec->area_rate > 2.0f) continue;
		if (param.show_detects) {
			cv::polylines(result_frame, rec->contour, true, COLOR_ACUA, 2);
		}
#if CALC_COEFFS
		// 細線化して3次スプライン近似
		if (calcCoeffs(work, rec->contour, rec->coeffs)) continue;
		if (param.show_detects) {
			drawSpline(result_frame);
		}
#endif
		// 輪郭のHu momentを計算
		cv::HuMoments(rec->moments, hu_moments);
		// 基準値と比較, メソッド1は時々一致しない, メソッド2,3だとほとんど一致しない, 完全一致なら0が返る
		const float analogous = (float)compHuMoments(HU_MOMENTS, hu_moments, 1);
		// Hu momentsが基準値との差が大きい時はスキップ
//		if (analogous < param.mMaxAnalogous) {
			// ラインの可能性が高い輪郭を追加
			rec->analogous = analogous;
			possibles.push_back(rec);
			if (param.show_detects) {
				cv::polylines(result_frame, rec->contour, true, COLOR_BLUE, 2);
			}
//		}
	}
	// 優先度の最も高いものを選択する
	if (possibles.size() > 0) {
		if (possibles.size() > 1) {
			// 優先度の降順にソートする
			std::sort(possibles.begin(), possibles.end(), comp_priority);
		}
		result = *(*possibles.begin());	// 先頭=優先度が最高
		result.type = TYPE_LINE;
		result.curvature = result.ex = result.ey = 0.0f;
	} else {
		result.type = TYPE_NON;
	}
	possibles.clear();

	RETURN(0, int);
}

//********************************************************************************
// 線分の検出処理(昔のテストコード)
//********************************************************************************
#if 0
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
#endif
