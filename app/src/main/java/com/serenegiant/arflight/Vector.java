package com.serenegiant.arflight;

import android.opengl.Matrix;

import java.util.Locale;

public class Vector {
	private float x;
	private float y;
	private float z;

	public static final float TO_RADIAN = (float)((1 / 180.0) * Math.PI);
	public static final float TO_DEGREE = (float)(1 / Math.PI * 180.0);

	public static final Vector zeroVector = new Vector();
	public static final Vector normVector = new Vector(1,1,1);

	private static final float[] matrix = new float[16];
	private static final float[] inVec = new float[4];
	private static final float[] outVec = new float[4];

	public Vector() {
	}

	public Vector(final float x, final float y) {
		this(x, y, 0.0f);
	}

	public Vector(final Vector v) {
		this(v.x, v.y, v.z);
	}

	public Vector(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vector vector(final float x, final float y, final float z) {
		return new Vector(x, y, z);
	}

	public static Vector vector(final Vector v) {
		return new Vector(v.x, v.y, v.z);
	}

	public Vector copy() {
		return new Vector(x, y, z);
	}

	public Vector clear(final float scalar) {
		x = y = z = scalar;
		return this;
	}

	public Vector set(final float x, final float y) {
		return set(x, y, 0.0f);
	}

	public Vector set(final Vector v) {
		return set(v.x, v.y, v.z);
	}

	public Vector set(final Vector v, final float a) {
		return set(v.x, v.y, v.z, a);
	}

	public Vector set(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vector set(final float x, final float y, final float z, final float a) {
		this.x = x * a;
		this.y = y * a;
		this.z = z * a;
		return this;
	}

	public Vector add(final float x, final float y) {
		return add(x, y, 0.0f);
	}

	public Vector add(final float x, final float y, final float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vector add(final float x, final float y, final float z, final float a) {
		this.x += x * a;
		this.y += y * a;
		this.z += z * a;
		return this;
	}

	public Vector add(final Vector v) {
		return add(v.x, v.y, v.z);
	}

	public Vector add(final Vector v, final float a) {
		return add(v.x, v.y, v.z, a);
	}

	public Vector sub(final float x, final float y) {
		return add(-x, -y, 0.0f);
	}

	public Vector sub(final Vector v) {
		return add(-v.x, -v.y, -v.z);
	}

	public Vector sub(final Vector v, final float a) {
		return add(-v.x, -v.y, -v.z, a);
	}

	public Vector sub(final float x, final float y, final float z) {
		return add(-x, -y, -z);
	}

	public Vector sub(final float x, final float y, final float z, final float a) {
		return add(-x, -y, -z, a);
	}

	// ベクトルにスカラ値をかけ算
	public Vector mult(final float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}

	// ベクトルをスカラ値で割り算
	public Vector div(final float scalar) {
		return mult(1 / scalar);
	}

	// ベクトルをスカラ値で剰余計算
	public Vector mod(final float scalar) {
		this.x %= scalar;
		this.y %= scalar;
		this.z %= scalar;
		return this;
	}

	/**
	 * x,y,zがそれぞれ角度(degree)であるとみなしてラジアンに変換する
	 * @return
	 */
	public Vector toRadian() {
		return mult(TO_RADIAN);
	}

	/**
	 * x,y,zがそれぞれラジアンであるとみなして角度に変換する
	 * @return
	 */
	public Vector toDegree() {
		return mult(TO_DEGREE);
	}

	public Vector limit(final float scalar) {
		while (x >= scalar) x -= scalar;
		while (x < -scalar) x += scalar;
		while (y >= scalar) y -= scalar;
		while (y < -scalar) y += scalar;
		while (z >= scalar) z -= scalar;
		while (z < -scalar) z += scalar;
		return this;
	}

	// ベクトルの長さを取得
	public float len() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	// ベクトルの長さの２乗を取得
	public float lenSquared() {
		return x * x + y * y + z * z;
	}

	// ベクトルを正規化(長さを1にする)
	public Vector normalize() {
		final float len = len();
		if (len != 0) {
			this.x /= len;
			this.y /= len;
			this.z /= len;
		}
		return this;
	}

	// ベクトルの内積を取得
	// 標準化ベクトルv2を含む直線にベクトルv1を真っ直ぐ下ろした（正射影した）時の長さ
	public float dotProduct(final Vector v) {
		return x * v.x + y * v.y + z * v.z;
	}

	// ベクトルの内積を取得
	public float dotProduct(final float x, final float y, final float z) {
		return this.x * x + this.y * y + this.z * z;
	}

	// ベクトルの外積を計算(2D)
	// v1×v2= x1*y2-x2*y1 = |v1||v2|sin(θ)
	public float crossProduct2(final Vector v) {
		return x * v.y - v.x * y;
	}

	// ベクトルの外積を計算(3D)
	// v1×v2= (y1*z2-z1*y2, z1*x2-x1*z2, x1*y2-y1*x2) = (x3, y3, z3) =  v3
	// 2つのベクトルに垂直な方向を向いた大きさが|v1||v2|sinθのベクトル
	public Vector crossProduct(final Vector v) {
		return crossProduct(this, this, v);
	}

	// ベクトルの外積を計算(3D)
	public static Vector crossProduct(final Vector v3, final Vector v1, final Vector v2) {
		final float x3 = v1.y * v2.z - v1.z * v2.y;
		final float y3 = v1.z * v2.x - v1.x * v2.z;
		final float z3 = v1.x * v2.y - v1.y * v2.x;
		v3.x = x3; v3.y = y3; v3.z = z3;
		return v3;
	}

	// XY平面上でベクトルとX軸の角度を取得
	public float angleXY() {
		float angle = (float)(Math.atan2(y, x) * TO_DEGREE);
		if (angle < 0)
			angle += 360;
		return angle;
	}

	// XZ平面上でベクトルとX軸の角度を取得
	public float angleXZ() {
		float angle = (float)(Math.atan2(z, x) * TO_DEGREE);
		if (angle < 0)
			angle += 360;
		return angle;
	}

	// YZ平面上でベクトルとY軸の角度を取得
	public float angleYZ() {
		float angle = (float)(Math.atan2(z, y) * TO_DEGREE);
		if (angle < 0)
			angle += 360;
		return angle;
	}

	// ベクトル間の角度を取得
	// ベクトル１ Za=(X1,Y1,Z1)、ベクトル２ Zb=(X2,Y2,Z2)、求める角φとすると、
	// cos φ ＝ Za・Zb / (|Za| |Zb|)
	//  =(X1X2+Y1Y2+Z1Z2) / √{(X1^2 + Y1^2 + Z1^2)(X2^2 + Y2^2 + Z2^2)}
	// 上式のアークコサイン(cos^-1)を取ればOK。
	public float getAngle(final Vector v) {
		final double cos = dotProduct(v) / Math.sqrt(lenSquared() * v.lenSquared());
		return (float)(Math.acos(cos) * TO_DEGREE);
	}

	// Z軸周りに(XY平面上で)ベクトルを指定した角度[度]回転させる
	public Vector rotateXY(final float angle) {
		final double rad = angle * TO_RADIAN;
		final double cos = Math.cos(rad);
		final double sin = Math.sin(rad);

		final double newX = this.x * cos - this.y * sin;
		final double newY = this.x * sin + this.y * cos;

		this.x = (float)newX;
		this.y = (float)newY;

		return this;
	}

	// Y軸周りに(XZ平面上で)ベクトルを指定した角度[度]回転させる
	public Vector rotateXZ(final double angle) {
		final double rad = angle * TO_RADIAN;
		final double cos = Math.cos(rad);
		final double sin = Math.sin(rad);

		final double newX = this.x * cos - this.z * sin;
		final double newZ = this.x * sin + this.z * cos;

		this.x = (float)newX;
		this.z = (float)newZ;

		return this;
	}

	// X軸周りに(YZ平面上で)ベクトルを指定した角度[度]回転させる
	public Vector rotateYZ(final double angle) {
		final double rad = angle * TO_RADIAN;
		final double cos = Math.cos(rad);
		final double sin = Math.sin(rad);

		final double newY = this.y * cos - this.z * sin;
		final double newZ = this.y * sin + this.z * cos;

		this.y = (float)newY;
		this.z = (float)newZ;

		return this;
	}

	// ベクトルを回転
	// x軸：(1,0,0), y軸(0,1,0), z軸(0,0,1)
	public Vector rotate(final float angle, final float axisX, final float axisY, final float axisZ) {
		inVec[0] = (float)x;
		inVec[1] = (float)y;
		inVec[2] = (float)z;
		inVec[3] = 1;
		Matrix.setIdentityM(matrix, 0);
		Matrix.rotateM(matrix, 0, angle, axisX, axisY, axisZ);
		Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);
		x = outVec[0];
		y = outVec[1];
		z = outVec[2];
		return this;
	}

	public Vector rotate(final float angleX, final float angleY, final float angleZ) {
		return rotate(this, angleX, angleY, angleZ);
	}

	public static Vector rotate(final Vector v, final float angleX, final float angleY, final float angleZ) {
		inVec[0] = (float)v.x;
		inVec[1] = (float)v.y;
		inVec[2] = (float)v.z;
		inVec[3] = 1;
		Matrix.setIdentityM(matrix, 0);
		if (angleX != 0)
			Matrix.rotateM(matrix, 0, angleX, 1f, 0f, 0f);
		if (angleY != 0)
			Matrix.rotateM(matrix, 0, angleY, 0f, 1f, 0f);
		if (angleZ != 0)
			Matrix.rotateM(matrix, 0, angleZ, 0f, 0f, 1f);
		Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);
		v.x = outVec[0];
		v.y = outVec[1];
		v.z = outVec[2];
		return v;
	}

	public static Vector[] rotate(final Vector[] v, final float angleX, final float angleY, final float angleZ) {
		Matrix.setIdentityM(matrix, 0);
		if (angleX != 0)
			Matrix.rotateM(matrix, 0, angleX, 1f, 0f, 0f);
		if (angleY != 0)
			Matrix.rotateM(matrix, 0, angleY, 0f, 1f, 0f);
		if (angleZ != 0)
			Matrix.rotateM(matrix, 0, angleZ, 0f, 0f, 1f);
		final int n = (v != null) ? v.length : 0;
		for (int i = 0; i < n; i++) {
			if (v[i] == null) continue;
			inVec[0] = (float)v[i].x;
			inVec[1] = (float)v[i].y;
			inVec[2] = (float)v[i].z;
			inVec[3] = 1;
			Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);
			v[i].x = outVec[0];
			v[i].y = outVec[1];
			v[i].z = outVec[2];
		}
		return v;
	}

	public Vector rotate(final Vector angle, final float a) {
		rotate((float)angle.x * a, (float)angle.y * a, (float)angle.z * a);
		return this;
	}

	public Vector rotate(final Vector angle) {
		return rotate((float)angle.x, (float)angle.y, (float)angle.z);
	}

	public Vector rotate_inv(final float angleX, final float angleY, final float angleZ) {
		inVec[0] = (float)x;
		inVec[1] = (float)y;
		inVec[2] = (float)z;
		inVec[3] = 1;
		Matrix.setIdentityM(matrix, 0);
		if (angleZ != 0)
			Matrix.rotateM(matrix, 0, angleZ, 0f, 0f, 1f);
		if (angleY != 0)
			Matrix.rotateM(matrix, 0, angleY, 0f, 1f, 0f);
		if (angleX != 0)
			Matrix.rotateM(matrix, 0, angleX, 1f, 0f, 0f);
		Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);
		x = outVec[0];
		y = outVec[1];
		z = outVec[2];
		return this;
	}

	public Vector rotate_inv(final Vector angle, final float a) {
		rotate_inv((float)angle.x * a, (float)angle.y * a, (float)angle.z * a);
		return this;
	}

	public Vector rotate_inv(final Vector angle) {
		rotate_inv(angle, -1f);
		return this;
	}

	public float[] getQuat() {
		final float[] q = new float[4];
		q[0] = x;
		q[1] = y;
		q[2] = z;
		q[3] = 1;
		return q;
	}

	public Vector setQuat(final float[] q) {
		x = q[0];
		y = q[1];
		z = q[2];
		return this;
	}

	// ベクトル間の距離を取得する
	public float distance(final Vector v) {
		return distance(v.x, v.y, v.z);
	}

	public float distance(final float x, final float y) {
		return distance(x, y, this.z);
	}

	public float distance(final float x, final float y, final float z) {
		return (float)Math.sqrt(distSquared(x, y, z));
	}

	public float distSquared(final Vector v) {
		return distSquared(v.x, v.y, v.z);
	}

	public float distSquared(final float x, final float y) {
		return distSquared(x, y, this.z);
	}

	public float distSquared(final float x, final float y, final float z) {
		final float dx = this.x - x;
		final float dy = this.y - y;
		final float dz = this.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	// ベクトルの各成分を交換
	public Vector swap(final Vector v) {
		float w = x; x = v.x; v.x = w;
		w = y; y = v.y; v.y = w;
		w = z; z = v.z; v.z = w;
		return this;
	}

	// x成分とy成分を交換
	public Vector swapXY() {
		final float w = x; x = y; y = w;
		return this;
	}

	// 傾き
	public float slope(final Vector v) {
		if (v.x != x)
			return (v.y - y) / (v.x - x);
		else
			return (v.y - y >= 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
	}

	public float slope() {
		if (x != 0)
			return y / x;
		else
			return (y >= 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "(%f,%f,%f)", x, y, z);
	}

/*	public String toString(String fmt) {
		return String.format(Locale.US, fmt, x, y, z);
	} */
}