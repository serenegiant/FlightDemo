/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

#ifndef FLIGHTDEMO_IPFRAME_H
#define FLIGHTDEMO_IPFRAME_H

#include <queue>
#include <vector>
//#include <GLES2/gl2.h>
//#include <GLES2/gl2ext.h>
#include <GLES3/gl3.h>		// API>=18
#include <GLES3/gl3ext.h>	// API>=18
#include "opencv2/opencv.hpp"

#include "Mutex.h"
#include "Condition.h"

// キューに入れることができる最大フレーム数
#define MAX_QUEUED_FRAMES 1
// フレームプール中の最大フレーム数
#define MAX_POOL_SIZE 2

using namespace android;

class IPFrame {
private:
	mutable Mutex mPboMutex;
	mutable Mutex mFrameMutex;
	mutable Mutex mPoolMutex;
	Condition mFrameSync;
	// フレームプール
	std::vector<cv::Mat> mPool;
	// フレームキュー
	std::queue<cv::Mat> mFrames;
	long last_queued_time_ms;
	// glReadPixelsを呼ぶ際のピンポンバッファに使うPBOのバッファ名
	GLuint pbo[2];
	int pbo_ix;
	int pbo_width, pbo_height;
	volatile GLsizeiptr pbo_size;
protected:
	IPFrame();
	virtual ~IPFrame();
	void initFrame(const int &width, const int &height);
	void releaseFrame();
	// フレームプール・フレームキュー関係
	cv::Mat getFrame(long &last_queued_ms);
	cv::Mat obtainFromPool(const int &width, const int &height);
	void recycle(cv::Mat &frame);
	inline const bool canAddFrame() { Mutex::Autolock lock(mFrameMutex);  return mFrames.size() < MAX_QUEUED_FRAMES; };
	int addFrame(cv::Mat &frame);
	void clearFrames();
	inline const int width() const { return pbo_width; };
	inline const int height() const { return pbo_height; };
public:
	int handleFrame(const int &, const int &, const int &);
};
#endif //FLIGHTDEMO_IPFRAME_H
