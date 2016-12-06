//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTORLINE_H
#define FLIGHTDEMO_IPDETECTORLINE_H

#include "IPDetector.h"

class IPDetectorLine : public IPDetector {
private:
public:
	IPDetectorLine();
	virtual ~IPDetectorLine();
	virtual int detect(cv::Mat &src, std::vector<DetectRec_t> &contours, std::vector<const DetectRec_t *> &possibles,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
};
#endif //FLIGHTDEMO_IPDETECTORLINE_H
