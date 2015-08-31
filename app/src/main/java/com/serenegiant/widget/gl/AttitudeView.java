package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

public class AttitudeView extends GLModelView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeView";

	public AttitudeView(Context context) {
		this(context, null);
	}

	public AttitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	protected Screen getScreen() {
		if (DEBUG) Log.v(TAG, "getScreen");
		return new AttitudeScreen(this);
	}

	private static class AttitudeScreen extends GLScreen {
		private static final String TAG_SCREEN = "AttitudeScreen";

		private final GLAmbientLight ambientLight;
		private final GLPointLight pointLight;
		private final GLDirectionLight directionLight;
		private final GLMaterial material;

		private final GLLookAtCamera lookAtCamera;
		private final GLCamera2D guiCamera;

		// 3Dモデルデータ
		private final GLLoadableModel droneModel;
		private final GLLoadableModel frontLeftRotorModel;
		private final GLLoadableModel frontRightRotorModel;
		private final GLLoadableModel rearLeftRotorModel;
		private final GLLoadableModel rearRightRotorModel;
		// オブジェクト
		private final DroneObject droneObj;				// 機体本体

		private final GLCubeModel plateModel;
		private final Texture plateTexture;

		public AttitudeScreen(final IModelView modelView) {
			super(modelView);
			if (DEBUG) Log.v(TAG_SCREEN, "コンストラクタ");

			// ドローンの3Dモデル
			// 機体
			droneObj = new DroneObjectBebopRandom(0, 0, 0, 1.0f);
			final Texture droneTexture = new Texture(modelView, "model/bebop_drone_body_tex.png");
			droneModel = new GLLoadableModel(glGraphics);
			droneModel.loadModel(modelView, "model/bebop_drone_body.obj");
			droneModel.setTexture(droneTexture);
			// 左前ローター
			final Texture frontLeftTexture = new Texture(modelView, "model/bebop_drone_rotor_front_tex.png");
			frontLeftRotorModel = new GLLoadableModel(glGraphics);
			frontLeftRotorModel.loadModel(modelView, "model/bebop_drone_rotor_cw.obj");
			frontLeftRotorModel.setTexture(frontLeftTexture);
			// 右前ローター
			final Texture frontRightTexture = new Texture(modelView, "model/bebop_drone_rotor_front_tex.png");
			frontRightRotorModel = new GLLoadableModel(glGraphics);
			frontRightRotorModel.loadModel(modelView, "model/bebop_drone_rotor_ccw.obj");
			frontRightRotorModel.setTexture(frontRightTexture);
			// 左後ローター
			final Texture rearLeftTexture = new Texture(modelView, "model/bebop_drone_rotor_rear_tex.png");
			rearLeftRotorModel = new GLLoadableModel(glGraphics);
			rearLeftRotorModel.loadModel(modelView, "model/bebop_drone_rotor_ccw.obj");
			rearLeftRotorModel.setTexture(rearLeftTexture);
			// 右後ローター
			final Texture rearRightTexture = new Texture(modelView, "model/bebop_drone_rotor_rear_tex.png");
			rearRightRotorModel = new GLLoadableModel(glGraphics);
			rearRightRotorModel.loadModel(modelView, "model/bebop_drone_rotor_cw.obj");
			rearRightRotorModel.setTexture(rearRightTexture);
			// 地面
			// テクスチャは正方形で2の乗数サイズでないとだめ
			plateTexture = new Texture(modelView, "model/ichimatsu_arrow.png");
			plateModel = new GLCubeModel(glGraphics, Vector.zeroVector, 30, 0.01f, 30, 10);
			plateModel.setTexture(plateTexture);

			ambientLight = new GLAmbientLight();

			pointLight = new GLPointLight();
			pointLight.setPosition(0, 10, 0);
//			pointLight.setSpecular(0.5f, 0.5f, 0.5f, 1);

			directionLight = new GLDirectionLight();
			directionLight.setDirection(5, 5, 5);

			material = new GLMaterial();

			// 2Dカメラ
			guiCamera = new GLCamera2D(glGraphics, 800, 480);
			// 視線カメラ
			lookAtCamera = new GLLookAtCamera(
				67, glGraphics.getViewWidth() / (float)glGraphics.getViewHeight(), 0.1f, 25f);
			lookAtCamera.setPosition(-4.5f, 4, -4.5f);

		}

		@Override
		public void update(final float deltaTime) {
//			if (DEBUG) Log.v(TAG_SCREEN, "update");
			// 機体
			droneObj.update(deltaTime);
			droneModel.setPosition(droneObj.position);
//			droneModel.rotate(droneObj.angle);
			// 左前ローター
			frontLeftRotorModel.setPosition(droneObj.mFrontLeftRotorObj.position);
			frontLeftRotorModel.rotate(droneObj.mFrontLeftRotorObj.angle);
			// 右前ローター
			frontRightRotorModel.setPosition(droneObj.mFrontRightRotorObj.position);
			frontRightRotorModel.rotate(droneObj.mFrontRightRotorObj.angle);
			// 左後ローター
			rearLeftRotorModel.setPosition(droneObj.mRearLeftRotorObj.position);
			rearLeftRotorModel.rotate(droneObj.mRearLeftRotorObj.angle);
			// 右後ローター
			rearRightRotorModel.setPosition(droneObj.mRearRightRotorObj.position);
			rearRightRotorModel.rotate(droneObj.mRearRightRotorObj.angle);
			// 視点カメラ
			lookAtCamera.setLookAt(droneModel.getPosition());	// 常に機体の方向を向くようにする
		}

		@Override
		public void draw(final float deltaTime) {
//			if (DEBUG) Log.v(TAG_SCREEN, "draw");
			// 画面表示更新
			final GL10 gl = glGraphics.getGL();

			gl.glClearColor(1, 1, 1, 0.1f);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glColor4f(1f, 1f, 1f, 1f);

			// カメラの準備
			lookAtCamera.setMatrix(gl);

			// ここから3Dの描画処理
//			gl.glEnable(GL10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
			gl.glEnable(GL10.GL_CULL_FACE);			// ポリゴンのカリングを有効にする
			gl.glCullFace(GL10.GL_BACK);			// 裏面を描画しない
			gl.glEnable(GL10.GL_LIGHTING);			// ライティングを有効化
//			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			ambientLight.enable(gl);
			pointLight.enable(gl, GL10.GL_LIGHT0);
			directionLight.enable(gl, GL10.GL_LIGHT1);
			material.enable(gl);


//			gl.glEnable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき
			// 床を描画
			gl.glColor4f(1f, 1f, 1f, 1f);
			plateModel.draw();
//			gl.glDisable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき

//			gl.glEnable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき
//			material.enable(gl);
			// モデルを描画
			material.enable(gl);
			gl.glPushMatrix();
			{
				rotateDrone(gl);
				gl.glTranslatef(0, -1.7f, 0);
				droneModel.draw();
				frontLeftRotorModel.draw();
				frontRightRotorModel.draw();
				rearLeftRotorModel.draw();
				rearRightRotorModel.draw();
			}
			gl.glPopMatrix();
//			gl.glDisable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき

			// 3D描画処理終了
			pointLight.disable(gl);
			directionLight.disable(gl);

			// ここから2Dの描画処理
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//			guiCamera.setViewportAndMatrix();
			// この後に2DのGL描画を行う

			gl.glDisable(GL10.GL_BLEND);
		}

		private void rotateDrone(final GL10 gl) {
			if (droneObj.angle.x != 0) {
				gl.glRotatef(droneObj.angle.x, 1, 0, 0);
				GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
			if (droneObj.angle.y != 0) {
				gl.glRotatef(droneObj.angle.y, 0, 1, 0);
				GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
			if (droneObj.angle.z != 0) {
				gl.glRotatef(droneObj.angle.z, 0, 0, 1);
				GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
		}

		@Override
		public void resume() {
			if (DEBUG) Log.v(TAG_SCREEN, "resume");
			plateTexture.reload();
			plateModel.resume();
			droneModel.resume();
		}

		@Override
		public void pause() {
			if (DEBUG) Log.v(TAG_SCREEN, "pause");
			plateModel.pause();
			droneModel.pause();
		}

		@Override
		public void dispose() {
			if (DEBUG) Log.v(TAG_SCREEN, "dispose");
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
}
