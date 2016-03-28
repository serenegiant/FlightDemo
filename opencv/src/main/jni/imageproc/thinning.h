//
// Created by saki on 16/03/28.
//

#ifndef FLIGHTDEMO_THINNING_H
#define FLIGHTDEMO_THINNING_H
#include <iostream>
#include <stdio.h>
#include "opencv2/opencv.hpp"

class Thinning {
private:
	cv::Mat *kpb;
	cv::Mat *kpw;
	cv::Mat src_w;
	cv::Mat src_b;
	cv::Mat src_f;
	void init(const int &width, const int &height);
protected:
public:
	Thinning(const int &width, const int &height);
	virtual ~Thinning();
	int apply(cv::Mat &src, cv::Mat &dst, const int &max_loop);
};
#endif //FLIGHTDEMO_THINNING_H
