package com.serenegiant.widget;

import android.graphics.PathEffect;
import android.graphics.Rect;

public interface IMovable {

	/**
	 * 選択枠の矩形をコピーして返す
	 * @return
	 */
	public abstract Rect getFrameRect();

	/**
	 * サイズ・位置が変更された時のコールバックリスナーを設定
	 * @param listener
	 */
	public abstract void setOnBoundsChangedListener(
													   OnBoundsChangedListener listener);

	/**
	 * サイズ・位置が変更された時のコールバックリスナーを取得
	 * @return
	 */
	public abstract OnBoundsChangedListener getOnBoundsChangedListener();

	/**
	 * 枠を表示するかどうかを取得
	 * @return
	 */
	public abstract boolean isShowFrame();

	/**
	 * 枠を表示するかどうかを設定
	 * @param showFrame
	 */
	public abstract void setShowFrame(boolean showFrame);

	/**
	 * 移動可能かどうかを取得
	 * @return
	 */
	public abstract boolean isMovable();

	/**
	 * 移動可能かどうかを設定
	 * @param mMovable
	 */
	public abstract void setMovable(boolean movable);

	/**
	 * サイズ変更可能かどうかを取得
	 * @return
	 */
	public abstract boolean isResizable();

	/**
	 * サイズ変更かどうかをセット
	 * @param mResizable
	 */
	public abstract void setResizable(boolean resizable);

	/**
	 * アスペクト比固定でサイズ変更するかどうかを取得
	 * @return
	 */
	public abstract boolean keepAspect();
	/**
	 * アスペクト比固定でサイズ変更するかどうかを設定
	 * setSetAspectRatioで設定した値は上書きされる
	 */
	public abstract void setKeepAspect(boolean keep);
	/**
	 * 固定するアスペクト比をセット
	 * @param aspect 0未満の場合はアスペクト比固定解除
	 */
	public abstract void setSetAspectRatio(float aspect);
	/**
	 * 枠の色を取得
	 */
	public abstract int getFrameColor();

	/**
	 * 枠の色を設定
	 * @param cl
	 */
	public abstract void setFrameColor(int cl);

	/**
	 * 枠のアルファ値を取得
	 * @return
	 */
	public abstract int getFrameAlpha();

	/**
	 * 枠のアルファ値を設定
	 * @param alpha
	 */
	public abstract void setFrameAlpha(int alpha);

	/**
	 * 枠の描画幅を取得
	 * @return
	 */
	public abstract float getFrameWidth();

	/**
	 * 枠の描画幅を設定
	 * @param width
	 */
	public abstract void setFrameWidth(float width);

	/**
	 * 枠の表示効果を取得
	 * @return
	 */
	public abstract PathEffect getFrameEffect();

	/**
	 * 枠の表示効果を設定
	 * @param effect
	 */
	public abstract void setFrameEffect(PathEffect effect);

	/**
	 * 選択状態を設定
	 * @param selected
	 */
	public void setSelected(boolean selected);
	/**
	 * 選択状態を反転
	 */
	public void toggleSelected();
	/**
	 * 選択中かどうかを取得
	 * @return
	 */
	public boolean isSelected();

	public int minWidth();
	public int minHeight();
}