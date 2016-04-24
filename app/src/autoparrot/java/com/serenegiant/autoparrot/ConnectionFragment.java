package com.serenegiant.autoparrot;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.aceparrot.AutoPilotFragment2;
import com.serenegiant.aceparrot.AutoPilotFragment2NewAPI;
import com.serenegiant.aceparrot.BaseConnectionFragment;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.aceparrot.ConfigAppFragment;
import com.serenegiant.aceparrot.GalleyFragment;
import com.serenegiant.aceparrot.R;
import com.serenegiant.aceparrot.ScriptFragment;
import com.serenegiant.arflight.ARDeviceServiceAdapter;
import com.serenegiant.arflight.ManagerFragment;

public class ConnectionFragment extends BaseConnectionFragment {

	public static ConnectionFragment newInstance(final boolean newAPI) {
		ConnectionFragment fragment = new ConnectionFragment();
		final Bundle args = fragment.setNewAPI(newAPI);
		return fragment;
	}

	public ConnectionFragment() {
		super();
	}

	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			fragment = getFragment(position, true);
			break;
		case R.id.download_button:
			fragment = getFragment(position, false);
			break;
		case R.id.gallery_button:
			fragment = GalleyFragment.newInstance();
			break;
		case R.id.script_button:
			fragment = ScriptFragment.newInstance();
			break;
		case R.id.config_show_btn:
			fragment = ConfigAppFragment.newInstance();
			break;
		}
		replace(fragment);
	}

	protected boolean onLongClick(final View view, final int position) {
		mVibrator.vibrate(50);
		Fragment fragment = null;
		final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
		final String itemValue = adapter.getItemName(position);
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());
			final int id = view.getId();
			switch (id) {
			case R.id.pilot_button:
			case R.id.download_button:
			case R.id.gallery_button:
			case R.id.script_button:
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
					switch (id) {
					case R.id.pilot_button:
						fragment = AutoPilotFragment2.newInstance(device, "test001", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.download_button:
						fragment = AutoPilotFragment2.newInstance(device, "test002", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.gallery_button:
						fragment = AutoPilotFragment2.newInstance(device, "test003", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.script_button:
						fragment = AutoPilotFragment2.newInstance(device, "test004", AutoPilotFragment2.MODE_TRACKING, isNewAPI());
						break;
					}
					break;
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					switch (id) {
					case R.id.pilot_button:
						fragment = AutoPilotFragment2.newInstance(device, "test011", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.download_button:
						fragment = AutoPilotFragment2.newInstance(device, "test012", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.gallery_button:
						fragment = AutoPilotFragment2.newInstance(device, "test013", AutoPilotFragment2.MODE_TRACE, isNewAPI());
						break;
					case R.id.script_button:
						fragment = AutoPilotFragment2.newInstance(device, "test014", AutoPilotFragment2.MODE_TRACKING, isNewAPI());
						break;
					}
					break;
				}
				break;
			case R.id.config_show_btn:
				switch (product) {
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER:
					if (BuildConfig.USE_SKYCONTROLLER) {
						fragment = AutoPilotFragment2.newInstance(device, "test005", AutoPilotFragment2NewAPI.MODE_TRACE, isNewAPI());
					}
					break;
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
					fragment = AutoPilotFragment2.newInstance(device, "test006", AutoPilotFragment2NewAPI.MODE_TRACE, isNewAPI());
					break;
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					fragment = AutoPilotFragment2.newInstance(device, "test016", AutoPilotFragment2NewAPI.MODE_TRACE, isNewAPI());
					break;
				}
				break;
			}
		}
		if (fragment != null) {
			replace(fragment);
			return true;
		}
		return false;
	}
}
