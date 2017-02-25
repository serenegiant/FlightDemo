package com.serenegiant.arflight.drone;

import android.graphics.SurfaceTexture;
import android.opengl.GLES10;
import android.util.Log;

import com.serenegiant.gameengine.v1.DynamicTexture;
import com.serenegiant.gameengine.FileIO;
import com.serenegiant.gameengine.v1.IGLGameView;
import com.serenegiant.gameengine.v1.StaticTexture;

public class AttitudeScreenSkyController extends AttitudeScreenBase implements IVideoScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenSkyController";

	private DynamicTexture mVideoFrameTexture;

	public AttitudeScreenSkyController(final IGLGameView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
		if (mVideoFrameTexture == null) {
			mVideoFrameTexture = new DynamicTexture();
			mVideoFrameTexture.setSize(640, 368);
		}
	}

	@Override
	public void pause() {
		droneModel.pause();
		if (mVideoFrameTexture != null) {
			mVideoFrameTexture.release();
			mVideoFrameTexture = null;
		}
		super.pause();
	}

	@Override
	public void release() {
		if (mVideoFrameTexture != null) {
			mVideoFrameTexture.release();
			mVideoFrameTexture = null;
		}
		super.release();
	}

	@Override
	protected void initModel() {
		if (mVideoFrameTexture == null) {
			mVideoFrameTexture = new DynamicTexture();
			mVideoFrameTexture.setSize(640, 368);
		}
		// 機体
		switch (mCtrlType) {
		case CTRL_PILOT:
			droneObj = new SkyControllerObject(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			break;
		case CTRL_ATTITUDE:
			droneObj = new SkyControllerObject(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			break;
		case CTRL_CALIBRATION:
			droneObj = new SkyControllerObjectCalibration(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new SkyControllerObjectRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			break;
		}
		mShowGround = false;
		// 3Dモデルの読み込み
		final FileIO io = getView().getFileIO();
		StaticTexture droneTexture = null;
		try {
			droneTexture = new StaticTexture((IGLGameView)getView(), "skycontroller_tex.png");
		} catch (final Exception e) {
			droneTexture = new StaticTexture((IGLGameView)getView(), "model/skycontroller_tex.png");
		}
		droneModel = loadModel(io, "model/skycontroller.obj");
		droneModel.setTexture(droneTexture);
	}

	private volatile boolean mVideoEnabled;

	@Override
	public void setEnableVideo(final boolean enable) {
		if (DEBUG) Log.v(TAG, "setEnableVideo:" + enable);
		mVideoEnabled = enable;
	}

	@Override
	public SurfaceTexture getVideoTexture() {
		if (DEBUG) Log.v(TAG, "getVideoTexture:" + mVideoFrameTexture);
		return mVideoFrameTexture != null ? mVideoFrameTexture.getSurfaceTexture() : null;
	}

	@Override
	protected void drawBackground() {
		if (mVideoEnabled && (mVideoFrameTexture != null) && mVideoFrameTexture.isAvailable()) {
//			gl.glPushMatrix();
			mVideoFrameTexture.bind();
//			gl.glMultMatrixf(mVideoFrameTexture.texMatrix(), 0);	// これを入れると表示サイズがおかしい
			mFullScreenDrawer.draw();
			mVideoFrameTexture.unbind();
//			gl.glPopMatrix();
		} else {
			super.drawBackground();
		}
	}

	protected void drawModel() {
		moveDrone();

//		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
		GLES10.glEnable(GLES10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		GLES10.glColor4f(1.0f, 1.0f, 1.0f, mAlpha);
//--------------------------------------------------------------------------------
		droneModel.draw();
//--------------------------------------------------------------------------------
		GLES10.glDisable(GLES10.GL_COLOR_MATERIAL);
		GLES10.glDisable(GLES10.GL_BLEND);
	}

}