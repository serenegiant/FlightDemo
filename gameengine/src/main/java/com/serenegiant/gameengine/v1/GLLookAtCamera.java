package com.serenegiant.gameengine.v1;

import android.opengl.GLU;
import android.util.Log;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

public class GLLookAtCamera {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLLookAtCamera";

	private final Vector position;
	private final Vector up;
	private final Vector lookAt;
	private final float fieldOfView;
	private final float aspectRatio;
	private final float near;
	private final float far;
	
	public GLLookAtCamera(float fieldOfView, float aspectRatio, float near, float far) {
		if (DEBUG) Log.v(TAG, String.format("GLLookAtCamera:fieldOfView=%f,aspectRatio=%f,near=%f,far=%f", fieldOfView, aspectRatio, near, far));
		this.fieldOfView = fieldOfView;
		this.aspectRatio = aspectRatio;
		this.near = near;
		this.far = far;
		
		position = new Vector();
		up = new Vector(0, 1, 0);
		lookAt = new Vector(0, 0, -1);
	}
	
	public Vector getPosition() {
		return position;
	}

	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
	}

	public void setPosition(Vector pos) {
		position.set(pos);
	}

	public Vector getUp() {
		return up;
	}
	
	public Vector getLookAt() {
		return lookAt;
	}

	public void setLookAt(float x, float y, float z) {
		lookAt.set(x, y, z);
	}

	public void setLookAt(Vector pos) {
		lookAt.set(pos);
	}


	public void setMatrix(GL10 gl) {
//		if (DEBUG) Log.v(TAG, "setMatrix:" + gl);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		if (DEBUG) GLHelper.checkGlError(gl, "GLLookAtCamera#glMatrixMode");
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, fieldOfView, aspectRatio, near, far);
		if (DEBUG) GLHelper.checkGlError(gl, "GLLookAtCamera#gluPerspective");
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, position.x, position.y, position.z,
						 lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z);
		if (DEBUG) GLHelper.checkGlError(gl, "GLLookAtCamera#gluLookAt");
	}
	
	private final Vector v = new Vector();
	
	public float getAngle(Vector axis) {
		v.set(lookAt).sub(position);
		return v.getAngle(axis);
	}
	
	public float angleXZ() {
		v.set(lookAt).sub(position);
		v.y = 0;
		return v.angleXZ();
	}
}
