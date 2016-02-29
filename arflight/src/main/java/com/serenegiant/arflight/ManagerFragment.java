package com.serenegiant.arflight;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.serenegiant.net.NetworkChangedReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerFragment extends Fragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "ManagerFragment";

	public interface ManagerCallback {
		public void onServicesDevicesListUpdated(final List<ARDiscoveryDeviceService> devices);
	}

	/**
	 * ManagerFragmentを取得する
	 * @param activity
	 * @return
	 */
	public static ManagerFragment getInstance(final Activity activity) {
		ManagerFragment result = null;
		if ((activity != null) && !activity.isFinishing()) {
			final FragmentManager fm = activity.getFragmentManager();
			result = (ManagerFragment)fm.findFragmentByTag(TAG);
			if (result == null) {
				result = new ManagerFragment();
				fm.beginTransaction().add(result, TAG).commit();
			}
		}
		return result;
	}

	/**
	 * 指定したインデックスのARDiscoveryDeviceServiceインスタンスを取得する
	 * @param activity
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static ARDiscoveryDeviceService getDevice(final Activity activity, final int index) {
		ARDiscoveryDeviceService result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getDevice(index);
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスを取得する
	 * @param activity
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static ARDiscoveryDeviceService getDevice(final Activity activity, final String name) {
		ARDiscoveryDeviceService result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getDevice(name);
		return result;
	}

	/**
	 * 指定したインデックスのARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static IDeviceController getController(final Activity activity, final int index) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(index);
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static IDeviceController getController(final Activity activity, final String name) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(name);
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param device
	 * @return
	 */
	public static IDeviceController getController(final Activity activity, final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(device);
		return result;
	}

	/**
	 * 指定したIDeviceControllerを取り除く(IFlightController#releaseとかは呼ばない)
	 * @param activity
	 * @param controller
	 */
	public static void releaseController(final Activity activity, final IDeviceController controller) {
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			fragment.releaseController(controller);
	}

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private NetworkChangedReceiver mNetworkChangedReceiver;
	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private boolean mRegistered = false;
	private ServiceConnection ardiscoveryServiceConnection;
	private IBinder discoveryServiceBinder;
	private BroadcastReceiver mDevicesListUpdatedReceiver;
	private final Object mDeviceSync = new Object();
	private final List<ARDiscoveryDeviceService> mDevices = new ArrayList<ARDiscoveryDeviceService>();
	private final Object mControllerSync = new Object();
	private final Map<String, IDeviceController> mControllers = new HashMap<String, IDeviceController>();

	private final List<ManagerCallback> mCallbacks = new ArrayList<ManagerCallback>();

	public ManagerFragment() {
		// デフォルトコンストラクタが必要
//		setRetainInstance(true);	// Activityから切り離されても破棄されないようにする
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.i(TAG, "onAttach:");
		startServices();
		initBroadcastReceiver();
		initServiceConnection();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.i(TAG, "onResume:");
		startDiscovery();
		mNetworkChangedReceiver = NetworkChangedReceiver.registerNetworkChangedReceiver(getActivity(), mOnNetworkChangedListener);
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.i(TAG, "onPause:");
		if (mNetworkChangedReceiver != null) {
			NetworkChangedReceiver.unregisterNetworkChangedReceiver(getActivity(), mNetworkChangedReceiver);
			mNetworkChangedReceiver = null;
		}
		stopDiscovery();
		super.onPause();
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.i(TAG, "onDetach:");
		super.onDetach();
	}

	public void startDiscovery() {
		mDeviceListUpdatedReceiverDelegate.onServicesDevicesListUpdated();
		registerReceivers();
		initServices();
	}

	public void stopDiscovery() {

		unregisterReceivers();
		closeServices();
	}

	/**
	 * コールバックを追加する
	 * @param callback
	 */
	public void addCallback(final ManagerCallback callback) {
		synchronized (mDeviceSync) {
			boolean found = false;
			for (ManagerCallback cb: mCallbacks) {
				if (cb.equals(callback)) {
					found = true;
					break;
				}
			}
			if (!found) {
				mCallbacks.add(callback);
			}
		}
		callOnServicesDevicesListUpdated();
	}

	/**
	 * コールバックを除去する
	 * @param callback
	 */
	public void removeCallback(final ManagerCallback callback) {
		synchronized (mDeviceSync) {
			for (; mCallbacks.remove(callback) ;) {};
		}
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final String name) {
		IDeviceController result;
		synchronized (mControllerSync) {
			result = mControllers.get(name);
		}
		if (result == null && !TextUtils.isEmpty(name)) {
			final ARDiscoveryDeviceService device = getDevice(name);
			if (device != null) {
				result = createController(device);
			}
		}
		return result;
	}

	/**
	 * 指定したindexのARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final int index) {
		IDeviceController result = null;
		final ARDiscoveryDeviceService device = getDevice(index);
		if (device != null) {
			synchronized (mControllerSync) {
				result = mControllers.get(device.getName());
			}
			if (result == null) {
				result = createController(device);
			}
		}
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param device
	 * @return
	 */
	public IDeviceController getController(final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		if (device != null) {
			synchronized (mControllerSync) {
				result = mControllers.get(device.getName());
			}
			if (result == null) {
				result = createController(device);
			}
		}
		return result;
	}


	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスを取得する
	 * @param name
	 * @return nameに一致するものがなければnull
	 */
	public ARDiscoveryDeviceService getDevice(final String name) {
		ARDiscoveryDeviceService result = null;
		if (!TextUtils.isEmpty(name)) {
			synchronized (mDeviceSync) {
				for (ARDiscoveryDeviceService device : mDevices) {
					if (name.equals(device.getName())) {
						result = device;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 指定したindexのARDiscoveryDeviceServiceインスタンスを取得する
	 * @param index
	 * @return indexが範囲外ならnull
	 */
	public ARDiscoveryDeviceService getDevice(final int index) {
		ARDiscoveryDeviceService device = null;
		synchronized (mDeviceSync) {
			if ((index >= 0) && (index < mDevices.size())) {
				device = mDevices.get(index);
			}
		}
		return device;
	}

	/**
	 * IDeviceControllerを生成する (FIXME JumpingSumoは未対応)
	 * @param device
	 * @return
	 */
	public IDeviceController createController(final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		if (device != null) {
			if (DEBUG) Log.v(TAG, "getProductID=" + device.getProductID());
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				result = new FlightControllerBebop(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
				result = new FlightControllerBebop2(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
				if (BuildConfig.USE_SKYCONTROLLER) {
					result = new SkyController(getActivity(), device);
				}
				break;
			case ARDISCOVERY_PRODUCT_NSNETSERVICE:
			case ARDISCOVERY_PRODUCT_JS:		// FIXME JumpingSumoは未対応
			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
				result = new FlightControllerMiniDrone(getActivity(), device);
				break;
			}
			if (result != null) {
				synchronized (mControllerSync) {
					mControllers.put(device.getName(), result);
				}
			}
		}
		return result;
	}

	/**
	 * 指定したIDeviceControllerをHashMapから取り除く
	 * @param controller
	 */
	public void releaseController(final IDeviceController controller) {
		synchronized (mControllerSync) {
			if (mControllers.containsValue(controller)) {
				mControllers.remove(controller.getName());
			}
		}
	}

	/**
	 * 指定したARDiscoveryDeviceServiceをListから取り除く
	 * @param device
	 */
	public void releaseDevice(final ARDiscoveryDeviceService device) {
		synchronized (mDeviceSync) {
			mDevices.remove(device);
		}
	}

	private void startServices() {
		//startService(new Intent(this, ARDiscoveryService.class));
	}

	private void initServices() {
		if (discoveryServiceBinder == null) {
			final Context app = getActivity().getApplicationContext();
			final Intent intent = new Intent(app, ARDiscoveryService.class);
			app.bindService(intent, ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
		} else if (!ardiscoveryServiceBound) {
			ardiscoveryService = ((ARDiscoveryService.LocalBinder) discoveryServiceBinder).getService();
			ardiscoveryServiceBound = true;

			ardiscoveryService.start();
		}
	}

	private void closeServices() {
		if (DEBUG) Log.d(TAG, "closeServices ...");

		if (ardiscoveryServiceBound) {
			ardiscoveryServiceBound = false;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ardiscoveryService.stop();
						getActivity().getApplicationContext().unbindService(ardiscoveryServiceConnection);
						discoveryServiceBinder = null;
						ardiscoveryService = null;
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}).start();
		}
	}

	private void initBroadcastReceiver() {
		mDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(mDeviceListUpdatedReceiverDelegate);
	}

	private void initServiceConnection() {
		ardiscoveryServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(final ComponentName name, final IBinder service) {
				discoveryServiceBinder = service;
				ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
				ardiscoveryServiceBound = true;

				ardiscoveryService.start();
			}

			@Override
			public void onServiceDisconnected(final ComponentName name) {
				ardiscoveryService = null;
				ardiscoveryServiceBound = false;
			}
		};
	}

	private void registerReceivers() {
		if (!mRegistered) {
			mRegistered = true;
			final LocalBroadcastManager localBroadcastMgr
				= LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
			localBroadcastMgr.registerReceiver(mDevicesListUpdatedReceiver,
				new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
		}
	}

	private void unregisterReceivers() {
		mRegistered = false;
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(
			getActivity().getApplicationContext());
		localBroadcastMgr.unregisterReceiver(mDevicesListUpdatedReceiver);
	}

	private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate
		mDeviceListUpdatedReceiverDelegate
		= new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
		@Override
		public void onServicesDevicesListUpdated() {
			if (DEBUG) Log.d(TAG, "onServicesDevicesListUpdated ...");

			if (ardiscoveryService != null) {
				final List<ARDiscoveryDeviceService> list = ardiscoveryService.getDeviceServicesArray();

				if (list != null) {
					synchronized (mDeviceSync) {
						mDevices.clear();
						mDevices.addAll(list);
					}
					callOnServicesDevicesListUpdated();
				}
			}
		}
	};

	private void callOnServicesDevicesListUpdated() {
		synchronized (mDeviceSync) {
			for (final ManagerCallback cb: mCallbacks) {
				try {
					cb.onServicesDevicesListUpdated(mDevices);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
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

	protected void stopController(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		if (controller.isStarted()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final ProgressDialog dialog = new ProgressDialog(getActivity());
					dialog.setTitle(R.string.disconnecting);
					dialog.setIndeterminate(true);
					dialog.show();

					new Thread(new Runnable() {
						@Override
						public void run() {
							controller.stop();
							releaseController(controller);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}).start();
				}
			});
		} else {
			releaseController(controller);
		}
	}

	private final DeviceConnectionListener mConnectionListener
		= new DeviceConnectionListener() {
		@Override
		public void onConnect(final IDeviceController controller) {
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			stopController(controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
		}
	};

	private final NetworkChangedReceiver.OnNetworkChangedListener mOnNetworkChangedListener
		= new NetworkChangedReceiver.OnNetworkChangedListener() {
		@Override
		public void onNetworkChanged(final int isConnectedOrConnecting, final int isConnected, final int activeNetworkFlag) {
			if (mRegistered && (ardiscoveryService != null)) {
				if (NetworkChangedReceiver.isWifiNetworkReachable()) {
					if (DEBUG) Log.v(TAG, "startWifiDiscovering");
					ardiscoveryService.startWifiDiscovering();
				} else {
					if (DEBUG) Log.v(TAG, "stopWifiDiscovering");
					ardiscoveryService.stopWifiDiscovering();
				}
			}
		}
	};
}
