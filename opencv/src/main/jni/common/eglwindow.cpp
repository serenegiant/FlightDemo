/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#define LOG_TAG "EGLWindow"
#if 1	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
	#define USE_LOGALL
	#undef LOG_NDEBUG
	#undef NDEBUG
	#define PRINT_DIAG 1
	#define DEBUG_GL_CHECK		// GL関数のデバッグメッセージを表示する時
#endif

#include "utilbase.h"
#include "eglwindow.h"

// Android-specific extension.
#define	EGL_RECORDABLE_ANDROID	0x3142

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

/**
 * コンストラクタのwindowで指定したSurfaceにOpenGL|ES2で描画するためのオブジェクト
 * 生成時にこのウインドウにbindした状態になる
 */
EGLWindow::EGLWindow(ANativeWindow *window, int32_t width, int32_t height, bool low_reso)
:	mWindow(window),
	mEglConfig(0),
	mEglDisplay(EGL_NO_DISPLAY),
 	mEglSurface(EGL_NO_SURFACE),
	mEglContext(EGL_NO_CONTEXT),
 	window_width(0),
 	window_height(0),
	request_width(width),
 	request_height(height),
#ifdef MEAS_TIME
 	prev_swap_t(systemTime()),
 	swap_interval(0),
 	swap_count(-1),
#endif
	low_resolution(low_reso) {

	ENTER();

	initEGLContext();
	initEGLSurface();

	EXIT();
}

EGLWindow::~EGLWindow() {
	release();
}

inline bool EGLWindow::canDraw() {
	return mEglDisplay != EGL_NO_DISPLAY;
}

int EGLWindow::bind() {
	ENTER();
	// EGLレンダンリングコンテキストをEGLウインドウサーフェースにアタッチ
	if (LIKELY(mEglDisplay != EGL_NO_DISPLAY)) {
		EGLBoolean ret = eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
		GLCHECK("eglMakeCurrent");
		if (UNLIKELY(ret == EGL_FALSE)) {
			LOGW("bind:Failed to eglMakeCurrent");
			RETURN(-1, int)
		}
#if UPDATE_WINDOW_SIZE_EVERYTIME
		updateWindowSize();
#else
		SET_VIEWPORT
#endif
		RETURN(0, int);
	} else {
		RETURN(-1, int);
	}
}

int EGLWindow::unbind() {
	ENTER();
	// EGLレンダリングコンテキストとEGLサーフェースをデタッチ
	if (LIKELY(mEglDisplay != EGL_NO_DISPLAY)) {
		MARK("eglMakeCurrent");
		eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
		GLCHECK("eglMakeCurrent");
	}
	RETURN(0, int);
}

int EGLWindow::resetViewport() {
	ENTER();
	updateWindowSize();
	RETURN(0, int);
}

// complete native execution prior to subsequent GL rendering calls
int EGLWindow::startRender() {
//	ENTER();
	eglWaitNative(EGL_CORE_NATIVE_ENGINE);
#if UPDATE_WINDOW_SIZE_EVERYTIME
	updateWindowSize();
#else
	SET_VIEWPORT
#endif
	return 0;	// RETURN(0, int);
}

int EGLWindow::swapBuffers() {
//	ENTER();
#ifdef MEAS_TIME
	nsecs_t t = systemTime();
	swap_count++;
	if UNLIKELY(swap_count == 0) {
		prev_swap_t = t;
		swap_interval = 0;
	} else {
		swap_interval += (t - prev_swap_t);
		prev_swap_t = t;
		if UNLIKELY((swap_count % 100) == 0) {
			MARK("swapBuffers:interval=%5.2f", (swap_interval / (1000000.f * swap_count)));
		}
	}
#endif
	eglWaitGL();
	GLCHECK("eglWaitGL");
	EGLint err = 0;
    EGLBoolean ret = eglSwapBuffers(mEglDisplay, mEglSurface);
    if (UNLIKELY(!ret)) {
        err = eglGetError();
        LOGW("eglSwapBuffers:err=%d", err);
/*		if (err == EGL_BAD_SURFACE) {
            // Recreate surface
        	release();
            err = initEGLSurface();
            // Still consider glContext is valid
        } else if ( err == EGL_CONTEXT_LOST || err == EGL_BAD_CONTEXT ) {
            // Context has been lost!!
        	// ndk_helperのGLContextだとterminateとinitEGLContextを呼んでるけど
        	// それだとsurfaceとdisplayも破棄されるのでinitEGLSurfaceから
        	// 呼ばないとだめだと思う
        	destroyEGLContext();	// terminate
            err = initEGLContext();
            initEGLSurface();
        } */
    	return -err;	// RETURN(-err, int);
    }
	return -err;	// RETURN(-err, int);
}

// @param color = ARGB
int EGLWindow::clear(int color, bool need_swap) {
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
    	swapBuffers();

    RETURN(0, int);
}

//********************************************************************************
//********************************************************************************
int EGLWindow::getConfig() {
	ENTER();

	// 有効にするEGLパラメータ:RGB565
	const EGLint attribs_rgb565[] = {
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
        // 今のところ2D表示だけなのでデプスバッファは気にしない
		// デプスバッファとして使用する最小バッファサイズ, 16ビット
//		EGL_DEPTH_SIZE,		16,
		// ステンシルバッファとして使用する最小バッファサイズ, 8ビット
//		EGL_STENCIL_SIZE,	8,
//		EGL_RECORDABLE_ANDROID, 1,
		// 終端マーカ
		EGL_NONE,
	};

	// 有効にするEGLパラメータ:RGBA8888(RGBA)
	const EGLint attribs_rgba8888[] = {
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
		EGL_NONE,
	};

	// 条件に合うEGLフレームバッファ設定のリストを取得
	EGLint numConfigs;
	EGLBoolean ret = EGL_FALSE;
	EGLint err;
	ret = eglChooseConfig(mEglDisplay, attribs_rgba8888, &mEglConfig, 1, &numConfigs);
	if UNLIKELY(!ret || !numConfigs) {
		// fall back to rgb565
		ret = eglChooseConfig(mEglDisplay, attribs_rgb565, &mEglConfig, 1, &numConfigs);
		if UNLIKELY(!ret || !numConfigs) {
			err = eglGetError();
			LOGW("EGLWindow:failed to eglChooseConfig,err=%d", err);
			RETURN(-err, int);
		}
	}
	RETURN(ret, int);
}


int EGLWindow::initEGLContext() {
	ENTER();

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
//	EGLint majar, minor;
	ret = eglInitialize(mEglDisplay, &mMajar, &mMinor);
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglInitialize,err=%d", err);
		RETURN(-err, int);
	}
	MARK("EGL ver.%d.%d", mMajar, mMinor);
	// コンフィグレーションを選択
	ret = getConfig();
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to getConfig,err=%d", err);
		RETURN(-err, int);
	}

	// EGLレンダリングコンテキストを取得(OpenGL|ES2.0)
	mEglContext = eglCreateContext(mEglDisplay, mEglConfig, NULL, attrib_list);

	EGLint value;
	eglQueryContext(mEglDisplay, mEglContext, EGL_CONTEXT_CLIENT_VERSION, &value);
	MARK("EGLContext created, client version %d", value);
    RETURN(0, int);
}

int EGLWindow::initEGLSurface() {

	EGLBoolean ret = EGL_FALSE;
	EGLint err;
	int32_t format;

	ENTER();
	// EGLウインドウサーフェースを取得
	// XXX 一度でもANativeWindow_lockを呼び出してしまっているとeglCreateWindowSurfaceが失敗する
	mEglSurface = eglCreateWindowSurface(mEglDisplay, mEglConfig, mWindow, NULL);
	if (UNLIKELY(mEglSurface == EGL_NO_SURFACE)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglCreateWindowSurface");
		RETURN(-err, int);
	}
	// EGLフレームバッファ設定情報を取得
    /* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
     * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
     * As soon as we picked a EGLConfig, we can safely reconfigure the
     * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID. */
	ret = eglGetConfigAttrib(mEglDisplay, mEglConfig, EGL_NATIVE_VISUAL_ID, &format);
	if (UNLIKELY(!ret)) {
		err = eglGetError();
		LOGW("EGLWindow:failed to eglGetConfigAttrib,err=%d", err);
		RETURN(-err, int);
	}
	MARK("format=%d", format);
	// NativeWindowへバッファを設定
//	ANativeWindow_setBuffersGeometry(mWindow, 0, 0, format); // 下で必ずANativeWindow_setBuffersGeometryが呼び出されるからここには不要
	if (!low_resolution) {
		// XXX サイズのチェックを入れるとNexus7(2012)でFullHDの際に画面が出ない。bad packetばかりになる
//		int32_t w = ANativeWindow_getWidth(mWindow);
//		int32_t h = ANativeWindow_getHeight(mWindow);
//		if ((request_width && (w > request_width)) || (request_height && (h > request_height)))
		ANativeWindow_setBuffersGeometry(mWindow, request_width, request_height, format);
	} else {
		// 描画ピクセル数を少なくしてパフォーマンス改善するために、
		// EGLWindowの横幅を640に固定、縦はアスペクト比に応じて調整する場合
		// 4:3 = 16:12 => 3 x 16 / 4 = 12, 12 x 16 / 16 = 12
		// 16:9 => 9 x 16 / 16 = 9
		if (request_height && request_width) {
			const int res = (request_height * 16) / request_width;
			ANativeWindow_setBuffersGeometry(mWindow, 640, res * 40, format);
		} else {
			ANativeWindow_setBuffersGeometry(mWindow, 640, 480, format);
		}
	}
	// 元々ここでEGLウインドウサーフェースを取得していたけどndk_helperのEGLContext.cppだと
	// ANativeWindow_setBuffersGeometryよりも先だったので合わせた

	// EGLレンダンリングコンテキストをEGLウインドウサーフェースにアタッチ
	if (eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext) == EGL_FALSE) {
		err = eglGetError();
		LOGW("EGLWindow:Fail to eglMakeCurrent");
		RETURN(-err, int);
	}
#if PRINT_DIAG
	{	// FIXME デバッグのためにウインドウサイズをログへ出力する(バッファのサイズではなくView自体のサイズ)
		int32_t w = ANativeWindow_getWidth(mWindow);
		int32_t h = ANativeWindow_getHeight(mWindow);
		MARK("View(%d,%d)", w, h);
	}
#endif
	window_width = window_height = 0;
	updateWindowSize();
    clear(0/*0xff00ff00*/, true);

	RETURN(0, int);
}

void EGLWindow::destroyEGLContext() {
	ENTER();

	unbind();
	// EGLレンダリングコンテキストを破棄
	if (mEglContext != EGL_NO_CONTEXT) {
		MARK("eglDestroyContext");
		eglDestroyContext(mEglDisplay, mEglContext);
	}
	mEglContext = EGL_NO_CONTEXT;

	EXIT();
}

void EGLWindow::release() {
	ENTER();

	if (!bind())
		clear(0/*0xffff0000*/, true);
	// EGLレンダリングコンテキストを破棄
	destroyEGLContext();
	// EGLサーフェースを破棄
	if (mEglSurface != EGL_NO_SURFACE) {
		MARK("eglDestroySurface");
		eglDestroySurface(mEglDisplay, mEglSurface);
		mEglSurface = EGL_NO_SURFACE;
	}
	// EGLディスプレイを破棄
	if (mEglDisplay != EGL_NO_DISPLAY) {
		MARK("eglTerminate");
		eglTerminate(mEglDisplay);
		// eglReleaseThreadを入れるとSC-06D(4.1.2)がハングアップする
		// ・・・Android4.2以降でサポートされたと書いてるWebが有った
		// EGL1.2以上でサポート
//		if ((mMajar > 1) || ((mMajar == 1) && (mMinor >= 2)))
//			eglReleaseThread();
	}
	mEglDisplay = EGL_NO_DISPLAY;
	mEglConfig = 0;
	MARK("destroyEGL:finished");
	EXIT();
}

void EGLWindow::updateWindowSize() {
	EGLint err;
	EGLint width = window_width, height = window_height;
	// 画面サイズ・フォーマットの取得
	EGLBoolean ret = eglQuerySurface(mEglDisplay, mEglSurface, EGL_WIDTH, &width);
	if (ret) {
		ret = eglQuerySurface(mEglDisplay, mEglSurface, EGL_HEIGHT, &height);
		GLCHECK("eglQuerySurface:height");
	}
	if (!ret) {
		err = eglGetError();
		LOGW("EGLWindow:Fail to eglQuerySurface:err=%d", err);
		return;
	}
	if ((width != window_width) || (height != window_height)) {
		window_width = width;
		window_height = height;
		MARK("window(%d,%d),request(%d,%d)",
			window_width, window_height, request_width, request_height);
	}
	SET_VIEWPORT
}
