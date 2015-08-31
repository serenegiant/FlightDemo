package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;


public abstract class GLPolygonModel {
	protected final GLGraphics glGraphics;
	protected final Vector mOffset = new Vector();
	protected final Vector mPosition = new Vector();
	protected final Vector mAngle = new Vector();
	protected Texture mTexture;
	protected Vertex mVertex;
	protected boolean mNeedTextureDrawing;
	protected float mScale = 1.0f;
	public float mWidth, mHeight, mDepth;
	
	protected GLPolygonModel(GLGraphics glGraphics) {
		this(glGraphics, Vector.zeroVector, 1f);
	}
	
	protected GLPolygonModel(GLGraphics glGraphics, Vector offset) {
		this(glGraphics, offset, 1.0f);
	}

	protected GLPolygonModel(GLGraphics glGraphics, float scale) {
		this(glGraphics, Vector.zeroVector, scale);
	}
	
	protected GLPolygonModel(GLGraphics glGraphics, Vector offset, float scale) {
		this.glGraphics = glGraphics;
		mOffset.set(offset);
		mPosition.set(0, 0, 0);
		mScale = scale;
		mWidth = mHeight =  mDepth = 1f;
	}

	public Vertex getVertex() {
		return mVertex;
	}

	public void setVertex(Vertex vertex) {
		mVertex = vertex;
	}

	public Texture getTexture() {
		return mTexture;
	}

	public void setTexture(Texture texture) {
		mNeedTextureDrawing = (texture != null);
		mTexture = texture;
	}

	public void setTextureDrawing(boolean needTextureDrawing) {
		mNeedTextureDrawing = needTextureDrawing;
	}
	
	public boolean needTextureDrawing() {
		return mNeedTextureDrawing && (mTexture != null);
	}
		
	public void draw() {
		final GL10 gl = glGraphics.getGL();
		final boolean hasTexture = needTextureDrawing();
		// 準備
		gl.glPushMatrix();
		if (hasTexture) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
			mTexture.bind();
		}	
		gl.glEnable(GL10.GL_DEPTH_TEST);
		mVertex.bind();	

//--------------------------------------------------------------------------------
		// 原点をオフセット
		gl.glTranslatef(
			mPosition.x + mOffset.x,
			mPosition.y + mOffset.y,
			mPosition.z + mOffset.z);
		// 回転
		if (mAngle.x != 0)
			gl.glRotatef(mAngle.x, 1, 0, 0);
		if (mAngle.y != 0)
			gl.glRotatef(mAngle.y, 0, 1, 0);
		if (mAngle.z != 0)
			gl.glRotatef(mAngle.z, 0, 0, 1);
		// サイズ変更
		gl.glScalef(mScale, mScale, mScale);
		// 描画
		mVertex.draw(GL10.GL_TRIANGLES);		// vertex, indexの数はVertexクラスに任せる
//--------------------------------------------------------------------------------

		// 後始末
		gl.glDisable(GL10.GL_DEPTH_TEST);
		if (hasTexture) {
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		
		mVertex.unbind();
		gl.glPopMatrix();
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
	
	public void setPosition(Vector pos) {
		mPosition.set(pos);
	}

	public void setPosition(float x, float y, float z) {
		mPosition.set(x, y, z);
	}

	public Vector getPosition() {
		return mPosition;
	}

	public void rotate(Vector angle) {
		mAngle.set(angle);
	}

	public void rotate(float x, float y, float z) {
		mAngle.set(x, y, z);
	}

	public void rotateX(float angle) {
		mAngle.x = angle;
	}

	public void rotateY(float angle) {
		mAngle.y = angle;
	}

	public void rotateZ(float angle) {
		mAngle.z = angle;
	}
	
	public void setScale(float scale) {
		mScale = scale;
	}
	
	public float getScale() {
		return mScale;
	}
}
