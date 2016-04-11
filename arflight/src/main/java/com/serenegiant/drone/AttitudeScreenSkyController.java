package com.serenegiant.drone;

import android.graphics.SurfaceTexture;
import android.util.Log;

import com.serenegiant.gameengine1.DynamicTexture;
import com.serenegiant.gameengine1.FileIO;
import com.serenegiant.gameengine1.GLLoadableModel;
import com.serenegiant.gameengine1.IModelView;
import com.serenegiant.gameengine1.StaticTexture;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenSkyController extends AttitudeScreenBase implements IVideoScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBebop2";

	private DynamicTexture mVideoFrameTexture;

	public AttitudeScreenSkyController(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
	}

	@Override
	public void pause() {
		droneModel.pause();
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
		mVideoFrameTexture = new DynamicTexture(mModelView);
		mVideoFrameTexture.setSize(640, 368);
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
		final FileIO io = mModelView.getFileIO();
		StaticTexture droneTexture = null;
		try {
			droneTexture = new StaticTexture(mModelView, "bebop_drone2_body_tex.png");
		} catch (final Exception e) {
			droneTexture = new StaticTexture(mModelView, "model/bebop_drone2_body_tex.png");
		}
		droneModel = loadModel(io, "model/bebop_drone2_body.obj");
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
	protected void drawBackground(final GL10 gl) {
		if (mVideoEnabled && (mVideoFrameTexture != null) && mVideoFrameTexture.isAvailable()) {
//			gl.glPushMatrix();
			mVideoFrameTexture.bind();
//			gl.glMultMatrixf(mVideoFrameTexture.texMatrix(), 0);	// これを入れると表示サイズがおかしい
			mFullScreenDrawer.draw();
			mVideoFrameTexture.unbind();
//			gl.glPopMatrix();
		} else {
			super.drawBackground(gl);
		}
	}

	protected void drawModel(final GL10 gl) {
		moveDrone(gl);

//		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		gl.glColor4f(1.0f, 1.0f, 1.0f, mAlpha);
//--------------------------------------------------------------------------------
		droneModel.draw();
//--------------------------------------------------------------------------------
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glDisable(GL10.GL_BLEND);
	}

}