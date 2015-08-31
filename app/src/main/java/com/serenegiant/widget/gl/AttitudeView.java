package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

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

		private final Vector modelOffset = new Vector(0, 4f, 0);
		private final Texture droneTexture;
		private final GLLoadableModel droneModel;
		private final DroneObject droneObj;

		private final GLCubeModel plateModel;
		private final Texture plateTexture;

		public AttitudeScreen(final IModelView modelView) {
			super(modelView);
			if (DEBUG) Log.v(TAG_SCREEN, "コンストラクタ");

			// ドローンの3Dモデル
			droneObj = new DroneObject(0f, 0f, 0f, 0.3f);
			droneTexture = new Texture(modelView, "model/myrocket.png");
			droneModel = new GLLoadableModel(glGraphics, modelOffset, 2.0f);
			droneModel.loadModel(modelView, "model/myrocket.obj");
			droneModel.setTexture(droneTexture);
/*			droneTexture = new Texture(modelView, "model/bebop_drone_body_tex.png");
			droneModel = new GLLoadableModel(glGraphics, modelOffset, 2.0f);
			droneModel.loadModel(modelView, "model/bebop_drone_body.obj");
			droneModel.setTexture(droneTexture); */
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
			directionLight.setDirection(5, 10, 5);

			material = new GLMaterial();

			// 2Dカメラ
			guiCamera = new GLCamera2D(glGraphics, 800, 480);
			// 視線カメラ
			lookAtCamera = new GLLookAtCamera(
				67, glGraphics.getViewWidth() / (float)glGraphics.getViewHeight(), 0.1f, 25f);
			lookAtCamera.setPosition(-20, 10, 0);

		}

		@Override
		public void update(final float deltaTime) {
//			if (DEBUG) Log.v(TAG_SCREEN, "update");
			// 機体データの更新処理
			// FIXME 未実装
			droneObj.update(deltaTime);
			droneModel.setPosition(droneObj.position);
			droneModel.rotate(droneObj.angle);
			lookAtCamera.setLookAt(droneModel.getPosition());	// 常に機体の方向を向くようにする
		}

		@Override
		public void draw(final float deltaTime) {
//			if (DEBUG) Log.v(TAG_SCREEN, "draw");
			// 画面表示更新
			final GL10 gl = glGraphics.getGL();

			gl.glClearColor(1, 1, 1, 1);
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
			gl.glColor4f(1f, 1f, 1f, 0);
			plateModel.draw();
//			gl.glDisable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき

//			gl.glEnable(GL10.GL_COLOR_MATERIAL);    // 環境光と拡散光のマテリアル色として頂点色を使うとき
//			material.enable(gl);
			// モデルを描画
			material.enable(gl);
			droneModel.draw();
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
