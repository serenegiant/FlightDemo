//
// Created by saki on 16/03/30.
//

#ifndef FLIGHTDEMO_IPBASE_H
#define FLIGHTDEMO_IPBASE_H

#include <vector>
#include <string>
#include <iostream>
#include <sstream>
#include <iomanip>
#include "opencv2/opencv.hpp"

#define RESULT_FRAME_TYPE_NON 0			// 数値のみ返す
#define RESULT_FRAME_TYPE_SRC 1
#define RESULT_FRAME_TYPE_DST 2
#define RESULT_FRAME_TYPE_SRC_LINE 3
#define RESULT_FRAME_TYPE_DST_LINE 4
#define RESULT_FRAME_TYPE_MAX 5

typedef struct Coeff4 {
	float a, b, c, d;
} Coeff4_t;

#define EPS 1e-8

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
	SMOOTH_DILATION,
} SmoothType_t;

typedef struct DetectRec DetectRec_t;
struct DetectRec {
	std::vector< cv::Point > contour;	// 近似輪郭
	cv::RotatedRect area_rect;			// 内包する最小矩形
	cv::RotatedRect ellipse;
	cv::Moments moments;
	cv::Point center;			// 重心
	std::vector<Coeff4_t> coeffs;
	DetectType_t type;
	float area_rate;			// 凸包図形面積に対する最小矩形の面積の比...基本的に1以上のはず, w * h / area
	float area;					// 凸包図形面積
	float aspect;
	float analogous;			// 100-基準図形のHu momentsとの差の絶対値,
	float length;				// 長軸長さ
	float width;				// 短軸長さ
	float curvature;			// 曲率
	float ex, ey;				// 楕円の中心

	DetectRec() {
		type = TYPE_NON;
		area_rate = area = aspect = analogous = length = width = curvature = ex = ey = 0.0f;
	}

	DetectRec(const DetectRec_t &src)
	:	contour(src.contour),
		area_rect(src.area_rect),
		ellipse(src.ellipse),
		moments(src.moments),
		center(src.center),
		coeffs(src.coeffs) {

		type = src.type;
		area_rate = src.area_rate;
		area = src.area;
		aspect = src.aspect;
		analogous = src.analogous;
		length = src.length;
		width = src.width;
		curvature = src.curvature;
		ex = src.ex;
		ey = src.ey;
	}

	DetectRec_t clear() {
		type = TYPE_NON;
		contour.clear();
		coeffs.clear();
		area_rate = area = aspect = analogous = length = width = curvature = ex = ey = 0.0f;
		return *this;
	}

	DetectRec_t assign(const std::vector< cv::Point > &src) {
		if (src.size()) {
			contour.assign(src.begin(), src.end());
		} else {
			contour.clear();
		}
		return *this;
	}

	DetectRec_t assign(const DetectRec_t &src) {
		if (src.contour.size()) {
			contour.assign(src.contour.begin(), src.contour.end());
		} else {
			contour.clear();
		}
		area_rect = src.area_rect;
		ellipse = src.ellipse;
		moments = src.moments;
		center = src.center;
		if (src.coeffs.size()) {
			coeffs.assign(src.coeffs.begin(), src.coeffs.end());
		} else {
			coeffs.clear();
		}
		type = src.type;
		area_rate = src.area_rate;
		area = src.area;
		aspect = src.aspect;
		analogous = src.analogous;
		length = src.length;
		width = src.width;
		curvature = src.curvature;
		ex = src.ex;
		ey = src.ey;
		return *this;
	}

	DetectRec_t operator =(const std::vector< cv::Point > &src) {
		return assign(src);
	}

	DetectRec_t operator =(const DetectRec_t &src) {
		return assign(src);
	}
};

typedef struct DetectParam DetectParam_t;
struct DetectParam {
public:
	bool changed;
	int mResultFrameType;
	bool mEnableExtract;
	SmoothType_t mSmoothType;
	bool mEnableCanny;
	int mMaxThinningLoop;	// 画像全体に対して細線化
	bool mFillInnerContour;	// 大きな輪郭内の空隙を塗りつぶす
	ApproxType_t mApproxType;
	double mApproxFactor;
	double mCannyThreshold1;
	double mCannyThreshold2;
	float mMaxAnalogous;
	int extractColorHSV[6];	// 抽出色(HSV上下限, 0,1,2: HSV下限, 3,4,5:HSV上限)
	double mTrapeziumRate;	// 台形歪率, 0:歪なし, 正:下辺が長い, 負:上限が長い
	float mAreaLimitMin;	// 輪郭検出時の最小面積
	float mAreaLimitMax;	// 輪郭検出時の最大面積
	float mAreaErrLimit1;	// 輪郭検出時の面積誤差1
	float mAreaErrLimit2;	// 輪郭検出時の面積誤差2
	float mMinLineAspect;	// ライン検出時の最小アスペクト比
	// これより下は内部計算
	bool needs_result;	// これは内部計算
	bool show_src;		// これは内部計算
	bool show_detects;	// これは内部計算
	cv::Mat perspectiveTransform;	// 透視変換行列, 台形歪補正用, 内部計算
	// 幅と高さはchangedにかかわらず毎フレーム更新
	int width, height;

	/** 値をセットして更新, src#changed=trueの時のみ */
	void set(const DetectParam_t &src) {
		mResultFrameType = src.mResultFrameType;
		mEnableExtract = src.mEnableExtract;
		mSmoothType = src.mSmoothType;
		mEnableCanny = src.mEnableCanny;
		mMaxThinningLoop = src.mMaxThinningLoop;
		mFillInnerContour = src.mFillInnerContour;
		mApproxType = src.mApproxType;
		mApproxFactor = src.mApproxFactor;
		mCannyThreshold1 = src.mCannyThreshold1;
		mCannyThreshold2 = src.mCannyThreshold2;
		mMaxAnalogous = src.mMaxAnalogous;
		memcpy(extractColorHSV, src.extractColorHSV, sizeof(int) * 6);
		mAreaLimitMin = src.mAreaLimitMin;
		mAreaLimitMax = src.mAreaLimitMax;
		mAreaErrLimit1 = src.mAreaErrLimit1;
		mAreaErrLimit2 = src.mAreaErrLimit2;
		mMinLineAspect = src.mMinLineAspect;
		mTrapeziumRate = src.mTrapeziumRate;
		perspectiveTransform = src.perspectiveTransform;
		// 計算
		needs_result = mResultFrameType != RESULT_FRAME_TYPE_NON;
		show_src = (mResultFrameType == RESULT_FRAME_TYPE_SRC) || (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE);
		show_detects = needs_result && (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE) || (mResultFrameType == RESULT_FRAME_TYPE_DST_LINE);
		changed = false;
	}
};

class IPBase {
protected:
	// 繰り返し使うのでstaticに生成しておく
	static const cv::Scalar COLOR_YELLOW;
	static const cv::Scalar COLOR_GREEN;
	static const cv::Scalar COLOR_ORANGE;
	static const cv::Scalar COLOR_ACUA;
	static const cv::Scalar COLOR_PINK;
	static const cv::Scalar COLOR_BLUE;
	static const cv::Scalar COLOR_RED;
	static const cv::Scalar COLOR_WHITE;
	static const cv::Scalar COLOR_BLACK;

	IPBase();
	virtual ~IPBase();

	void findContours(cv::Mat &src,
		std::vector<std::vector< cv::Point>> &contours,
		const cv::RetrievalModes &mode = cv::RETR_EXTERNAL,
		const cv::ContourApproximationModes &method = cv::CHAIN_APPROX_NONE);
	void findContours(cv::Mat &src,
		std::vector<std::vector< cv::Point>> &contours,
		std::vector<cv::Vec4i> &hierarchy,
		const cv::RetrievalModes &mode = cv::RETR_EXTERNAL,
		const cv::ContourApproximationModes &method = cv::CHAIN_APPROX_NONE);
	int colorExtraction(const cv::Mat &src, cv::Mat *dst,
	    int convert_code,	// cv:cvtColorの第3引数, カラー変換方法
	    int method,
		const int lower[], const int upper[]
	);
	static void clear_stringstream(std::stringstream &ss);
	// RotatedRectを指定線色で描画する
	static void draw_rect(cv::Mat img, cv::RotatedRect rect, cv::Scalar color);
	/**
	 * doubleの引数の符号を返す
	 * @return 0:引数がゼロの時, -1:引数が負の時, 1:引数が正の時
	 */
	static inline int sign(const double v) {
		return (v > 0) - (v < 0);
	}

	/** Hu momentsを基準値と比較
	 * methodは1〜3
	 * cv::matchShapesが画像を引数にしているのを計算済みのHu momentsを渡せるようにしただけ
	 * @return 0: 完全一致
	 */
	static double compHuMoments(const double ma[], const double mb[], int method);
};
#endif //FLIGHTDEMO_IPBASE_H
