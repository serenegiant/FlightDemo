package com.serenegiant.arflight.drone;

import android.graphics.SurfaceTexture;
import android.util.Log;

import com.serenegiant.gameengine.v1.DynamicTexture;
import com.serenegiant.gameengine.v1.GLCamera2D;
import com.serenegiant.gameengine.v1.GLScreen;
import com.serenegiant.gameengine.IModelView;
import com.serenegiant.gameengine.v1.StaticTexture;
import com.serenegiant.gameengine.v1.TextureDrawer2D;
import com.serenegiant.gameengine.v1.TextureRegion;
import com.serenegiant.gameengine.v1.TouchEvent;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenNull extends GLScreen implements IVideoScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBase";

//	protected final GLLookAtCamera lookAtCamera;

	protected final GLCamera2D guiCamera;
	private DynamicTexture mVideoFrameTexture;
	private volatile boolean mVideoEnabled;

	// 背景
	private StaticTexture backgroundTexture;
	private TextureRegion backgroundRegion;
	protected TextureDrawer2D mFullScreenDrawer;

	public AttitudeScreenNull(final IModelView modelView) {
		super(modelView);
		if (DEBUG) Log.v(TAG, String.format("コンストラクタ(%d,%d)", getWidth(), getHeight()));
		// 背景
		backgroundTexture = new StaticTexture(modelView, "background.png");
		backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, getWidth(), getHeight());
		mFullScreenDrawer = new TextureDrawer2D(glGraphics, getWidth(), getHeight());
		// 2Dカメラ
		guiCamera = new GLCamera2D(glGraphics, getWidth(), getHeight());
		// ライブ映像受け取り用のテクスチャオブジェクトを生成
		mVideoFrameTexture = new DynamicTexture(getView());
		mVideoFrameTexture.setSize(640, 368);
	}

	@Override
	public void update(final float deltaTime) {
	}

	@Override
	public void draw(final float deltaTime) {
//		if (DEBUG) Log.v(TAG_SCREEN, "draw");
		// 画面表示更新
		final GL10 gl = glGraphics.getGL();

		// ここから2Dの描画処理
		guiCamera.setMatrix();
		gl.glDisable(GL10.GL_LIGHTING);				// ライティングを無効化
		gl.glDisable(GL10.GL_CULL_FACE);			// ポリゴンのカリングを無効にする
		gl.glDisable(GL10.GL_DEPTH_TEST);			// デプステストを無効にする
//		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 背景を描画
		if (mVideoEnabled && (mVideoFrameTexture != null) && mVideoFrameTexture.isAvailable()) {
//			gl.glPushMatrix();
			mVideoFrameTexture.bind();
//			gl.glMultMatrixf(mVideoFrameTexture.texMatrix(), 0);	// これを入れると表示サイズがおかしい
			mFullScreenDrawer.draw();
			mVideoFrameTexture.unbind();
//			gl.glPopMatrix();
		} else {
			backgroundTexture.bind();
			mFullScreenDrawer.draw();
			backgroundTexture.unbind();
		}
	}

	@Override
	public void setAlpha(final float alpha) {
		// 何もしない
	}

	@Override
	public void resume() {
		if (DEBUG) Log.v(TAG, "resume");
		backgroundTexture.reload();
	}

	@Override
	public void pause() {
		if (DEBUG) Log.v(TAG, "pause");
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release");
		backgroundTexture.release();
		if (mVideoFrameTexture != null) {
			mVideoFrameTexture.release();
			mVideoFrameTexture = null;
		}
	}

	@Override
	public boolean backKey() {
		return false;
	}

	@Override
	public void onTouchEvent(final TouchEvent event) {
	}

	@Override
	public void onAccelEvent() {
	}

	@Override
	public void setEnableVideo(final boolean enable) {
		mVideoEnabled = enable;
	}

	@Override
	public SurfaceTexture getVideoTexture() {
		return mVideoFrameTexture != null ? mVideoFrameTexture.getSurfaceTexture() : null;
	}

}
