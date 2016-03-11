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

#define RESULT_FRAME_TYPE_NON 0			// 数値のみ返す
#define RESULT_FRAME_TYPE_SRC 1
#define RESULT_FRAME_TYPE_DST 2
#define RESULT_FRAME_TYPE_SRC_LINE 3
#define RESULT_FRAME_TYPE_DST_LINE 4
#define RESULT_FRAME_TYPE_MAX 5

// キューに入れることができる最大フレーム数
#define MAX_QUEUED_FRAMES 8
// フレームプール中の最大フレーム数
#define MAX_POOL_SIZE 8

typedef enum DetectType {
	TYPE_NON = -1,
	TYPE_LINE = 0,
	TYPE_CURVE = 1,
	TYPE_CORNER = 2,
} DetectType_t;

typedef enum ApproxType {
	APPROX_ABS = 0,		// mApproxFactorの値は絶対値[ピクセル]
	APPROX_RELATIVE,	// mApproxFactorの値は輪郭周長に対する割合
} ApproxType_t;

typedef enum SmoothType {
	SMOOTH_NON = 0,
	SMOOTH_GAUSSIAN,
	SMOOTH_MEDIAN,
	SMOOTH_BLUR,
} SmoothType_t;

typedef struct DetectRec {
	std::vector< cv::Point > contour;	// 近似輪郭
	cv::RotatedRect area_rect;	// 内包する最小矩形
	DetectType_t type;
	float area_rate;			// 近似輪郭の面積に対する近似輪郭を内包する最小矩形の面積の比...基本的に1以上のはず
	float area_vertex;			// 頂点1つあたりの面積, 大きい方が角数が少ない
	float area;
	float aspect;
	float analogous;			// 100-基準図形のHu momentsとの差の絶対値,
	float length;				// 長軸長さ
	float width;				// 短軸長さ
	float curvature;			// 曲率
} DetectRec_t;

typedef struct DetectParam DetectParam_t;
struct DetectParam {
public:
	volatile int mResultFrameType;
	volatile bool mEnableExtract;
	volatile SmoothType_t mSmoothType;
	volatile bool mEnableCanny;
	volatile ApproxType_t mApproxType;
	volatile double mApproxFactor;
	volatile double mCannythreshold1;
	volatile double mCannythreshold2;
	volatile float mMaxAnalogous;
	// これより下は内部計算
	bool needs_result;	// これは内部計算
	bool show_src;		// これは内部計算
	bool show_detects;	// これは内部計算
	void set(const DetectParam_t &other) {
		mResultFrameType = other.mResultFrameType;
		mEnableExtract = other.mEnableExtract;
		mSmoothType = other.mSmoothType;
		mEnableCanny = other.mEnableCanny;
		mApproxType = other.mApproxType;
		mApproxFactor = other.mApproxFactor;
		mCannythreshold1 = other.mCannythreshold1;
		mCannythreshold2 = other.mCannythreshold2;
		mMaxAnalogous = other.mMaxAnalogous;
		needs_result = mResultFrameType != RESULT_FRAME_TYPE_NON;
		show_src = (mResultFrameType == RESULT_FRAME_TYPE_SRC) || (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE);
		show_detects = needs_result && (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE) || (mResultFrameType == RESULT_FRAME_TYPE_DST_LINE);
	}
} ;

class ImageProcessor {
private:
	jobject mWeakThiz;
	jclass mClazz;
	volatile bool mIsRunning;
	DetectParam_t mParam;

	mutable Mutex mMutex;
	mutable Mutex mPoolMutex;
	Condition mSync;
	pthread_t processor_thread;
	// フレームプール
	std::vector<cv::Mat> mPool;
	// フレームキュー
	std::queue<cv::Mat> mFrames;
	int mExtractColorHSV[6];	// 0,1,2: HSV下限, 3,4,5:HSV上限
	// glReadPixelsを呼ぶ際のピンポンバッファに使うPBOのバッファ名
	GLuint pbo[2];
	int pbo_ix;
	// 処理スレッドの実行関数
	static void *processor_thread_func(void *vptr_args);
	void do_process(JNIEnv *env);
	int callJavaCallback(JNIEnv *env, DetectRec_t &detect_result, cv::Mat &result, const DetectParam_t &param);
protected:
	int pre_process(cv::Mat &frame, cv::Mat &src, cv::Mat &bk_result, cv::Mat &result, const DetectParam_t &param);
	int findContours(cv::Mat &src, cv::Mat &result,
		std::vector<std::vector< cv::Point>> &contours,	// 輪郭データ
		std::vector<DetectRec_t> &approxes,	// 近似輪郭
		const DetectParam_t &param);
	int colorExtraction(const cv::Mat &src, cv::Mat *dst,
	    int convert_code,	// cv:cvtColorの第3引数, カラー変換方法
	    int method,
		const int lower[], const int upper[]
	);
	// 直線ラインの検出処理
	int detect_line(std::vector<DetectRec_t> &contours,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
	int detect_circle(std::vector<DetectRec_t> &contours,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
	int detect_corner(std::vector<DetectRec_t> &contours,
		cv::Mat &result_frame, DetectRec_t &possible, const DetectParam_t &param);
	// フレームプール・フレームキュー関係
	cv::Mat getFrame();
	cv::Mat obtainFromPool(const int &width, const int &height);
	void recycle(cv::Mat &frame);
	inline const bool canAddFrame() { Mutex::Autolock lock(mMutex);  return mFrames.size() < MAX_QUEUED_FRAMES; };
	int addFrame(cv::Mat &frame);
	void clearFrames();
public:
	ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz);
	virtual ~ImageProcessor();
	void release(JNIEnv *env);
	int start(const int &width, const int &height);	// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	int stop();		// これはJava側の描画スレッド内から呼ばれる(EGLContextが有る)
	int handleFrame(const int &width, const int &height, const int &unused = 0);
	inline const bool isRunning() const { return mIsRunning; };
	inline void setResultFrameType(const int &result_frame_type) { mParam.mResultFrameType = result_frame_type % RESULT_FRAME_TYPE_MAX; };
	inline const int getResultFrameType() const { return mParam.mResultFrameType; };
	inline void setEnableExtract(const int &enable) { mParam.mEnableExtract = enable != 0; };
	inline const int getEnableExtract() const { return mParam.mEnableExtract ? 1 : 0; };
	inline void setEnableSmooth(const SmoothType_t &smooth_type) { mParam.mSmoothType = smooth_type; };
	inline const SmoothType_t getEnableSmooth() const { return mParam.mSmoothType; };
	inline void setEnableCanny(const int &enable) { mParam.mEnableCanny = enable != 0; };
	inline const int getEnableCanny() const { return mParam.mEnableCanny ? 1 : 0; };
	/** 抽出色の上下限をHSVで設定 */
	int setExtractionColor(const int lower[], const int upper[]);
};