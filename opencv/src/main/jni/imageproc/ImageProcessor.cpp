//
// Created by saki on 16/03/01.
//

#if 0	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
	#define USE_LOGALL
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
	mIsRunning(false)
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

	for ( ; mIsRunning ; ) {
		// フレームデータの取得待ち
		cv::Mat frame = getFrame();
		if (!mIsRunning) break;
		// OpenCVでの解析処理
		LOGI("get Mat");
		// FIXME 未実装
		// Java側のコールバックメソッドを呼び出す
		if (LIKELY(mIsRunning && fields.callFromNative && mClazz && mWeakThiz)) {
			// FIXME コピーされるけどfloat配列の方がいいかも
//			jobject buf = env->NewDirectByteBuffer(callback_frame->getFrame(), NULL/* FIXME */);
//			env->CallStaticVoidMethod(mClazz, fields.callFromNative, mWeakThiz, buf);
//			env->ExceptionClear();
//			env->DeleteLocalRef(buf);
		}
	}

	EXIT();
}

//================================================================================
//
//================================================================================
int ImageProcessor::handleFrame(const uint8_t *frame, const int &width, const int &height) {
	ENTER();

	// 受け取ったフレームデータをMatにしてキューする
	cv::Mat mat = cv::Mat(width, height, CV_8UC4, (void *)frame);
	addFrame(mat);

	RETURN(0, int);
}

//================================================================================
// フレームキュー
//================================================================================
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

int ImageProcessor::addFrame(cv::Mat &frame) {
	ENTER();

	Mutex::Autolock lock(mMutex);

	if (mFrames.size() >= MAX_QUEUED_FRAMES) {
		// 先頭を破棄する
		mFrames.pop();
	}
	mFrames.push(frame);
	mSync.signal();

	RETURN(0, int);
}

//********************************************************************************
//********************************************************************************
static void nativeClassInit(JNIEnv* env, jclass clazz) {
	ENTER();

	fields.callFromNative = env->GetStaticMethodID(clazz, "callFromNative",
         "(Ljava/lang/ref/WeakReference;Ljava/nio/ByteBuffer;)V");
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

//================================================================================
//================================================================================
static JNINativeMethod methods[] = {
	{ "nativeClassInit",	"()V",   (void*)nativeClassInit },
	{ "nativeCreate",		"(Ljava/lang/ref/WeakReference;)J", (void *) nativeCreate },
	{ "nativeRelease",		"(J)V", (void *) nativeRelease },
	{ "nativeStart",		"(J)I", (void *) nativeStart },
	{ "nativeStop",			"(J)I", (void *) nativeStop },
	{ "nativeHandleFrame",	"(JLjava/nio/ByteBuffer;II)I", (void *) nativeHandleFrame },
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
