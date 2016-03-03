//
// Created by saki on 16/03/01.
//

#ifndef FLIGHTDEMO_IMAGEPROCESSOR_H
#define FLIGHTDEMO_IMAGEPROCESSOR_H
#endif //FLIGHTDEMO_IMAGEPROCESSOR_H

#include <vector>
#include <queue>

#include "Mutex.h"
#include "Condition.h"
#include "opencv2/opencv.hpp"

using namespace android;

class ImageProcessor {
private:
	jobject mWeakThiz;
	jclass mClazz;
	volatile bool mIsRunning;
	volatile bool mShowDetects;
	mutable Mutex mMutex;
	Condition mSync;
	pthread_t processor_thread;
	static void *processor_thread_func(void *vptr_args);
	// フレームキュー
	std::queue<cv::Mat> mFrames;
protected:
	void do_process(JNIEnv *env);
	cv::Mat getFrame();
	int addFrame(cv::Mat &frame);
public:
	ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz);
	virtual ~ImageProcessor();
	void release(JNIEnv *env);
	int start();
	int stop();
	int handleFrame(const uint8_t *frame, const int &width, const int &height);
	inline const bool isRunning() const { return mIsRunning; };
	inline void setShowDetects(const bool &show_detects) { mShowDetects = show_detects; };
	inline const bool getShowDetects() const { return mShowDetects; };
};