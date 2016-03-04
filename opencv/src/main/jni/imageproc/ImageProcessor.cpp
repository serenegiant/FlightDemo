//
// Created by saki on 16/03/01.
//

#if 0	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
//	#define USE_LOGALL
	#define USE_LOGD
	#undef LOG_NDEBUG
	#undef NDEBUG
#endif

#include <jni.h>

#include "utilbase.h"
#include "common_utils.h"
#include "JNIHelp.h"
#include "Errors.h"

#include "ImageProcessor.h"

// キューに入れることができる最大フレーム数
#define MAX_QUEUED_FRAMES 8

struct fields_t {
    jmethodID callFromNative;
    jmethodID arrayID;	// ByteBufferがdirectBufferでない時にJava側からbyte[]を取得するためのメソッドid
};
static fields_t fields;

using namespace android;

ImageProcessor::ImageProcessor(JNIEnv* env, jobject weak_thiz_obj, jclass clazz)
:	mWeakThiz(env->NewGlobalRef(weak_thiz_obj)),
	mClazz((jclass)env->NewGlobalRef(clazz)),
	mIsRunning(false),
	mResultFrameType(RESULT_FRAME_TYPE_SRC)
{
}

ImageProcessor::~ImageProcessor() {
}

void ImageProcessor::release(JNIEnv *env) {
	ENTER();

	if (LIKELY(env)) {
		if (mWeakThiz) {
			env->DeleteGlobalRef(mWeakThiz);
			mWeakThiz = NULL;
		}
		if (mClazz) {
			env->DeleteGlobalRef(mClazz);
			mClazz = NULL;
		}
	}

	EXIT();
}

/** プロセッシングスレッド開始 */
int ImageProcessor::start() {
	ENTER();
	int result = -1;

	if (!isRunning()) {
		mMutex.lock();
		{
			mIsRunning = true;
			result = pthread_create(&processor_thread, NULL, processor_thread_func, (void *)this);
		}
		mMutex.unlock();
	} else {
		LOGW("already running");
	}

	RETURN(result, int);
}

/** プロセッシングスレッド終了 */
int ImageProcessor::stop() {
	ENTER();

	bool b = isRunning();
	if (LIKELY(b)) {
		mMutex.lock();
		{
			MARK("signal to processor thread");
			mIsRunning = false;
			mSync.broadcast();
		}
		mMutex.unlock();
		MARK("プロセッサスレッド終了待ち");
		if (pthread_join(processor_thread, NULL) != EXIT_SUCCESS) {
			LOGW("terminate processor thread: pthread_join failed");
		}
		mMutex.lock();
		{
			for ( ; !mFrames.empty(); ) {
				mFrames.pop();
			}
		}
		mMutex.unlock();
	}
	RETURN(0, int);
}

/** プロセッシングスレッドの実行関数 */
void *ImageProcessor::processor_thread_func(void *vptr_args) {
	ENTER();

	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(vptr_args);
	if (LIKELY(processor)) {
		// Java側へアクセスできるようにするためにJavaVMへアタッチする
		JavaVM *vm = getVM();
		CHECK(vm);
		JNIEnv *env;
		vm->AttachCurrentThread(&env, NULL);
		CHECK(env);
		processor->do_process(env);
		LOGD("プロセッサループ終了, JavaVMからデタッチする");
		vm->DetachCurrentThread();
		LOGD("デタッチ終了");
	}

	PRE_EXIT();
	pthread_exit(NULL);
}

/** プロセッシングスレッドの実体 */
void ImageProcessor::do_process(JNIEnv *env) {
	ENTER();

	float detected[20];
	for ( ; mIsRunning ; ) {
		// フレームデータの取得待ち
		cv::Mat frame = getFrame();
		if (!mIsRunning) break;
		// FIXME 未実装 OpenCVでの解析処理
		try {
			cv::Mat src, result;

			// RGBAのままだとHSVに変換できないので一旦BGRに買える
			cv::cvtColor(frame, src, cv::COLOR_RGBA2BGR, 1);
			cv::normalize(src, src, 0, 255, cv::NORM_MINMAX);
			// 色抽出処理, 色相は問わず彩度が低くて明度が高い領域を抽出
			colorExtraction(&src, &src, cv::COLOR_BGR2HSV, 0, 180, 0, 10, 70, 255);
			// グレースケールに変換(RGBA->Y)
			cv::cvtColor(src, src, cv::COLOR_BGR2GRAY, 1);
			// 平滑化
//			cv::Sobel(src, src, CV_32F, 1, 1);
//			cv::convertScaleAbs(src, src, 1, 0);
			// エッジ検出(Cannyの結果は2値化されてる)
			cv::Canny(src, src, 50, 200);
			// 2値化
//			cv::threshold(src, src, 60, 255, cv::THRESH_BINARY);
//			cv::threshold(src, src, 60, 255, cv::THRESH_BINARY_INV);
			// 表示用にカラー画像に戻す
			const bool show_src = (mResultFrameType == RESULT_FRAME_TYPE_SRC) || (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE);
			if (show_src) {
				result = frame;
			} else {
				cv::cvtColor(src, result, cv::COLOR_GRAY2RGBA);
			}

//			LOGI("src(cols=%d,rows=%d,dims=%d,depth=%d,elemSize=%d)", src.cols, src.rows, src.dims, src.depth(), src.elemSize());
//			LOGI("src(cols=%d,rows=%d,dims=%d,depth=%d,elemSize=%d)", src.cols, src.rows, src.dims, src.depth(), src.elemSize());
//			LOGI("result(cols=%d,rows=%d,dims=%d,depth=%d,elemSize=%d)", result.cols, result.rows, result.dims, result.depth(), result.elemSize());
#if 1
			const bool show_detects = (mResultFrameType == RESULT_FRAME_TYPE_SRC_LINE) || (mResultFrameType == RESULT_FRAME_TYPE_DST_LINE);
			// 確率的ハフ変換による直線検出
			std::vector<cv::Vec4i> lines;
#if 1
			cv::HoughLinesP(src, lines,
				2,			// 距離分解能[ピクセル]
				CV_PI/180,	// 角度分解能[ラジアン]
				70,			// Accumulatorのしきい値
				20,			// 最小長さ[ピクセル]
				20			// 2点が同一線上にあるとみなす最大距離[ピクセル]
			);
#endif
			// 検出結果をcolor_dstに書き込み
			if (show_detects) {
				for (size_t i = 0; i < lines.size(); i++ ) {
					cv::Vec4i l = lines[i];
					cv::line(result, cv::Point(l[0], l[1]), cv::Point(l[2], l[3]), cv::Scalar(255, 0, 0), 3, 8);
				}
			}
#else
			// ハフ変換による直線検出
			std::vector<cv::Vec2i> lines;
			cv::HoughLines(src, lines, 1, CV_PI/180, 100);
			// 検出結果をcolor_dstに書き込み
			if (show_detects) {
				for (size_t i = 0; i < lines.size(); i++ ) {
					float rho = lines[i][0];
					float theta = lines[i][1];
					double a = cos(theta), b = sin(theta);
					double x0 = a*rho, y0 = b*rho;
					cv::Point pt1(cvRound(x0 + 1000*(-b)), cvRound(y0 + 1000*(a)));
					cv::Point pt2(cvRound(x0 - 1000*(-b)), cvRound(y0 - 1000*(a)));
					line(result, pt1, pt2, cv::Scalar(255, 0, 0), 3, 8 );
				}
			}
#endif
			// Java側のコールバックメソッドを呼び出す
			if (LIKELY(mIsRunning && fields.callFromNative && mClazz && mWeakThiz)) {
				// 結果 FIXME ByteBufferで返す代わりにコピーされるけどfloat配列の方がいいかも
				jobject buf_detected = env->NewDirectByteBuffer(detected, sizeof(float) * 20);
				// 解析画像
				jobject buf_frame = env->NewDirectByteBuffer(result.data, result.total() * result.elemSize());
				// コールバックメソッドを呼び出す
				env->CallStaticVoidMethod(mClazz, fields.callFromNative, mWeakThiz, buf_frame, buf_detected);
				env->ExceptionClear();
				env->DeleteLocalRef(buf_detected);
				env->DeleteLocalRef(buf_frame);
			}
		} catch (cv::Exception e) {
			LOGE("do_process failed:%s", e.msg.c_str());
			continue;
		}
	}

	EXIT();
}

/** 指定したHSV色範囲に収まる領域を抽出する */
int ImageProcessor::colorExtraction(cv::Mat *src, cv::Mat *dst,
	int convert_code,	// cv:cvtColorの第3引数, カラー変換方法
	int HLower, int HUpper,	// 色相範囲 [0,180]
	int SLower, int SUpper,	// 彩度範囲 [0,255]
	int VLower, int VUpper) {	// 明度範囲 [0,255]

	ENTER();

	int result = 0;

    cv::Mat colorImage;
    int lower[3];
    int upper[3];

	try {
		cv::Mat lut = cv::Mat(256, 1, CV_8UC3);

		cv::cvtColor(*src, colorImage, convert_code);

		lower[0] = HLower;
		lower[1] = SLower;
		lower[2] = VLower;

		upper[0] = HUpper;
		upper[1] = SUpper;
		upper[2] = VUpper;

		for (int i = 0; i < 256; i++) {
			for (int k = 0; k < 3; k++) {
				if (lower[k] <= upper[k]) {
					if ((lower[k] <= i) && (i <= upper[k])) {
						lut.data[i*lut.step+k] = 255;
					} else{
						lut.data[i*lut.step+k] = 0;
					}
				} else {
					if ((i <= upper[k]) || (lower[k] <= i)) {
						lut.data[i*lut.step+k] = 255;
					} else {
						lut.data[i*lut.step+k] = 0;
					}
				}
			}
		}

		// LUTを使用して二値化
		cv::LUT(colorImage, lut, colorImage);

		// Channel毎に分解
		std::vector<cv::Mat> planes;
		cv::split(colorImage, planes);

		// マスクを作成
		cv::Mat maskImage;
		cv::bitwise_and(planes[0], planes[1], maskImage);
		cv::bitwise_and(maskImage, planes[2], maskImage);

		// 出力
		cv::Mat maskedImage;
		src->copyTo(maskedImage, maskImage);
		*dst = maskedImage;
	} catch (cv::Exception e) {
		LOGE("colorExtraction failed:%s", e.msg.c_str());
		result = -1;
	}
    RETURN(result, int);
}

//================================================================================
//
//================================================================================
int ImageProcessor::handleFrame(const uint8_t *frame, const int &width, const int &height) {
	ENTER();

	// 受け取ったフレームデータをMatにしてキューする
	cv::Mat mat = cv::Mat(height, width, CV_8UC4, (void *)frame);
	addFrame(mat);

	RETURN(0, int);
}

//================================================================================
// フレームキュー
//================================================================================
/** フレームキューからフレームを取得, フレームキューが空ならブロックする */
cv::Mat ImageProcessor::getFrame() {
	ENTER();

	cv::Mat result;

	Mutex::Autolock lock(mMutex);
	if (mFrames.empty()) {
		mSync.wait(mMutex);
	}
	if (mIsRunning && !mFrames.empty()) {
		result = mFrames.front();
		mFrames.pop();
	}

	RET(result);
}

/** フレームキューにフレームを追加する, キュー中のフレーム数が最大数を超えると先頭を破棄する */
int ImageProcessor::addFrame(cv::Mat &frame) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	if (mFrames.size() >= MAX_QUEUED_FRAMES) {
		// キュー中のフレーム数が最大数を超えたので先頭を破棄する
		mFrames.pop();
	}
	mFrames.push(frame.clone());	// コピーを追加する
	mSync.signal();

	RETURN(0, int);
}

//********************************************************************************
//********************************************************************************
static void nativeClassInit(JNIEnv* env, jclass clazz) {
	ENTER();

	fields.callFromNative = env->GetStaticMethodID(clazz, "callFromNative",
         "(Ljava/lang/ref/WeakReference;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)V");
	if (UNLIKELY(!fields.callFromNative)) {
		LOGW("can't find com.serenegiant.ImageProcessor#callFromNative");
	}
	env->ExceptionClear();
	// ByteBufferがdirectBufferでない時にJava側からbyte[]を取得するためのメソッドidを取得
    jclass byteBufClass = env->FindClass("java/nio/ByteBuffer");

	if (LIKELY(byteBufClass)) {
		fields.arrayID = env->GetMethodID(byteBufClass, "array", "()[B");
		if (!fields.arrayID) {
			LOGE("Can't find java/nio/ByteBuffer#array");
		}
	} else {
		LOGE("Can't find java/nio/ByteBuffer");
	}
	env->ExceptionClear();

	EXIT();
}

static ID_TYPE nativeCreate(JNIEnv *env, jobject thiz,
	jobject weak_thiz_obj) {

	ImageProcessor *processor = NULL;

	jclass clazz = env->GetObjectClass(thiz);
	if (LIKELY(clazz)) {
		processor = new ImageProcessor(env, weak_thiz_obj, clazz);
		setField_long(env, thiz, "mNativePtr", reinterpret_cast<ID_TYPE>(processor));
	} else {
		jniThrowRuntimeException(env, "can't find com.serenegiant.ImageProcessor");
	}

	RETURN(reinterpret_cast<ID_TYPE>(processor), ID_TYPE);
}

static void nativeRelease(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	setField_long(env, thiz, "mNativePtr", 0);
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		// 終了処理
		processor->release(env);
		SAFE_DELETE(processor);
	}

	EXIT();
}

static jint nativeStart(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->start();
	}

	RETURN(result, jint);
}

static jint nativeStop(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->stop();
	}

	RETURN(result, jint);
}

static int nativeHandleFrame(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jobject byteBuf_obj, jint width, jint height) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		// フレーム処理
		// 引数のByteBufferをnativeバッファに変換出来るかどうか試してみる
		void *buf = env->GetDirectBufferAddress(byteBuf_obj);
	    jlong dstSize;
		jbyteArray byteArray = NULL;
		if (LIKELY(buf)) {
			// ダイレクトバッファだった＼(^o^)／
			dstSize = env->GetDirectBufferCapacity(byteBuf_obj);
		} else {
			// 引数のByteBufferがダイレクトバッファじゃなかった(´・ω・｀)
			// ByteBuffer#arrayを呼び出して内部のbyte[]を取得できるかどうか試みる
			byteArray = (jbyteArray)env->CallObjectMethod(byteBuf_obj, fields.arrayID);
			if (UNLIKELY(byteArray == NULL)) {
				// byte[]を取得できなかった時
				LOGE("byteArray is null");
		        RETURN(BAD_VALUE, jint);
			}
	        buf = env->GetByteArrayElements(byteArray, NULL);
	        dstSize = env->GetArrayLength(byteArray);
		}
		// 配列の長さチェック
	    if (LIKELY(dstSize >= (width * height) << 2)) {	// RGBA
			result = processor->handleFrame((uint8_t *)buf, width, height);
		} else {
	        LOGE("nativeHandleFrame saw wrong dstSize %lld", dstSize);
	        result = BAD_VALUE;
	    }
		// ByteBufferのデータを開放
	    if (byteArray) {
	        env->ReleaseByteArrayElements(byteArray, (jbyte *)buf, 0);
	    }
	}

	RETURN(result, jint);
}

static jint nativeSetResultFrameType(JNIEnv *env, jobject thiz,
	ID_TYPE id_native, jint result_frame_type) {

	ENTER();

	jint result = -1;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		processor->setResultFrameType(result_frame_type);
	}

	RETURN(result, jint);
}

static jint nativeGetResultFrameType(JNIEnv *env, jobject thiz,
	ID_TYPE id_native) {

	ENTER();

	jint result = 0;
	ImageProcessor *processor = reinterpret_cast<ImageProcessor *>(id_native);
	if (LIKELY(processor)) {
		result = processor->getResultFrameType();
	}

	RETURN(result, jint);
}

//================================================================================
//================================================================================
static JNINativeMethod methods[] = {
	{ "nativeClassInit",		"()V",   (void*)nativeClassInit },
	{ "nativeCreate",			"(Ljava/lang/ref/WeakReference;)J", (void *) nativeCreate },
	{ "nativeRelease",			"(J)V", (void *) nativeRelease },
	{ "nativeStart",			"(J)I", (void *) nativeStart },
	{ "nativeStop",				"(J)I", (void *) nativeStop },
	{ "nativeHandleFrame",		"(JLjava/nio/ByteBuffer;II)I", (void *) nativeHandleFrame },
	{ "nativeSetResultFrameType",	"(JI)I", (void *) nativeSetResultFrameType },
	{ "nativeGetResultFrameType",	"(J)I", (void *) nativeGetResultFrameType },
};


int register_ImageProcessor(JNIEnv *env) {
	// ネイティブメソッドを登録
	if (registerNativeMethods(env,
		"com/serenegiant/opencv/ImageProcessor",
		methods, NUM_ARRAY_ELEMENTS(methods)) < 0) {
		return -1;
	}
	return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
#if LOCAL_DEBUG
    LOGD("JNI_OnLoad");
#endif

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register native methods
    int result = register_ImageProcessor(env);

	setVM(vm);

#if LOCAL_DEBUG
    LOGD("JNI_OnLoad:finished:result=%d", result);
#endif
    return JNI_VERSION_1_6;
}
