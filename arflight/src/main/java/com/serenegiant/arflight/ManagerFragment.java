package com.serenegiant.arflight;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import com.serenegiant.arflight.controllers.FlightControllerBebop;
import com.serenegiant.arflight.controllers.FlightControllerBebop2;
import com.serenegiant.arflight.controllers.FlightControllerBebop2NewAPI;
import com.serenegiant.arflight.controllers.FlightControllerBebopNewAPI;
import com.serenegiant.arflight.controllers.FlightControllerMiniDrone;
import com.serenegiant.arflight.controllers.FlightControllerMiniDroneNewAPI;
import com.serenegiant.arflight.controllers.SkyController;
import com.serenegiant.arflight.controllers.SkyControllerNewAPI;
import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.utils.HandlerThreadHandler;

import java.lang.ref.WeakReference;
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

	public interface StartControllerListener {
		public void onResult(final IDeviceController controller, final boolean success);
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
	public static IDeviceController getController(final Activity activity, final int index, final boolean newAPI) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(index, newAPI);
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static IDeviceController getController(final Activity activity, final String name, final boolean newAPI) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(name, newAPI);
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param device
	 * @return
	 */
	public static IDeviceController getController(final Activity activity, final ARDiscoveryDeviceService device, final boolean newAPI) {
		IDeviceController result = null;
		final ManagerFragment fragment = getInstance(activity);
		if (fragment != null)
			result = fragment.getController(device, newAPI);
		return result;
	}

	public static IDeviceController startController(final Activity activity, final IDeviceController controller, final StartControllerListener listener) {
		if (controller != null) {
			final ManagerFragment fragment = getInstance(activity);
			if (fragment != null) {
				fragment.startController(controller, listener);
			} else {
				throw new RuntimeException("not attached to Activity");
			}
		}
		return controller;
	}

	/**
	 * 指定したIDeviceControllerを取り除く, IFlightController#releaseを呼んで破棄する
	 * @param activity
	 * @param controller
	 */
	public static void releaseController(final Activity activity, final IDeviceController controller) {
		if (controller == null) return;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null) {
			fragment.releaseController(controller);
		} else {
			if (DEBUG) Log.w(TAG, "no activity, try to release on private thread.");
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (controller != null) {
							controller.release();
						}
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}, TAG).start();
		}
	}

	/**
	 * 全てのARDiscoveryDeviceServiceとIDeviceControllerを取り除く
	 * @param activity
	 */
	public static void releaseAll(final Activity activity) {
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			fragment.releaseAll();
	}

	private final Object mSync = new Object();
	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();
	private Handler mAsyncHandler;

	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private boolean mRegistered = false;
	private IBinder discoveryServiceBinder;
	private final List<ARDiscoveryDeviceService> mDevices = new ArrayList<ARDiscoveryDeviceService>();
	private final Map<String, WeakReference<IDeviceController>> mControllers = new HashMap<String, WeakReference<IDeviceController>>();

	private final List<ManagerCallback> mCallbacks = new ArrayList<ManagerCallback>();

	public ManagerFragment() {
		// デフォルトコンストラクタが必要
//		setRetainInstance(true);	// Activityから切り離されても破棄されないようにする
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.i(TAG, "onAttach:");
		synchronized (mSync) {
			mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.i(TAG, "onResume:");
//		startDiscovery();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.i(TAG, "onPause:");
		stopDiscovery();
		super.onPause();
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.i(TAG, "onDetach:");
		releaseAll();
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				try {
					mAsyncHandler.getLooper().quit();
				} catch (final Exception e) {
					//
				}
				mAsyncHandler = null;
			}
		}
		super.onDetach();
	}

	public void startDiscovery() {
		if (DEBUG) Log.v(TAG, "startDiscovery:");
		mDeviceListUpdatedReceiverDelegate.onServicesDevicesListUpdated();
		bindServices();
		registerReceivers();
	}

	public void stopDiscovery() {
		if (DEBUG) Log.v(TAG, "stopDiscovery:");
		unregisterReceivers();
		unbindServices();
	}

	/**
	 * コールバックを追加する
	 * @param callback
	 */
	public void addCallback(final ManagerCallback callback) {
		synchronized (mDevices) {
			boolean found = false;
			for (final ManagerCallback cb: mCallbacks) {
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
		synchronized (mDevices) {
			for (; mCallbacks.remove(callback) ;) {}
		}
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final String name, final boolean newAPI) {
		IDeviceController result = null;
		synchronized (mControllers) {
			final ARDiscoveryDeviceService device = getDevice(name);
			if (device != null) {
				result = internalGetController(name, newAPI);
				if (result == null) {
					if (device != null) {
						result = createController(device, newAPI);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 指定したindexのARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final int index, final boolean newAPI) {
		IDeviceController result = null;
		synchronized (mControllers) {
			final ARDiscoveryDeviceService device = getDevice(index);
			if (device != null) {
				result = internalGetController(device.getName(), newAPI);
				if (result == null) {
					result = createController(device, newAPI);
				}
			}
		}
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param device
	 * @return
	 */
	public IDeviceController getController(final ARDiscoveryDeviceService device, final boolean newAPI) {
		IDeviceController result = null;
		synchronized (mControllers) {
			if (device != null) {
				result = internalGetController(device.getName(), newAPI);
				if (result == null) {
					result = createController(device, newAPI);
				}
			}
		}
		return result;
	}

	private IDeviceController internalGetController(final String name, final boolean newAPI) {
		IDeviceController result = null;
		if (mControllers.containsKey(name)) {
			final WeakReference<IDeviceController> weak_controller = mControllers.get(name);
			result = weak_controller != null ? weak_controller.get() : null;
			if (result == null) {
				mControllers.remove(name);
			}
		}
		if ((result != null) && (result.isNewAPI() != newAPI)) {
			if (DEBUG) Log.i(TAG, "internalGetController:release,newAPI=" + newAPI);
			result.release();
			result = null;
			mControllers.remove(name);
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
			synchronized (mDevices) {
				for (final ARDiscoveryDeviceService device : mDevices) {
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
		synchronized (mDevices) {
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
	public IDeviceController createController(final ARDiscoveryDeviceService device, final boolean newAPI) {
		if (DEBUG) Log.i(TAG, "createController:" + device + ",ardiscoveryServiceBound=" + ardiscoveryServiceBound + ",newAPI=" + newAPI);
		IDeviceController result = null;
		if (device != null) {
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				if (newAPI) {
					result = new FlightControllerBebopNewAPI(getActivity(), device);
				} else {
					result = new FlightControllerBebop(getActivity(), device);
				}
				break;
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
				if (newAPI) {
					result = new FlightControllerBebop2NewAPI(getActivity(), device);
				} else {
					result = new FlightControllerBebop2(getActivity(), device);
				}
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
				if (BuildConfig.USE_SKYCONTROLLER) {
					if (newAPI) {
						result = new SkyControllerNewAPI(getActivity(), device);
					} else {
						result = new SkyController(getActivity(), device);
					}
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
				if (newAPI) {
					result = new FlightControllerMiniDroneNewAPI(getActivity(), device);
				} else {
					result = new FlightControllerMiniDrone(getActivity(), device);
				}
				break;
			}
			if (result != null) {
				synchronized (mControllers) {
					mControllers.put(device.getName(), new WeakReference<IDeviceController>(result));
				}
			}
		} else {
			Log.w(TAG, "deviceがnullやん");
		}
		if (DEBUG) Log.i(TAG, "createController:終了,result=" + result);
		return result;
	}

	public void startController(final IDeviceController controller, final StartControllerListener listener) {
		if (DEBUG) Log.i(TAG, "startController:" + controller);

		if (controller != null) {
			final Activity activity = getActivity();
			if (activity != null) {
				showProgress(R.string.connecting, true, new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(final DialogInterface dialog) {
						if (DEBUG) Log.w(TAG, "startController:ユーザーキャンセル");
							controller.cancelStart();
						}
					}
				);
			}

			queueEvent(new Runnable() {
				@Override
				public void run() {
					if (DEBUG) Log.v(TAG, "startController:接続開始");
					boolean failed = true;
					synchronized (mControllers) {
						if (mControllers.containsKey(controller.getName())) {
							try {
								if (DEBUG) Log.v(TAG, "startController:IDeviceController#start");
								failed = controller.start();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						} else {
							Log.w(TAG, "controller is already removed:" + controller);
						}
					}
					hideProgress();

					if (listener != null) {
						try {
							listener.onResult(controller, !failed);
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					}
					if (DEBUG) Log.v(TAG, "startController:接続完了");
				}
			});
		}
	}

	/**
	 * 指定したIDeviceControllerをHashMapから取り除く, IDeviceController#releaseを呼んで開放する
	 * @param controller
	 */
	public void releaseController(final IDeviceController controller) {
		if (DEBUG) Log.i(TAG, "releaseController:" + controller);
		if (controller != null) {
			synchronized (mControllers) {
				mControllers.remove(controller.getName());
			}
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing()) {
				showProgress(R.string.disconnecting, false, null);
			}

			queueEvent(new Runnable() {
				@Override
				public void run() {
					if (DEBUG) Log.v(TAG, "releaseController:終了中");
					try {
						controller.release();
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
					hideProgress();
					if (DEBUG) Log.v(TAG, "releaseController:終了");
				}
			});
		}
	}

	/**
	 * 指定したARDiscoveryDeviceServiceをListから取り除く
	 * @param device
	 */
	public void releaseDevice(final ARDiscoveryDeviceService device) {
		if (DEBUG) Log.i(TAG, "releaseDevice:" + device);
		synchronized (mDevices) {
			mDevices.remove(device);
		}
	}

	/**
	 * 全てのARDiscoveryDeviceServiceをListから取り除き
	 * 全てのIDeviceControllerをHashMapから取り除く
	 */
	public void releaseAll() {
		if (DEBUG) Log.i(TAG, "releaseAll:");
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mControllers) {
					for (final WeakReference<IDeviceController> weak_controller: mControllers.values()) {
						final IDeviceController controller = weak_controller != null ? weak_controller.get() : null;
						if (controller != null) {
							if (DEBUG) Log.i(TAG, "releaseAll:" + controller);
							controller.release();
						}
					}
					mControllers.clear();
				}
			}
		}).start();
		synchronized (mDevices) {
			mDevices.clear();
		}
	}

	private void bindServices() {
		if (DEBUG) Log.d(TAG, "bindServices ...:binder=" + discoveryServiceBinder);
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

	private void unbindServices() {
		if (DEBUG) Log.d(TAG, "unbindServices ...");

		if (ardiscoveryServiceBound) {
			ardiscoveryServiceBound = false;
			final Activity activity = getActivity();
			final Context app_context = activity != null ? activity.getApplicationContext() : null;
			queueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						ardiscoveryService.stop();
						app_context.unbindService(ardiscoveryServiceConnection);
						discoveryServiceBinder = null;
						ardiscoveryService = null;
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			});
		}
	}

	private final ServiceConnection ardiscoveryServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			discoveryServiceBinder = service;
			ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
			ardiscoveryServiceBound = true;
			ardiscoveryService.start();
		}
		@Override
		public void onServiceDisconnected(final ComponentName name) {
			discoveryServiceBinder = null;
			ardiscoveryService = null;
			ardiscoveryServiceBound = false;
		}
	};

	private NetworkChangedReceiver mNetworkChangedReceiver;
	private void registerReceivers() {
		if (DEBUG) Log.v(TAG, "registerReceivers:mRegistered=" + mRegistered);
		if (!mRegistered) {
			mRegistered = true;
			final LocalBroadcastManager localBroadcastMgr
				= LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
			localBroadcastMgr.registerReceiver(mDevicesListUpdatedReceiver,
				new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
			if (mNetworkChangedReceiver == null) {
				mNetworkChangedReceiver = NetworkChangedReceiver.registerNetworkChangedReceiver(getActivity(), mOnNetworkChangedListener);
			}
		}
	}

	private void unregisterReceivers() {
		if (DEBUG) Log.v(TAG, "unregisterReceivers:mRegistered=" + mRegistered);
		mRegistered = false;
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(
			getActivity().getApplicationContext());
		localBroadcastMgr.unregisterReceiver(mDevicesListUpdatedReceiver);
		if (mNetworkChangedReceiver != null) {
			NetworkChangedReceiver.unregisterNetworkChangedReceiver(getActivity(), mNetworkChangedReceiver);
			mNetworkChangedReceiver = null;
		}
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
					synchronized (mDevices) {
						mDevices.clear();
						mDevices.addAll(list);
					}
					callOnServicesDevicesListUpdated();
				}
			}
		}
	};

	private final BroadcastReceiver mDevicesListUpdatedReceiver
		= new ARDiscoveryServicesDevicesListUpdatedReceiver(mDeviceListUpdatedReceiverDelegate);

	private void callOnServicesDevicesListUpdated() {
		synchronized (mDevices) {
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

	protected void queueEvent(final Runnable task) {
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				mAsyncHandler.post(task);
			} else {
				throw new RuntimeException("mAsyncHandler already released");
			}
		}
	}

	protected void queueEvent(final Runnable task, final long delay) {
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				if (delay > 0) {
					mAsyncHandler.postDelayed(task, delay);
				} else {
					mAsyncHandler.post(task);
				}
			} else {
				throw new RuntimeException("mAsyncHandler already released");
			}
		}
	}

	protected void stopController(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "stopController:" + controller);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final boolean show_progress = controller.isStarted();
				final ProgressDialog dialog;
				if (show_progress) {
					dialog = new ProgressDialog(getActivity());
					dialog.setTitle(R.string.disconnecting);
					dialog.setIndeterminate(true);
					dialog.show();
				} else {
					dialog = null;
				}

				queueEvent(new Runnable() {
					@Override
					public void run() {
						try {
							controller.stop();
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
						if (dialog != null) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}
				});
			}
		});
	}

	private final DeviceConnectionListener mConnectionListener
		= new DeviceConnectionListener() {
		@Override
		public void onConnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onConnect:" + controller);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onDisconnect:" + controller);
			releaseController(controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
		}

		@Override
		public void onUpdateWiFiSignal(final IDeviceController controller, final int rssi) {
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

	private ProgressDialog mProgress;

	private synchronized void showProgress(final int title_resID, final boolean cancelable,
		final DialogInterface.OnCancelListener cancel_listener) {

		final Activity activity = getActivity();

		if ((activity != null) && !activity.isFinishing()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgress = ProgressDialog.show(activity, getString(title_resID), null, true, cancelable, cancel_listener);
				}
			});
		}
	}

	private synchronized void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}
		});
	}

}
