package com.serenegiant.gameengine1;

import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * 2D画面設定用のカメラ</br>
 * スクリーンサイズ変更時にsetScreenSizeを呼び出す</br>
 * 描画前にsetMatrixを呼び出す
 */
public class GLCamera2D implements Camera2D {
	public final Vector position = new Vector();
	protected float mZoom = 1.0f;
	protected float screenWidth;
	protected float screenHeight;
	protected float viewWidth;	// 2013/09/14追加 viewToCameraが少しでも早くなることを期待して
	protected float viewHeight;	// 2013/09/14追加 viewToCameraが少しでも早くなることを期待して
	private final BaseGraphics mGraphics;
	private float w2, h2;
	
	/**
	 * コンストラクタ
	 * @param graphics GLGraphicsのインスタンス
	 * @param screenWidth 画面横幅(描画幅、実端末の横幅でなくても良い 普段は縦長なら480f、横長なら800f)
	 * @param screenHeight 画面高さ(描画高さ、実端末の高さでなくても良い 普段は縦長なら800f、横長なら480f)
	 */
	public GLCamera2D(final BaseGraphics graphics, final float screenWidth, final float screenHeight) {
		mGraphics = graphics;
		setScreenSize(screenWidth, screenHeight);
	}
	
	/**
	 * 投影行列を設定</br>
	 * 2D描画前に呼び出す
	 */
	public void setMatrix() {
		final GL10 gl = ((GLGraphics)mGraphics).getGL();

		// 投影行列の設定
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(position.x - w2 * mZoom,
					position.x + w2 * mZoom,
					position.y - h2 * mZoom,
					position.y + h2 * mZoom,
					1, -1);
		// デフォルト塗りつぶし色を設定
		gl.glClearColor(0f, 0f, 0f, 1f);	// 黒色
		// これ以降はModelViewを扱う
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/**
	 * 画面サイズの変更 
	 * @param screenWidth
	 * @param screenHeight
	 */
	@Override
	public void setScreenSize(final float screenWidth, final float screenHeight) {
		this.screenWidth =  screenWidth;
		this.screenHeight = screenHeight;
		viewWidth = mGraphics.getViewWidth();
		viewHeight = mGraphics.getViewHeight();
		w2 = screenWidth / 2f;
		h2 = screenHeight / 2f;
		position.set(w2, h2);
	}
		
	/**
	 * ビュー座標(デバイス座標)からカメラ座標への変換 
	 * @param pos
	 */
	@Override
	public void viewToCamera(final Vector pos) {
		pos.x = (pos.x / viewWidth) * screenWidth * mZoom;
		pos.y = (1 - pos.y / viewHeight) * screenHeight * mZoom;
		pos.add(position).sub(w2 * mZoom, h2 * mZoom);
	}
	
	/**
	 * 拡大率をセット
	 */
	@Override
	public void setZoom(final float zoom) {
		mZoom = zoom;
	}
	
	/**
	 * 現在の拡大率を取得
	 */
	@Override
	public float getZoom() {
		return mZoom;
	}

}
