/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#define LOG_TAG "EGLBase"

#include "utilbase.h"
#include "eglbase.h"

// 毎回Windowサイズを取得するようにする時は1
#define UPDATE_WINDOW_SIZE_EVERYTIME 0

//	glViewport(0, 0, request_width, request_height); \	// こっちだと低解像度設定の時に一部分しか表示できない
//	glViewport(0, 0, window_width, window_height);
#if 1
#define SET_VIEWPORT \
	glViewport(0, 0, window_width, window_height); \
	GLCHECK("glViewport");
#else
#define SET_VIEWPORT \
	glViewport(0, 0, request_width, request_height); \
	GLCHECK("glViewport");
#endif

/*public*/
EGLBase::EGLBase(EGLBase *shared_context, const bool &with_depth_buffer, const bool &isRecordable)
:	mEglDisplay(EGL_NO_DISPLAY),
	mEglContext(EGL_NO_CONTEXT),
 	mEglConfig(0),
	mWithDepthBuffer(with_depth_buffer),
	mIsRecordable(isRecordable) {
	initEGLContext(shared_context ? shared_context->mEglContext : EGL_NO_CONTEXT, with_depth_buffer, isRecordable);
}

/*public*/
EGLBase::EGLBase(EGLBase *shared_context)
:	mEglDisplay(EGL_NO_DISPLAY),
	mEglContext(EGL_NO_CONTEXT),
	mEglConfig(0),
	mWithDepthBuffer(shared_context ? shared_context->mWithDepthBuffer: false),
	mIsRecordable(shared_context ? shared_context-> mIsRecordable : false) {
	initEGLContext(shared_context ? shared_context->mEglContext : EGL_NO_CONTEXT, mWithDepthBuffer, mIsRecordable);
}

#if 0
// EGLContextはvoidへのポインタなのでなんでも受け付けてしまって危険なので(例えばEGLBaseへのポインタでも)コメントアウト
/*public*/
EGLBase::EGLBase(EGLContext shared_context, const bool &with_depth_buffer, const bool &isRecordable)
:	mEglDisplay(EGL_NO_DISPLAY),
	mEglContext(EGL_NO_CONTEXT),
 	mEglConfig(0),
	mWithDepthBuffer(with_depth_buffer),
	mIsRecordable(isRecordable) {
	initEGLContext(shared_context, with_depth_buffer, isRecordable);
}
#endif

/*public*/
EGLBase::~EGLBase() {
	releaseEGLContext();
}

/*private(friend)*/
int EGLBase::getConfig(const int &version, const bool &with_depth_buffer, const bool &isRecordable) {
	ENTER();

	// 有効にするEGLパラメータ:RGB565
	EGLint attribs_rgb565[] = {
		// レンダリングタイプ(OpenGL|ES2)
		EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
		// サーフェースタイプ, ダブルバッファを使用するのでEGL_WINDOW_BIT
		EGL_SURFACE_TYPE,	EGL_WINDOW_BIT,
		// 赤色で使用する最小フレームバッファサイズ, 8ビット
		EGL_RED_SIZE,		5,
		// 緑色で使用する最小フレームバッファサイズ, 8ビット
		EGL_GREEN_SIZE,		6,
		// 青色で使用する最小フレームバッファサイズ, 8ビット
		EGL_BLUE_SIZE,		5,
		// アルファで使用する最小フレームバッファサイズ, 8ビット
//		EGL_ALPHA_SIZE, 8,
		// ステンシルバッファとして使用する最小バッファサイズ, 8ビット
//		EGL_STENCIL_SIZE,	8,
        // 今のところ2D表示だけなのでデプスバッファは気にしない
		// デプスバッファとして使用する最小バッファサイズ, 16ビット
//		EGL_DEPTH_SIZE,		16,
//		EGL_RECORDABLE_ANDROID, 1,
		// 終端マーカ
		EGL_NONE,			EGL_NONE,
		EGL_NONE,			EGL_NONE,
		EGL_NONE,
	};

	// 有効にするEGLパラメータ:RGBA8888(RGBA)
	EGLint attribs_rgba8888[] = {
		// レンダリングタイプ(OpenGL|ES2)
		EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
		// サーフェースタイプ, ダブルバッファを使用するのでEGL_WINDOW_BIT
		EGL_SURFACE_TYPE,	EGL_WINDOW_BIT,
		// 赤色で使用する最小フレームバッファサイズ, 8ビット
		EGL_RED_SIZE,		8,
		// 緑色で使用する最小フレームバッファサイズ, 8ビット
		EGL_GREEN_SIZE,		8,
		// 青色で使用する最小フレームバッファサイズ, 8ビット
		EGL_BLUE_SIZE,		8,
		// アルファで使用する最小フレームバッファサイズ, 8ビット
        EGL_ALPHA_SIZE, 	8,
        // 今のところ2D表示だけなのでデプスバッファは気にしない
		// デプスバッファとして使用する最小バッファサイズ, 16ビット
//		EGL_DEPTH_SIZE,		16,
		// ステンシルバッファとして使用する最小バッファサイズ, 8ビット
//		EGL_STENCIL_SIZE,	8,
//		EGL_RECORDABLE_ANDROID, 1,
		// 終端マーカ
		EGL_NONE,			EGL_NONE,
		EGL_NONE,			EGL_NONE,
		EGL_NONE,
	};

	// 条件に合うEGLフレームバッファ設定のリストを取得
	EGLint numConfigs;
	EGLBoolean ret = EGL_FALSE;
	EGLint err;

	int offset = NUM_ARRAY_ELEMENTS(attribs_rgba8888) - 5;
	if (with_depth_buffer) {
		attribs_rgba8888[offset++] = EGL_DEPTH_SIZE;
		attribs_rgba8888[offset++] = 16;
	}
	if (isRecordable) {
		attribs_rgba8888[offset++] = EGL_RECORDABLE_ANDROID;
		attribs_rgba8888[offset++] = 1;
	}
	ret = eglChooseConfig(mEglDisplay, attribs_rgba8888, &mEglConfig, 1, &numConfigs);
	if (UNLIKELY(!ret || !numConfigs)) {
		// fall back to rgb565
		offset = NUM_ARRAY_ELEMENTS(attribs_rgb565) - 5;
		if (with_depth_buffer) {
			attribs_rgb565[offset++] = EGL_DEPTH_SIZE;
			attribs_rgb565[offset++] = 16;
		}
		if (isRecordable) {
			attribs_rgb565[offset++] = EGL_RECORDABLE_ANDROID;
			attribs_rgb565[offset++] = 1;
		}
		ret = eglChooseConfig(mEglDisplay, attribs_rgb565, &mEglConfig, 1, &numConfigs);
		if (UNLIKELY(!ret || !numConfigs)) {
			err = eglGetError();
			LOGE("EGLWindow:failed to eglChooseConfig,err=%d", err);
			RETURN(-err, int);
		}
	}
	RETURN(ret, int);
}

/*private(friend)*/
int EGLBase::initEGLContext(EGLContext shared_context, const bool &with_depth_buffer, const bool &isRecordable) {
	ENTER();

	if (!shared_context) shared_context = EGL_NO_CONTEXT;

	const EGLint attrib_list[] = {
		EGL_CONTEXT_CLIENT_VERSION, 2,		// OpenGL|ES2.0を指定
		EGL_NONE,
	};

	EGLBoolean ret = EGL_FALSE;
	EGLint err;
	// EGLディスプレイコネクションを取得
	mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
	if (UNLIKELY(mEglDisplay == EGL_NO_DISPLAY)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglGetDisplay:err=%d", err);
		RETURN(-err, int);
	}
	// EGLディスプレイコネクションを初期化
	ret = eglInitialize(mEglDisplay, &mMajar, &mMinor);
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglInitialize,err=%d", err);
		RETURN(-err, int);
	}
	MARK("EGL ver.%d.%d", mMajar, mMinor);
	// コンフィグレーションを選択
	ret = getConfig(2, with_depth_buffer, isRecordable);
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to getConfig,err=%d", err);
		RETURN(-err, int);
	}

	// EGLレンダリングコンテキストを取得(OpenGL|ES2.0)
	mEglContext = eglCreateContext(mEglDisplay, mEglConfig, shared_context, attrib_list);

	EGLint value;
	eglQueryContext(mEglDisplay, mEglContext, EGL_CONTEXT_CLIENT_VERSION, &value);
	MARK("EGLContext created, client version %d", value);
    RETURN(0, int);
}

/*private(friend)*/
void EGLBase::releaseEGLContext() {
	ENTER();

	// EGLディスプレイを破棄
	if (mEglDisplay != EGL_NO_DISPLAY) {
		eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		GLCHECK("eglMakeCurrent");
		// EGLレンダリングコンテキストを破棄
		MARK("eglDestroyContext");
		eglDestroyContext(mEglDisplay, mEglContext);
		MARK("eglTerminate");
		eglTerminate(mEglDisplay);
		// eglReleaseThreadを入れるとSC-06D(4.1.2)がハングアップする
		// ・・・Android4.2以降でサポートされたと書いてるWebが有った
		// EGL1.2以上でサポート
//		if ((mMajar > 1) || ((mMajar == 1) && (mMinor >= 2)))
//			eglReleaseThread();
	}
	mEglDisplay = EGL_NO_DISPLAY;
	mEglContext = EGL_NO_CONTEXT;
	mEglConfig = 0;
	MARK("release:finished");
	EXIT();
}

/*private(friend)*/
EGLSurface EGLBase::createWindowSurface(ANativeWindow *window,
	const int32_t &request_width, const int32_t &request_height,
	const bool &low_resolution) {

	EGLBoolean ret = EGL_FALSE;
	EGLint err;
	int32_t format;

	EGLSurface surface = EGL_NO_SURFACE;
	ENTER();
	// EGLウインドウサーフェースを取得
	// XXX 一度でもANativeWindow_lockを呼び出してしまっているとeglCreateWindowSurfaceが失敗する
	surface = eglCreateWindowSurface(mEglDisplay, mEglConfig, window, NULL);
	if (UNLIKELY(surface == EGL_NO_SURFACE)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglCreateWindowSurface");
		return EGL_NO_SURFACE;
	}
	// EGLフレームバッファ設定情報を取得
	ret = eglGetConfigAttrib(mEglDisplay, mEglConfig, EGL_NATIVE_VISUAL_ID, &format);
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglGetConfigAttrib,err=%d", err);
		return EGL_NO_SURFACE;
	}
	MARK("format=%d", format);
	// NativeWindowへバッファを設定
	if (!low_resolution) {
		// XXX サイズのチェックを入れるとNexus7(2012)でFullHDの際に画面が出ない。bad packetばかりになる
		ANativeWindow_setBuffersGeometry(window, request_width, request_height, format);
	} else {
		// 描画ピクセル数を少なくしてパフォーマンス改善するために、
		// EGLWindowの横幅を640に固定、縦はアスペクト比に応じて調整する場合
		// 4:3 = 16:12 => 3 x 16 / 4 = 12, 12 x 16 / 16 = 12
		// 16:9 => 9 x 16 / 16 = 9
		if (request_height && request_width) {
			const int res = (request_height * 16) / request_width;
			ANativeWindow_setBuffersGeometry(window, 640, res * 40, format);
		} else {
			ANativeWindow_setBuffersGeometry(window, 640, 480, format);
		}
	}
	// EGLレンダンリングコンテキストをEGLウインドウサーフェースにアタッチ
	if (eglMakeCurrent(mEglDisplay, surface, surface, mEglContext) == EGL_FALSE) {
		GLCHECK("eglMakeCurrent");
		err = eglGetError();
		LOGW("EGLWindow:Fail to eglMakeCurrent");
		return EGL_NO_SURFACE;
	}
	return surface;
}

/*private(friend)*/
EGLSurface EGLBase::createOffscreenSurface(const int32_t &request_width, const int32_t &request_height) {
	EGLint surfaceAttribs[] = {
            EGL_WIDTH, request_width,
            EGL_HEIGHT, request_height,
            EGL_NONE
    };
    eglWaitGL();
	EGLSurface result = EGL_NO_SURFACE;
	result = eglCreatePbufferSurface(mEglDisplay, mEglConfig, surfaceAttribs);
	GLCHECK("eglCreatePbufferSurface");
	if (!result) {
		LOGE("surface was null");
		return EGL_NO_SURFACE;
	}
	makeCurrent(result);
	return result;
}

/*private(friend)*/
int EGLBase::makeCurrent(EGLSurface surface) {
	// EGLレンダンリングコンテキストをEGLウインドウサーフェースにアタッチ
	if (LIKELY(mEglDisplay != EGL_NO_DISPLAY)) {
		EGLBoolean ret = eglMakeCurrent(mEglDisplay, surface, surface, mEglContext);
		GLCHECK("eglMakeCurrent");
		if (UNLIKELY(ret == EGL_FALSE)) {
			LOGW("bind:Failed to eglMakeCurrent");
			RETURN(-1, int);
		}
		RETURN(0, int);
	} else {
		RETURN(-1, int);
	}
}

/*private(friend)*/
int EGLBase::makeDefault() {
	ENTER();
	// EGLレンダリングコンテキストとEGLサーフェースをデタッチ
	if (LIKELY(mEglDisplay != EGL_NO_DISPLAY)) {
		MARK("eglMakeCurrent");
		EGLBoolean ret = eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		GLCHECK("eglMakeCurrent");
		if (UNLIKELY(ret == EGL_FALSE)) {
			LOGW("makeDefault:Failed to eglMakeCurrent");
			RETURN(-1, int);
		}
	}
	RETURN(0, int);
}

/*private(friend)*/
int EGLBase::swap(EGLSurface surface) {
	eglWaitGL();
	GLCHECK("eglWaitGL");
	EGLint err = 0;
    EGLBoolean ret = eglSwapBuffers(mEglDisplay, surface);
    if (UNLIKELY(!ret)) {
        err = eglGetError();
        LOGW("eglSwapBuffers:err=%d", err);
    	return -err;	// RETURN(-err, int);
    }
	return -err;	// RETURN(-err, int);
}

/*private(friend)*/
void EGLBase::releaseSurface(EGLSurface surface) {
    if (surface != EGL_NO_SURFACE) {
    	eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		GLCHECK("eglMakeCurrent");
    	eglDestroySurface(mEglDisplay, surface);
    }
}

/*private(friend)*/
bool EGLBase::isCurrent(EGLSurface surface) {
	return eglGetCurrentSurface(EGL_DRAW) == surface;
}

/*public*/
EGLBaseSurface::EGLBaseSurface(EGLBase *egl, ANativeWindow *window,
	const int32_t &request_width, const int32_t &request_height, const bool &low_reso)
:	mEgl(egl),
 	mEglSurface(egl->createWindowSurface(window, request_width, request_height, low_reso)),
	window_width(0), window_height(0) {

	updateWindowSize();
}

/*public*/
EGLBaseSurface::EGLBaseSurface(EGLBase *egl, const int32_t &request_width, const int32_t &request_height)
:	mEgl(egl),
 	mEglSurface(egl->createOffscreenSurface(request_width, request_height)),
	window_width(request_width), window_height(request_height) {

	updateWindowSize();
}

/*public*/
EGLBaseSurface::~EGLBaseSurface() {
	release();
}

/*public*/
int EGLBaseSurface::bind() {
	ENTER();
	int result = mEgl->makeCurrent(mEglSurface);
	if (LIKELY(!result)) {
#if UPDATE_WINDOW_SIZE_EVERYTIME
		updateWindowSize();
#else
		SET_VIEWPORT
#endif
	}
	return result;
}

/*public*/
int EGLBaseSurface::unbind() {
	return mEgl->makeDefault();
};

/*public*/
int EGLBaseSurface::swap() {
	return mEgl->swap(mEglSurface);
}

/*public*/
void EGLBaseSurface::release() {
	if (mEglSurface != EGL_NO_SURFACE) {
		if (!bind())
			clear(0/*0xffff0000*/, true);
		mEgl->makeDefault();
		mEgl->releaseSurface(mEglSurface);
		mEglSurface = EGL_NO_SURFACE;
	}
}

bool EGLBaseSurface::isCurrent() {
	return mEgl->isCurrent(mEglSurface);
}

/*public*/
// @param color = ARGB
int EGLBaseSurface::clear(const int &color, const bool &need_swap) {
	ENTER();

	const GLclampf a = (color & 0xff000000) / (GLfloat)0xff000000;
	const GLclampf r = (color & 0x00ff0000) / (GLfloat)0x00ff0000;
	const GLclampf g = (color & 0x0000ff00) / (GLfloat)0x0000ff00;
	const GLclampf b = (color & 0x000000ff) / (GLfloat)0x000000ff;

    glClearColor(r, g, b, a);
	GLCHECK("glClearColor");
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	GLCHECK("glClear");
    if (need_swap)
    	swap();

    RETURN(0, int);
}

/*private*/
void EGLBaseSurface::updateWindowSize() {
	EGLint err;
	EGLint width = window_width, height = window_height;
	// 画面サイズ・フォーマットの取得
	EGLBoolean ret = eglQuerySurface(mEgl->mEglDisplay, mEglSurface, EGL_WIDTH, &width);
	if (ret) {
		ret = eglQuerySurface(mEgl->mEglDisplay, mEglSurface, EGL_HEIGHT, &height);
		GLCHECK("eglQuerySurface:height");
	}
	if (!ret) {
		err = eglGetError();
		LOGW("EGLWindow:Fail to eglQuerySurface:err=%d", err);
		return;
	}
	window_width = width;
	window_height = height;
	MARK("window(%d,%d)", window_width, window_height);
	SET_VIEWPORT
}
