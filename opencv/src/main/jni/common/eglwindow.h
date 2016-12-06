/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef EGLWINDOW_H
#define EGLWINDOW_H

#pragma interface

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <android/native_window.h>
#include "glutils.h"
#include "Timers.h"

//#define MEAS_TIME
/**
 * コンストラクタのwindowで指定したSurfaceにOpenGL|ES2で描画するためのオブジェクト
 */
class EGLWindow {
private:
	ANativeWindow *mWindow;
	EGLConfig mEglConfig;
	EGLDisplay mEglDisplay;
	EGLSurface mEglSurface;
	EGLContext mEglContext;
	EGLint mMajar, mMinor;
	int32_t window_width, window_height;
	int32_t request_width, request_height;
	bool low_resolution;	// このEGLWindowのサイズを最小限(4:3なら640x480, 16:9なら640x360)
#ifdef MEAS_TIME
	nsecs_t prev_swap_t, swap_interval;
	int32_t swap_count;
#endif
	int getConfig();
	int initEGLSurface();
	int initEGLContext();
	void destroyEGLContext();
	void release();
	void updateWindowSize();
public:
	EGLWindow(ANativeWindow *window, int32_t width, int32_t height, bool low_reso = false);
	~EGLWindow();
	inline bool canDraw();
	int bind();
	int unbind();
	int resetViewport();
	int startRender();
	int swapBuffers();
	int clear(int color, bool need_swap);
};

#endif //EGLWINDOW_H
