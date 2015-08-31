package com.serenegiant.widget.gl;

public class AttitudeScreenBebopRandom extends AttitudeScreenBase {

	public AttitudeScreenBebopRandom(final IModelView modelView) {
		super(modelView);
	}

	@Override
	protected void initModel() {
		// ドローンの3Dモデル
		// 機体
		droneObj = new DroneObjectBebopRandom(0, 0, 0, 1.0f);
		final Texture droneTexture = new Texture(mModelView, "model/bebop_drone_body_tex.png");
		droneModel = new GLLoadableModel(glGraphics);
		droneModel.loadModel(mModelView, "model/bebop_drone_body.obj");
		droneModel.setTexture(droneTexture);
		// 左前ローター
		final Texture frontLeftTexture = new Texture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontLeftRotorModel = new GLLoadableModel(glGraphics);
		frontLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(frontLeftTexture);
		// 右前ローター
		final Texture frontRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontRightRotorModel = new GLLoadableModel(glGraphics);
		frontRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(frontRightTexture);
		// 左後ローター
		final Texture rearLeftTexture = new Texture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearLeftRotorModel = new GLLoadableModel(glGraphics);
		rearLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		rearLeftRotorModel.setTexture(rearLeftTexture);
		// 右後ローター
		final Texture rearRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearRightRotorModel = new GLLoadableModel(glGraphics);
		rearRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		rearRightRotorModel.setTexture(rearRightTexture);
		//
		mShowGround = false;
	}
}
