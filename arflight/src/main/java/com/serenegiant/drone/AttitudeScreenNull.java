package com.serenegiant.drone;

import android.util.Log;

import com.serenegiant.gameengine1.FileIO;
import com.serenegiant.gameengine1.GLAmbientLight;
import com.serenegiant.gameengine1.GLCamera2D;
import com.serenegiant.gameengine1.GLCubeModel;
import com.serenegiant.gameengine1.GLDirectionLight;
import com.serenegiant.gameengine1.GLLoadableModel;
import com.serenegiant.gameengine1.GLLookAtCamera;
import com.serenegiant.gameengine1.GLPointLight;
import com.serenegiant.gameengine1.GLScreen;
import com.serenegiant.gameengine1.IModelView;
import com.serenegiant.gameengine1.StaticTexture;
import com.serenegiant.gameengine1.TextureDrawer2D;
import com.serenegiant.gameengine1.TextureRegion;
import com.serenegiant.gameengine1.TouchEvent;
import com.serenegiant.gameengine1.Vertex;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenNull extends GLScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBase";

	protected final GLLookAtCamera lookAtCamera;
	protected final GLCamera2D guiCamera;

	// 背景
	private StaticTexture backgroundTexture;
	private TextureRegion backgroundRegion;
	protected TextureDrawer2D mFullScreenDrawer;
	protected volatile float mAlpha;

	public AttitudeScreenNull(final IModelView modelView) {
		super(modelView);
		if (DEBUG) Log.v(TAG, String.format("コンストラクタ(%d,%d)", screenWidth, screenHeight));
		// 背景
		backgroundTexture = new StaticTexture(modelView, "background.png");
		backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, screenWidth, screenHeight);
		mFullScreenDrawer = new TextureDrawer2D(glGraphics, screenWidth, screenHeight);

		// 2Dカメラ
		guiCamera = new GLCamera2D(glGraphics, screenWidth, screenHeight);
		// 視線カメラ
		lookAtCamera = new GLLookAtCamera(
			67, screenWidth / (float)screenHeight, 0.01f, 30f);
		lookAtCamera.setPosition(0, 4, -6.4f);

		mAlpha = 1.0f;
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
		backgroundTexture.bind();
		mFullScreenDrawer.draw();
		backgroundTexture.unbind();

	}

	public void setAlpha(final float alpha) {
		mAlpha = alpha;
	}

	@Override
	public void resume() {
		if (DEBUG) Log.v(TAG, "resume");
		mAlpha = 1.0f;
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

}
