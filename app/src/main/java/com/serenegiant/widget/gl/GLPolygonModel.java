package com.serenegiant.widget.gl;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;


public abstract class GLPolygonModel implements Cloneable {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLPolygonModel";

	protected final GLGraphics glGraphics;
	protected final Vector mOffset = new Vector();
	protected final Vector mPosition = new Vector();
	protected final Vector mAngle = new Vector();
	protected Texture mTexture;
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

	public GLPolygonModel clone() throws CloneNotSupportedException {
		final GLPolygonModel result = (GLPolygonModel)super.clone();
		result.mVertex = mVertex.clone();
		result.mOffset.set(mOffset);
		result.mPosition.set(mPosition);
		result.mAngle.set(mAngle);
		return result;
	}

	public Vertex getVertex() {
		return mVertex;
	}

	public void setVertex(final Vertex vertex) {
		mVertex = vertex;
	}

	public Texture getTexture() {
		return mTexture;
	}

	public void setTexture(final Texture texture) {
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
			gl.glEnable(GL10.GL_TEXTURE_2D);
			GLHelper.checkGlError(gl, "GLPolygonModel#glEnable");
			mTexture.bind();
		}	
		gl.glEnable(GL10.GL_DEPTH_TEST);
		GLHelper.checkGlError(gl, "GLPolygonModel#glEnable");
		mVertex.bind();

//--------------------------------------------------------------------------------
		// 原点をオフセット
		gl.glTranslatef(
			mPosition.x/* + mOffset.x*/,
			mPosition.y/* + mOffset.y*/,
			mPosition.z/* + mOffset.z*/);
		GLHelper.checkGlError(gl, "GLPolygonModel#glTranslatef");
		// 回転
		if (mAngle.x != 0) {
			gl.glRotatef(mAngle.x, 1, 0, 0);
			GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		if (mAngle.y != 0) {
			gl.glRotatef(mAngle.y, 0, 1, 0);
			GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		if (mAngle.z != 0) {
			gl.glRotatef(mAngle.z, 0, 0, 1);
			GLHelper.checkGlError(gl, "GLPolygonModel#glRotatef");
		}
		// サイズ変更
		gl.glScalef(mScale, mScale, mScale);
		// モデルの原点をオフセット
		gl.glTranslatef(mOffset.x, mOffset.y, mOffset.z);	// XXX
		GLHelper.checkGlError(gl, "GLPolygonModel#glScalef");
		// 描画
		mVertex.draw(GL10.GL_TRIANGLES);		// vertex, indexの数はVertexクラスに任せる
//--------------------------------------------------------------------------------

		// 後始末
		gl.glDisable(GL10.GL_DEPTH_TEST);
		GLHelper.checkGlError(gl, "GLPolygonModel#glDisable");
		if (hasTexture) {
			gl.glDisable(GL10.GL_TEXTURE_2D);
			GLHelper.checkGlError(gl, "GLPolygonModel#glDisable");
		}
		
		mVertex.unbind();
		gl.glPopMatrix();
		GLHelper.checkGlError(gl, "GLPolygonModel#glPopMatrix");
	}
	
	public void resume() {
		if (mVertex != null)
			mVertex.resume();
		if (mTexture != null)
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