//
// Created by saki on 16/03/30.
//

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
