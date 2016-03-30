//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTOR_H
#define FLIGHTDEMO_IPDETECTOR_H
#include "IPBase.h"

class IPDetector : public IPBase {
private:
	int mWidth, mHeight;
protected:
	inline const int width() const { return mWidth; };
	inline const int height() const { return mHeight; };
public:
	inline void resize(const int &width, const int &height) { mWidth = width; mHeight = height; };
	virtual int detect(std::vector<DetectRec_t> &contours,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param) = 0;
};
#endif //FLIGHTDEMO_IPDETECTOR_H
