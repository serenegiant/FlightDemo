package com.serenegiant.widget.gl;

import android.graphics.SurfaceTexture;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenMinidrone extends AttitudeScreenBase {

	public AttitudeScreenMinidrone(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
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
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectMinidrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 16, -18f);
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectMinidroneRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-18, 16, -18);
			break;
		}
		final StaticTexture droneTexture = new StaticTexture(mModelView, "model/minidrone_red.png");
		droneModel = new GLLoadableModel(glGraphics);
		droneModel.loadModel(mModelView, "model/minidrone_body.obj");
		droneModel.setTexture(droneTexture);
		// 左前ローター
//		final StaticTexture frontTexture = new StaticTexture(mModelView, "model/minidrone_red.png");
		frontLeftRotorModel = new GLLoadableModel(glGraphics);
		frontLeftRotorModel.loadModel(mModelView, "model/minidrone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(droneTexture);
		// 右前ローター
//		final Texture frontRightTexture = new Texture(mModelView, "model/minidrone_red.png");
		frontRightRotorModel = new GLLoadableModel(glGraphics);
		frontRightRotorModel.loadModel(mModelView, "model/minidrone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(droneTexture);
		// 左後ローター
//		final StaticTexture rearTexture = new StaticTexture(mModelView, "model/minidrone_red.png");
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);
//		rearLeftRotorModel = new GLLoadableModel(glGraphics);
//		rearLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		rearLeftRotorModel.setTexture(droneTexture);
		// 右後ローター
//		final Texture rearRightTexture = new Texture(mModelView, "model/minidrone_red.png");
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);
//		rearRightRotorModel = new GLLoadableModel(glGraphics);
//		rearRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		rearRightRotorModel.setTexture(droneTexture);
		//
		mShowGround = false;
	}

/*	@Override
	protected void drawBackground(final GL10 gl) {
		super.drawBackground(gl);
	} */
}
