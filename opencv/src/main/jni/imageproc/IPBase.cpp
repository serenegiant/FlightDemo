//
// Created by saki on 16/03/30.
//

#include "utilbase.h"

#include "IPBase.h"

const cv::Scalar IPBase::COLOR_YELLOW = cv::Scalar(255, 255, 0);
const cv::Scalar IPBase::COLOR_GREEN = cv::Scalar(0, 255, 0);
const cv::Scalar IPBase::COLOR_ORANGE = cv::Scalar(255, 127, 0);
const cv::Scalar IPBase::COLOR_ACUA = cv::Scalar(0, 255, 255);
const cv::Scalar IPBase::COLOR_PINK = cv::Scalar(255, 127, 255);
const cv::Scalar IPBase::COLOR_BLUE = cv::Scalar(0, 0, 255);
const cv::Scalar IPBase::COLOR_RED = cv::Scalar(255, 0, 0);
const cv::Scalar IPBase::COLOR_WHITE = cv::Scalar(255, 255, 255);
const cv::Scalar IPBase::COLOR_BLACK = cv::Scalar(0, 0, 0);

IPBase::IPBase() {
	ENTER();

	EXIT();
}

IPBase::~IPBase() {
	ENTER();

	EXIT();
}

void IPBase::findContours(cv::Mat &src, std::vector<std::vector< cv::Point>> &contours,
	const cv::RetrievalModes &mode, const cv::ContourApproximationModes &method) {

	ENTER();

	// 一回り大きな画像を用意,黒で塗りつぶす
	cv::Mat new_src = cv::Mat::zeros(cv::Size(src.cols + 16, src.rows + 16), CV_8UC3);
	// 移動行列(8ピクセルずつずらす)
	cv::Mat affine = (cv::Mat_<double>(2,3)<<1.0, 0.0, 8, 0.0, 1.0, 8);
	// 元画像を8ピクセルずつずらして書き込む
	cv::warpAffine(src, new_src, affine, new_src.size(), CV_INTER_LINEAR, cv::BORDER_TRANSPARENT);
	// 元の画像領域を示すROI領域を作成
	cv::Rect roi_rect(8, 8, src.cols, src.rows); // x,y,w,h
	src = new_src(roi_rect);
	cv::findContours(src, contours,
		mode, 		// RETR_EXTERNAL:輪郭検出方法は外形のみ, RETR_LIST:階層なし, RETR_CCOMP:2階層, RETR_TREE:階層
		method);	// 輪郭データ近似方法, CHAIN_APPROX_NONE:無し,  CHAIN_APPROX_SIMPLE:直線は頂点のみにする,  CHAIN_APPROX_TC89_L1, CHAIN_APPROX_TC89_KCOS

	EXIT();
}

void IPBase::findContours(cv::Mat &src, std::vector<std::vector< cv::Point>> &contours, std::vector<cv::Vec4i> &hierarchy,
	const cv::RetrievalModes &mode, const cv::ContourApproximationModes &method) {

	ENTER();

	// 一回り大きな画像を用意,黒で塗りつぶす
	cv::Mat new_src = cv::Mat::zeros(cv::Size(src.cols + 16, src.rows + 16), CV_8UC3);
	// 移動行列(8ピクセルずつずらす)
	cv::Mat affine = (cv::Mat_<double>(2,3)<<1.0, 0.0, 8, 0.0, 1.0, 8);
	// 元画像を8ピクセルずつずらして書き込む
	cv::warpAffine(src, new_src, affine, new_src.size(), CV_INTER_LINEAR, cv::BORDER_TRANSPARENT);
	// 元の画像領域を示すROI領域を作成
	cv::Rect roi_rect(8, 8, src.cols, src.rows); // x,y,w,h
	src = new_src(roi_rect);
	cv::findContours(src, contours, hierarchy,
		mode, 		// RETR_EXTERNAL:輪郭検出方法は外形のみ, RETR_LIST:階層なし, RETR_CCOMP:2階層, RETR_TREE:階層
		method);	// 輪郭データ近似方法, CHAIN_APPROX_NONE:無し,  CHAIN_APPROX_SIMPLE:直線は頂点のみにする,  CHAIN_APPROX_TC89_L1, CHAIN_APPROX_TC89_KCOS

	EXIT();
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
int IPBase::colorExtraction(const cv::Mat &src, cv::Mat *dst,
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
			// 指定したHSV範囲からLUT(Look Up Table)を作成・・・これは設定変えた時だけでいい
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

// stringstreamをクリアして再利用できるようにする
void IPBase::clear_stringstream(std::stringstream &ss) {
	static const std::string empty_string;

	ss.str(empty_string);
	ss.clear();
	ss << std::dec;     // clear()でも元に戻らないので、毎回指定する。
}

// RotatedRectを指定線色で描画する
void IPBase::draw_rect(cv::Mat img, cv::RotatedRect rect, cv::Scalar color) {
	cv::Point2f vertices[4];
	rect.points(vertices);
	for (int i = 0; i < 4; i++) {
		cv::line(img, vertices[i], vertices[(i+1)%4], color);
	}
}

/** Hu momentsを比較
 * methodは1〜3
 * cv::matchShapesが画像を引数にしているのを計算済みのHu momentsを渡せるようにしただけ
 * @return 0: 完全一致
 */
double IPBase::compHuMoments(const double ma[], const double mb[], int method) {
	static const double eps = 1.e-5;

    int sma, smb;
    double mmm;
    double result = 0;

    switch (method) {
    case 1:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(ma[i]);
            double amb = fabs(mb[i]);

			sma = sign(ma[i]);
			smb = sign(mb[i]);

            if (ama > eps && amb > eps) {
                ama = 1. / (sma * log10(ama));
                amb = 1. / (smb * log10(amb));
                result += fabs(-ama + amb);
            }
        }
        break;

    case 2:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(ma[i]);
            double amb = fabs(mb[i]);

			sma = sign(ma[i]);
			smb = sign(mb[i]);

            if (ama > eps && amb > eps) {
                ama = sma * log10(ama);
                amb = smb * log10(amb);
                result += fabs(-ama + amb);
            }
        }
        break;

    case 3:
        for (int i = 0; i < 7; i++ ) {
            double ama = fabs(ma[i]);
            double amb = fabs(mb[i]);

			sma = sign(ma[i]);
			smb = sign(mb[i]);

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
