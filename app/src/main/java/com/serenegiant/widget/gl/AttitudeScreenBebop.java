package com.serenegiant.widget.gl;

import android.graphics.SurfaceTexture;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeScreenBebop extends AttitudeScreenBase {

	private DynamicTexture mVideoFrameTexture;

	public AttitudeScreenBebop(final IModelView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
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
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectBebop(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectBebopRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);
			break;
		}
		final StaticTexture droneTexture = new StaticTexture(mModelView, "model/bebop_drone_body_tex.png");
		droneModel = new GLLoadableModel(glGraphics);
		droneModel.loadModel(mModelView, "model/bebop_drone_body.obj");
		droneModel.setTexture(droneTexture);
		// 左前ローター
		final StaticTexture frontTexture = new StaticTexture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontLeftRotorModel = new GLLoadableModel(glGraphics);
		frontLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(frontTexture);
		// 右前ローター
//		final Texture frontRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_front_tex.png");
		frontRightRotorModel = new GLLoadableModel(glGraphics);
		frontRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(frontTexture);
		// 左後ローター
		final StaticTexture rearTexture = new StaticTexture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);
//		rearLeftRotorModel = new GLLoadableModel(glGraphics);
//		rearLeftRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_ccw.obj");
		rearLeftRotorModel.setTexture(rearTexture);
		// 右後ローター
//		final Texture rearRightTexture = new Texture(mModelView, "model/bebop_drone_rotor_rear_tex.png");
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);
//		rearRightRotorModel = new GLLoadableModel(glGraphics);
//		rearRightRotorModel.loadModel(mModelView, "model/bebop_drone_rotor_cw.obj");
		rearRightRotorModel.setTexture(rearTexture);
		//
		mShowGround = false;
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
		if (mVideoEnabled && (mVideoFrameTexture != null)) {
			mVideoFrameTexture.bind();
			mFullScreenDrawer.draw();
		} else {
			super.drawBackground(gl);
		}
	}
}
