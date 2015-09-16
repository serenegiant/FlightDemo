package com.serenegiant.gl;

import android.graphics.Bitmap;
import android.opengl.GLException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLGraphics implements BaseGraphics {
	private final IModelView mGLView;
	private GL10 mGL;
	final ByteOrder mNativeOrder = ByteOrder.nativeOrder();
	final ByteBuffer readPixel;
	protected static final float[] projectionM = new float[16];	// プロジェクション行列
	protected static final float[] modelViewM = new float[16];	// モデルビュー行列
	protected static final int[] viewPort = new int[4];
	protected static final float[] pos = new float[4];

	public GLGraphics(final IModelView glView) {
		mGLView = glView;
		readPixel = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
	}

	public GL10 getGL() {
		return mGL;
	}

	void setGL(final GL10 gl) {
		mGL = gl;
	}

/*	public void requestRender() {
		mGLView.requestRender();
	} */

	@Override
	public int getViewWidth() {
		return mGLView.getWidth();
	}

	@Override
	public int getViewHeight() {
		return mGLView.getHeight();
	}

	/**
	 * 指定した座標のpixel値(RGBカラー値)を返す。アルファ値は含まない
	 * @param x x座標(ワールド座標)
	 * @param y y座標(ワールド座標)
	 * @return 指定した座標のRGBカラー値、アルファ値は含まない
	 */
	@Override
	public final int getPixel(final int x, final int y) {
		// glReadPixelsはGL_RGBAとGL_UNSIGNED_BYTEの組み合わせ、または
		// GL_IMPLEMENTATION_COLOR_READ_FORMAT_OESとGL_IMPLEMENTATION_COLOR_READ_TYPE_OES(glGetIntegervで取得する)
		// の組み合わせの2通りのみ有効
		readPixel.clear();	// 使いまわしているのでpositionをリセットしないとダメ
		mGL.glReadPixels(x, y, 1, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, readPixel);
		readPixel.clear();
		int cl = readPixel.getInt();
		if (mNativeOrder == ByteOrder.LITTLE_ENDIAN) {
			// color = (alpha << 24) | (red << 16) | (green << 8) | blue					ARGB
			// glReadPixelsで返って来た値 = (alpha<< 24) | (blue << 16) | (green << 8) | red	ABGR
//			cl = (cl & 0xff000000) | ((cl & 0x000000ff) << 16) | ((cl & 0x00ff0000) >>> 16) | (cl & 0x0000ff00);	// ARGB
			cl = ((cl & 0x000000ff) << 16) | ((cl & 0x00ff0000) >>> 16) | (cl & 0x0000ff00);	// RGB
		}
		return cl;
	}

	@Override
	public Bitmap createBitmap(final int x, final int y, final int width, final int height) {
		final int glTextureBuffer[] = new int[width * height];
		final int bitmapSource[] = new int[width * height];
		final IntBuffer intBuffer = IntBuffer.wrap(glTextureBuffer);
		intBuffer.position(0);

		try {
			mGL.glReadPixels(x, y, width, height,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
			// remember, that OpenGL bitmap is incompatible with Android bitmap
			// and so, some correction need.
			int offset1, offset2, pixel, red, blue;
			for (int i = 0; i < height; i++) {
				offset1 = i * width;
				offset2 = (height - i - 1) * width;
				for (int j = 0; j < width; j++) {
					pixel = glTextureBuffer[offset1 + j];
					blue = (pixel >> 16) & 0xff;
					red = (pixel << 16) & 0x00ff0000;
					bitmapSource[offset2 + j] = (pixel & 0xff00ff00) | red | blue;
				}
			}
		} catch (final GLException e) {
			return null;
		}
		return Bitmap.createBitmap(bitmapSource, width, height, Bitmap.Config.ARGB_8888);
	}

	@Override
	public void pushMatrix() {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void popMatrix() {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void rotate(final float degree) {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void rotate(final float degrees, final float px, final float py) {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void scale(final float scaleX, final float scaleY) {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void translate(final float dx, final float dy) {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}

	@Override
	public void setScaleXY(final float scaleX, final float scaleY) {
		// TODO 未実装(直接GLメソッドを呼ぶこと)
	}
}
