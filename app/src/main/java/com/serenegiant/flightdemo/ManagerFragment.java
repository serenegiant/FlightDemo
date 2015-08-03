package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.serenegiant.arflight.IDeviceController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "ManagerFragment";

	public interface ManagerCallback {
		public void onServicesDevicesListUpdated(List<ARDiscoveryDeviceService> devices);
	}

	public static ManagerFragment getInstance(Activity activity) {
		ManagerFragment result = null;
		if (activity != null) {
			final FragmentManager fm = activity.getFragmentManager();
			result = (ManagerFragment)fm.findFragmentByTag(TAG);
			if (result == null) {
				result = new ManagerFragment();
				fm.beginTransaction().add(result, TAG).commit();
			}
		}
		return result;
	}

	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private ServiceConnection ardiscoveryServiceConnection;
	private IBinder discoveryServiceBinder;
	private BroadcastReceiver mDevicesListUpdatedReceiver;
	private final List<ARDiscoveryDeviceService> mDevices = new ArrayList<ARDiscoveryDeviceService>();
	private final Map<String, IDeviceController> mControllers = new HashMap<String, IDeviceController>();

	private final Object mSync = new Object();
	private final List<ManagerCallback> mCallbacks = new ArrayList<ManagerCallback>();

	public ManagerFragment() {
		// デフォルトコンストラクタが必要
		setRetainInstance(true);	// Activityから切り離されても破棄されないようにする
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
		registerReceivers();
		initServices();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.i(TAG, "onPause:");
		unregisterReceivers();
		closeServices();
		super.onPause();
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.i(TAG, "onDetach:");
		super.onDetach();
	}

	public void addCallback(final ManagerCallback callback) {
		synchronized (mSync) {
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

	public void removeCallback(final ManagerCallback callback) {
		synchronized (mSync) {
			for (; mCallbacks.remove(callback) ;) {};
		}
	}

	public IDeviceController getDeviceController(final String name) {
		IDeviceController result = mControllers.get(name);
		if (result == null && !TextUtils.isEmpty(name)) {
			for (ARDiscoveryDeviceService device: mDevices) {
				if (name.equals(device.getName())) {
					// FIXME 未実装
				}
			}
		}
		return result;
	}

	public ARDiscoveryDeviceService getDevice(final String name) {
		ARDiscoveryDeviceService result = null;
		if (!TextUtils.isEmpty(name)) {
			for (ARDiscoveryDeviceService device : mDevices) {
				if (name.equals(device.getName())) {
					result = device;
					break;
				}
			}
		}
		return result;
	}

	private void startServices() {
		//startService(new Intent(this, ARDiscoveryService.class));
	}

	private void initServices() {
		if (discoveryServiceBinder == null) {
			final Context app = getActivity().getApplicationContext();
			final Intent intent = new Intent(app, ARDiscoveryService.class);
			app.bindService(intent, ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
		} else {
			ardiscoveryService = ((ARDiscoveryService.LocalBinder) discoveryServiceBinder).getService();
			ardiscoveryServiceBound = true;

			ardiscoveryService.start();
		}
	}

	private void closeServices() {
		Log.d(TAG, "closeServices ...");

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
					} catch (Exception e) {
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
			public void onServiceConnected(ComponentName name, IBinder service) {
				discoveryServiceBinder = service;
				ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
				ardiscoveryServiceBound = true;

				ardiscoveryService.start();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				ardiscoveryService = null;
				ardiscoveryServiceBound = false;
			}
		};
	}

	private void registerReceivers() {
		final LocalBroadcastManager localBroadcastMgr
			= LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
		localBroadcastMgr.registerReceiver(mDevicesListUpdatedReceiver,
			new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));

	}

	private void unregisterReceivers() {
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(
			getActivity().getApplicationContext());
		localBroadcastMgr.unregisterReceiver(mDevicesListUpdatedReceiver);
	}

	private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate
		mDeviceListUpdatedReceiverDelegate
		= new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
		@Override
		public void onServicesDevicesListUpdated() {
			Log.d(TAG, "onServicesDevicesListUpdated ...");

			if (ardiscoveryService != null) {
				final List<ARDiscoveryDeviceService> list = ardiscoveryService.getDeviceServicesArray();

				if (list != null) {
					synchronized (mSync) {
						mDevices.clear();
						mDevices.addAll(list);
					}
					callOnServicesDevicesListUpdated();
				}
			}
		}
	};

	private void callOnServicesDevicesListUpdated() {
		synchronized (mSync) {
			for (ManagerCallback cb: mCallbacks) {
				try {
					cb.onServicesDevicesListUpdated(mDevices);
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}
}
