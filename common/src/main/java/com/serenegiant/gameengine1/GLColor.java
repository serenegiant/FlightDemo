package com.serenegiant.gameengine1;

import android.graphics.Color;

import javax.microedition.khronos.opengles.GL10;

public class GLColor {
	private final float[] mColors = new float[4];
	
	/**
	 * デフォルトコンストラクタ</br>
	 * r=g=b=a=0
	 */
	public GLColor() {	
	}
	
	/**
	 * GLColor形式で初期化する
	 * @param color
	 */
	public GLColor(GLColor color) {
		if (color != null) {
			System.arraycopy(color.mColors, 0, mColors, 0, 4);
		}
	}

	/**
	 * 浮動小数点形式で初期化する
	 * @param r 赤, 0〜1f
	 * @param g 緑, 0〜1f
	 * @param b 青, 0〜1f
	 * @param a アルファ, 0〜1f
	 */
	public GLColor(float r, float g, float b, float a) {
		set(r, g, b, a);
	}
	
	/**
	 * 整数形式で初期化する
	 * @param r 赤, 0〜255
	 * @param g 緑, 0〜255
	 * @param b 青, 0〜255
	 * @param a アルファ, 0〜255
	 */
	public GLColor(int r, int g, int b, int a) {
		set(r, g, b, a);
	}
	
	/**
	 * ARGB形式で色を初期化
	 * @param color ARGB
	 * @return
	 */
	public GLColor(int color) {
		set(color);
	}

	/**
	 * GLColor形式で色を設定
	 * @param color
	 * @return このGLColorインスタンスへの参照
	 */
	public GLColor set(GLColor color) {
		if (color != null) {
			System.arraycopy(color.mColors, 0, mColors, 0, 4);
		}
		return this;
	}
	
	/**
	 *  浮動小数点形式で色を設定
	 * @param r 赤, 0〜1f
	 * @param g 緑, 0〜1f
	 * @param b 青, 0〜1f
	 * @param a アルファ, 0〜1f
	 * @return このGLColorインスタンスへの参照
	 */
	public GLColor set(float r, float g, float b, float a) {
		mColors[0] = r;
		mColors[1] = g;
		mColors[2] = b;
		mColors[3] = a;
		return this;
	}

	/**
	 * 整数形式で色を設定
	 * @param r 赤, 0〜255
	 * @param g 緑, 0〜255
	 * @param b 青, 0〜255
	 * @param a アルファ, 0〜255
	 * @return このGLColorインスタンスへの参照
	 */
	public GLColor set(int r, int g, int b, int a) {
		mColors[0] = r / 255f;
		mColors[1] = g / 255f;
		mColors[2] = b / 255f;
		mColors[3] = a / 255f;
		return this;
	}

	/**
	 * ARGB形式で色を設定
	 * @param color ARGB
	 * @return
	 */
	public GLColor set(int color) {
		mColors[0] = ((color & 0x00ff0000) >>> 16) / 255f;	// r
		mColors[1] = ((color & 0x0000ff00) >>> 8) / 255f;	// g
		mColors[2] = ((color & 0x000000ff)) / 255f;			// b
		mColors[3] = ((color & 0xff000000) >>> 24) / 255f;	// a
		return this;
	}
	
	/**
	 * 色をコピー
	 * @param color
	 * @return
	 */
	public GLColor copy(GLColor color) {
		if (color != null) {
			System.arraycopy(color.mColors, 0, mColors, 0, 4);
		}
		return this;
	}
	
	/**
	 * ARGB形式で色を取得
	 * @return int ARGB
	 */
	public int argb() {
		return ((int)(mColors[0] * 255f) << 16)
			| ((int)(mColors[1] * 255f) << 8)
			| (int)(mColors[2] * 255f)
			| ((int)(mColors[3] * 255f) << 24);
	}
	
	/**
	 * 整数形式の色取得の下請け
	 * @param i
	 * @return
	 */
	private int colorx(int i) {
		return (int)(mColors[i] * 255f);
	}
	
	/**
	 * 赤成分値を浮動小数点形式で取得
	 * @return float 赤成分値, 0〜1f
	 */
	public float red() {
		return mColors[0];
	}
	
	/**
	 * 赤成分値を整数形式で取得
	 * @return int 赤成分値, 0〜255
	 */
	public int redx() {
		return colorx(0);
	}

	/**
	 * 緑成分値を浮動小数点形式で取得
	 * @return float 緑成分値, 0〜1f
	 */
	public float green() {
		return mColors[1];
	}
	
	/**
	 * 緑成分値を整数形式で取得
	 * @return int 緑成分値, 0〜255
	 */
	public int greenx() {
		return colorx(1);
	}

	/**
	 * 青成分値を浮動小数点形式で取得
	 * @return float 青成分値, 0〜1f
	 */
	public float blue() {
		return mColors[2];
	}
	
	/**
	 * 青成分値を整数形式で取得
	 * @return int 青成分値, 0〜255
	 */
	public int bluex() {
		return colorx(2);
	}

	/**
	 * アルファ値を浮動小数点形式で取得
	 * @return float アルファ値, 0〜1f
	 */
	public float alpha() {
		return mColors[3];
	}
	
	/**
	 * アルファ値を整数形式で取得
	 * @return int アルファ値, 0〜255
	 */
	public int alphax() {
		return colorx(3);
	}

	/**
	 * 色を設定(glColor4fのエイリアス)
	 * @param gl
	 * @return このGLColorインスタンスへの参照
	 */
	public GLColor apply(GL10 gl) {
		return glColor4f(gl);
	}

	/**
	 * 色を設定 
	 * @param gl
	 * @return このGLColorインスタンスへの参照
	 */
	public GLColor glColor4f(GL10 gl) {
		gl.glColor4f(mColors[0], mColors[1], mColors[2], mColors[3]);
		return this;
	}

	/**
	 * 色を設定 
	 * @param gl
	 * @param color
	 * @return GLColor
	 */
	public static GLColor glColor4f(GL10 gl, GLColor color) {
		if (color != null) {
			color.glColor4f(gl);
		}
		return color;
	}
	
	public static final GLColor BLACK = new GLColor(Color.BLACK);	
	public static final GLColor BLUE = new GLColor(Color.BLUE);
	public static final GLColor CYAN = new GLColor(Color.CYAN);	
	public static final GLColor DKGRAY = new GLColor(Color.DKGRAY);
	public static final GLColor GRAY = new GLColor(Color.GRAY);
	public static final GLColor GREEN = new GLColor(Color.GREEN);	
	public static final GLColor LTGRAY = new GLColor(Color.LTGRAY);
	public static final GLColor MAGENTA = new GLColor(Color.MAGENTA);
	public static final GLColor RED = new GLColor(Color.RED);
	public static final GLColor TRANSPARENT = new GLColor(Color.TRANSPARENT);	
	public static final GLColor WHITE = new GLColor(Color.WHITE);
	public static final GLColor YELLOW = new GLColor(Color.YELLOW);
}
