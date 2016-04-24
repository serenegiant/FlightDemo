package com.serenegiant.aceparrot;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.HandlerThreadHandler;

import java.util.HashMap;
import java.util.Map;

public class BaseFragment extends Fragment {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = BaseFragment.class.getSimpleName();

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private Handler mAsyncHandler;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected Vibrator mVibrator;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity);
		mVibrator = (Vibrator)getActivity().getSystemService(Activity.VIBRATOR_SERVICE);
		mIsReplacing = false;
	}

	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
		mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
		mIsReplacing = false;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mAsyncHandler != null) {
			try {
				mAsyncHandler.getLooper().quit();
			} catch (final Exception e) {
				// ignore
			}
			mAsyncHandler = null;
		}
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		mLocalBroadcastManager = null;
		mVibrator = null;
		super.onDetach();
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

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mIsReplacing = false;
	}

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		removeRequestPopBackStack();
		mResetColorFilterTasks.clear();
		super.onPause();
	}

	protected void loadArguments(final Bundle savedInstanceState) {
	}

	private boolean mIsReplacing;
	protected boolean isReplacing() {
		return mIsReplacing;
	}

	/**
	 * 指定したフラグメントに切り替える。元のフラグメントはbackstackに追加する。
	 * @param fragment nullなら何もしない
	 * @return
	 */
	protected Fragment replace(final Fragment fragment) {
		if (fragment != null) {
			mIsReplacing = true;
			getFragmentManager().beginTransaction()
				.addToBackStack(null)
				.replace(R.id.container, fragment)
				.commit();
		}
		return fragment;
	}

	protected void clearReplacing() {
		mIsReplacing = false;
	}

	/**
	 * １つ前のフラグメントに戻る
	 */
	protected void popBackStack() {
		mIsReplacing = false;
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
	protected void removeEvent(final Runnable task) {
		if (mAsyncHandler != null) {
			mAsyncHandler.removeCallbacks(task);
		} else {
			removeFromUIThread(task);
		}
	}
	/**
	 * 指定時間後に指定したタスクをプライベートスレッド上で実行する
	 * @param task
	 * @param delay_msec
	 */
	protected void queueEvent(final Runnable task, final long delay_msec) {
		if (mAsyncHandler != null) {
			if (delay_msec <= 0) {
				mAsyncHandler.post(task);
			} else {
				mAsyncHandler.postDelayed(task, delay_msec);
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
				mIsReplacing = false;
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
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					image.setColorFilter(color);
				}
			});
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

	protected interface AnimationCallback {
		public void onAnimationEnd(final View target, final int type);
	}

	protected static final int ANIM_CANCEL = 0;
	protected static final int ANIM_FADE_OUT = -1;
	protected static final int ANIM_FADE_IN = -2;
	protected static final int ANIM_ZOOM_OUT = -3;
	protected static final int ANIM_ZOOM_IN = -4;

	/**
	 * アルファ値をstartからstopまで変化させる
	 * @param target
	 * @param type ゼロ以下は内部で使用するので使用不可
	 * @param start [0.0f-1.0f]
	 * @param stop  [0.0f-1.0f]
	 * @param duration [ミリ秒]
	 * @param startDelay [ミリ秒]
	 */
	@SuppressLint("NewApi")
	protected final void alphaAnimation(final View target, final int type,
		final float start, final float stop, final long duration, final long startDelay,
		final AnimationCallback callback) {
//		if (DEBUG) Log.v(TAG, "fadeOut,target=" + target);
		if (target == null) return;
		target.clearAnimation();
		if (target.getVisibility() == View.VISIBLE) {
			target.setTag(R.id.anim_type, type);
			target.setTag(R.id.anim_callback, callback);
			target.setScaleX(1.0f);
			target.setScaleY(1.0f);
			target.setAlpha(start);
			final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", start, stop );
			objectAnimator.addListener(mAnimatorListener);
			if (BuildCheck.isAndroid4_3())
				objectAnimator.setAutoCancel(true);		// API >= 18 同じターゲットに対して別のAnimatorが開始したら自分をキャンセルする
			objectAnimator.setDuration(duration > 0 ? duration : 500);	// 0.5秒かけて実行
			objectAnimator.setStartDelay(startDelay > 0 ? startDelay : 0);	// 開始までの時間
		    objectAnimator.start();						// アニメーションを開始
		}
	}

	/**
	 * アルファ値を0→1まで変化(Viewをフェードイン)させる
	 * @param target
	 * @param startDelay
	 */
	@SuppressLint("NewApi")
	protected final void fadeIn(final View target, final long duration, final long startDelay) {
//		if (DEBUG) Log.v(TAG, "fadeIn:target=" + target);
		if (target == null) return;
		target.clearAnimation();
		target.setVisibility(View.VISIBLE);
		target.setTag(R.id.anim_type, ANIM_FADE_IN);	// フェードインの時の印
		target.setScaleX(1.0f);
		target.setScaleY(1.0f);
		target.setAlpha(0.0f);
		final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", 0f, 1f );
		objectAnimator.addListener(mAnimatorListener);
		if (BuildCheck.isJellyBeanMR2())
			objectAnimator.setAutoCancel(true);		// API >= 18 同じターゲットに対して別のAnimatorが開始したら自分をキャンセルする
		objectAnimator.setDuration(duration > 0 ? duration : 500);	// 0.5秒かけて実行
		objectAnimator.setStartDelay(startDelay > 0 ? startDelay : 0);	// 開始までの時間
	    objectAnimator.start();						// アニメーションを開始
	}

	/**
	 * アルファ値を1→0まで変化(Viewをフェードアウト)させる
	 * @param target
	 * @param startDelay
	 */
	@SuppressLint("NewApi")
	protected final void fadeOut(final View target, final long duration, final long startDelay) {
//		if (DEBUG) Log.v(TAG, "fadeOut,target=" + target);
		if (target == null) return;
		target.clearAnimation();
		if (target.getVisibility() == View.VISIBLE) {
			target.setTag(R.id.anim_type, ANIM_FADE_OUT);	// フェードアウトの印
			target.setScaleX(1.0f);
			target.setScaleY(1.0f);
			target.setAlpha(1.0f);
			final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", 1f, 0f );
			objectAnimator.addListener(mAnimatorListener);
			if (BuildCheck.isAndroid4_3())
				objectAnimator.setAutoCancel(true);		// API >= 18 同じターゲットに対して別のAnimatorが開始したら自分をキャンセルする
			objectAnimator.setDuration(duration > 0 ? duration : 500);	// 0.5秒かけて実行
			objectAnimator.setStartDelay(startDelay > 0 ? startDelay : 0);	// 開始までの時間
		    objectAnimator.start();						// アニメーションを開始
		}
	}

	/**
	 * スケールを0→1まで変化(Viewをズームイン)させる
	 * @param target
	 * @param startDelay
	 */
	@SuppressLint("NewApi")
	protected final void zoomIn(final View target, final long duration, final long startDelay) {
//		if (DEBUG) Log.v(TAG, "zoomIn:target=" + target);
		if (target == null) return;
		target.clearAnimation();
		target.setVisibility(View.VISIBLE);
		target.setTag(R.id.anim_type, ANIM_ZOOM_IN);	// ズームインの時の印
		target.setScaleX(0.0f);
		target.setScaleY(0.0f);
		target.setAlpha(1.0f);
		final PropertyValuesHolder scale_x = PropertyValuesHolder.ofFloat( "scaleX", 0.01f, 1f);
		final PropertyValuesHolder scale_y = PropertyValuesHolder.ofFloat( "scaleY", 0.01f, 1f);
		final ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(target, scale_x, scale_y);
		objectAnimator.addListener(mAnimatorListener);
		if (BuildCheck.isJellyBeanMR2())
			objectAnimator.setAutoCancel(true);		// API >= 18 同じターゲットに対して別のAnimatorが開始したら自分をキャンセルする
		objectAnimator.setDuration(duration > 0 ? duration : 500);	// 0.5秒かけて実行
		objectAnimator.setStartDelay(startDelay > 0 ? startDelay : 0);	// 開始までの時間
	    objectAnimator.start();						// アニメーションを開始
	}

	/**
	 * スケールを1→0まで変化(Viewをズームアウト)させる
	 * @param target
	 * @param startDelay
	 */
	@SuppressLint("NewApi")
	protected final void zoomOut(final View target, final long duration, final long startDelay) {
//		if (DEBUG) Log.v(TAG, "zoomIn:target=" + target);
		if (target == null) return;
		target.clearAnimation();
		target.setVisibility(View.VISIBLE);
		target.setTag(R.id.anim_type, ANIM_ZOOM_OUT);	// ズームアウトの時の印
		target.setScaleX(1.0f);
		target.setScaleY(1.0f);
		target.setAlpha(1.0f);
		final PropertyValuesHolder scale_x = PropertyValuesHolder.ofFloat( "scaleX", 1f, 0f);
		final PropertyValuesHolder scale_y = PropertyValuesHolder.ofFloat( "scaleY", 1f, 0f);
		final ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(target, scale_x, scale_y);
		objectAnimator.addListener(mAnimatorListener);
		if (BuildCheck.isJellyBeanMR2())
			objectAnimator.setAutoCancel(true);		// API >= 18 同じターゲットに対して別のAnimatorが開始したら自分をキャンセルする
		objectAnimator.setDuration(duration > 0 ? duration : 500);			// 0.5秒かけて実行
		objectAnimator.setStartDelay(startDelay > 0 ? startDelay : 0);	// 開始までの時間
		objectAnimator.start();						// アニメーションを開始
	}

	/**
	 * アニメーション用コールバックリスナー
	 */
	private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(final Animator animation) {
//			if (DEBUG) Log.v(TAG, "onAnimationStart:");
		}
		@Override
		public void onAnimationEnd(final Animator animation) {
			if (animation instanceof ObjectAnimator) {
				final Activity activity = getActivity();
				if ((activity == null) || (activity.isFinishing())) return;
				final ObjectAnimator anim = (ObjectAnimator)animation;
				final View target = (View)anim.getTarget();
				if (target == null) return;	// これはありえないはずだけど
				int anim_type = 0;
				try {
					anim_type = (Integer)target.getTag(R.id.anim_type);
				} catch (final Exception e) {
				}
				final int id = target.getId();
				switch (anim_type) {
				case ANIM_CANCEL:
					break;
				case ANIM_FADE_OUT:	// フェードアウト
				case ANIM_FADE_IN:	// フェードイン
				{
					final boolean fadeIn = anim_type == ANIM_FADE_IN;
					if (!fadeIn) {
						target.setVisibility(View.GONE);
					}
					break;
				}
				case ANIM_ZOOM_IN:	// ズームイン
				{
					// 静止画モードならTHUMBNAIL_HIDE_DELAY経過後にフェードアウトさせる
					// 既に動画モードに切り替えられていればすぐにフェードアウトさせる.
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							fadeOut(target, -1, 500);
						}
					}, 0);
					break;
				}
				case ANIM_ZOOM_OUT:	// ズームアウト
				{
					target.setVisibility(View.GONE);
					break;
				}
				default:
				{
					final AnimationCallback callback = (AnimationCallback)target.getTag(R.id.anim_callback);
					if (callback != null) {
						try {
							callback.onAnimationEnd(target, anim_type);
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				}
			}
		}
		@Override
		public void onAnimationCancel(final Animator animation) {
		}
		@Override
		public void onAnimationRepeat(final Animator animation) {
		}
	};

	protected LayoutInflater getThemedLayoutInflater(final LayoutInflater inflater) {
		final Activity context = getActivity();
		final SharedPreferences pref = context.getPreferences(0);
		final int layout_style;
		switch (pref.getInt(AppConst.KEY_ICON_TYPE, 0)) {
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
