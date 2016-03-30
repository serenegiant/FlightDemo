//
// Created by saki on 16/03/30.
//

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
	std::vector<DetectRec_t> &contours,	// 近似輪郭
	cv::Mat &result_frame,				// 結果書き込み用Mat
	DetectRec_t &possible,				// 結果
	const DetectParam_t &param) {		// パラメータ

	ENTER();

	RETURN(0, int);
}
