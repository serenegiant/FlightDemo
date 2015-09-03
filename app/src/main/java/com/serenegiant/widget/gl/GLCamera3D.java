package com.serenegiant.widget.gl;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * 3D描画用のカメラ</br>
 * カメラは原点位置固定で、z軸の負方向を向いているが、
 * 見かけ上position位置に存在し、xz平面上でyaw角回転、
 * zyまたはxy平面上でpitch角回転しているように見えるように
 * ワールド座標系全体を回転・移動させる
 */
public class GLCamera3D {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLCamera3D";

	private final Vector position = new Vector();
	private float mYaw;
	private float mPitch;
	private float mFieldOfView;
	private float mAspectRatio;
	private float mNear;
	private float mFar;
	
	/**
	 * コンストラクタ</br>
	 * カメラ位置を原点から移動/回転させていなければ、カメラはz軸の負方向を向いているので、
	 * z軸の-near〜-far間が表示される
	 * @param fieldOfView 視野角(人の視野角は約67度)
	 * @param aspectRatio 縦横描画比,通常は実画面のviewWidth/viewHeightを設定
	 * @param near 視錐台の手前位置(カメラから前方クリップ面までの距離, 正値)
	 * @param far 視錐台の奥位置(カメラから後方クリップ面までの距離, 正値)
	 */
	public GLCamera3D(final float fieldOfView, final float aspectRatio, final float near, final float far) {
		mFieldOfView = fieldOfView;
		mAspectRatio = aspectRatio;
		mNear = near;
		mFar = far;
	}
	
	/**
	 * カメラ位置を取得
	 * @return
	 */
	public Vector getPosition() {
		return position;
	}
	
	/**
	 * カメラ位置を設定(Vector)
	 * @param pos
	 */
	public void setPosition(final Vector pos) {
		position.set(pos);
	}
	
	/**
	 * カメラ位置を設定(float x 3)
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setPosition(final float x, final float y, final float z) {
		position.set(x, y, z);
	}
	
	/**
	 * カメラのyaw回転角を取得(y軸回転)
	 * @return
	 */
	public float getYaw() {
		return mYaw;
	}
	
	/**
	 * カメラのyaw回転角を設定(y軸回転)</br>
	 * 内部で0〜360度に制限
	 * @param yaw
	 */
	public void setYaw(float yaw) {
		if (yaw < 0)
			yaw += 360;
		if (yaw >= 360)
			yaw -= 360;
		mYaw = yaw;
	}
	
	/**
	 * カメラのpitch回転角を取得(xまたはz軸回転、上下に振り上げる/振り下げる)
	 * @return
	 */
	public float getPitch() {
		return mPitch;
	}

	/**
	 * カメラのpitch回転角を設定(xまたはz軸回転、上下に振り上げる/振り下げる)</br>
	 * 内部で±90度内に制限
	 * @param pitch
	 */
	public void setPitch(float pitch) {
		if (pitch < -90)
			pitch = -90;
		if (pitch > 90)
			pitch = 90;
		mPitch = pitch;
	}
	
	/**
	 * カメラの回転角を設定
	 * @param yaw
	 * @param pitch
	 */
	public void setAngles(float yaw, float pitch) {
		if (yaw < 0)
			yaw += 360;
		if (yaw >= 360)
			yaw -= 360;
		if (pitch < -90)
			pitch = -90;
		if (pitch > 90)
			pitch = 90;
		mYaw = yaw;
		mPitch = pitch;
	}
	
	/**
	 * カメラを回転
	 * @param yawInc
	 * @param pitchInc
	 */
	public void rotate(float yawInc, float pitchInc) {
		mYaw += yawInc;
		if (mYaw < 0)
			mYaw += 360;
		if (mYaw >= 360)
			mYaw -= 360;
		mPitch += pitchInc;
		if (mPitch < -90)
			mPitch = -90;
		if (mPitch > 90)
			mPitch = 90;		
	}
	
	/**
	 * 投影行列を設定</br>
	 * 3D描画前に呼び出す
	 * @param gl
	 */
	public void setMatrix(final GL10 gl) {
		if (DEBUG) Log.v(TAG, "setMatrix:" + gl);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#glMatrixMode");
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, mFieldOfView, mAspectRatio, mNear, mFar);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#gluPerspective");
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#glMatrixMode");
		gl.glLoadIdentity();
		gl.glRotatef(-mPitch, 1, 0, 0);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#glRotatef");
		gl.glRotatef(-mYaw, 0, 1, 0);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#glRotatef");
		gl.glTranslatef(-position.x, -position.y, -position.z);
		if (DEBUG) GLHelper.checkGlError(gl, "GLCamera3D#glTranslatef");
	}
	
	private final float[] matrix = new float[16];
	private final float[] inVec = {0, 0, -1, 1};
	private final float[] outVec = new float[4];
	private final Vector direction = new Vector();
	
	/**
	 * カメラの視線方向ベクトルを取得
	 * @return Vector このベクトルを変更しても視線方向は変わらない
	 */
	public Vector getDirection() {
		Matrix.setIdentityM(matrix, 0);
		Matrix.rotateM(matrix, 0, mYaw, 0, 1, 0);
		Matrix.rotateM(matrix,  0, mPitch, 1, 0, 0);
		Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);
		direction.set(outVec[0], outVec[1], outVec[2]);
		return direction;
	}
}
