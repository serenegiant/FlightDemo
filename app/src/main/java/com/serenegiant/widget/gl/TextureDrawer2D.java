package com.serenegiant.widget.gl;

import javax.microedition.khronos.opengles.GL10;

/**
 * テクスチャを全面に描画するためのクラス</br>
 * テクスチャは予めbindしておくこと</br>
 */
public class TextureDrawer2D {
	private final float[] vertexBuffer;
	private final Vertex vertex;

	public TextureDrawer2D(final GLGraphics glGraphics) {
		vertexBuffer = new float[4 * 4];
		vertex = new Vertex(Vertex.DIM_2D, glGraphics, 4, 6, false, true, false);
		final short[] index = new short[6];
		index[0] = (short)(0);
		index[1] = (short)(1);
		index[2] = (short)(2);
		index[3] = (short)(2);
		index[4] = (short)(3);
		index[5] = (short)(0);
		vertex.setIndexs(index, 0, index.length);
	}
	
	/**
	 * xy平面上へスプライトを1個描画する。テクスチャは予めbindしておくこと</br>
	 * 3D空間中に描画する場合には予めtranlatef/rotatef/scalefしておくこと
	 * @param width 描画幅
	 * @param height 描画高さ
	 * @param region スプライト定義
	 */
	public void draw(final float width, final float height, final TextureRegion region) {
		// vertexの割り当て
		vertexBuffer[0] = 0;		// 頂点0:x
		vertexBuffer[1] = 0;		// 頂点0:y
		vertexBuffer[2] = 0;		// 頂点0:tex:x	// region.u1;
		vertexBuffer[3] = 0;		// 頂点0:tex:y	// region.v2;

		vertexBuffer[4] = 1;		// 頂点1:x
		vertexBuffer[5] = 0;		// 頂点1:y
		vertexBuffer[6] = 1;		// 頂点1:tex:x	// region.u2;
		vertexBuffer[7] = 0;		// 頂点1:tex:y	// region.v2;

		vertexBuffer[8] = 1;		// 頂点2:x
		vertexBuffer[9] = 1;		// 頂点2:y
		vertexBuffer[10] = 1;		// 頂点2:tex:x	// region.u2;
		vertexBuffer[11] = 1;		// 頂点2:tex:y	// region.v1;

		vertexBuffer[12] = 0;		// 頂点3:x
		vertexBuffer[13] = 1;		// 頂点3:y
		vertexBuffer[14] = 0;		// 頂点3:tex:x	// region.u1;
		vertexBuffer[15] = 1;		// 頂点3:tex:y	// region.v1;
		vertex.setVertex(vertexBuffer, 0, 16);
		// 描画処理
		draw();
	}

	/**
	 * 以前の描画と同じ条件xy平面上へスプライトを1個描画する。テクスチャは予めbindしておくこと</br>
	 * 3D空間中に描画する場合には予めtranlatef/rotatef/scalefしておくこと
	 */
	public void draw() {
		vertex.bind();
		vertex.draw(GL10.GL_TRIANGLES, 0, 6);
		vertex.unbind();
	}
}
