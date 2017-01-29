package com.serenegiant.aceparrot;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.serenegiant.dialog.MessageDialogFragment;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.utils.PermissionCheck;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public class BaseFragment extends Fragment implements MessageDialogFragment.MessageDialogListener {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = BaseFragment.class.getSimpleName();

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private Handler mAsyncHandler;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected Vibrator mVibrator;
	private Toast mToast;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@SuppressWarnings("deprecation")
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
		cancelToast();
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
	public final void onStart() {
		super.onStart();
		if (BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	public synchronized final void onResume() {
		super.onResume();
		if (!BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	public synchronized final void onPause() {
		if (!BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onPause();
	}

	@Override
	public final void onStop() {
		if (BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onStop();
	}

	protected void internalOnResume() {
		if (DEBUG) Log.v(TAG, "internalOnResume:");
		mIsReplacing = false;
	}

	protected void internalOnPause() {
		if (DEBUG) Log.v(TAG, "internalOnPause:");
		removeRequestPopBackStack();
		mResetColorFilterTasks.clear();
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
		try {
			getFragmentManager().popBackStack();
		} catch (final Exception e) {
			//
		}
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
		if (task != null) {
			mUIHandler.removeCallbacks(task);
		}
	}

	/**
	 * 指定時間後に指定したタスクをUIスレッド上で実行する。
	 * @param task UIスレッド上で行う処理
	 * @param delay_msec 0以下ならrunOnUiThreadと同じ
	 */
	protected void runOnUiThread(final Runnable task, final long delay_msec) {
		if (task != null) {
			if (delay_msec <= 0) {
				runOnUiThread(task);
			} else if (task != null) {
				mUIHandler.postDelayed(task, delay_msec);
			}
		}
	}

	/**
	 * プライベートスレッドでの実行待ちタスクを削除する
	 * @param task
	 */
	protected void removeEvent(final Runnable task) {
		if (task != null) {
			if (mAsyncHandler != null) {
				mAsyncHandler.removeCallbacks(task);
			} else {
				removeFromUIThread(task);
			}
		}
	}
	/**
	 * 指定時間後に指定したタスクをプライベートスレッド上で実行する
	 * @param task
	 * @param delay_msec
	 */
	protected void queueEvent(final Runnable task, final long delay_msec) {
		if (task != null) {
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
	private final Map<View, ResetColorFilterTask> mResetColorFilterTasks = new HashMap<View, ResetColorFilterTask>();

	/**
	 * タッチレスポンス用のカラーフィルターを規定時間適用する
	 * @param view
	 */
	protected void setColorFilter(final View view) {
		setColorFilter(view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
	}

	/**
	 * 指定したImageViewに指定した色でカラーフィルターを適用する。
	 * reset_delayが0より大きければその時間経過後にカラーフィルターをクリアする
	 * @param view
	 * @param color
	 * @param reset_delay ミリ秒
	 */
	protected void setColorFilter(final View view, final int color, final long reset_delay) {
		if (view instanceof ImageView) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((ImageView)view).setColorFilter(color);
				}
			});
			if (reset_delay > 0) {
				ResetColorFilterTask task = mResetColorFilterTasks.get(view);
				if (task == null) {
					task = new ResetColorFilterTask(((ImageView)view));
				}
				removeFromUIThread(task);
				runOnUiThread(task, reset_delay);	// UIスレッド上で遅延実行
			}
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					view.setBackgroundColor(color);
				}
			});
			if (reset_delay > 0) {
				ResetColorFilterTask task = mResetColorFilterTasks.get(view);
				if (task == null) {
					task = new ResetColorFilterTask(view);
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
		private final View mView;
		ResetColorFilterTask(final View view) {
			mView = view;
		}
		@Override
		public void run() {
			if (mView instanceof ImageView) {
				((ImageView)mView).setColorFilter(0);
			} else {
				mView.setBackgroundColor(0);
			}
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
		final int layout_style = getLayoutStyle(pref.getInt(AppConst.KEY_ICON_TYPE, 0));
		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context contextThemeWrapper = new ContextThemeWrapper(context, layout_style);
		// clone the inflater using the ContextThemeWrapper
		return inflater.cloneInContext(contextThemeWrapper);
	}

	protected int getLayoutStyle(final int type) {
		final int result;
		switch (type) {
		case 1:
			result = R.style.AppTheme_001;
			break;
		case 2:
			result = R.style.AppTheme_002;
			break;
//		case 0:
		default:
			result = R.style.AppTheme;
			break;
		}
		return result;
	}

//================================================================================
// Android6以降の動的パーミッション関係の処理
//================================================================================

	/**
	 * Callback listener from MessageDialogFragmentV4
	 * @param dialog
	 * @param requestCode
	 * @param permissions
	 * @param result
	 */
	@SuppressLint("NewApi")
	@Override
	public void onMessageDialogResult(final MessageDialogFragment dialog, final int requestCode, final String[] permissions, final boolean result) {
		if (result) {
			// request permission(s) when user touched/clicked OK
			if (BuildCheck.isMarshmallow()) {
				requestPermissions(permissions, requestCode);
				return;
			}
		}
		// check permission and call #checkPermissionResult when user canceled or not Android6
		final Context context = getActivity();
		for (final String permission: permissions) {
			checkPermissionResult(requestCode, permission, PermissionCheck.hasPermission(context, permission));
		}
	}

	/**
	 * callback method when app(Fragment) receive the result of permission result from ANdroid system
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);	// 何もしてないけど一応呼んどく
		final int n = Math.min(permissions.length, grantResults.length);
		for (int i = 0; i < n; i++) {
			checkPermissionResult(requestCode, permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
		}
	}

	/**
	 * check the result of permission request
	 * if app still has no permission, just show Toast
	 * @param requestCode
	 * @param permission
	 * @param result
	 */
	protected void checkPermissionResult(final int requestCode, final String permission, final boolean result) {
		// show Toast when there is no permission
		if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
			onUpdateAudioPermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_audio, Toast.LENGTH_SHORT).show();
			}
		}
		if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
			onUpdateExternalStoragePermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_ext_storage, Toast.LENGTH_SHORT).show();
			}
		}
		if (Manifest.permission.INTERNET.equals(permission)) {
			onUpdateNetworkPermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_network, Toast.LENGTH_SHORT).show();
			}
		}
	}

	protected void onUpdateAudioPermission(final boolean hasPermission) {
	}

	protected void onUpdateExternalStoragePermission(final boolean hasPermission) {
	}

	protected void onUpdateNetworkPermission(final boolean hasPermission) {
	}

	protected static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x01;
	protected static final int REQUEST_PERMISSION_AUDIO_RECORDING = 0x02;
	protected static final int REQUEST_PERMISSION_NETWORK = 0x03;
	protected static final int REQUEST_PERMISSION_LOCATION = 0x04;
	protected static final int REQUEST_PERMISSION_LOCATION_COARSE = 0x05;
	protected static final int REQUEST_PERMISSION_LOCATION_FINE = 0x06;

	/**
	 * check whether this app has write external storage
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionWriteExternalStorage() {
		if (!PermissionCheck.hasWriteExternalStorage(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE,
				R.string.permission_title, R.string.permission_ext_storage_request,
				new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission of audio recording
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionAudio() {
		if (!PermissionCheck.hasAudio(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_AUDIO_RECORDING,
				R.string.permission_title, R.string.permission_audio_recording_request,
				new String[] {Manifest.permission.RECORD_AUDIO});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission of network access
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionNetwork() {
		if (!PermissionCheck.hasNetwork(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_NETWORK,
				R.string.permission_title, R.string.permission_network_request,
				new String[] {Manifest.permission.INTERNET});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to coarse and fine location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocation() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_reason,
				new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
							  Manifest.permission.ACCESS_FINE_LOCATION});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to coarse location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocationCoarse() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_reason,
				new String[] { Manifest.permission.ACCESS_COARSE_LOCATION});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to fine location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocationFine() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_reason,
				new String[] { Manifest.permission.ACCESS_FINE_LOCATION});
			return false;
		}
		return true;
	}

//================================================================================
	@IntDef({Toast.LENGTH_SHORT, Toast.LENGTH_LONG})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Duration {}

	protected void showToast(final String message, @Duration final int duration) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mToast != null) {
					mToast.cancel();
					mToast = null;
				}
				final Activity activity = getActivity();
				if ((activity == null) || activity.isFinishing()) return;
				mToast = Toast.makeText(activity, message, duration);
				mToast.show();
			}
		});
	}

	protected void showToast(@StringRes final int messageId, @Duration final int duration) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mToast != null) {
					mToast.cancel();
					mToast = null;
				}
				final Activity activity = getActivity();
				if ((activity == null) || activity.isFinishing()) return;
				mToast = Toast.makeText(activity, messageId, duration);
				mToast.show();
			}
		});
	}

	protected void cancelToast() {
		if (mToast != null) {
			try {
				mToast.cancel();
			} catch (final Exception e) {
				// ignore
			}
		}
		mToast = null;
	}
}
