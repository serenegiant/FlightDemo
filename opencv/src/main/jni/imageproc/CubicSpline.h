//
// Created by saki on 16/03/31.
//

#ifndef FLIGHTDEMO_CUBICSPLINE_H
#define FLIGHTDEMO_CUBICSPLINE_H

#include <vector>
#include <stdlib.h>
#include <stdio.h>
#include <algorithm>

#include "opencv2/opencv.hpp"

#include "utilbase.h"

#include "IPBase.h"

template <typename VEC2D>
class CubicSpline {
private:
	std::vector<Coeff4_t> mCoeffs;
	std::vector<cv::Point2f> mData;

	float calc_h(size_t index) {
		return mData[index + 1].x - mData[index].x;
	}

	float calc_v(size_t index) {
		return (6.0f * ((mData[index + 1].y - mData[index].y) / calc_h(index)
			- (mData[index].y - mData[index - 1].y) / calc_h(index - 1)));
	}

	// 第1引数が第2引数よりも小さい時に真(正)を返す
	static bool comp_func(const cv::Point2f &left, const cv::Point2f &right) {
		return left.x < right.x;
	}

	int init(const bool &sort) {
		ENTER();

		int result = -1;
		const size_t n = mData.size();
//		LOGI("n=%d", n);
		if (LIKELY(n > 2)) {
			if (UNLIKELY(sort)) {
				std::sort(mData.begin(), mData.end(), comp_func);
				// 同じx値が複数あるとゼロ除算になるので省く...yの平均値を使う方が良いのかも
				std::vector<cv::Point2f> temp;
				cv::Point2f v = mData[0];
				temp.push_back(v);
				for (int i = 1; i < n; i++) {
					const float a = v.x - mData[i].x;
//					LOGD("x=%e,fabs=%e", v.x, a);
					if (a != 0) {
						v = mData[i];
						temp.push_back(mData[i]);
//						LOGD("x=%e", v.x);
					}
				}
				mData.assign(temp.begin(), temp.end());
				if (temp.size() < 2) RETURN(result, int);
			}
			try {
				// Set data to a matrix A
				cv::Mat A = cv::Mat::zeros(n, n, CV_32F);
				cv::Mat b(n, 1, CV_32F);

				A.at<float>(0, 0) = 1.0f;
				b.at<float>(0, 0) = 0.0f;
				for (size_t r = 1; r < n - 1; ++r) {
					A.at<float>(r, r - 1) = calc_h(r - 1);
					A.at<float>(r, r + 1) = calc_h(r);
					A.at<float>(r, r) = 2.0f * (A.at<float>(r, r - 1) + A.at<float>(r, r + 1));

					b.at<float>(r, 0) = calc_v(r);
				}
				A.at<float>(n - 1, n - 1) = 1.0f;
				b.at<float>(n - 1, 0) = 0.0f;

				// Calculate parameters c_i
				cv::Mat u = A.inv(cv::DECOMP_LU) * b;

				// Calculate coefficients
				mCoeffs.resize(n - 1);
				for (size_t index = 0; index < mCoeffs.size(); ++index) {
					mCoeffs[index].b = u.at<float>(index, 0) / 2.0f;
					mCoeffs[index].a =
						(u.at<float>(index + 1, 0) - u.at<float>(index))
						/ (6.0f * (mData[index + 1].x - mData[index].x));
					mCoeffs[index].d = mData[index].y;
					mCoeffs[index].c =
						(mData[index + 1].y - mData[index].y) / (mData[index + 1].x - mData[index].x)
						- (mData[index + 1].x - mData[index].x) * (2.0f * u.at<float>(index, 0) + u.at<float>(index + 1, 0)) / 6.0f;
				}
				result = 0;
			} catch (cv::Exception e) {
				LOGE("init failed:%s", e.msg.c_str());
			} catch (...) {
				LOGE("init unknown exception:");
			}
		}

		RETURN(result, int);
	}
public:
	CubicSpline(const std::vector<VEC2D> &data, const bool &sort = true) {
		ENTER();

		mData.clear();
		mData.reserve(data.size());
		for (auto iter = data.begin(); iter != data.end() ; iter++) {
			mData.push_back(cv::Point2f((*iter).x, (*iter).y));
		}
		init(sort);

		EXIT();
	}

	CubicSpline(const std::vector<cv::Point2f> &data = std::vector<cv::Point2f>(), const bool &sort = true) {
		ENTER();

		mData.clear();
		mData.assign(data.begin(), data.end());
		init(sort);

		EXIT();
	}

	virtual ~CubicSpline() {
		ENTER();

		EXIT();
	}

	int reset(const std::vector<VEC2D> &data = std::vector<VEC2D>(), const bool &sort = true) {
		mData.clear();
		mData.reserve(data.size());
		for (auto iter = data.begin(); iter != data.end() ; iter++) {
			mData.push_back(cv::Point2f((*iter).x, (*iter).y));
		}
		return init(sort);
	}

	int reset(const std::vector<cv::Point2f> &data = std::vector<cv::Point2f>(), const bool &sort = true) {
		mData.clear();
		mData.assign(data.begin(), data.end());
		return init(sort);
	}

	const std::vector<Coeff4_t> getCoeffs() {
		return mCoeffs;
	}

	cv::Point2f getValue(const size_t &index, const float &ratio) {
		ENTER();

		VEC2D result;

		if ((ratio >= 0.0f) && (ratio <= 1.0f) && (index + 1 < mData.size())) {
			result.x = ratio * mData[index + 1].x + (1.0f - ratio) * mData[index].x;
			const float tmp = result.x - mData[index].x;
			result.y = mCoeffs[index].a * tmp * tmp * tmp
				+ mCoeffs[index].b * tmp * tmp
				+ mCoeffs[index].c * tmp
				+ mCoeffs[index].d;
		}
		RET(result);
	}

	size_t getDataNum() {
		return mData.size();
	}
};

#endif //FLIGHTDEMO_CUBICSPLINE_H
