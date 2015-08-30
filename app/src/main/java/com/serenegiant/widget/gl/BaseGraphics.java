package com.serenegiant.widget.gl;

import android.graphics.Bitmap;

public interface BaseGraphics {
	public int getViewWidth();
	public int getViewHeight();
	/**
	 * 指定した座標のpixel値(RGBカラー値)を返す。アルファ値は含まない
	 * @param x x座標
	 * @param y y座標
	 * @return 指定した座標のRGBカラー値、アルファ値は含まない
	 */
	public int getPixel(int x, int y);
	public Bitmap createBitmap(int x, int y, int width, int height);
	
	public void pushMatrix();
	public void popMatrix();
	public void rotate(float degree);
	public void rotate(float degrees, float px, float py);
	public void scale(float scaleX, float scaleY);
	public void translate(float dx, float dy);
	public void setScaleXY(float scaleX, float scaleY);

}
