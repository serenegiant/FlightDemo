//
// Created by saki on 16/03/30.
//

#include "utilbase.h"

#include "IPDetectorLine.h"

static double HU_MOMENTS[] = {
//	0.383871,0.119557,0.000044,0.000022,0.000000,0.000008,0.000000
	3.673166e-01,1.071715e-01,1.763543e-04,9.209628e-05,1.173179e-08,3.007932e-05,-3.488433e-10
};


// 検出したオブジェクトの優先度の判定
// 第1引数が第2引数よりも小さい(=前にある=優先度が高い)時に真(正)を返す
static bool comp_line_priority(const DetectRec &left, const DetectRec &right) {
//	// 頂点1つあたりの面積の比較(大きい方)
//	const bool b1 = left.area_vertex > right.area_vertex;
	// 類似性(小さい方, 曲線だと大きくなってしまう)
	const bool b2 = left.analogous < right.analogous;
	// 近似輪郭と実輪郭の面積比(小さい方, 曲線だと大きくなってしまう)
	const bool b3 = left.area_rate < right.area_rate;
	// アスペクト比の比較(大きい方)
	const bool b4 = left.aspect > right.aspect;
	// 長さの比較(大きい方)
	const bool b5 = left.length > right.length;
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

// 直線検知
#define DETECT_LINES 0

//********************************************************************************
//********************************************************************************
IPDetectorLine::IPDetectorLine() {
	ENTER();

	EXIT();
}

IPDetectorLine::~IPDetectorLine() {
	ENTER();

	EXIT();
}

int IPDetectorLine::detect(
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	cv::Mat &result_frame,				// 結果書き込み用Mat
	DetectRec_t &possible,				// 結果
	const DetectParam_t &param) {		// パラメータ

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

	// 検出した輪郭の数分ループする
	for (auto iter = contours.begin(); iter != contours.end(); iter++) {
		DetectRec_t rec = *iter;		// 輪郭レコード
		// 輪郭を内包する最小矩形(回転あり)を取得
		cv::RotatedRect area_rect = rec.area_rect;
		// アスペクト比が正方形に近いものはスキップ
		if (LIKELY(rec.aspect < param.mMinLineAspect)) continue;
		if (param.show_detects) {
			cv::polylines(result_frame, rec.contour, true, COLOR_ORANGE, 2);
//			draw_rect(result_frame, area_rect, COLOR_ORANGE);
		}
		// 最小矩形と元輪郭の面積比が大き過ぎる場合スキップ
		if ((rec.area_rate < 0.67f) && (rec.area_rate > 1.5f)) continue;	// ±50%以上ずれている時はスキップ
//		if ((rec.area_rate < 0.5f) && (rec.area_rate > 2.0f)) continue;	// ±100%以上ずれている時はスキップ
		const float area_vertex = rec.area / rec.contour.size();
		// 面積の割に頂点が多いものもスキップ これを入れるとエッジがギザギザの時に検出できなくなる
//		if (area_vertex < 200.0f) continue;		// 1頂点あたり200ピクセルよりも小さい
		if (param.show_detects) {
			cv::polylines(result_frame, rec.contour, true, COLOR_ACUA, 2);
//			draw_rect(result_frame, area_rect, COLOR_ACUA);
		}
		// 輪郭のHu momentを計算
		cv::HuMoments(cv::moments(rec.contour), hu_moments);
		// 基準値と比較, メソッド1は時々一致しない, メソッド2,3だとほとんど一致しない, 完全一致なら0が返る
		const float analogous = (float)compHuMoments(HU_MOMENTS, hu_moments, 1);
		// Hu momentsが基準値との差が大きい時はスキップ
//		if (analogous < param.mMaxAnalogous) {
			// ラインの可能性が高い輪郭を追加
			possible.type = TYPE_LINE;
			possible.contour.assign(rec.contour.begin(), rec.contour.end());
			possible.area_rect = rec.area_rect;
			possible.area = rec.area;
			possible.area_rate = rec.area_rate;
			possible.aspect = rec.aspect;
			possible.length = rec.length;	// 長軸長さ
			possible.width = rec.width;		// 短軸長さ
			possible.analogous = analogous;
			possibles.push_back(possible);
			if (param.show_detects) {
				cv::polylines(result_frame, rec.contour, true, COLOR_BLUE, 2);
//				draw_rect(result_frame, area_rect, COLOR_BLUE);
			}
//		}
	}
	// 優先度の最も高いものを選択する
	if (possibles.size() > 0) {
		// 優先度の降順にソートする
		std::sort(possibles.begin(), possibles.end(), comp_line_priority);
		possible = *possibles.begin();	// 先頭=優先度が最高
		possible.type = TYPE_LINE;
		possible.curvature = possible.ex = possible.ey = 0.0f;
		// 近似輪郭の面積と最小矩形の面積の比が大きい時は曲がっているかもしれないので楕円フィッティングして曲率を計算してみる
		if ((possible.area_rate > 1.2f) && (possible.contour.size() > 6)) {	// 5点以上あれば楕円フィッティング出来るけど7点以上にする
			try {
				cv::RotatedRect ellipse = cv::fitEllipse(possible.contour);
				// 長軸/短軸長さなので1/2にして半径相当の値にする
				ellipse.size.width /= 2.0f;
				ellipse.size.height /= 2.0f;
//				LOGI("fit ellipse:(%f,%f),%f", ellipse.size.width, ellipse.size.height, ellipse.angle);
				const double a = fmax(ellipse.size.width, ellipse.size.height);
				if (a > 0) {
					const double b = fmin(ellipse.size.width, ellipse.size.height);
					possible.curvature = (float)(b / a / a);
					possible.ex = ellipse.center.x;
					possible.ey = ellipse.center.y;
//					LOGI("fit ellipse:(%f,%f),%f,%f", ellipse.size.width, ellipse.size.height, ellipse.angle, possible.curvature);
					if (param.show_detects) {
						cv::ellipse(result_frame, ellipse.center, ellipse.size, ellipse.angle, 0, 360, COLOR_RED);
					}
				}
			} catch (cv::Exception e) {
				LOGE("fitEllipse failed:%s", e.msg.c_str());
			}
		}
		if (param.show_detects) {
			// ラインとして検出した輪郭線を赤で描画する
			cv::polylines(result_frame, possible.contour, true, COLOR_RED, 2);	// 赤色
			// 中央から検出したオブジェクトの中心に向かって線を引く
			cv::line(result_frame, cv::Point(width() >> 1, height() >> 1), possible.area_rect.center, COLOR_RED, 8, 8);
		}
	} else {
		possible.type = TYPE_NON;
	}

	RETURN(0, int);
}
