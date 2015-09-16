package com.serenegiant.gl;

import javax.microedition.khronos.opengles.GL10;

/**
 * テクスチャを全面に描画するためのクラス</br>
 * テクスチャは予めbindしておくこと</br>
 */
public class TextureDrawer2D {
	private final Vertex vertex;

	private static final short[] index = { 0, 1, 2, 2, 3, 0,};

	public TextureDrawer2D(final GLGraphics glGraphics, final int width, final int height) {
		vertex = new Vertex(Vertex.DIM_2D, glGraphics, 4, 6, false, true, false);
		vertex.setIndexs(index, 0, index.length);
		// vertexの割り当て
		final float[] vertexBuffer = new float[4 * 4];
		vertexBuffer[0] = 0;		// 頂点0:x
		vertexBuffer[1] = height;	// 頂点0:y
		vertexBuffer[2] = 0;		// 頂点0:tex:x	// region.u1;
		vertexBuffer[3] = 0;		// 頂点0:tex:y	// region.v2;

		vertexBuffer[4] = width;	// 頂点1:x
		vertexBuffer[5] = height;	// 頂点1:y
		vertexBuffer[6] = 1;		// 頂点1:tex:x	// region.u2;
		vertexBuffer[7] = 0;		// 頂点1:tex:y	// region.v2;

		vertexBuffer[8] = width;	// 頂点2:x
		vertexBuffer[9] = 0;		// 頂点2:y
		vertexBuffer[10] = 1;		// 頂点2:tex:x	// region.u2;
		vertexBuffer[11] = 1;		// 頂点2:tex:y	// region.v1;

		vertexBuffer[12] = 0;		// 頂点3:x
		vertexBuffer[13] = 0;		// 頂点3:y
		vertexBuffer[14] = 0;		// 頂点3:tex:x	// region.u1;
		vertexBuffer[15] = 1;		// 頂点3:tex:y	// region.v1;
		vertex.setVertex(vertexBuffer, 0, 16);
	}
	
	/**
	 * テクスチャは予めbindしておくこと</br>
	 * 3D空間中に描画する場合には予めtranlatef/rotatef/scalefしておくこと
	 */
	public void draw() {
		vertex.bind();
		vertex.draw(GL10.GL_TRIANGLES, 0, 6);
		vertex.unbind();
	}
}
