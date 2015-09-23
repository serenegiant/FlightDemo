package com.serenegiant.widget;
/*
 * AudioVideoPlayerSample
 * Sample project to play audio and video from MPEG4 file using MediaCodec.
 *
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: PlayerTextureView.java
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
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

public class PlayerTextureView extends TextureView implements AspectRatioViewInterface {

	public static final int SCALE_MODE_KEEP_ASPECT = 0;	// アスペクト比を保って最大化
	public static final int SCALE_MODE_STREACH = 1;		// 画面サイズに合わせて拡大縮小
	public static final int SCALE_MODE_CROP = 2;		// アスペクト比を保って短辺がフィットするようにCROP_CENTER

	private double mRequestedAspect = -1.0;
	private int mScaleMode = SCALE_MODE_KEEP_ASPECT;
	/**
	 * 拡大縮小回転移動のための射影行列
	 */
	protected final Matrix mImageMatrix = new Matrix();

	public PlayerTextureView(Context context) {
		this(context, null, 0);
	}

	public PlayerTextureView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlayerTextureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onResume() {
	}

	/**
	 * 映像の拡大縮小方法をセット
  	 * SCALE_MODE_KEEP_ASPECT, SCALE_MODE_STREACH, SCALE_MODE_CROP
	 * @param scale_mode
	 */
	 public void setScaleMode(final int scale_mode) {
		if (mScaleMode != scale_mode) {
			mScaleMode = scale_mode;
		}
	 }

	/**
	 * set aspect ratio of this view
	 * <code>aspect ratio = width / height</code>.
	 */
	public void setAspectRatio(double aspectRatio) {
		if (aspectRatio < 0) {
			throw new IllegalArgumentException();
		}
		if (mRequestedAspect != aspectRatio) {
			mRequestedAspect = aspectRatio;
			requestLayout();
		}
		if (mScaleMode == SCALE_MODE_CROP) {
			init();
		}
	}

	/**
	 * measure view size with keeping aspect ratio
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if ((mRequestedAspect > 0) && (mScaleMode == SCALE_MODE_KEEP_ASPECT)) {
			int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
			int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

			final int horizPadding = getPaddingLeft() + getPaddingRight();
			final int vertPadding = getPaddingTop() + getPaddingBottom();
			initialWidth -= horizPadding;
			initialHeight -= vertPadding;

			final double viewAspectRatio = (double)initialWidth / initialHeight;
			final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

			// stay size if the difference of calculated aspect ratio is small enough from specific value
			if (Math.abs(aspectDiff) > 0.01) {
				if (aspectDiff > 0) {
					// adjust heght from width
					initialHeight = (int) (initialWidth / mRequestedAspect);
				} else {
					// adjust width from height
					initialWidth = (int) (initialHeight * mRequestedAspect);
				}
				initialWidth += horizPadding;
				initialHeight += vertPadding;
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
			}
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void reset() {
		init();
	}

	private void init() {
		// apply matrix
		mImageMatrix.reset();
		switch (mScaleMode) {
		case SCALE_MODE_KEEP_ASPECT:
		case SCALE_MODE_STREACH:
			// 何もしない
			break;
		case SCALE_MODE_CROP: // FIXME もう少し式を整理できそう
			final int view_width = getWidth();
			final int view_height = getHeight();
			final double video_width = mRequestedAspect * view_height;
			final double video_height = view_height;
			final double scale_x = view_width / video_width;
			final double scale_y = view_height / video_height;
			final double scale = Math.max(scale_x,  scale_y);	// SCALE_MODE_CROP
//			final double scale = Math.min(scale_x, scale_y);	// SCALE_MODE_KEEP_ASPECT
			final double width = scale * video_width;
			final double height = scale * video_height;
//			Log.v(TAG, String.format("size(%1.0f,%1.0f),scale(%f,%f),mat(%f,%f)",
//				width, height, scale_x, scale_y, width / view_width, height / view_height));
			mImageMatrix.postScale((float)(width / view_width), (float)(height / view_height), view_width / 2, view_height / 2);
			break;
		}
		setTransform(mImageMatrix);
	}
}
