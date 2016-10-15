package com.serenegiant.arflight.drone;

import android.graphics.SurfaceTexture;
import android.util.Log;

import com.serenegiant.gameengine.v1.DynamicTexture;
import com.serenegiant.gameengine.v1.FileIO;
import com.serenegiant.gameengine.v1.GLLoadableModel;
import com.serenegiant.gameengine.v1.IModelView;
import com.serenegiant.gameengine.v1.StaticTexture;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenBebop2 extends AttitudeScreenBase implements IVideoScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBebop2";

	private DynamicTexture mVideoFrameTexture;

	public AttitudeScreenBebop2(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
		if (guardModel != null) {
			guardModel.resume();
		}
		frontLeftRotorModel.resume();
		frontRightRotorModel.resume(false);	// テクスチャを共有しているのでテクスチャのリロードは不要
		rearLeftRotorModel.resume();
		rearRightRotorModel.resume(false);	// テクスチャを共有しているのでテクスチャのリロードは不要
		if (mVideoFrameTexture == null) {
			mVideoFrameTexture = new DynamicTexture(mModelView);
			mVideoFrameTexture.setSize(640, 368);
		}
	}

	@Override
	public void pause() {
		droneModel.pause();
		if (guardModel != null) {
			guardModel.pause();
		}
		frontLeftRotorModel.pause();
		frontRightRotorModel.pause();
		rearLeftRotorModel.pause();
		rearRightRotorModel.pause();
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
			mVideoFrameTexture = new DynamicTexture(mModelView);
			mVideoFrameTexture.setSize(640, 368);
		}
		// 機体
		switch (mCtrlType) {
		case CTRL_PILOT:
			droneObj = new DroneObjectBebop2(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectBebop2(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_CALIBRATION:
			droneObj = new DroneObjectBebop2Calibration(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			mShowGround = false;
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectBebop2Random(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			mShowGround = false;
			break;
		}
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
//		// ガード(ハル) Bebop2は無し
//		StaticTexture guardTexture = null;
//		try {
//			guardTexture = new StaticTexture(mModelView, "bebop_drone_bumper_tex.png");
//		} catch (final Exception e) {
//			guardTexture = new StaticTexture(mModelView, "model/bebop_drone_bumper_tex2.png");
//		}
//		guardModel = loadModel(io, "model/bebop_drone_bumper.obj");
//		guardModel.setTexture(guardTexture);

		// 左前ローター
		StaticTexture frontTexture = null;
		try {
			frontTexture = new StaticTexture(mModelView, "bebop_drone2_rotor_front_tex.png");
		} catch (final Exception e) {
			frontTexture = new StaticTexture(mModelView, "model/bebop_drone2_rotor_front_tex.png");
		}
		frontLeftRotorModel = loadModel(io, "model/bebop_drone2_rotor_ccw.obj");
		frontLeftRotorModel.setTexture(frontTexture);
		// 右前ローター
		frontRightRotorModel = loadModel(io, "model/bebop_drone2_rotor_cw.obj");
		frontRightRotorModel.setTexture(frontTexture);	// テクスチャは左前と共通
		// 左後ローター
		final StaticTexture rearTexture = new StaticTexture(mModelView, "model/bebop_drone2_rotor_rear_tex.png");
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);	// コピーコンストラクタ
		rearLeftRotorModel.setTexture(rearTexture);
		// 右後ローター
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);	// コピーコンストラクタ
		rearRightRotorModel.setTexture(rearTexture);	// テクスチャは左後と共通
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
}
