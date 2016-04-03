//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTORCORNER_H
#define FLIGHTDEMO_IPDETECTORCORNER_H

#include "IPDetector.h"

class IPDetectorCorner : public IPDetector {
public:
	IPDetectorCorner();
	virtual ~IPDetectorCorner();
	virtual int detect(cv::Mat &src, std::vector<DetectRec_t> &contours, std::vector<const DetectRec_t *> &possibles,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
};
#endif //FLIGHTDEMO_IPDETECTORCORNER_H
