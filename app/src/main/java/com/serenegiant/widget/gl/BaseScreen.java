package com.serenegiant.widget.gl;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;

public abstract class BaseScreen implements Screen {
	protected String TAG = getClass().getSimpleName();
	
	public int screenIx = 0;
	private boolean canClose = false;
	protected final IModelView mModelView;
	protected int screenWidth, screenHeight;

	public BaseScreen(final IModelView modelView) {
		mModelView = modelView;
		screenWidth = modelView.getWidth();
		screenHeight = modelView.getHeight();
	}
		
	@Override
	public boolean confirmCanClose(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mModelView.getContext());
		builder.setMessage(message);
		// 肯定応答用ボタンの登録
		builder.setPositiveButton(android.R.string.ok,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					canClose = true;
					dialog.dismiss();
				}
			}
		);
		// 否定応答用ボタンの登録
		builder.setNegativeButton(android.R.string.cancel,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}
		);
		// ダイアログ表示
		builder.show();
		return canClose;
	}

	@Override
	public void setScreenSize(final int width, final int height) {
		screenWidth = width;
		screenHeight = height;
	}
	
	@Override
	public void onSizeChanged(final int width, final int height) {
	}

	@Override
	public void onAdSizeChanged() {
	}
	
	@Override
	public boolean onOptionMenu(final int id) {
		return false;
	}

	@Override
	public void onTouchEvent(final TouchEvent event) {
//		mModelView.requestRender();
	}
	
	@Override
	public void onAccelEvent() {
//		game.requestRender();
	}

	/**
	 * 指定した位置・大きさのRectを生成するためのヘルパーメソッド
	 * @param center_x
	 * @param center_y
	 * @param width
	 * @param height
	 * @return
	 */
	public static Rect makeBoundsRect(final int center_x, final int center_y, final int width, final int height) {
		return new Rect(center_x - width / 2, center_y - height / 2, center_x + width / 2, center_y + height / 2);
	}
}
