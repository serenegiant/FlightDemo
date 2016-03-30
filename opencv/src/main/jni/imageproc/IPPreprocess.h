//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPPREPROCESS_H
#define FLIGHTDEMO_IPPREPROCESS_H

#include "IPBase.h"
#include "Thinning.h"

class IPPreprocess : virtual public IPBase {
private:
	Thinning mThinning;
protected:
	IPPreprocess();
	virtual ~IPPreprocess();

	int pre_process(cv::Mat &frame, cv::Mat &src, cv::Mat &result, const DetectParam_t &param);
	int findPossibleContours(cv::Mat &src, cv::Mat &result,
		std::vector<std::vector< cv::Point>> &contours,	// 輪郭データ
		std::vector<DetectRec_t> &approxes,	// 近似輪郭
		const DetectParam_t &param);
};
#endif //FLIGHTDEMO_IPPREPROCESS_H
