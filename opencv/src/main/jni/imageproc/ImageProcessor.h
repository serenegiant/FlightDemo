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

#define RESULT_FRAME_TYPE_NON 0
#define RESULT_FRAME_TYPE_SRC 1
#define RESULT_FRAME_TYPE_DST 2
#define RESULT_FRAME_TYPE_SRC_LINE 3
#define RESULT_FRAME_TYPE_DST_LINE 4
#define RESULT_FRAME_TYPE_MAX 5

struct DetectRec {
	std::vector< cv::Point > contour;	// 近似輪郭
	cv::RotatedRect area_rect;	// 内包する最小矩形
	int type;
	float area_rate;			// 近似輪郭の面積に対する近似輪郭を内包する最小矩形の面積の比...基本的に1以上のはず
	float area_vertex;			// 頂点1つあたりの面積, 大きい方が角数が少ない
	float area;
	float aspect;
	float analogous;			// 100-基準図形のHu momentsとの差の絶対値,
	float length;				// 長軸長さ
	float width;				// 短軸長さ
};

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
	// 直線ラインの検出処理
	int detect_line(std::vector< std::vector< cv::Point > > &contours,
		const bool needs_result, const bool show_detects, const cv::Mat result_frame,
		struct DetectRec &possible);
	int detect_circle(std::vector< std::vector< cv::Point > > &contours,
		const bool needs_result, const bool show_detects, const cv::Mat result_frame,
		struct DetectRec &possible);
	int detect_corner(std::vector< std::vector< cv::Point > > &contours,
		const bool needs_result, const bool show_detects, const cv::Mat result_frame,
		struct DetectRec &possible);
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