package com.serenegiant.widget.gl;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;


public abstract class GLPolygonModel {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLPolygonModel";

	protected final GLGraphics glGraphics;
	protected final Vector mOffset = new Vector();
	protected final Vector mPosition = new Vector();
	protected final Vector mAngle = new Vector();
	protected StaticTexture mTexture;
	protected Vertex mVertex;
	protected boolean mNeedTextureDrawing;
	protected float mScale = 1.0f;
	public float mWidth, mHeight, mDepth;
	
	protected GLPolygonModel(final GLGraphics glGraphics) {
		this(glGraphics, Vector.zeroVector, 1f);
	}
	
	protected GLPolygonModel(final GLGraphics glGraphics, final Vector offset) {
		this(glGraphics, offset, 1.0f);
	}

	protected GLPolygonModel(final GLGraphics glGraphics, final float scale) {
		this(glGraphics, Vector.zeroVector, scale);
	}
	
	protected GLPolygonModel(final GLGraphics glGraphics, final Vector offset, final float scale) {
		this.glGraphics = glGraphics;
		mOffset.set(offset);
		mPosition.set(0, 0, 0);
		mScale = scale;
		mWidth = mHeight =  mDepth = 1f;
	}

	/**
	 * コピーコンストラクタ
	 * @param other
	 */
	protected GLPolygonModel(final GLPolygonModel other) {
		glGraphics = other.glGraphics;	// シャローコピー
		mOffset.set(other.mOffset);
		mPosition.set(other.mPosition);
		mAngle.set(other.mAngle);
		mTexture = other.mTexture;	// シャローーコピー
		mVertex = new Vertex(other.mVertex);
		mNeedTextureDrawing = other.mNeedTextureDrawing;
		mScale = other.mScale;
		mWidth = other.mWidth;
		mHeight = other.mHeight;
		mDepth = other.mDepth;
	}

	public Vertex getVertex() {
		return mVertex;
	}

	public void setVertex(final Vertex vertex) {
		mVertex = vertex;
	}

	public StaticTexture getTexture() {
		return mTexture;
	}

	public void setTexture(final StaticTexture texture) {
		mNeedTextureDrawing = (texture != null);
		mTexture = texture;
	}

	public void setTextureDrawing(final boolean needTextureDrawing) {
		mNeedTextureDrawing = needTextureDrawing;
	}
	
	public boolean needTextureDrawing() {
		return mNeedTextureDrawing && (mTexture != null);
	}
		
	public void draw() {
		final GL10 gl = glGraphics.getGL();
//		if (DEBUG) Log.v(TAG, "draw:gl=" + gl);
		final boolean hasTexture = needTextureDrawing();
		// 準備
		gl.glPushMatrix();
		if (hasTexture) {
//			gl.glEnable(GL10.GL_TEXTURE_2D);
			if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glEnable");
			mTexture.bind();
		}	
		gl.glEnable(GL10.GL_DEPTH_TEST);
		if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glEnable");
		mVertex.bind();

//--------------------------------------------------------------------------------
		// 原点をオフセット
		gl.glTranslatef(
			mPosition.x/* + mOffset.x*/,
			mPosition.y/* + mOffset.y*/,
			mPosition.z/* + mOffset.z*/);
		if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glTranslatef");
		// 回転
		if (mAngle.x != 0) {
			gl.glRotatef(mAngle.x, 1, 0, 0);
			if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		if (mAngle.y != 0) {
			gl.glRotatef(mAngle.y, 0, 1, 0);
			if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		if (mAngle.z != 0) {
			gl.glRotatef(mAngle.z, 0, 0, 1);
			if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		// サイズ変更
		gl.glScalef(mScale, mScale, mScale);
		// モデルの原点をオフセット
		gl.glTranslatef(mOffset.x, mOffset.y, mOffset.z);	// XXX
		if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glScalef");
		// 描画
		mVertex.draw(GL10.GL_TRIANGLES);		// vertex, indexの数はVertexクラスに任せる
//--------------------------------------------------------------------------------

		// 後始末
		gl.glDisable(GL10.GL_DEPTH_TEST);
		if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glDisable");
		if (hasTexture) {
			mTexture.unbind();
//			gl.glDisable(GL10.GL_TEXTURE_2D);
			if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glDisable");
		}
		
		mVertex.unbind();
		gl.glPopMatrix();
		if (DEBUG) GLHelper.checkGlError(gl, "GLPolygonModel#glPopMatrix");
	}
	
	public void resume() {
		resume(true);
	}

	public void resume(final boolean needReloadTexture) {
		if (mVertex != null)
			mVertex.resume();
		if (needReloadTexture && (mTexture != null))
			mTexture.reload();	// 2013/05/16
	}
	
	public void pause() {
		if (mVertex != null)
			mVertex.pause();
	}
	
	public void setPosition(final Vector pos) {
		mPosition.set(pos);
	}

	public void setPosition(final float x, final float y, final float z) {
		mPosition.set(x, y, z);
	}

	public Vector getPosition() {
		return mPosition;
	}

	public void rotate(final Vector angle) {
		mAngle.set(angle);
	}

	public void rotate(final float x, final float y, final float z) {
		mAngle.set(x, y, z);
	}

	public void rotateX(final float angle) {
		mAngle.x = angle;
	}

	public void rotateY(final float angle) {
		mAngle.y = angle;
	}

	public void rotateZ(final float angle) {
		mAngle.z = angle;
	}
	
	public void setScale(final float scale) {
		mScale = scale;
	}
	
	public float getScale() {
		return mScale;
	}
}
