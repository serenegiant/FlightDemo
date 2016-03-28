//
// Created by saki on 16/03/28.
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

#include "utilbase.h"
#include "common_utils.h"

#include "Thinning.h"

Thinning::Thinning(const int &width, const int &height)
:	kpb(new cv::Mat[8]),
	kpw(new cv::Mat[8]),
	src_w(height, width, CV_32FC1),
	src_b(height, width, CV_32FC1),
	src_f(height, width, CV_32FC1) {
	ENTER();

	init(width, height);

	EXIT();
}

Thinning::~Thinning() {
	ENTER();

	SAFE_DELETE(kpb);
	SAFE_DELETE(kpw);

	EXIT();
}

void Thinning::init(const int &width, const int &height) {
	ENTER();

	// 白黒それぞれ8個のカーネルを準備
	kpb[0]=(cv::Mat_<float>(3,3) << 1,1,0,1,0,0,0,0,0);
	kpb[1]=(cv::Mat_<float>(3,3) << 1,1,1,0,0,0,0,0,0);
	kpb[2]=(cv::Mat_<float>(3,3) << 0,1,1,0,0,1,0,0,0);
	kpb[3]=(cv::Mat_<float>(3,3) << 0,0,1,0,0,1,0,0,1);
	kpb[4]=(cv::Mat_<float>(3,3) << 0,0,0,0,0,1,0,1,1);
	kpb[5]=(cv::Mat_<float>(3,3) << 0,0,0,0,0,0,1,1,1);
	kpb[6]=(cv::Mat_<float>(3,3) << 0,0,0,1,0,0,1,1,0);
	kpb[7]=(cv::Mat_<float>(3,3) << 1,0,0,1,0,0,1,0,0);

	kpw[0]=(cv::Mat_<float>(3,3) << 0,0,0,0,1,1,0,1,0);
	kpw[1]=(cv::Mat_<float>(3,3) << 0,0,0,0,1,0,1,1,0);
	kpw[2]=(cv::Mat_<float>(3,3) << 0,0,0,1,1,0,0,1,0);
	kpw[3]=(cv::Mat_<float>(3,3) << 1,0,0,1,1,0,0,0,0);
	kpw[4]=(cv::Mat_<float>(3,3) << 0,1,0,1,1,0,0,0,0);
	kpw[5]=(cv::Mat_<float>(3,3) << 0,1,1,0,1,0,0,0,0);
	kpw[6]=(cv::Mat_<float>(3,3) << 0,1,0,0,1,1,0,0,0);
	kpw[7]=(cv::Mat_<float>(3,3) << 0,0,0,0,1,1,0,0,1);

	EXIT();
}

int Thinning::apply(cv::Mat &src, cv::Mat &dst, const int &max_loop) {

	ENTER();

	// 原画像を2値化(しきい値は用途に合わせて考える)
    // src_f:2値化した画像(32F)
    // src_w:作業バッファ
    // src_b:作業バッファ(反転)
	src.convertTo(src_f, CV_32FC1);
	src_f.mul(1./255.);
    cv::threshold(src_f, src_f, 0.5, 1.0, CV_THRESH_BINARY);
	cv::threshold(src_f, src_w, 0.5, 1.0, CV_THRESH_BINARY);
	cv::threshold(src_f, src_b, 0.5, 1.0, CV_THRESH_BINARY_INV);

	double sum = 1.0;
	for (int i = 0; i < max_loop; i++) {
		sum = 0.0;
		cv::filter2D(src_w, src_w, CV_32FC1, kpw[i]);
		cv::filter2D(src_b, src_b, CV_32FC1, kpb[i]);
		// 各カーネルで注目するのは3画素ずつなので、マッチした注目画素の濃度は3となる
		// カーネルの値を1/9にしておけば、しきい値は0.99で良い		cv::threshold(src_w, src_w, 2.99, 1.0, CV_THRESH_BINARY);
		cv::threshold(src_b, src_b, 2.99, 1.0, CV_THRESH_BINARY);
		cv::bitwise_and(src_w, src_b, src_w);
		//この時点でのsrc_wが消去候補点となり、全カーネルで候補点が0となった時に処理が終わる
		sum += cv::sum(src_w).val[0];
		//原画像から候補点を消去(二値画像なのでXor)
		cv::bitwise_xor(src_f, src_w, src_f);
		//作業バッファを更新
		src_f.copyTo(src_w);
		cv::threshold(src_f, src_b, 0.5, 1.0, CV_THRESH_BINARY_INV);
		if (sum <= 0) break;
	}
	src_f.convertTo(dst, CV_8UC4);	// CV_8UC4でいいんかな?

	RETURN(sum > 0.0 ? 1 : 0, int);
}