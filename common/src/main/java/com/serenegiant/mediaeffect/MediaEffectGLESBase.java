package com.serenegiant.mediaeffect;
/*
 * Copyright (c) 2015 saki t_saki@serenegiant.com
 *
 * File name: MediaEffectKernel.java
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

import android.opengl.GLES20;
import android.util.Log;

import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.glutils.TextureOffscreen;

/**
 * OpenGL|ES2のフラグメントシェーダーで映像効果を与える時の基本クラス
 */
public abstract class MediaEffectGLESBase implements IEffect {
	private static final boolean DEBUG = false;
	private static final String TAG = "MediaEffectGLESBase";

	protected FullFrameRect mDrawer;
	protected TextureOffscreen mOutputOffscreen;
	protected boolean mEnabled = true;

	public MediaEffectGLESBase(final String shader) {
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
		mDrawer = new FullFrameRect(new Texture2dProgram(GLES20.GL_TEXTURE_2D, shader));
	}

	/**
	 * If you know the source texture came from MediaSource,
	 * using #apply(MediaSource) is much efficient instead of this
	 * @param src_tex_ids
	 * @param width
	 * @param height
	 * @param out_tex_id
	 */
	@Override
	public void apply(final int [] src_tex_ids, final int width, final int height, final int out_tex_id) {
		if (!mEnabled) return;
		if (mOutputOffscreen == null) {
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		if ((out_tex_id != mOutputOffscreen.getTexture())
			|| (width != mOutputOffscreen.getWidth())
			|| (height != mOutputOffscreen.getHeight())) {
			mOutputOffscreen.assignTexture(out_tex_id, width, height);
		}
		mOutputOffscreen.bind();
		mDrawer.draw(src_tex_ids[0], mOutputOffscreen.getTexMatrix(), 0);
		mOutputOffscreen.unbind();
	}

	@Override
	public void apply(final ISource src) {
		if (!mEnabled) return;
		if (src instanceof MediaSource) {
			final TextureOffscreen output_tex = ((MediaSource)src).getOutputTexture();
			final int[] src_tex_ids = src.getSourceTexId();
			output_tex.bind();
			mDrawer.draw(src_tex_ids[0], output_tex.getTexMatrix(), 0);
			output_tex.unbind();
		} else {
			apply(src.getSourceTexId(), src.getWidth(), src.getHeight(), src.getOutputTexId());
		}
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		if (mDrawer != null) {
			mDrawer.release();
			mDrawer = null;
		}
		if (mOutputOffscreen != null) {
			mOutputOffscreen.release();
			mOutputOffscreen = null;
		}
	}

	@Override
	public MediaEffectGLESBase resize(final int width, final int height) {
		if ((mOutputOffscreen == null) || (width != mOutputOffscreen.getWidth())
			|| (height != mOutputOffscreen.getHeight())) {
			if (mOutputOffscreen != null)
				mOutputOffscreen.release();
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		mDrawer.getProgram().setTexSize(width, height);
		return this;
	}

	@Override
	public boolean enabled() {
		return mEnabled;
	}

	@Override
	public IEffect setEnable(final boolean enable) {
		mEnabled = enable;
		return this;
	}
}
