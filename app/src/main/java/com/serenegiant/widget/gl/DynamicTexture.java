package com.serenegiant.widget.gl;

import android.graphics.SurfaceTexture;
import android.util.Log;

import com.serenegiant.glutils.GLHelper;

import javax.microedition.khronos.opengles.GL10;

public class DynamicTexture extends Texture {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = DynamicTexture.class.getSimpleName();

	private SurfaceTexture mSurfaceTexture;
	private volatile boolean mNeedUpdate;

	public DynamicTexture(final IModelView modelView) {
		super(modelView, GL_TEXTURE_EXTERNAL_OES);
		init();
	}

	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		if (mSurfaceTexture != null) {
			mSurfaceTexture.release();
			mSurfaceTexture = null;
		}
		super.release();
	}

	public SurfaceTexture getSurfaceTexture() {
		return mSurfaceTexture;
	}

	public void bind() {
//		if (DEBUG) Log.v(TAG, "bind:mNeedUpdate=" + mNeedUpdate);
		super.bind();
		if (mSurfaceTexture != null) {
			if (mNeedUpdate) {
				mNeedUpdate = false;
				mSurfaceTexture.updateTexImage();
				mSurfaceTexture.getTransformMatrix(mTexMatrix);
			}
		}
	}

	public void setSize(final int width, final int height) {
		if (DEBUG) Log.v(TAG, String.format("setSize(%d,%d):", width, height) + mSurfaceTexture);
		if (mSurfaceTexture != null) {
			mSurfaceTexture.setDefaultBufferSize(width, height);
		}
	}

	private void init() {
		if (DEBUG) Log.v(TAG, "init:");
		textureID = GLHelper.initTex(glGraphics.getGL(), mTexTarget, GL10.GL_LINEAR);
		mSurfaceTexture = new SurfaceTexture(textureID);
		mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
	}

	private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
		@Override
		public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//			if (DEBUG) Log.v(TAG, "onFrameAvailable:");
			mNeedUpdate = true;
		}
	};

}
