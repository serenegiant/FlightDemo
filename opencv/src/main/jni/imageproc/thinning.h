//
// Created by saki on 16/03/28.
//

#ifndef FLIGHTDEMO_THINNING_H
#define FLIGHTDEMO_THINNING_H
#include <iostream>
#include <stdio.h>
#include "opencv2/opencv.hpp"

#define USE_FILTER2D 0

class Thinning {
private:
#if USE_FILTER2D
	cv::Mat kpb[8];
	cv::Mat kpw[8];
	cv::Mat src_w;
	cv::Mat src_b;
	cv::Mat src_f;
#endif
protected:
public:
	Thinning(const int &width, const int &height);
	virtual ~Thinning();
	void resize(const int &width, const int &height);
	int apply(cv::Mat &src, cv::Mat &dst, const int &max_loop);
};
#endif //FLIGHTDEMO_THINNING_H
