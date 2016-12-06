//
// Created by saki on 16/03/01.
//

#ifndef FLIGHTDEMO_IMAGEPROCESSOR_H
#define FLIGHTDEMO_IMAGEPROCESSOR_H
#endif //FLIGHTDEMO_IMAGEPROCESSOR_H

#include "Mutex.h"
#include "Condition.h"
#include "IPBase.h"
#include "IPPreprocess.h"
#include "IPFrame.h"
#include "IPDetectorLine.h"
#include "IPDetectorCurve.h"
#include "IPDetectorCorner.h"

using namespace android;

class ImageProcessor : virtual public IPPreprocess, virtual public IPFrame {
private:
	jobject mWeakThiz;
	jclass mClazz;
	volatile bool mIsRunning;
	DetectParam_t mParam;
	IPDetectorLine mLineDetector;
	IPDetectorCurve mCurveDetector;
	IPDetectorCorner mCornerDetector;

	mutable Mutex mMutex;
	Condition mSync;
	pthread_t processor_thread;
	// 処理スレッドの実行関数
	static void *processor_thread_func(void *vptr_args);
	void do_process(JNIEnv *env);
	int callJavaCallback(JNIEnv *env, DetectRec_t &detect_result, cv::Mat &result, const long &last_queued_time_ms, const DetectParam_t &param);
protected:
public:
	ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz);
	virtual ~ImageProcessor();
	void release(JNIEnv *env);
	int start(const int &width, const int &height);	// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	int stop();		// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	inline const bool isRunning() const { return mIsRunning; };
	void setResultFrameType(const int &result_frame_type);
	inline const int getResultFrameType() const { return mParam.mResultFrameType; };
	void setEnableExtract(const int &enable);
	inline const int getEnableExtract() const { return mParam.mEnableExtract ? 1 : 0; };
	void setEnableSmooth(const SmoothType_t &smooth_type);
	inline const SmoothType_t &getEnableSmooth() const { return mParam.mSmoothType; };
	void setEnableCanny(const int &enable);
	inline const int getEnableCanny() const { return mParam.mEnableCanny ? 1 : 0; };
	/** 抽出色の上下限をHSVで設定 */
	int setExtractionColor(const int lower[], const int upper[]);
	/** 台形歪係数を設定 */
	int setTrapeziumRate(const double &trapezium_rate);
	inline const double getTrapeziumRate() const { return mParam.mTrapeziumRate;  };
	int setAreaLimit(const float &min, const float &max);
	int setAspectLimit(const float &min);
	int setAreaErrLimit(const float &limit1, const float &limit2);
	int setMaxThinningLoop(const int &max_loop);
	inline const int getMaxThinningLoop() const { return mParam.mMaxThinningLoop; };
	int setFillInnerContour(const bool &fill);
	inline const int getFillInnerContour() const  { return mParam.mFillInnerContour; };
};