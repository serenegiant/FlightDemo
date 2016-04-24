package com.serenegiant.autoparrot;

import android.app.Fragment;
import android.util.Log;
import android.view.View;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.aceparrot.BaseBridgeFragment;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.aceparrot.ConfigAppFragment;
import com.serenegiant.aceparrot.GalleyFragment;
import com.serenegiant.aceparrot.R;
import com.serenegiant.aceparrot.ScriptFragment;
import com.serenegiant.arflight.ARDeviceInfoAdapter;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.ManagerFragment;


/**
 * スカイコントローラーに接続してスカイコントローラーが
 * 検出している機体の一覧取得＆選択を行うためのFragment
 */
public class BridgeFragment extends BaseBridgeFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = BridgeFragment.class.getSimpleName();

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device, final boolean newAPI) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device, newAPI);
		return fragment;
	}

	public BridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
	}


	@Override
	protected void onClick(final View view) {
		if (DEBUG) Log.v(TAG, "onClick:");
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			fragment = getFragment(mDeviceListView.getCheckedItemPosition(), true);
			break;
		case R.id.download_button:
			fragment = getFragment(mDeviceListView.getCheckedItemPosition(), false);
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
		if (fragment != null) {
			replace(fragment);
		}
	}

	@Override
	protected boolean onLongClick(final View view) {
		if (DEBUG) Log.v(TAG, "onLongClick:");
		Fragment fragment = null;
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
		final int position = mDeviceListView.getCheckedItemPosition();
//			final String itemValue = adapter.getItemName(position);
		final DeviceInfo info = adapter.getItem(position);
		final ARDiscoveryDeviceService device = mController.getDeviceService();
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
			final int id = view.getId();
			switch (id) {
			case R.id.pilot_button:
			case R.id.download_button:
			case R.id.gallery_button:
			case R.id.script_button:
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					switch (id) {
					case R.id.pilot_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test001", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
						break;
					case R.id.download_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test002", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
						break;
					case R.id.gallery_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test003", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
						break;
					case R.id.script_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test004", AutoPilotFragment2.MODE_TRACKING, mController.isNewAPI());
						break;
					default:
						Log.w(TAG, "未知のview idが来た。なんでやねん:" + id);
						break;
					}
					break;
				default:
					Log.w(TAG, "未知の機体が来た:" + product);
					break;
				}
				break;
			default:
				Log.w(TAG, "未知のview idが来た:" + id);
				break;
			}
		} else {
			Log.w(TAG, "機体が取得できなかった:position=" + position);
		}
		if (fragment != null) {
			mIsConnectToDevice = mNeedRequestDeviceList = true;
			replace(fragment);
			return true;
		}
		return false;
	}

}
