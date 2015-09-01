package com.serenegiant.glutils;
/*
 * Copyright (c) 2015 saki t_saki@serenegiant.com
 *
 * File name: EglTask.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL;

import com.serenegiant.utils.MessageTask;

public abstract class EglTask extends MessageTask {
//	private static final boolean DEBUG = false;
//	private static final String TAG = "EglTask";

	public static final int EGL_FLAG_DEPTH_BUFFER = 1;
	public static final int EGL_FLAG_RECORDABLE = 2;

	private EGLBase mEgl = null;
	private EGLBase.EglSurface mEglHolder;

	public EglTask(final EGLContext shared_context, final int flags) {
//		if (DEBUG) Log.i(TAG, "shared_context=" + shared_context);
		init(flags, 3, shared_context);
	}

	public EglTask(final int max_version, final EGLContext shared_context, final int flags) {
//		if (DEBUG) Log.i(TAG, "shared_context=" + shared_context);
		init(flags, max_version, shared_context);
	}

	@Override
	protected void onInit(final int request, final int flags, final int max_version, final Object shared_context) {
		if ((shared_context == null) || (shared_context instanceof EGLContext))
			mEgl = new EGLBase(max_version, ((EGLContext)shared_context),
				(flags & EGL_FLAG_DEPTH_BUFFER) == EGL_FLAG_DEPTH_BUFFER,
				(flags & EGL_FLAG_RECORDABLE) == EGL_FLAG_RECORDABLE);
		if (mEgl == null) {
			callOnError(new RuntimeException("failed to create EglCore"));
			releaseSelf();
		} else {
			mEglHolder = new EGLBase.EglSurface(mEgl, 1, 1);
			mEglHolder.makeCurrent();
		}
	}

	@Override
	protected Request takeRequest() throws InterruptedException {
		final Request result = super.takeRequest();
		mEglHolder.makeCurrent();
		return result;
	}

	@Override
	protected void onBeforeStop() {
		mEglHolder.makeCurrent();
	}

	@Override
	protected void onRelease() {
		mEglHolder.release();
		mEgl.release();
	}

	protected EGLBase getEgl() {
		return mEgl;
	}

	protected EGLContext getContext() {
		return mEgl != null ? mEgl.getContext() : null;
	}

	protected GL getGl() {
		return mEgl != null ? mEgl.getGl() : null;
	}

	protected void makeCurrent() {
		mEglHolder.makeCurrent();
	}
}
