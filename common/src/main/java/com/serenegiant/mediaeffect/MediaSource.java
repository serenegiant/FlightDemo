package com.serenegiant.mediaeffect;
/*
 * Copyright (c) 2015 saki t_saki@serenegiant.com
 *
 * File name: MediaSource.java
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

import android.util.Log;

import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.TextureOffscreen;

public class MediaSource implements ISource {
	private static final boolean DEBUG = false;
	private static final String TAG = "MediaSource";

	protected TextureOffscreen mSourceScreen;
	protected TextureOffscreen mOutputScreen;
	protected int mWidth, mHeight;
	protected int[] mSrcTexIds = new int[1];
	protected boolean needSwap;

	/**
	 * コンストラクタ
	 * GLコンテキスト内で生成すること
	 */
	public MediaSource() {
		resize(1, 1);
	}

	public MediaSource(final int width, final int height) {
		resize(width, height);
	}

	@Override
	public ISource reset() {
		needSwap = false;
		mSrcTexIds[0] = mSourceScreen.getTexture();
		return this;
	}

	@Override
	public ISource resize(final int width, final int height) {
		if (DEBUG) Log.v(TAG, String.format("resize(%d,%d):", width, height));
		if (mWidth != width || mHeight != height) {
			if (mSourceScreen != null) {
				mSourceScreen.release();
				mSourceScreen = null;
			}
			if (mOutputScreen != null) {
				mOutputScreen.release();
				mOutputScreen = null;
			}
			if ((width > 0) && (height > 0)) {
				mSourceScreen = new TextureOffscreen(width, height, false);
				mOutputScreen = new TextureOffscreen(width, height, false);
				mWidth = width;
				mHeight = height;
				mSrcTexIds[0] = mSourceScreen.getTexture();
			}
		}
		needSwap = false;
		return this;
	}

	@Override
	public ISource apply(final IEffect effect) {
		if (mSourceScreen != null) {
			if (needSwap) {
				final TextureOffscreen temp = mSourceScreen;
				mSourceScreen = mOutputScreen;
				mOutputScreen = temp;
				mSrcTexIds[0] = mSourceScreen.getTexture();
			}
			needSwap = !needSwap;
//			effect.apply(mSrcTexIds, mOutputScreen.getTexWidth(), mOutputScreen.getTexHeight(), mOutputScreen.getTexture());
			effect.apply(this); // このメソッド呼び出しは1つ上のコメントしてある行と結果は等価だけど効率はいい。
		}
		return this;
	}

	@Override
	public int getWidth() {
		return mOutputScreen.getTexWidth();
	}

	@Override
	public int getHeight() {
		return mOutputScreen.getTexHeight();
	}

	@Override
	public int[] getSourceTexId() {
		return mSrcTexIds;
	}

	@Override
	public int getOutputTexId() {
		return mOutputScreen.getTexture();
	}

	@Override
	public float[] getTexMatrix() {
		return mOutputScreen.getTexMatrix();
	}

	@Override
	public TextureOffscreen getOutputTexture() {
		return needSwap ? mOutputScreen : mSourceScreen;
	}

	@Override
	public void release() {
		if (mSourceScreen != null) {
			mSourceScreen.release();
			mSourceScreen = null;
		}
		if (mOutputScreen != null) {
			mOutputScreen.release();
			mOutputScreen = null;
		}
	}

	public MediaSource bind(final int index) {
		mSourceScreen.bind();
		return this;
	}

	public MediaSource unbind(final int index) {
		mSourceScreen.unbind();
		reset();
		return this;
	}

	public MediaSource setSource(final FullFrameRect drawer, final int tex_id, final float[] tex_matrix) {
		mSourceScreen.bind();
		try {
			drawer.draw(tex_id, tex_matrix, 0);
		} catch (RuntimeException e) {
			Log.w(TAG, e);
		} finally {
			mSourceScreen.unbind();
		}
		reset();
		return this;
	}
}
