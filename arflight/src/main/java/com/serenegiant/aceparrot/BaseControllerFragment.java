package com.serenegiant.aceparrot;

import android.os.Bundle;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.BuildConfig;

import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IFlightController;
import jp.co.rediscovery.arflight.ISkyController;
import jp.co.rediscovery.arflight.ManagerFragment;

import static com.serenegiant.aceparrot.AppConst.*;

public abstract class BaseControllerFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private final String TAG = "BaseControllerFragment:" + getClass().getSimpleName();

	/** 前のフラグメントに戻るまでの遅延時間[ミリ秒] */
	protected static final long POP_BACK_STACK_DELAY = 2000;

	private ARDiscoveryDeviceService mDevice;
	private DeviceInfo mDeviceInfo;
	protected IDeviceController mController;

	public BaseControllerFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

//	@Override
//	public void onAttach(final Activity activity) {
//		super.onAttach(activity);
//		if (DEBUG) Log.v(TAG, "onAttach:");
//	}

	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:savedInstanceState=" + savedInstanceState);
		mController = null;
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		if (args != null) {
			mDevice = args.getParcelable(ARFLIGHT_EXTRA_DEVICE_SERVICE);
			mDeviceInfo = args.getParcelable(ARFLIGHT_EXTRA_DEVICE_INFO);
			getController();
		}
		if (DEBUG) Log.v(TAG, "onCreate:mController=" + mController);
	}

	/** Viewが生成される前に毎回行う処理 */
	protected abstract void onBeforeCreateView();

//	@Override
//	public void onSaveInstanceState(final Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
//	}

//	@Override
//	public synchronized void onStart() {
//		super.onStart();
//		if (DEBUG) Log.v(TAG, "onStart:");
//	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		getController();
		if (DEBUG) Log.v(TAG, "internalOnResume:");
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.v(TAG, "internalOnPause:isFinishing=" + getActivity().isFinishing());
		if (mController instanceof ISkyController) {
			((ISkyController)mController).disconnectFrom();
		} else if ((mController != null) && canReleaseController()) {
			try {
				releaseDeviceController(false);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		mController = null;
		super.internalOnPause();
	}

	protected Bundle setDevice(final ARDiscoveryDeviceService device) {
		if (DEBUG) Log.v(TAG, "setDevice:" + device);
		mDevice = device;
		mDeviceInfo = null;
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putParcelable(ARFLIGHT_EXTRA_DEVICE_SERVICE, device);
		args.remove(ARFLIGHT_EXTRA_DEVICE_INFO);
		setArguments(args);
		return args;
	}

	protected Bundle setDevice(final ARDiscoveryDeviceService bridge, final DeviceInfo info) {
		if ((info != null) && !BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		mDevice = bridge;
		mDeviceInfo = info;
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putParcelable(ARFLIGHT_EXTRA_DEVICE_SERVICE, bridge);
		if (info != null) {
			args.putParcelable(ARFLIGHT_EXTRA_DEVICE_INFO, info);
		} else {
			args.remove(ARFLIGHT_EXTRA_DEVICE_INFO);
		}
		setArguments(args);
		return args;
	}

	/**
	 * コントローラーが破棄可能かを取得
	 * @return true: onPauseの際にISkyControllerでなければ破棄する
	 */
	protected boolean canReleaseController() {
		return !isReplacing();
	}

	protected ARDiscoveryDeviceService getDevice() {
		return mDevice;
	}

	protected DeviceInfo getDeviceInfo() {
		return mDeviceInfo;
	}

	protected int getProductId() {
		return mDeviceInfo != null ? mDeviceInfo.productId()
			: (mDevice != null
				? mDevice.getProductID()
				: ARDISCOVERY_PRODUCT_ENUM.eARDISCOVERY_PRODUCT_UNKNOWN_ENUM_VALUE.getValue());
	}

	protected ARDISCOVERY_PRODUCT_ENUM getProduct() {
		return ARDiscoveryService.getProductFromProductID(getProductId());
	}

	protected boolean isStarted() {
		return mController != null && mController.isStarted();
	}

	protected int getState() {
		return mController != null ? mController.getState() : IFlightController.STATE_STOPPED;
	}

	protected int getAlarm() {
		return mController != null ? mController.getAlarm() : DroneStatus.ALARM_DISCONNECTED;
	}

	protected IDeviceController getController() {
		if (mController == null) {
			final IDeviceController controller = ManagerFragment.getController(getActivity(), mDevice);
			if (BuildConfig.USE_SKYCONTROLLER) {
				if ((mDeviceInfo != null) && (controller instanceof ISkyController)) {
					if (DEBUG) Log.d(TAG, "ブリッジ接続");
					// スカイコントローラー経由のブリッジ接続の時
					final ISkyController bridge = (ISkyController)controller;
					final ARDISCOVERY_PRODUCT_ENUM product
						= ARDiscoveryService.getProductFromProductID(mDeviceInfo.productId());
					switch (product) {
					case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
						bridge.connectToDevice(mDeviceInfo);
						mController = controller;
						break;
					case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
						bridge.connectToDevice(mDeviceInfo);
						mController = controller;
						break;
					}
				}
			}

			// 直接機体に接続している時かブリッジ接続できなかった時
			if (mController == null) {
				mController = controller;
			}
		}
		if (DEBUG) Log.v(TAG, "getController:終了" + mController);
		return mController;
	}

	/** デバイスへ接続開始, 既に接続していればonConnectを呼び出すだけ */
	protected synchronized boolean startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		boolean result = false;
		getController();
		if (mController != null) {
			final int state = getState();
			if ((state != IFlightController.STATE_STARTED)
				&& (state != IFlightController.STATE_STARTING)) {
				if (DEBUG) Log.v(TAG, "未接続");
				updateBattery(mController, -1);

				ManagerFragment.startController(getActivity(), mController, new ManagerFragment.StartControllerListener() {
					@Override
					public void onResult(final IDeviceController controller, final boolean success) {
						if (!success) {
							if (DEBUG) Log.w(TAG, "DeviceControllerを開始できなかった");
							try {
								popBackStack();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						}
					}
				});
			} else {
				if (DEBUG) Log.v(TAG, "既にstartしている");
//				mController.requestAllSettings();
//				mController.requestAllStates();
				// sendAllSettingsとかsendAllStatesは接続した直後に1回しか有効じゃないのかも
//				updateBattery();
				result = true;
				onConnect(mController);
			}
		} else {
			throw new RuntimeException("IDeviceControllerを取得できなかった");
		}
		return result;
	}

	/** デバイスとの接続解除&開放  */
	protected synchronized void releaseDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "releaseDeviceController:");
		final int state = getState();
		final IDeviceController controller = mController;
		mController = null;
		if ((state == IFlightController.STATE_STARTED)
			|| (state == IFlightController.STATE_STARTING)) {

			ManagerFragment.releaseController(getActivity(), controller);
		}
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 * @param controller
	 * @param percent
	 */
	protected abstract void updateBattery(final IDeviceController controller, final int percent);

	/**
	 * WiFi信号強度が変化した時のコールバック
	 * @param controller
	 * @param rssi
	 */
	protected abstract void updateWiFiSignal(final IDeviceController controller, final int rssi);

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param controller
	 * @param alert_state
	 */
	protected abstract void updateAlarmState(final IDeviceController controller, final int alert_state);

	/**
	 * 接続された
	 * @param controller
	 */
	protected abstract void onConnect(final IDeviceController controller);

	/**
	 * 切断された
	 * @param controller
	 */
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		releaseDeviceController(true);
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	protected void updateSkyControllerBattery(final IDeviceController controller, final int percent) {
		if (DEBUG) Log.v(TAG, "updateSkyControllerBattery:controller=" + controller);
	}

	/**
	 * 接続された
	 * @param controller
	 */
	protected void onSkyControllerConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onConnectSkyController:");
	}

	/**
	 * 切断された
	 * @param controller
	 */
	protected void onSkyControllerDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onSkyControllerDisconnect:controller=" + controller);
		releaseDeviceController(true);
	}

	/**
	 * スカイコントローラーのアラーム状態が変化した時のコールバック
	 * @param alert_state
	 */
	protected void updateSkyControllerAlarmState(final IDeviceController controller, int alert_state) {
		if (DEBUG) Log.v(TAG, "updateSkyControllerAlarmState:controller=" + controller);
	}

}
