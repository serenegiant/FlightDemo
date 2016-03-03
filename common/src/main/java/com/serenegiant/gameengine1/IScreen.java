package com.serenegiant.gameengine1;

public interface IScreen {

	public void update(final float deltaTime);
	public void draw(final float deltaTime);
	public void setAlpha(final float alpha);
	public void onSizeChanged(int width, int height);
	public void setScreenSize(int width, int height);
	public void onAdSizeChanged();
	public void pause();
	public void resume();
	/**
	 * screenインスタンスが破棄される時の処理</br>
	 * 通常のsetScreenでの切替時は呼ばれるが、電断とかバックグラウンドで自動的にアプリが破棄された時とかは呼ばれない事があるかも</br>
	 * なのでゲーム状態の保存等はpause()で行うこと。disposeはオブジェクトの破棄を行う。
	 */
	public void release();
	/**
	 * バックキーを押して離した時の処理</br>
	 * ここはUIスレッド内で実行される
	 * @return trueなら処理済み、falseならデフォルトの処理を上位で行う
	 */
	public boolean backKey();
	/**
	 * オプションメニューを選択した時の処理
	 * @param id
	 * @return trueなら処理済み、falseならデフォルトの処理を上位で行う
	 */
	public boolean onOptionMenu(int id);
	public boolean confirmCanClose(String message);
	public void onTouchEvent(TouchEvent event);
	public void onAccelEvent();
}
