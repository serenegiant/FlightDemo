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

#define RESULT_FRAME_TYPE_SRC 0
#define RESULT_FRAME_TYPE_DST 1
#define RESULT_FRAME_TYPE_SRC_LINE 2
#define RESULT_FRAME_TYPE_DST_LINE 3
#define RESULT_FRAME_TYPE_MAX 4

class ImageProcessor {
private:
	jobject mWeakThiz;
	jclass mClazz;
	volatile bool mIsRunning;
	volatile int mResultFrameType;
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
	int colorExtraction(cv::Mat *src, cv::Mat *dst,
	    int convert_code,	// cv:cvtColorの第3引数, カラー変換方法
	    int HLower, int HUpper,	// 色相範囲 [0,180]
	    int SLower, int SUpper,	// 彩度範囲 [0,255]
	    int VLower, int VUpper	// 明度範囲 [0,255]
	);
public:
	ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz);
	virtual ~ImageProcessor();
	void release(JNIEnv *env);
	int start();
	int stop();
	int handleFrame(const uint8_t *frame, const int &width, const int &height);
	inline const bool isRunning() const { return mIsRunning; };
	inline void setResultFrameType(const int &result_frame_type) { mResultFrameType = result_frame_type % RESULT_FRAME_TYPE_MAX; };
	inline const int getResultFrameType() const { return mResultFrameType; };
};