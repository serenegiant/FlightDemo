package com.serenegiant.widget.gl;

import android.content.Context;

public interface IModelView {
	public static final int MODEL_BEBOP = 0;
	public static final int MODEL_MINIDRONE = 1;
	public static final int MODEL_JUMPINGSUMO = 2;
	public static final int MODEL_NUM = 3;

	public void setModel(final int model, final int type);
	public GLGraphics getGLGraphics();
	public FileIO getFileIO();
	public FileIO getExtFileIO();
	public FileIO getAssetIO();
	public Context getContext();
	//
	public int getNextPickId();
	public boolean isLandscape();
	public IScreen getCurrentScreen();
	public void setScreen(final IScreen screen);

	/**
	 * 目標FPS設定
	 * @param fps
	 */
	public void setFpsRequest(final float fps);

	public void onResume();
	public void onPause();
	public void release();

	/**
	 * Viewを表示するかどうかを設定
	 * @param visibility View#VISIBLE/INVISIBLE/GONE
	 */
	public void setVisibility(final int visibility);
	/**
	 * 描画要求
	 */
	public void requestRender();

	/**
	 * Viewの幅を取得
	 * @return
	 */
	public int getWidth();

	/**
	 * Viewの高さを取得
	 * @return
	 */
	public int getHeight();

	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[度]
	 * @param pitch 前後の傾き(機種の上げ下げ)[度]
	 * @param yaw 水平回転[度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz);
}
