package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class BaseFragment extends Fragment {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = BaseFragment.class.getSimpleName();

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private Handler mHandler;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
		final HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mHandler = new Handler(thread.getLooper());
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mHandler != null) {
			mHandler.getLooper().quit();
			mHandler = null;
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		loadArguments(savedInstanceState);
	}

/*	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
	} */

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		removeRequestPopBackStack();
		mResetColorFilterTasks.clear();
		super.onPause();
	}

	protected void loadArguments(final Bundle savedInstanceState) {
	}

	/**
	 * 指定したフラグメントに切り替える。元のフラグメントはbackstackに追加する。
	 * @param fragment nullなら何もしない
	 * @return
	 */
	protected Fragment replace(final Fragment fragment) {
		if (fragment != null) {
			getFragmentManager().beginTransaction()
				.addToBackStack(null)
				.replace(R.id.container, fragment)
				.commit();
		}
		return fragment;
	}

	/**
	 * １つ前のフラグメントに戻る
	 */
	protected void popBackStack() {
		getFragmentManager().popBackStack();
	}

	/**
	 * UIスレッド上で実行依頼する
	 * @param task
	 */
	protected void runOnUiThread(final Runnable task) {
		if (task != null) {
			try {
				if (mUIThreadId != Thread.currentThread().getId()) {
					mUIHandler.post(task);
				} else {
					task.run();
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * UIスレッド上での実行要求を削除する
	 * @param task
	 */
	protected void removeFromUIThread(final Runnable task) {
		mUIHandler.removeCallbacks(task);
	}

	/**
	 * 指定時間後に指定したタスクをUIスレッド上で実行する。
	 * @param task UIスレッド上で行う処理
	 * @param delay_msec 0以下ならrunOnUiThreadと同じ
	 */
	protected void runOnUiThread(final Runnable task, final long delay_msec) {
		if (delay_msec <= 0) {
			runOnUiThread(task);
		} else if (task != null) {
			mUIHandler.postDelayed(task, delay_msec);
		}
	}

	/**
	 * プライベートスレッドでの実行待ちタスクを削除する
	 * @param task
	 */
	protected void remove(final Runnable task) {
		if (mHandler != null) {
			mHandler.removeCallbacks(task);
		} else {
			removeFromUIThread(task);
		}
	}
	/**
	 * 指定時間後に指定したタスクをプライベートスレッド上で実行する
	 * @param task
	 * @param delay_msec
	 */
	protected void post(final Runnable task, final long delay_msec) {
		if (mHandler != null) {
			if (delay_msec <= 0) {
				mHandler.post(task);
			} else {
				mHandler.postDelayed(task, delay_msec);
			}
		} else {
			runOnUiThread(task, delay_msec);
		}
	}

	/**
	 * 指定時間後に前のフラグメントに戻る
	 * @param delay
	 */
	protected void requestPopBackStack(final long delay) {
		removeFromUIThread(mPopBackStackTask);
		runOnUiThread(mPopBackStackTask, delay);	// UIスレッド上で遅延実行
	}

	/**
	 * 指定時間後に前のフラグメントに戻るのをキャンセル
	 */
	protected void removeRequestPopBackStack() {
		removeFromUIThread(mPopBackStackTask);
	}

	/**
	 * 一定時間後にフラグメントを終了するためのRunnable
	 * 切断された時に使用
	 */
	private final Runnable mPopBackStackTask = new Runnable() {
		@Override
		public void run() {
			try {
				popBackStack();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	/**
	 * タッチレスポンス用にカラーフィルターを適用する時間
	 */
	protected static final long TOUCH_RESPONSE_TIME_MS = 100;	// 200ミリ秒
	/**
	 * タッチレスポンス時のカラーフィルター色
	 */
	protected static final int TOUCH_RESPONSE_COLOR = 0x7f331133;

	/**
	 * カラーフィルタクリア用のRunnableのキャッシュ
	 */
	private final Map<ImageView, ResetColorFilterTask> mResetColorFilterTasks = new HashMap<ImageView, ResetColorFilterTask>();

	/**
	 * タッチレスポンス用のカラーフィルターを規定時間適用する
	 * @param image
	 */
	protected void setColorFilter(final ImageView image) {
		setColorFilter(image, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
	}

	/**
	 * 指定したImageViewに指定した色でカラーフィルターを適用する。
	 * reset_delayが0より大きければその時間経過後にカラーフィルターをクリアする
	 * @param image
	 * @param color
	 * @param reset_delay ミリ秒
	 */
	protected void setColorFilter(final ImageView image, final int color, final long reset_delay) {
		if (image != null) {
			image.setColorFilter(color);
			if (reset_delay > 0) {
				ResetColorFilterTask task = mResetColorFilterTasks.get(image);
				if (task == null) {
					task = new ResetColorFilterTask(image);
				}
				removeFromUIThread(task);
				runOnUiThread(task, reset_delay);	// UIスレッド上で遅延実行
			}
		}
	}

	/**
	 * 一定時間後にImageView(とImageButton)のカラーフィルターをクリアするためのRunnable
	 */
	private static class ResetColorFilterTask implements Runnable {
		private final ImageView mImage;
		public ResetColorFilterTask(final ImageView image) {
			mImage = image;
		}
		@Override
		public void run() {
			mImage.setColorFilter(0);
		}
	}

	protected LayoutInflater getThemedLayoutInflater(final LayoutInflater inflater) {
		final Activity context = getActivity();
		final SharedPreferences pref = context.getPreferences(0);
		final int layout_style;
		switch (pref.getInt(ConfigFragment.KEY_ICON_TYPE, 0)) {
		case 1:
			layout_style = R.style.AppTheme_001;
			break;
		case 2:
			layout_style = R.style.AppTheme_002;
			break;
//		case 0:
		default:
			layout_style = R.style.AppTheme;
			break;
		}
		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context contextThemeWrapper = new ContextThemeWrapper(context, layout_style);
		// clone the inflater using the ContextThemeWrapper
		return inflater.cloneInContext(contextThemeWrapper);
	}
}
