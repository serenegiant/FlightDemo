package com.serenegiant.aceparrot;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.BuildConfig;
import com.serenegiant.arflight.R;

import java.util.List;

import jp.co.rediscovery.arflight.ARDeviceServiceAdapter;
import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.ManagerFragment;

/**
 * Created by saki on 2017/07/15.
 *
 */
public abstract class BaseConnectionFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = BaseConnectionFragment.class.getSimpleName();
	
	public BaseConnectionFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		loadArguments(savedInstanceState);
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		return internalCreateView(local_inflater, container, savedInstanceState, R.layout.fragment_connection);
	}

	protected abstract View internalCreateView(final LayoutInflater inflater,
		final ViewGroup container, final Bundle savedInstanceState, @LayoutRes final int layout_id);

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		if (DEBUG) Log.d(TAG, "internalOnResume:");
		runOnUiThread(mStartDiscoveryOnUITask);
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.d(TAG, "internalOnPause:");

		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		if (manager != null) {
			manager.removeCallback(mManagerCallback);
			manager.stopDiscovery();
		}
		updateButtons(false);
		super.internalOnPause();
	}

	@Override
	protected void onUpdateLocationPermission(final String permission, final boolean hasPermission) {
		if (hasPermission) {
			runOnUiThread(mStartDiscoveryOnUITask, 100);
		}
	}

	private final Runnable mStartDiscoveryOnUITask = new Runnable() {
		@Override
		public void run() {
			if (checkPermissionLocation()) {
				final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
				manager.startDiscovery();
				manager.addCallback(mManagerCallback);
			}
			updateButtons(false);
		}
	};

	protected void updateButtons(final boolean visible) {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateButtonsOnUiThread(visible);
				}
			});
		}
	}

	protected abstract void updateButtonsOnUiThread(final boolean visible);

	protected abstract ARDeviceServiceAdapter getDeviceAdapter();
	
	/**
	 * 検出したデバイスのリストが更新された時のコールバック
	 */
	private ManagerFragment.ManagerCallback mManagerCallback = new ManagerFragment.ManagerCallback() {
		@Override
		public void onServicesDevicesListUpdated(final List<ARDiscoveryDeviceService> devices) {
			if (DEBUG) Log.v(TAG, "onServicesDevicesListUpdated:");
			final ARDeviceServiceAdapter adapter = getDeviceAdapter();
			adapter.clear();
			for (final ARDiscoveryDeviceService service : devices) {
				if (DEBUG) Log.d(TAG, "service :  " + service);
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
				switch (product) {
//				case ARDISCOVERY_PRODUCT_NSNETSERVICE:			// WiFi products category
				case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
				case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// Sky controller product
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// Sky controller 2 product
					if (BuildConfig.USE_SKYCONTROLLER) {
						adapter.add(service);
					}
					break;
//				case ARDISCOVERY_PRODUCT_BLESERVICE:			// BlueTooth products category
				case ARDISCOVERY_PRODUCT_MINIDRONE:				// DELOS product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:	// Delos EVO Light product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:	// Delos EVO Brick product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL:// Delos EVO Hydrofoil product
				case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:		// Delos3 product
				case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:		// WingX product
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_JS:					// JUMPING SUMO product
				case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:			// Jumping Sumo EVO Light product
				case ARDISCOVERY_PRODUCT_JS_EVO_RACE:			// Jumping Sumo EVO Race product
					// FIXME JumpingSumoは未実装
					break;
//				case ARDISCOVERY_PRODUCT_POWER_UP:				// Power up product
//				case ARDISCOVERY_PRODUCT_EVINRUDE:				// Evinrude product
//				case ARDISCOVERY_PRODUCT_UNKNOWNPRODUCT_4:		// Unknownproduct_4 product
//				case ARDISCOVERY_PRODUCT_USBSERVICE:			// AOA/iAP usb product category
//				case ARDISCOVERY_PRODUCT_UNSUPPORTED_SERVICE:	// Service is unsupported:
//				case ARDISCOVERY_PRODUCT_TINOS:					// Tinos product
//				case ARDISCOVERY_PRODUCT_MAX:					// Max of products
				default:
					break;
				}
/*				// ブルートゥース接続の時だけ追加する
				if (service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
					adapter.add(service.getName());
				} */
			}
			adapter.notifyDataSetChanged();
			onDeviceListUpdated(adapter);
		}
	};

	protected abstract void onDeviceListUpdated(final ARDeviceServiceAdapter adapter);
//--------------------------------------------------------------------------------
	protected Fragment getFragment(final int position, final boolean isPiloting, final boolean isVoiceControl) {
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceServiceAdapter adapter = getDeviceAdapter();
		final String itemValue = adapter.getItemName(position);
		final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
		Fragment fragment = null;
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());

			switch (product) {
			case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
			case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
				fragment = isPiloting ? createPilotFragment(device, null) : createMediaFragment(device, null);
				break;
			case ARDISCOVERY_PRODUCT_JS:					// JUMPING SUMO product
			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:			// Jumping Sumo EVO Light product
			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:			// Jumping Sumo EVO Race product
				//FIXME JumpingSumoは未実装
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE:				// DELOS product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:	// Delos EVO Light product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:	// Delos EVO Brick product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL:// Delos EVO Hydrofoil product
			case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:		// Delos3 product
			case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:		// WingX product
				fragment = isPiloting ?
					(isVoiceControl ? createVoicePilotFragment(device, null)
						: createPilotFragment(device, null))
					: createMediaFragment(device, null);
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// Sky controller product
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// Sky controller 2 product
				if (BuildConfig.USE_SKYCONTROLLER) {
					fragment = newBridgetFragment(device);
				}
				break;
			}
		}
		return fragment;
	}

	protected abstract BaseBridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device);

	protected Fragment createPilotFragment(final ARDiscoveryDeviceService device, final DeviceInfo inf) {
		return PilotFragment.newInstance(device, null);
	}

	protected Fragment createVoicePilotFragment(final ARDiscoveryDeviceService device, final DeviceInfo inf) {
		return VoicePilotFragment.newInstance(device, null);
	}

	protected Fragment createMediaFragment(final ARDiscoveryDeviceService device, final DeviceInfo inf) {
		return MediaFragment.newInstance(device, null);
	}
}
