//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTORLINE_H
#define FLIGHTDEMO_IPDETECTORLINE_H

#include "IPDetector.h"

class IPDetectorLine : public IPDetector {
public:
	IPDetectorLine();
	virtual ~IPDetectorLine();
	virtual int detect(std::vector<DetectRec_t> &contours,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
};
#endif //FLIGHTDEMO_IPDETECTORLINE_H
