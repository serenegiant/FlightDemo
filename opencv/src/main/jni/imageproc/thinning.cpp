//
// Created by saki on 16/03/28.
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

#include <jni.h>

#include "utilbase.h"
#include "common_utils.h"

#include "Thinning.h"


Thinning::Thinning(const int &width, const int &height)
#if USE_FILTER2D
:	src_w(height, width, CV_32FC1),
	src_b(height, width, CV_32FC1),
	src_f(height, width, CV_32FC1)
#endif
{
	ENTER();

#if USE_FILTER2D
	// 白黒それぞれ8個のカーネルを準備
	kpb[0] = ( cv::Mat_<float>(3,3) << 1,1,0,1,0,0,0,0,0 );
	kpb[1] = ( cv::Mat_<float>(3,3) << 1,1,1,0,0,0,0,0,0 );
	kpb[2] = ( cv::Mat_<float>(3,3) << 0,1,1,0,0,1,0,0,0 );
	kpb[3] = ( cv::Mat_<float>(3,3) << 0,0,1,0,0,1,0,0,1 );
	kpb[4] = ( cv::Mat_<float>(3,3) << 0,0,0,0,0,1,0,1,1 );
	kpb[5] = ( cv::Mat_<float>(3,3) << 0,0,0,0,0,0,1,1,1 );
	kpb[6] = ( cv::Mat_<float>(3,3) << 0,0,0,1,0,0,1,1,0 );
	kpb[7] = ( cv::Mat_<float>(3,3) << 1,0,0,1,0,0,1,0,0 );

	kpw[0] = ( cv::Mat_<float>(3,3) << 0,0,0,0,1,1,0,1,0 );
	kpw[1] = ( cv::Mat_<float>(3,3) << 0,0,0,0,1,0,1,1,0 );
	kpw[2] = ( cv::Mat_<float>(3,3) << 0,0,0,1,1,0,0,1,0 );
	kpw[3] = ( cv::Mat_<float>(3,3) << 1,0,0,1,1,0,0,0,0 );
	kpw[4] = ( cv::Mat_<float>(3,3) << 0,1,0,1,1,0,0,0,0 );
	kpw[5] = ( cv::Mat_<float>(3,3) << 0,1,1,0,1,0,0,0,0 );
	kpw[6] = ( cv::Mat_<float>(3,3) << 0,1,0,0,1,1,0,0,0 );
	kpw[7] = ( cv::Mat_<float>(3,3) << 0,0,0,0,1,1,0,0,1 );
#endif

	EXIT();
}

Thinning::~Thinning() {
	ENTER();

	EXIT();
}

#if USE_FILTER2D
int Thinning::apply(cv::Mat &src, cv::Mat &dst, const int &max_loop) {

	ENTER();

	// 原画像を2値化(しきい値は用途に合わせて考える)
    // src_f:2値化した画像(32F)
    // src_w:作業バッファ
    // src_b:作業バッファ(反転)
	src.convertTo(src_f, CV_32FC1);
	src_f /= 255;	//	src_f.mul(1./255.);
    cv::threshold(src_f, src_f, 0.5, 1.0, CV_THRESH_BINARY);
	cv::threshold(src_f, src_w, 0.5, 1.0, CV_THRESH_BINARY);
	cv::threshold(src_f, src_b, 0.5, 1.0, CV_THRESH_BINARY_INV);

	double sum;
	for (int i = 0; i < max_loop; i++) {
		sum = 0.0;
		cv::filter2D(src_w, src_w, CV_32FC1, kpw[i]);
		cv::filter2D(src_b, src_b, CV_32FC1, kpb[i]);
		// 各カーネルで注目するのは3画素ずつなので、マッチした注目画素の濃度は3となる
		// カーネルの値を1/9にしておけば、しきい値は0.99で良い
		cv::threshold(src_w, src_w, 2.99, 1.0, CV_THRESH_BINARY);
		cv::threshold(src_b, src_b, 2.99, 1.0, CV_THRESH_BINARY);
		cv::bitwise_and(src_w, src_b, src_w);
		//この時点でのsrc_wが消去候補点となり、全カーネルで候補点が0となった時に処理が終わる
		sum += cv::sum(src_w).val[0];
		//原画像から候補点を消去(二値画像なのでXor)
		cv::bitwise_xor(src_f, src_w, src_f);
		if (sum <= 0) break;
		//作業バッファを更新
		src_f.copyTo(src_w);
		cv::threshold(src_f, src_b, 0.5, 1.0, CV_THRESH_BINARY_INV);
	}
	src_f.convertTo(dst, CV_8UC1);	// CV_8UC4でいいんかな?
	dst *= 255;

	RETURN(sum > 0.0 ? 1 : 0, int);
}

#else
/**
 * Perform one thinning iteration.
 * Normally you wouldn't call this function directly from your code.
 *
 * @param  im    Binary image with range = 0-1
 * @param  iter  0=even, 1=odd
 */
static void thinningIteration(cv::Mat &im, int iter) {
	cv::Mat marker = cv::Mat::zeros(im.size(), CV_8UC1);

	for (int i = 1; i < im.rows-1; i++) {
		for (int j = 1; j < im.cols-1; j++) {
			uchar p2 = im.at<uchar>(i-1, j);
			uchar p3 = im.at<uchar>(i-1, j+1);
			uchar p4 = im.at<uchar>(i, j+1);
			uchar p5 = im.at<uchar>(i+1, j+1);
			uchar p6 = im.at<uchar>(i+1, j);
			uchar p7 = im.at<uchar>(i+1, j-1);
			uchar p8 = im.at<uchar>(i, j-1);
			uchar p9 = im.at<uchar>(i-1, j-1);

			int A  = (p2 == 0 && p3 == 1) + (p3 == 0 && p4 == 1) +
					(p4 == 0 && p5 == 1) + (p5 == 0 && p6 == 1) +
					(p6 == 0 && p7 == 1) + (p7 == 0 && p8 == 1) +
					(p8 == 0 && p9 == 1) + (p9 == 0 && p2 == 1);
			int B  = p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9;
			int m1 = iter == 0 ? (p2 * p4 * p6) : (p2 * p4 * p8);
			int m2 = iter == 0 ? (p4 * p6 * p8) : (p2 * p6 * p8);

			if (A == 1 && (B >= 2 && B <= 6) && m1 == 0 && m2 == 0)
				marker.at<uchar>(i,j) = 1;
		}
	}

	im &= ~marker;
}

void Thinning::resize(const int &width, const int &height) {
#if USE_FILTER2D
	src_w.create(height, width, CV_32FC1);
	src_b.create(height, width, CV_32FC1);
	src_f.create(height, width, CV_32FC1);
#endif
}

int Thinning::apply(cv::Mat &src, cv::Mat &dst, const int &max_loop) {
	ENTER();

	dst = src / 255;

	cv::Mat prev = cv::Mat::zeros(dst.size(), CV_8UC1);
	cv::Mat diff;

	int cnt = 1;
	for (int i = 0; i < max_loop; i++) {
		if (!cnt) break;
		thinningIteration(dst, 0);
		thinningIteration(dst, 1);
		cv::absdiff(dst, prev, diff);
		dst.copyTo(prev);
		cnt = cv::countNonZero(diff);
	}

	dst *= 255;

	RETURN(cnt, int);
}
#endif