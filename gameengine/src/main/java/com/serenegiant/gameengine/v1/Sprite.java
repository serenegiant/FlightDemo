package com.serenegiant.gameengine.v1;

import javax.microedition.khronos.opengles.GL10;

/**
 * 2Dのスプライトを1個だけ描画するためのクラス</br>
 * テクスチャは予めbindしておくこと</br>
 * SpriteBatcherでスプライトを3Dで回転させたりしようとすると
 * その都度beginBatch/endBatchを呼び出さないとダメなので
 * オーバーヘッドが大きいので、スプライト1個描画専用のクラスを作成
 */
public class Sprite {
	private final float[] vertexBuffer;
	private final Vertex vertex;

	public Sprite(final GLGraphics glGraphics) {
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
	 * @param center_x スプライト中心座標x
	 * @param center_y スプライト中心座標y
	 * @param width スプライトの描画幅
	 * @param height スプライトの描画高さ
	 * @param region スプライト定義
	 */
	public void draw(final float center_x, final float center_y, final float width, final float height, final TextureRegion region) {
		// vertexの割り当て
		final float halfWidth = width / 2;
		final float halfHeight = height / 2;
		
		final float x1 = center_x - halfWidth;
		final float y1 = center_y - halfHeight;
		final float x2 = center_x + halfWidth;
		final float y2 = center_y + halfHeight;
		
		vertexBuffer[0] = x1;
		vertexBuffer[1] = y1;
		vertexBuffer[2] = region.u1;
		vertexBuffer[3] = region.v2;

		vertexBuffer[4] = x2;
		vertexBuffer[5] = y1;
		vertexBuffer[6] = region.u2;
		vertexBuffer[7] = region.v2;

		vertexBuffer[8] = x2;
		vertexBuffer[9] = y2;
		vertexBuffer[10] = region.u2;
		vertexBuffer[11] = region.v1;

		vertexBuffer[12] = x1;
		vertexBuffer[13] = y2;
		vertexBuffer[14] = region.u1;
		vertexBuffer[15] = region.v1;
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
