package com.serenegiant.widget.gl;

import android.util.Log;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

public abstract class AttitudeScreenBase extends GLScreen {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBase";

	public static final int CTRL_RANDOM = 0;	// ランダム回転
	public static final int CTRL_PILOT = 1;		// 操縦に追随
	public static final int CTRL_ATTITUDE = 2;	// 機体姿勢に追随

	protected final GLAmbientLight ambientLight;
	protected final GLPointLight pointLight;
	protected final GLDirectionLight directionLight;
	protected final GLMaterial material;

	protected final GLLookAtCamera lookAtCamera;
	protected final GLCamera2D guiCamera;

	protected final int mCtrlType;
	// 3Dモデルデータ
	protected GLLoadableModel droneModel;
	protected GLLoadableModel frontLeftRotorModel;
	protected GLLoadableModel frontRightRotorModel;
	protected GLLoadableModel rearLeftRotorModel;
	protected GLLoadableModel rearRightRotorModel;
	// オブジェクト
	protected DroneObject droneObj;				// 機体本体
	// 地面
	private final GLCubeModel plateModel;
	private final Texture plateTexture;
	protected boolean mShowGround = true;
	// 背景
	private Texture backgroundTexture;
	private TextureRegion backgroundRegion;
	private TextureDrawer2D mDrawer;
	private final int screenCenterX, screenCenterY;

	public AttitudeScreenBase(final IModelView modelView, final int ctrl_type) {
		super(modelView);
		if (DEBUG) Log.v(TAG, String.format("コンストラクタ(%d,%d)", screenWidth, screenHeight));
		mCtrlType = ctrl_type;
		// 背景
		backgroundTexture = new Texture(modelView, "background.png");
		backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, screenWidth, screenHeight);
		screenCenterX = screenWidth / 2;
		screenCenterY = screenHeight / 2;
		mDrawer = new TextureDrawer2D(glGraphics);
		mDrawer.draw(screenWidth, screenHeight, backgroundRegion);

		// 地面
		// テクスチャは正方形で2の乗数サイズでないとだめ
		plateTexture = new Texture(modelView, "model/ichimatsu_arrow.png");
		plateModel = new GLCubeModel(glGraphics, Vector.zeroVector, 30, 0.01f, 30, 10);
		plateModel.setTexture(plateTexture);

		ambientLight = new GLAmbientLight();

		pointLight = new GLPointLight();
		pointLight.setPosition(0, 10, 0);
//		pointLight.setSpecular(0.5f, 0.5f, 0.5f, 1);

		directionLight = new GLDirectionLight();
		directionLight.setDirection(5, 5, 5);

		material = new GLMaterial();

		// 2Dカメラ
		guiCamera = new GLCamera2D(glGraphics, screenWidth, screenHeight);
		// 視線カメラ
		lookAtCamera = new GLLookAtCamera(
			67, glGraphics.getViewWidth() / (float)glGraphics.getViewHeight(), 0.1f, 25f);
		lookAtCamera.setPosition(-4.5f, 4, -4.5f);

		initModel();
	}

	/**
	 * 機体モデルを読み込み
	 */
	protected abstract void initModel();

	@Override
	public void update(final float deltaTime) {
//		if (DEBUG) Log.v(TAG_SCREEN, "update");
		// 機体
		droneObj.update(deltaTime);
		droneModel.setPosition(droneObj.position);
//		droneModel.rotate(droneObj.angle);
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
//		if (DEBUG) Log.v(TAG_SCREEN, "draw");
		// 画面表示更新
		final GL10 gl = glGraphics.getGL();

		// ここから2Dの描画処理
		guiCamera.setMatrix();
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_LIGHTING);			// ライティングを無効化
		gl.glDisable(GL10.GL_CULL_FACE);		// ポリゴンのカリングを無効にする
//		gl.glColor4f(1f, 1f, 1f, 1f);
//		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 背景を描画
		backgroundTexture.bind();
		mDrawer.draw();

		// ここから3Dの描画処理
		lookAtCamera.setMatrix(gl);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);	// アルファブレンド
//		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);	// 上書き
		gl.glColor4f(1f, 1f, 1f, 0f);
//		gl.glEnable(GL10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		gl.glEnable(GL10.GL_CULL_FACE);			// ポリゴンのカリングを有効にする
		gl.glCullFace(GL10.GL_BACK);			// 裏面を描画しない
		gl.glEnable(GL10.GL_LIGHTING);			// ライティングを有効化
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		ambientLight.enable(gl);
		pointLight.enable(gl, GL10.GL_LIGHT0);
		directionLight.enable(gl, GL10.GL_LIGHT1);
		material.enable(gl);


		// 床を描画
		if (mShowGround) {
			gl.glColor4f(1f, 1f, 1f, 1f);
			plateModel.draw();
		}
//		material.enable(gl);
		// モデルを描画
		material.enable(gl);
		gl.glPushMatrix();
		{
			moveDrone(gl);
			droneModel.draw();
			frontLeftRotorModel.draw();
			frontRightRotorModel.draw();
			rearLeftRotorModel.draw();
			rearRightRotorModel.draw();
		}
		gl.glPopMatrix();

		// 3D描画処理終了
		pointLight.disable(gl);
		directionLight.disable(gl);

		gl.glDisable(GL10.GL_BLEND);
	}

	private void moveDrone(final GL10 gl) {
		final Vector position = droneObj.getOffset();
		gl.glTranslatef(position.x, position.y, position.z);
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
		final Vector offset = droneObj.getOffset();
		gl.glTranslatef(-offset.x, -offset.y, -offset.z);
	}

	@Override
	public void resume() {
		if (DEBUG) Log.v(TAG, "resume");
		plateTexture.reload();
		backgroundTexture.reload();
		plateModel.resume();
		droneModel.resume();
	}

	@Override
	public void pause() {
		if (DEBUG) Log.v(TAG, "pause");
		plateModel.pause();
		droneModel.pause();
	}

	@Override
	public void dispose() {
		if (DEBUG) Log.v(TAG, "dispose");
		backgroundTexture.dispose();
		plateTexture.dispose();
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