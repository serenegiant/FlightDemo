package com.serenegiant.gameengine1;

import android.content.Context;

public interface LoadableInterface {
	public void load(Context context);
	public void reload(Context context);
	public void resume(Context context);
	public void pause();
	public void dispose();
}
