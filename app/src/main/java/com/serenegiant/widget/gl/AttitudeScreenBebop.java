package com.serenegiant.widget.gl;

import android.graphics.SurfaceTexture;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenBebop extends AttitudeScreenBase {
	private static final String TAG = "AttitudeScreenBebop";

	private DynamicTexture mVideoFrameTexture;

	public AttitudeScreenBebop(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
		frontLeftRotorModel.resume();
		frontRightRotorModel.resume(false);	// テクスチャを共有しているのでテクスチャのリロードは不要
		rearLeftRotorModel.resume();
		rearRightRotorModel.resume(false);	// テクスチャを共有しているのでテクスチャのリロードは不要
	}

	@Override
	public void pause() {
		droneModel.pause();
		frontLeftRotorModel.pause();
		frontRightRotorModel.pause();
		rearLeftRotorModel.pause();
		rearRightRotorModel.pause();
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
			droneObj = new DroneObjectBebop(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectBebop(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectBebopRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			mShowGround = false;
			break;
		}
		// 3Dモデルの読み込み
		final FileIO io = mModelView.getFileIO();
		final StaticTexture droneTexture = new StaticTexture(mModelView, "model/bebop_drone_body_tex.png");
		droneModel = loadModel(io, "model/bebop_drone_body.obj");
		droneModel.setTexture(droneTexture);

		// 左前ローター
		final StaticTexture frontTexture = new StaticTexture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontLeftRotorModel = loadModel(io, "model/bebop_drone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(frontTexture);
		// 右前ローター
		frontRightRotorModel = loadModel(io, "model/bebop_drone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(frontTexture);	// テクスチャは左前と共通
		// 左後ローター
		final StaticTexture rearTexture = new StaticTexture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);	// コピーコンストラクタ
		rearLeftRotorModel.setTexture(rearTexture);
		// 右後ローター
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);	// コピーコンストラクタ
		rearRightRotorModel.setTexture(rearTexture);	// テクスチャは左後と共通
	}

	private volatile boolean mVideoEnabled;
	public void setEnableVideo(final boolean enable) {
		mVideoEnabled = enable;
	}

	public SurfaceTexture getVideoTexture() {
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
