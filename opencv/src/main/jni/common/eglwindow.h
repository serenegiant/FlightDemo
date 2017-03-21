/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
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
