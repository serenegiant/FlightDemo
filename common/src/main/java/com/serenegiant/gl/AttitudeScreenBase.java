package com.serenegiant.gl;

import android.util.Log;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

public abstract class AttitudeScreenBase extends GLScreen {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeScreenBase";

	public static final int CTRL_RANDOM = 0;		// ランダム回転
	public static final int CTRL_PILOT = 1;			// 操縦に追随
	public static final int CTRL_ATTITUDE = 2;		// 機体姿勢に追随
	public static final int CTRL_CALIBRATION = 3;	// キャリブレーション用
	public static final int CTRL_NUM = 4;

	protected final GLAmbientLight ambientLight;
	protected final GLPointLight pointLight;
	protected final GLDirectionLight directionLight;
//	protected final GLMaterial material;

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
	protected final Object mDroneSync = new Object();
	// 地面
	private final GLCubeModel plateModel;
	private final StaticTexture plateTexture;
	protected boolean mShowGround = true;
	// 背景
	private StaticTexture backgroundTexture;
	private TextureRegion backgroundRegion;
	protected TextureDrawer2D mFullScreenDrawer;

	public AttitudeScreenBase(final IModelView modelView, final int ctrl_type) {
		super(modelView);
		if (DEBUG) Log.v(TAG, String.format("コンストラクタ(%d,%d)", screenWidth, screenHeight));
		mCtrlType = ctrl_type;
		// 背景
		backgroundTexture = new StaticTexture(modelView, "background.png");
		backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, screenWidth, screenHeight);
		mFullScreenDrawer = new TextureDrawer2D(glGraphics, screenWidth, screenHeight);

		// 地面
		// テクスチャは正方形で2の乗数サイズでないとだめ
		plateTexture = new StaticTexture(modelView, "model/ichimatsu_arrow.png");
		plateModel = new GLCubeModel(glGraphics, Vector.zeroVector, 30, 0.01f, 30, 10);
		plateModel.setTexture(plateTexture);

		ambientLight = new GLAmbientLight();

		pointLight = new GLPointLight();
		pointLight.setPosition(0, 10, 0);
		pointLight.setSpecular(0.5f, 0.5f, 0.5f, 1);

		directionLight = new GLDirectionLight();
		directionLight.setDirection(5, 5, 5);
//		material = new GLMaterial();

		// 2Dカメラ
		guiCamera = new GLCamera2D(glGraphics, screenWidth, screenHeight);
		// 視線カメラ
		lookAtCamera = new GLLookAtCamera(
			67, screenWidth / (float)screenHeight, 0.01f, 30f);
		lookAtCamera.setPosition(0, 4, -6.4f);

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
		// カメラを移動
//		updateCamera(deltaTime);
		// 最後に常に機体の方向を向くようにする・・・でもいつも原点にいるからセット不要?
		lookAtCamera.setLookAt(droneModel.getPosition());
	}

	protected void drawBackground(final GL10 gl) {
		// 背景を描画
//		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		backgroundTexture.bind();
		mFullScreenDrawer.draw();
		backgroundTexture.unbind();
	}

	protected void drawModel(final GL10 gl) {
		moveDrone(gl);
		droneModel.draw();
		frontLeftRotorModel.draw();
		frontRightRotorModel.draw();
		rearLeftRotorModel.draw();
		rearRightRotorModel.draw();
	}

	@Override
	public void draw(final float deltaTime) {
//		if (DEBUG) Log.v(TAG_SCREEN, "draw");
		// 画面表示更新
		final GL10 gl = glGraphics.getGL();

		// ここから2Dの描画処理
		guiCamera.setMatrix();
		gl.glDisable(GL10.GL_LIGHTING);				// ライティングを無効化
		gl.glDisable(GL10.GL_CULL_FACE);			// ポリゴンのカリングを無効にする
		gl.glDisable(GL10.GL_DEPTH_TEST);			// デプステストを無効にする
		drawBackground(gl);

		// ここから3Dの描画処理
		lookAtCamera.setMatrix(gl);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);	// アルファブレンド
//		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ZERO);	// 上書き
//		gl.glColor4f(1f, 1f, 1f, 0f);
		gl.glEnable(GL10.GL_LIGHTING);				// ライティングを有効化
		gl.glEnable(GL10.GL_DEPTH_TEST);			// デプステストを有効にする
		gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);		// デプスバッファをクリアする

		// 床を描画
		if (mShowGround) {
//			gl.glColor4f(1f, 1f, 1f, 1f);
//			plateTexture.bind();
			plateModel.draw();
//			plateTexture.unbind();
		}
		gl.glEnable(GL10.GL_LIGHTING);				// ライティングを有効化
		gl.glEnable(GL10.GL_CULL_FACE);         	// ポリゴンのカリングを有効にする
		gl.glCullFace(GL10.GL_BACK);				// 裏面を描画しない
		gl.glEnable(GL10.GL_DEPTH_TEST);			// デプステストを有効にする
		ambientLight.enable(gl);					// 環境光を有効にする
		pointLight.enable(gl, GL10.GL_LIGHT0);		// 点光源を有効にする
		directionLight.enable(gl, GL10.GL_LIGHT1);	// 平行光源を有効にする
//		material.enable(gl);						// これを入れると影が出なくなる
//		gl.glEnable(GL10.GL_COLOR_MATERIAL);		// 環境光と拡散光のマテリアル色として頂点色を使うとき
		// モデルを描画
		gl.glPushMatrix();							// 現在のマトリックスを保存
		drawModel(gl);
		gl.glPopMatrix();							// マトリックスを復元

		// 3D描画処理終了
		ambientLight.disable(gl);
		pointLight.disable(gl);
		directionLight.disable(gl);
		gl.glDisable(GL10.GL_LIGHTING);				// ライティングを無効化
//		material.disable(gl);
//		gl.glDisable(GL10.GL_COLOR_MATERIAL);		// 環境光と拡散光のマテリアル色として頂点色を使うとき
		gl.glDisable(GL10.GL_BLEND);				// ブレンディングを無効
	}

	private void moveDrone(final GL10 gl) {
		synchronized (mDroneSync) {
			final Vector position = droneObj.position;
			gl.glTranslatef(position.x, position.y, position.z);
			final Vector angle = droneObj.angle;
			if (angle.x != 0) {	// pitch
				gl.glRotatef(-angle.x, 1, 0, 0);
				if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
			if (angle.y != 0) {	// yaw
				gl.glRotatef(angle.y, 0, 1, 0);
				if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
			if (angle.z != 0) {	// roll
				gl.glRotatef(angle.z, 0, 0, 1);
				if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
			}
			final Vector offset = droneObj.getOffset();
			gl.glTranslatef(-offset.x, -offset.y, -offset.z);
		}
	}

	@Override
	public void resume() {
		if (DEBUG) Log.v(TAG, "resume");
		plateModel.resume();
		backgroundTexture.reload();
	}

	@Override
	public void pause() {
		if (DEBUG) Log.v(TAG, "pause");
		plateModel.pause();
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release");
		backgroundTexture.release();
		plateTexture.release();
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

	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[度], 0は水平
	 * @param pitch 前後の傾き(機種の上げ下げ)[度], 0は水平
	 * @param yaw 水平回転[度], 0は端末の向きと一致
	 * @param gaz 高さ[m]
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz) {
		synchronized (mDroneSync) {
			final Vector angle = droneObj.angle;
			// roll
			angle.z = roll;
			// pitch
			angle.x = pitch;
			// yaw
			angle.y = yaw;
			// FIXME 高度は未実装
		}
	}

	// 今のカメラの初期位置(-4.5f,4.0f,-4.5f)だと距離が約18.55
	private static final float DISTANCE_MAX = 29f;
	private static final float DISTANCE_MIN = 14f;
	private static final float DISTANCE_AVE = (DISTANCE_MAX + DISTANCE_MIN) / 2.0f;
	private static final float DISTANCE_MAX2 = DISTANCE_MAX * DISTANCE_MAX;
	private static final float DISTANCE_MIN2 = DISTANCE_MIN * DISTANCE_MIN;
	private static final float DISTANCE_AVE2 = DISTANCE_AVE * DISTANCE_AVE;
	private static final float SPEED_FACTOR = 0.1f;
	private static final float ACCEL_FACTOR = 10.0f;

	private final Vector prevAngle = new Vector();		// 前回の機体姿勢
	private final Vector deltaAngle = new Vector();		// 機体姿勢の変化量
	protected final Vector mCameraSpeed = new Vector();	// カメラの移動速度
	protected final Vector mCameraAccel = new Vector();	// カメラ移動の加速度

	private int cnt = 0;
	/**
	 * 視点カメラの位置を計算
	 * @param deltaTime
	 */
	private void updateCamera(final float deltaTime) {
		if (mCtrlType != CTRL_RANDOM) {
			deltaAngle.set(prevAngle);
			synchronized (mDroneSync) {
				// FIXME 画面タッチで操縦している時に指を離すして0に戻るときにも逆方向の変化量がでるので反対向きに動いてしまう
				deltaAngle.sub(droneObj.angle).toRadian();	// 前回と今回の機体姿勢の変化量[ラジアン]
				prevAngle.set(droneObj.angle);	// 今回の機体姿勢を保存[度]
			}
			// 機体の位置は常に(0,0,0)なのでカメラ位置=カメラ位置と機体位置の差ベクトル
			final Vector cameraPos = lookAtCamera.getPosition();
			// 機体とカメラの距離
			final float distance = cameraPos.len();
			if (distance > DISTANCE_MAX) {
				// 距離が離れすぎないようにする
				cameraPos.mult(DISTANCE_MAX / distance);
				mCameraAccel.mult(0);
			} else if (distance < DISTANCE_MIN) {
				// 距離が近づき過ぎないようにする
				cameraPos.mult(DISTANCE_MIN / distance);
				mCameraAccel.mult(0);
			} else {
				// 機体のroll/pitch角に応じて機体の加速度が変化するので加速度に換算
				deltaAngle.x = (float) Math.sin(deltaAngle.x) * ACCEL_FACTOR;    // pitch = [-ACCEL_FACTOR, +ACCEL_FACTOR]
				deltaAngle.z = (float) Math.sin(deltaAngle.z) * ACCEL_FACTOR;    // roll = [-ACCEL_FACTOR, +ACCEL_FACTOR]
				deltaAngle.y = 0;
				// 機体の加速度のx/zそれぞれの軸方向の成分を計算
				deltaAngle.rotateXZ(prevAngle.y).mult(-1.0f);    // 機体の加速度(逆方向に動かす)
				mCameraAccel.add(deltaAngle.x, 0, deltaAngle.z, 10);            // カメラの加速度へ加算
			}
			// 機体に追随させるために機体方向にも加速度を加算
			mCameraAccel.add(0.1f, 0f, 0.1f, deltaTime * (DISTANCE_AVE - distance) * ACCEL_FACTOR);
			if (DEBUG && (cnt % 30 == 0)) Log.v(TAG, "mCameraAccel=" + mCameraAccel);
			// カメラの位置を計算・・・現在位置に加速度✕時間を加算
			cameraPos.add(mCameraAccel.z, 0, mCameraAccel.x, deltaTime * SPEED_FACTOR);
			// 加速度を減衰させる
			mCameraAccel.mult(1 - deltaTime * 0.01f);	// 今は1秒で0.05=5%減衰
			if (mCameraAccel.lenSquared() < 1) {
				mCameraAccel.clear(0);
			}
		}
		cnt++;
	}

	/**
	 * 可能ならアプリのプライベートストレージからバイナリ形式の3Dモデルを読み込む
	 * 読み込めなければassetsから読み込んでプライベートストレージへバイナリ形式で書き出す
	 * @param io
	 * @param file_name
	 * @return
	 */
	protected GLLoadableModel loadModel(final FileIO io, final String file_name) {
		final String path = file_name.replace("/", "$");
		final GLLoadableModel model = new GLLoadableModel(glGraphics);
		try {
			model.setVertex(Vertex.load(glGraphics, io.readFile(path)));
		} catch (final Exception e) {
			Log.w(TAG, e);
			model.loadModel(mModelView, file_name);
			try {
				model.getVertex().save(io.writeFile(path));
			} catch (final IOException e2) {
				Log.w(TAG, e2);
			}
		}
		return model;
	}

}
