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

#ifndef EGLBASE_H_
#define EGLBASE_H_

#pragma interface

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <android/native_window.h>
#include "glutils.h"
#include "Timers.h"

class EGLBase {
public:
	EGLBase(EGLBase *shared_context, const bool &with_depth_buffer, const bool &isRecordable);
	EGLBase(EGLBase *shared_context);
#if 0
	// EGLContextはvoidへのポインタなのでなんでも受け付けてしまって危険なので(例えばEGLBaseへのポインタでも)コメントアウト
	EGLBase(EGLContext shared_context, const bool &with_depth_buffer, const bool &isRecordable);
#endif
	~EGLBase();
	inline EGLContext getContext() { return mEglContext; }
private:
    friend class EGLBaseSurface;
	EGLDisplay mEglDisplay;
	EGLContext mEglContext;
	EGLConfig mEglConfig;
	EGLint mMajar, mMinor;
	bool mWithDepthBuffer;
	bool mIsRecordable;
	int getConfig(const int &version, const bool &with_depth_buffer, const bool &isRecordable);
	int initEGLSurface();
	int initEGLContext(EGLContext shared_context, const bool &with_depth_buffer, const bool &isRecordable);
	void releaseEGLContext();
	EGLSurface createWindowSurface(ANativeWindow *window, const int32_t &request_width, const int32_t &request_height, const bool &low_resolution);
	EGLSurface createOffscreenSurface(const int32_t &request_width, const int32_t &request_height);
	int makeCurrent(EGLSurface surface);
	int makeDefault();
	int swap(EGLSurface surface);
	void releaseSurface(EGLSurface surface);
	bool isCurrent(EGLSurface surface);
};

class EGLBaseSurface {
public:
	EGLBaseSurface(EGLBase *egl, ANativeWindow *window, const int32_t &width, const int32_t &height, const bool &low_reso = false);
	EGLBaseSurface(EGLBase *egl, const int32_t &width, const int32_t &height);
	~EGLBaseSurface();
	int bind();
	int unbind();
	int swap();
	void release();
	bool isCurrent();
	int clear(const int &color, const bool &need_swap = false);
private:
	EGLBase *mEgl;
	EGLSurface mEglSurface;
	int32_t window_width, window_height;
	bool low_resolution;	// このEGLWindowのサイズを最小限にするかどうか(4:3なら640x480, 16:9なら640x360)
	void updateWindowSize();
};


#endif /* EGLBASE_H_ */
