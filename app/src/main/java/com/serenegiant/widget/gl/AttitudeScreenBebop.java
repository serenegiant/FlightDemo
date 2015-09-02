package com.serenegiant.widget.gl;

public class AttitudeScreenBebop extends AttitudeScreenBase {
	public AttitudeScreenBebop(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	protected void initModel() {
		// 機体
		switch (mCtrlType) {
		case CTRL_PILOT:
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectBebop(0, 0, 0, 1.0f);
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectBebopRandom(0, 0, 0, 1.0f);
			break;
		}
		final Texture droneTexture = new Texture(mModelView, "model/bebop_drone_body_tex.png");
		droneModel = new GLLoadableModel(glGraphics);
		droneModel.loadModel(mModelView, "model/bebop_drone_body.obj");
		droneModel.setTexture(droneTexture);
		// 左前ローター
		final Texture frontTexture = new Texture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontLeftRotorModel = new GLLoadableModel(glGraphics);
		frontLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(frontTexture);
		// 右前ローター
//		final Texture frontRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontRightRotorModel = new GLLoadableModel(glGraphics);
		frontRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(frontTexture);
		// 左後ローター
		final Texture rearTexture = new Texture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearLeftRotorModel = new GLLoadableModel(glGraphics);
		rearLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		rearLeftRotorModel.setTexture(rearTexture);
		// 右後ローター
//		final Texture rearRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearRightRotorModel = new GLLoadableModel(glGraphics);
		rearRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		rearRightRotorModel.setTexture(rearTexture);
		//
		mShowGround = false;
	}
}
