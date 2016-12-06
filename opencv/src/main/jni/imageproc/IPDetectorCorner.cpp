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

#include "IPDetectorCorner.h"

//********************************************************************************
//********************************************************************************
IPDetectorCorner::IPDetectorCorner() {
	ENTER();

	EXIT();
}

IPDetectorCorner::~IPDetectorCorner() {
	ENTER();

	EXIT();
}

int IPDetectorCorner::detect(
	cv::Mat &src,						// 解析画像
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	std::vector<const DetectRec_t *> &possibles,	// ワーク用
	cv::Mat &result_frame,				// 結果書き込み用Mat
	DetectRec_t &possible,				// 結果
	const DetectParam_t &param) {		// パラメータ

	ENTER();

	RETURN(0, int);
}
