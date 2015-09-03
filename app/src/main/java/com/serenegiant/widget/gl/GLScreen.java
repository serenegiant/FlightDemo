package com.serenegiant.widget.gl;

import android.opengl.GLU;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public abstract class GLScreen extends BaseScreen {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること

	protected final GLGraphics glGraphics;
	protected static final float[] projectionM = new float[16];	// プロジェクション行列
	protected static final float[] modelViewM = new float[16];	// モデルビュー行列
	protected static final int[] viewPort = new int[4];
	protected static final float[] pos = new float[4];

	public GLScreen(final IModelView modelView) {
		super(modelView);
		glGraphics = modelView.getGLGraphics();
	}
	
	/**
	 * 座標変換用の行列を更新する(プロジェクション行列とモデルビュー行列を取得)</br>
	 * @param gl
	 */
	public void updateMatrix(GL10 gl) {
		if ((gl instanceof GL11)) {
			GL11 gl11 = (GL11)gl;
			
			viewPort[2] = glGraphics.getViewWidth();
			viewPort[3] = glGraphics.getViewHeight();
			// プロジェクション変換行列を取得
			gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionM, 0);
			if (DEBUG) GLHelper.checkGlError(gl, "GLScreen#glGetFloatv");
			// モデルビュー変換行列を取得
			gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewM, 0);
			if (DEBUG) GLHelper.checkGlError(gl, "GLScreen#glGetFloatv");
		}
	}
	
	/**
	 *  ビュー座標(デバイス座標)座標 -> ワールド座標変換を行う。</br>
	 *  これを使うには適切なところでupdateMatrixを呼び出すこと
	 * @param viewPos
	 * @param worldPos
	 */
	public void viewToWorld(Vector viewPos, Vector worldPos) {
		if (GLU.gluUnProject(
			viewPos.x, glGraphics.getViewHeight() - viewPos.y, viewPos.z,	// スクリーン座標(zは適当に)
			modelViewM, 0, projectionM, 0, viewPort, 0, 					// モデルビュー行列、投影行列、ビューポートを与える
			pos, 0) == GL10.GL_TRUE) {										//ここにワールド座標に変換されたデータが格納される
			worldPos.set(pos[0], pos[1], pos[2]);
		}
		if (DEBUG) GLHelper.checkGlError(glGraphics.getGL(), "GLScreen#gluUnProject");
	}
	
	/**
	 * ワールド座標 -> ビュー座標(デバイス座標)変換を行う。</br>
	 * これを使うには適切なところでupdateMatrixを呼び出すこと
	 * @param worldPos
	 * @param viewPos
	 */
	public void worldToView(Vector worldPos, Vector viewPos) {
		if (GLU.gluProject(
			worldPos.x, worldPos.y, worldPos.z,				// ワールド座標
			modelViewM, 0, projectionM, 0, viewPort, 0, 	// モデルビュー行列、投影行列、ビューポートを与える
			pos, 0) == GL10.GL_TRUE) {						// ここにワールド座標に変換されたデータが格納される
			viewPos.set(pos[0], pos[1], pos[2]);
		}
		if (DEBUG) GLHelper.checkGlError(glGraphics.getGL(), "GLScreen#gluProject");
	}

	/**
	 * ビュー座標(デバイス座標) -> スクリーン座標変換を行う。</br>
	 * ビュー座標(デバイス座標)は左上が原点(0,0)。スクリーン座標は左下が原点(0,0)
	 * @param viewPos
	 * @return Vector スクリーン座標(元のviewPosに上書き)
	 */
	public Vector viewToScreen(Vector viewPos) {
		return viewToScreen(viewPos, viewPos);
	}

	/**
	 * ビュー座標(デバイス座標) -> スクリーン座標変換を行う。</br>
	 * ビュー座標(デバイス座標)は左上が原点(0,0)。スクリーン座標は左下が原点(0,0)
	 * @param viewPos
	 * @return Vector スクリーン座標
	 */
	public Vector viewToScreen(Vector viewPos, Vector screenPos) {
		screenPos.x = viewPos.x / glGraphics.getViewWidth() * screenWidth;
		screenPos.y = (glGraphics.getViewHeight() - viewPos.y) / glGraphics.getViewHeight() * screenHeight;
		return screenPos;
	}

	/**
	 * スクリーン座標 -> ビュー座標(デバイス座標)変換を行う。</br>
	 * ビュー座標(デバイス座標)は左上が原点(0,0)。スクリーン座標は左下が原点(0,0)
	 * @param screenPos
	 * @return Vector ビュー座標(元のscreenPosに上書き)
	 */
	public Vector screenToView(Vector screenPos) {
		return screenToView(screenPos, screenPos);
	}
	
	/**
	 * スクリーン座標 -> ビュー座標(デバイス座標)変換を行う。</br>
	 * ビュー座標(デバイス座標)は左上が原点(0,0)。スクリーン座標は左下が原点(0,0)
	 * @param screenPos
	 * @return Vector ビュー座標
	 */
	public Vector screenToView(Vector screenPos, Vector viewPos) {
		viewPos.x = screenPos.x / screenWidth * glGraphics.getViewWidth();
		viewPos.y = glGraphics.getViewHeight() - screenPos.y / screenHeight * glGraphics.getViewHeight();
		return viewPos;
	}
	
}
