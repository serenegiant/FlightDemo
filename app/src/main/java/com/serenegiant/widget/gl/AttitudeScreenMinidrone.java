package com.serenegiant.widget.gl;

import android.graphics.SurfaceTexture;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenMinidrone extends AttitudeScreenBase {

	public AttitudeScreenMinidrone(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
		frontLeftRotorModel.resume(false);
		frontRightRotorModel.resume(false);
		rearLeftRotorModel.resume(false);
		rearRightRotorModel.resume(false);
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

/*	@Override
	public void release() {
		super.release();
	} */

	@Override
	protected void initModel() {
		// 機体
		switch (mCtrlType) {
		case CTRL_PILOT:
			droneObj = new DroneObjectMinidrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 16, -18f);
			mShowGround = false;
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectMinidrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 16, -18f);
			mShowGround = false;
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectMinidroneRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-9, 8, -9);
			mShowGround = false;
			break;
		}
		// 3Dモデルの読み込み
		final FileIO io = mModelView.getFileIO();
		final StaticTexture droneTexture = new StaticTexture(mModelView, "model/minidrone_tex_red.png");
		droneModel = loadModel(io, "model/minidrone_body.obj");
		droneModel.setTexture(droneTexture);
		// 左前ローター
		frontLeftRotorModel = loadModel(io, "model/minidrone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 右前ローター
		frontRightRotorModel = loadModel(io, "model/minidrone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 左後ローター
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);	// コピーコンストラクタ
		rearLeftRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 右後ローター
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);	// コピーコンストラクタ
		rearRightRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
	}

/*	@Override
	protected void drawBackground(final GL10 gl) {
		super.drawBackground(gl);
	} */
}
