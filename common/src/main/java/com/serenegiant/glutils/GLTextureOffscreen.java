package com.serenegiant.glutils;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: GLTextureOffscreen.java
 *
*/

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * テクスチャへOpenGL|ESで描画するためのオフスクリーン描画クラス
 * テクスチャをカラーバッファとしてFBOに割り当てる
 */
public class GLTextureOffscreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
//	private static final String TAG = "GLTextureOffscreen";

	private final int TEX_TARGET;
	private final int mWidth, mHeight;					// 描画領域サイズ
	private int mTexWidth, mTexHeight;					// テクスチャサイズ
	private int mFBOTextureId;							// オフスクリーンのカラーバッファに使うテクスチャ
	private int mDepthBufferObj, mFrameBufferObj;		// オフスクリーン用のバッファオブジェクト
	private final float[] mTexMatrix = new float[16];	// テクスチャ座標変換行列

	/**
	 * コンストラクタ
	 * @param width オフスクリーンのサイズ
	 * @param height
	 * @param use_depth_buffer デプスバッファを使用する場合はtrue、深度は16ビット固定
	 */
	public GLTextureOffscreen(final int width, final int height, final boolean use_depth_buffer) {
//		if (DEBUG) Log.v(TAG, "コンストラクタ");
		TEX_TARGET = GLES20.GL_TEXTURE_2D;
		mWidth = width;
		mHeight = height;

		prepareFramebuffer(width, height, use_depth_buffer);
	}

	/**
	 * コンストラクタ
	 * @param target GLES20.GL_TEXTURE_2D
	 * @param width オフスクリーンのサイズ
	 * @param height
	 * @param use_depth_buffer
	 */
	public GLTextureOffscreen(final int target, final int width, final int height, final boolean use_depth_buffer) {
//		if (DEBUG) Log.v(TAG, "コンストラクタ");
		TEX_TARGET = target;
		mWidth = width;
		mHeight = height;

		prepareFramebuffer(width, height, use_depth_buffer);
	}

	@Override
	protected void finalize() throws Throwable {
		release();	// GLコンテキスト内じゃない可能性があるのであまり良くないけど
		super.finalize();
	}

	/**
	 * 破棄する
	 */
	public void release() {
//		if (DEBUG) Log.v(TAG, "release");
		releaseFrameBuffer();
	}

	/**
	 * オフスクリーン描画用のレンダリングバッファに切り替える
	 * Viewportも変更になるので必要であればunbind後にViewportの設定をすること
	 */
	public void bind() {
//		if (DEBUG) Log.v(TAG, "bind:");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferObj);
		GLES20.glViewport(0, 0, mWidth, mHeight);
	}

	/**
	 * デフォルトのレンダリングバッファに戻す
	 */
	public void unbind() {
//		if (DEBUG) Log.v(TAG, "unbind:");
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

	/**
	 * テクスチャ座標変換行列を取得(内部配列を直接返すので変更時は要注意)
	 * @return
	 */
	public float[] getTexMatrix() {
		return mTexMatrix;
	}

	/**
	 * テクスチャ変換行列のコピーを返す
	 * 領域チェックしていないのでoffset位置から16個以上確保しておくこと
	 * @param matrix
	 */
	public void getTexMatrix(final float[] matrix, final int offset) {
		System.arraycopy(mTexMatrix, 0, matrix, offset, mTexMatrix.length);
	}

	/**
	 * オフスクリーンテクスチャ名を取得
	 * このオフスクリーンへ書き込んだ画像をテクスチャとして使って他の描画を行う場合に使用できる
	 * @return
	 */
	public int getTexture() {
		return mFBOTextureId;
	}

    /**
     * オフスクリーン描画用のフレームバッファを準備する
     */
    private final void prepareFramebuffer(final int width, final int height, final boolean use_depth_buffer) {
		if (DEBUG) GLHelper.checkGlError("prepareFramebuffer start");

    	// テクスチャのサイズは2の乗数にする
		int w = 32;
		for (; w < width; w <<= 1);
		int h = 32;
		for (; h < height; h <<= 1);
		if (mTexWidth != w || mTexHeight != h) {
			mTexWidth = w;
			mTexHeight = h;
		}

		final int[] ids = new int[1];
//		releaseFrameBuffer();
        // カラーバッファのためにテクスチャを生成する
        GLES20.glGenTextures(1, ids, 0);
        GLHelper.checkGlError("glGenTextures");
        mFBOTextureId = ids[0];   // expected > 0
        GLES20.glBindTexture(TEX_TARGET, mFBOTextureId);
        GLHelper.checkGlError("glBindTexture " + mFBOTextureId);

        // テクスチャのメモリ領域を確保する
        GLES20.glTexImage2D(TEX_TARGET, 0, GLES20.GL_RGBA, mTexWidth, mTexHeight, 0,
               GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLHelper.checkGlError("glTexImage2D");

        // テクスチャのパラメータをセットする
        GLES20.glTexParameterf(TEX_TARGET, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST /*GLES20.GL_LINEAR*/);
        GLES20.glTexParameterf(TEX_TARGET, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST /*GLES20.GL_LINEAR*/);
        GLES20.glTexParameteri(TEX_TARGET, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(TEX_TARGET, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLHelper.checkGlError("glTexParameter");

		if (use_depth_buffer) {
			// デプスバッファが必要な場合は、レンダーバッファオブジェクトを生成・初期化する
			GLES20.glGenRenderbuffers(1, ids, 0);
			mDepthBufferObj = ids[0];
			GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferObj);
			// デプスバッファは16ビット
			GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mTexWidth, mTexHeight);
		}
        // フレームバッファオブジェクトを生成してbindする
        GLES20.glGenFramebuffers(1, ids, 0);
        GLHelper.checkGlError("glGenFramebuffers");
        mFrameBufferObj = ids[0];    // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferObj);
        GLHelper.checkGlError("glBindFramebuffer " + mFrameBufferObj);

        // フレームバッファにカラーバッファ(テクスチャ)を接続する
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
        		TEX_TARGET, mFBOTextureId, 0);
        GLHelper.checkGlError("glFramebufferTexture2D");
		if (use_depth_buffer) {
			// フレームバッファにデプスバッファを接続する
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mDepthBufferObj);
	        GLHelper.checkGlError("glFramebufferRenderbuffer");
		}

        // 正常に終了したかどうかを確認する
        final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // デフォルトのフレームバッファに戻す
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		// テクスチャ座標変換行列を初期化
		Matrix.setIdentityM(mTexMatrix, 0);
		mTexMatrix[0] = width / (float)mTexWidth;
		mTexMatrix[5] = height / (float)mTexHeight;
    }

    /**
     * オフスクリーンフレームバッファを破棄
     */
    private final void releaseFrameBuffer() {
        final int[] ids = new int[1];
        // オフスクリーンのカラーバッファ用のテクスチャを破棄
        if (mFBOTextureId > 0) {
        	ids[0] = mFBOTextureId;
            GLES20.glDeleteTextures(1, ids, 0);
            mFBOTextureId = -1;
        }
		// デプスバッファがある時はデプスバッファを破棄
		if (mDepthBufferObj > 0) {
			ids[0] = mDepthBufferObj;
			GLES20.glDeleteRenderbuffers(1, ids, 0);
			mDepthBufferObj = 0;
		}
		// フレームバッファを破棄
        if (mFrameBufferObj > 0) {
        	ids[0] = mFrameBufferObj;
            GLES20.glDeleteFramebuffers(1, ids, 0);
            mFrameBufferObj = -1;
        }
    }

}
