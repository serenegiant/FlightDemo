/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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
