package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceControllerListener;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IVideoStreamController;

public abstract class ControlFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = "ControlFragment";

	protected static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	protected Handler mHandler;

	private ARDiscoveryDeviceService mDevice;
	protected IDeviceController mController;

	protected boolean mIsFlying = false;	// FIXME mFlyingStateを参照するようにしてmIsFlyingフラグは削除する

	public ControlFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	@Override
	public synchronized void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			mDevice = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			mController = ManagerFragment.getController(getActivity(), mDevice);
		}
		final HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mHandler = new Handler(thread.getLooper());
		if (DEBUG) Log.v(TAG, "onCreate:savedInstanceState=" + savedInstanceState + ",mController=" + mController);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
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
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mController != null) {
			mController.addListener(mDeviceControllerListener);
		}
	}

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mController != null) {
			mController.removeListener(mDeviceControllerListener);
		}
		super.onPause();
	}

	protected Bundle setDevice(final ARDiscoveryDeviceService device) {
		if (DEBUG) Log.v(TAG, "setDevice:" + device);
		mDevice = device;
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_DEVICE_SERVICE, device);
		setArguments(args);
		return args;
	}

	protected ARDiscoveryDeviceService getDevice() {
		return mDevice;
	}

	protected boolean isConnected() {
		return mController != null ? mController.isConnected() : false;
	}

	protected int getState() {
		return mController != null ? mController.getState() : IDeviceController.STATE_STOPPED;
	}

	protected int getAlarm() {
		return mController != null ? mController.getAlarm() : DroneStatus.ALARM_DISCONNECTED;
	}

	protected void startVideoStreaming() {
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(true);
		}
	}

	protected void stopVideoStreaming() {
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(false);
		}
	}

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

	protected void removeFromUIThread(final Runnable task) {
		mUIHandler.removeCallbacks(task);
	}

	/**
	 * 指定時間後に指定したタスクをUIスレッド上で実行する。
	 * @param task UIスレッド上で行う処理
	 * @param delay_msec 0以下ならrunOnUiThreadと同じ
	 */
	protected void postUIThread(final Runnable task, final long delay_msec) {
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
			postUIThread(task, delay_msec);
		}
	}

	protected synchronized boolean startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		boolean result = false;
		if (mController == null) {
			mController = ManagerFragment.getController(getActivity(), mDevice);
		}
		if (mController != null) {
			final int state = getState();
			if ((state != IDeviceController.STATE_STARTED)
				&& (state != IDeviceController.STATE_STARTING)) {
				if (DEBUG) Log.v(TAG, "未接続");
				updateBattery();

				final MainActivity activity = (MainActivity)getActivity();
				if (activity != null) {
					activity.showProgress(R.string.connecting, true, mOnCancelListener);
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						final boolean failed = mController.start();
						activity.hideProgress();

//						mIsConnected = !failed;
						if (failed) {
							if (DEBUG) Log.w(TAG, "DeviceControllerを開始できなかった");
							try {
								getFragmentManager().popBackStack();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						}
					}
				}).start();
			} else {
				if (DEBUG) Log.v(TAG, "既にstartしている");
//				mController.sendAllSettings();
//				mController.sendAllStates();
				// sendAllSettingsとかsendAllStatesは接続した直後に1回しか有効じゃないのかも
//				updateBattery();
				result = true;
				onConnect(mController);
			}
			this.stopMove();
		} else {
			Log.e(TAG, "controllerがnull!");
		}
		return result;
	}

	protected synchronized void stopDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		mIsFlying = false;
		final int state = getState();
		final IDeviceController controller = mController;
		mController = null;
		if ((state == IDeviceController.STATE_STARTED)
			|| (state == IDeviceController.STATE_STARTING)) {

			final MainActivity activity = (MainActivity)getActivity();
			if (activity != null) {
				activity.showProgress(R.string.disconnecting, false, null);
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					controller.stop();
					if (activity != null) {
						ManagerFragment.releaseController(activity, controller);
						activity.hideProgress();
					}
				}
			}).start();
		}
	}

	/**
	 * 移動停止
	 */
	protected void stopMove() {
		if (DEBUG) Log.v(TAG, "stopMove:");
		if (mController != null) {
			mController.setMove(0, 0, 0, 0, 0);
/*			// 上下移動量をクリア, 正:上, 負:下
			mController.setGaz(0);
			// 回転量をクリア, 正:右回り, 負:左回り
			mController.setYaw(0);
			// 前後移動量をクリア, 正:前, 負:後
			mController.setPitch(0);
			// 左右移動量をクリア, 正:右, 負:左
			mController.setRoll(0);
			// pitch/roll移動フラグをクリア
			mController.setFlag(0); */
		}
	}

	/**
	 * 非常停止指示
	 */
	protected void emergencyStop() {
		stopMove();
		if (mController != null) {
			mController.sendEmergency();
		}
	}

	protected void updateBattery() {
	}

	protected void updateFlyingState(final int state) {
	}

	protected void updateAlarmState(final int alert_state) {
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param picture_state IDeviceController#PICTURE_XXX
	 */
	protected void updatePictureState(final int picture_state) {
	}

	protected void onConnect(final IDeviceController controller) {
		startVideoStreaming();
	}

	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		stopMove();
		stopVideoStreaming();
		stopDeviceController(true);
	}

	protected void onAlarmStateChangedUpdate(int alert_state) {
	}

	private final DeviceControllerListener mDeviceControllerListener
		= new DeviceControllerListener() {
		@Override
		public void onConnect(IDeviceController controller) {
			ControlFragment.this.onConnect(controller);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "mDeviceControllerListener#onDisconnect");
			ControlFragment.this.onDisconnect(controller);
		}

		@Override
		public void onUpdateBattery(final int percent) {
			updateBattery();
		}

		@Override
		public void onFlyingStateChangedUpdate(final int state) {
			updateFlyingState(state);
		}

		@Override
		public void onAlarmStateChangedUpdate(int alarm_state) {
			if (DEBUG) Log.v(TAG, "mDeviceControllerListener#onAlarmStateChangedUpdate:state=" + alarm_state);
			updateAlarmState(alarm_state);
		}

		@Override
		public void onFlatTrimChanged() {
			postUIThread(new Runnable() {
				@Override
				public void run() {
					final Activity activity = getActivity();
					if ((activity != null) && !activity.isFinishing()) {
						Toast.makeText(activity, R.string.action_flat_trim_finished, Toast.LENGTH_SHORT).show();
					}
				}
			}, 0);
		}

		@Override
		public void onStillCaptureStateChanged(int state) {
			updatePictureState(state);
		}

	};

	private final DialogInterface.OnCancelListener mOnCancelListener
		= new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			if (DEBUG) Log.w(TAG, "ユーザーキャンセル");
			if (getState() == IDeviceController.STATE_STARTING) {
				mController.cancelStart();
			}
		}
	};

}
