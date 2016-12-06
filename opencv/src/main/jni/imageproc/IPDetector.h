//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPDETECTOR_H
#define FLIGHTDEMO_IPDETECTOR_H

#include <stdlib.h>
#include <algorithm>
#include <iomanip>

#include "Thinning.h"
#include "CubicSpline.h"

#include "IPBase.h"

#define CALC_COEFFS 0

class IPDetector : virtual public IPBase {
private:
	int mWidth, mHeight;
protected:
#if CALC_COEFFS
	Thinning mThinning;
	CubicSpline<cv::Point> mCubicSpline;
#endif
	inline const int width() const { return mWidth; };
	inline const int height() const { return mHeight; };
	int calcCoeffs(cv::Mat &work, const std::vector< cv::Point> &contour, std::vector<Coeff4_t> &coeffs);
	void drawSpline(cv::Mat &dst);
public:
	IPDetector();
	virtual ~IPDetector();
	void resize(const int &width, const int &height);
	virtual int detect(cv::Mat &src, std::vector<DetectRec_t> &contours, std::vector<const DetectRec_t *> &possibles_work,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param) = 0;
};
#endif //FLIGHTDEMO_IPDETECTOR_H
