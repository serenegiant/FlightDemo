package com.serenegiant.arflight.drone;

import android.opengl.GLES10;
import android.util.Log;

import com.serenegiant.gameengine.FileIO;
import com.serenegiant.gameengine.v1.GLAmbientLight;
import com.serenegiant.gameengine.v1.GLCamera2D;
import com.serenegiant.gameengine.v1.GLCubeModel;
import com.serenegiant.gameengine.v1.GLDirectionLight;
import com.serenegiant.gameengine.v1.GLLoadableModel;
import com.serenegiant.gameengine.v1.GLLookAtCamera;
import com.serenegiant.gameengine.v1.GLPointLight;
import com.serenegiant.gameengine.v1.GLScreen;
import com.serenegiant.gameengine.v1.IGLGameView;
import com.serenegiant.gameengine.v1.StaticTexture;
import com.serenegiant.gameengine.v1.TextureDrawer2D;
import com.serenegiant.gameengine.v1.TextureRegion;
import com.serenegiant.gameengine.TouchEvent;
import com.serenegiant.gameengine.v1.Vertex;
import com.serenegiant.glutils.es1.GLHelper;
import com.serenegiant.math.Vector;

import java.io.IOException;

public abstract class AttitudeScreenBase extends GLScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
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
	protected GLLoadableModel guardModel;
	protected GLLoadableModel frontLeftRotorModel;
	protected GLLoadableModel frontRightRotorModel;
	protected GLLoadableModel rearLeftRotorModel;
	protected GLLoadableModel rearRightRotorModel;

	// オブジェクト
	protected DroneObject droneObj;				// 機体本体
	protected final Object mDroneSync = new Object();
	protected volatile boolean mHasGuard;
	// 地面
	private final GLCubeModel plateModel;
	private final StaticTexture plateTexture;
	protected boolean mShowGround = true;
	// 背景
	private StaticTexture backgroundTexture;
	private TextureRegion backgroundRegion;
	protected TextureDrawer2D mFullScreenDrawer;
	protected volatile float mAlpha;

	public AttitudeScreenBase(final IGLGameView modelView, final int ctrl_type) {
		super(modelView);
		if (DEBUG) Log.v(TAG, String.format("コンストラクタ(%d,%d)", getWidth(), getHeight()));
		mCtrlType = ctrl_type;
		// 背景
		backgroundTexture = new StaticTexture(modelView, "background.png");
		backgroundRegion = new TextureRegion(backgroundTexture, 0, 0, getWidth(), getHeight());
		mFullScreenDrawer = new TextureDrawer2D(glGraphics, getWidth(), getHeight());

		// 地面
		// テクスチャは正方形で2の乗数サイズでないとだめ
		plateTexture = new StaticTexture(modelView, "model/ichimatsu_arrow.png");
		plateModel = new GLCubeModel(Vector.zeroVector, 30, 0.01f, 30, 10);
		plateModel.setTexture(plateTexture);

		ambientLight = new GLAmbientLight();

		pointLight = new GLPointLight();
		pointLight.setPosition(0, 10, 0);
		pointLight.setSpecular(0.5f, 0.5f, 0.5f, 1);

		directionLight = new GLDirectionLight();
		directionLight.setDirection(5, 5, 5);
//		material = new GLMaterial();

		// 2Dカメラ
		guiCamera = new GLCamera2D(glGraphics, getWidth(), getHeight());
		// 視線カメラ
		lookAtCamera = new GLLookAtCamera(
			67, getWidth() / (float)getHeight(), 0.01f, 30f);
		lookAtCamera.setPosition(0, 4, -6.4f);

		mAlpha = 1.0f;
		initModel();
	}

	/**
	 * 機体モデル/ハル・車輪を読み込み
	 */
	protected abstract void initModel();

	@Override
	public void update(final float deltaTime) {
//		if (DEBUG) Log.v(TAG_SCREEN, "update");
		// 機体
		droneObj.update(deltaTime);
		// XXX 他のオブジェクトの描画都合でmodel側の回転ベクトル未使用。位置座標のみセット
		droneModel.setPosition(droneObj.position);
//		droneModel.rotate(droneObj.angle);
		// ガード
		if (guardModel != null) {
			guardModel.setPosition(droneObj.mGuardObject.position);
			guardModel.rotate(droneObj.mGuardObject.angle);
		}
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
		// 最後にカメラが常に機体の方向を向くようにする・・・でもいつも原点にいるからセット不要?
		lookAtCamera.setLookAt(droneModel.getPosition());
	}

	protected void drawBackground() {
		// 背景を描画
		backgroundTexture.bind();
		mFullScreenDrawer.draw();
		backgroundTexture.unbind();
	}

	protected void drawModel() {
		moveDrone();

//		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
		GLES10.glEnable(GLES10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		GLES10.glColor4f(1.0f, 1.0f, 1.0f, mAlpha);
//--------------------------------------------------------------------------------
		droneModel.draw();
		if (mHasGuard && (guardModel != null)) {
			guardModel.draw();
		}
		frontLeftRotorModel.draw();
		frontRightRotorModel.draw();
		rearLeftRotorModel.draw();
		rearRightRotorModel.draw();
//--------------------------------------------------------------------------------
		GLES10.glDisable(GLES10.GL_COLOR_MATERIAL);
		GLES10.glDisable(GLES10.GL_BLEND);
	}

	@Override
	public void draw(final float deltaTime) {
//		if (DEBUG) Log.v(TAG_SCREEN, "draw");

		// ここから2Dの描画処理
		guiCamera.setMatrix();
		GLES10.glDisable(GLES10.GL_LIGHTING);				// ライティングを無効化
		GLES10.glDisable(GLES10.GL_CULL_FACE);			// ポリゴンのカリングを無効にする
		GLES10.glDisable(GLES10.GL_DEPTH_TEST);			// デプステストを無効にする
//		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 背景を描画
		drawBackground();

		// ここから3Dの描画処理
		lookAtCamera.setMatrix();
		GLES10.glEnable(GLES10.GL_BLEND);
		GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);	// アルファブレンド
//		GLES10.glBlendFunc(GLES10.GL_ONE, GL10.GL_ZERO);	// 上書き
//		GLES10.glColor4f(1f, 1f, 1f, 0f);
		GLES10.glEnable(GLES10.GL_LIGHTING);		// ライティングを有効化
		GLES10.glEnable(GLES10.GL_DEPTH_TEST);		// デプステストを有効にする
		GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT);	// デプスバッファをクリアする

		// 床を描画
		if (mShowGround) {
//			gl.glColor4f(1f, 1f, 1f, 1f);
//			plateTexture.bind();
			plateModel.draw();
//			plateTexture.unbind();
		}
		GLES10.glEnable(GLES10.GL_LIGHTING);		// ライティングを有効化
		GLES10.glEnable(GLES10.GL_CULL_FACE);       // ポリゴンのカリングを有効にする
		GLES10.glCullFace(GLES10.GL_BACK);			// 裏面を描画しない
		GLES10.glEnable(GLES10.GL_DEPTH_TEST);		// デプステストを有効にする
		ambientLight.enable();						// 環境光を有効にする
		pointLight.enable(GLES10.GL_LIGHT0);		// 点光源を有効にする
		directionLight.enable(GLES10.GL_LIGHT1);	// 平行光源を有効にする
//		material.enable();							// これを入れると影が出なくなる
//		GLES10.glEnable(GLES10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		// モデルを描画
		GLES10.glPushMatrix();						// 現在のマトリックスを保存
		drawModel();
		GLES10.glPopMatrix();						// マトリックスを復元

		// 3D描画処理終了
		ambientLight.disable();
		pointLight.disable();
		directionLight.disable();
		GLES10.glDisable(GLES10.GL_LIGHTING);		// ライティングを無効化
//		material.disable();
//		GLES10.glDisable(GLES10.GL_COLOR_MATERIAL);	// 環境光と拡散光のマテリアル色として頂点色を使うとき
		GLES10.glDisable(GLES10.GL_BLEND);			// ブレンディングを無効
	}

	public void setAlpha(final float alpha) {
		mAlpha = alpha;
	}

	protected void moveDrone() {
		synchronized (mDroneSync) {
			final Vector position = droneObj.position;
			GLES10.glTranslatef(position.x, position.y, position.z);
			final Vector angle = droneObj.angle;
			if (angle.x != 0) {	// pitch
				GLES10.glRotatef(-angle.x, 1, 0, 0);
				if (DEBUG) GLHelper.checkGlError("GLPolygonModel#glRotatef");
			}
			if (angle.y != 0) {	// yaw
				GLES10.glRotatef(angle.y, 0, 1, 0);
				if (DEBUG) GLHelper.checkGlError("GLPolygonModel#glRotatef");
			}
			if (angle.z != 0) {	// roll
				GLES10.glRotatef(angle.z, 0, 0, 1);
				if (DEBUG) GLHelper.checkGlError("GLPolygonModel#glRotatef");
			}
			final Vector offset = droneObj.getOffset();
			GLES10.glTranslatef(-offset.x, -offset.y, -offset.z);
		}
	}

	@Override
	public void resume() {
		if (DEBUG) Log.v(TAG, "resume");
		mAlpha = 1.0f;
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

	public void startEngine() {
		if (DEBUG) Log.v(TAG, "startEngine:");
		droneObj.startEngine();
	}

	public void stopEngine() {
		if (DEBUG) Log.v(TAG, "stopEngine:");
		droneObj.stopEngine();
	}

	public void setRotorSpeed(final float speed) {
		droneObj.setRotorSpeed(speed);
	}

	public void hasGuard(final boolean hasGuard) {
		if (DEBUG) Log.v(TAG, "hasGuard:" + hasGuard);
		mHasGuard = hasGuard;
	}

	public void setAxis(final int axis) {
		if (DEBUG) Log.v(TAG, "setAxis:" + axis);
		if (droneObj instanceof ICalibrationModelObject) {
			((ICalibrationModelObject) droneObj).setAxis(axis);
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
		final GLLoadableModel model = new GLLoadableModel();
		try {
			// プライベートストレージからキャッシュしてあるモデルデータの読み込みを試みる
			model.setVertex(Vertex.load(io.readFile(path)));
		} catch (final Exception e) {
			// キャッチュからの読み込みができなかったのでassetsから読み込む
			model.loadModel((IGLGameView)getView(), file_name);
			try {
				model.getVertex().save(io.writeFile(path));
			} catch (final IOException e2) {
				Log.w(TAG, e2);
			}
		}
		return model;
	}

}
