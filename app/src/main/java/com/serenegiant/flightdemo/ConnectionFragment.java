package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import java.util.ArrayList;
import java.util.List;

public class ConnectionFragment extends Fragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ConnectionFragment.class.getSimpleName();

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PilotFragment.
	 */
	public static ConnectionFragment newInstance() {
		ConnectionFragment fragment = new ConnectionFragment();
		final Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	private ListView listView;

	public ConnectionFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		manager.addCallback(mManagerCallback);
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause:");

		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		manager.removeCallback(mManagerCallback);

		super.onPause();
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

		final List<String> deviceNames = new ArrayList<String>();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
			R.layout.list_item_1line, android.R.id.text1, deviceNames);

		listView = (ListView)rootView.findViewById(R.id.list);
		final View empty_view = rootView.findViewById(R.id.empty_view);
		listView.setEmptyView(empty_view);
		listView.setAdapter(adapter);

		// ListView Item Click Listener
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final ManagerFragment manager = ManagerFragment.getInstance(getActivity());

				final String itemValue = ((ArrayAdapter<String>)parent.getAdapter()).getItem(position);
				final ARDiscoveryDeviceService service = manager.getDevice(itemValue);
				// 製品名を取得
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());

				Fragment fragment = null;
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_JS:		// JumpingSumo
					break;
				case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
					fragment = PilotFragment.newInstance(service);
					break;
				}
				if (fragment != null) {
					getFragmentManager().beginTransaction()
						.addToBackStack(null)
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(R.id.container, fragment).commit();
				}
			}
		});
	}

	private ManagerFragment.ManagerCallback mManagerCallback = new ManagerFragment.ManagerCallback() {
		@Override
		public void onServicesDevicesListUpdated(List<ARDiscoveryDeviceService> devices) {
			final ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
			adapter.clear();
			for (ARDiscoveryDeviceService service : devices) {
				Log.d(TAG, "service :  " + service);
				// 今はローリングスパイダーだけを追加する
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_JS:		// JumpingSumo
					break;
				case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
					adapter.add(service.getName());
					break;
				}
/*				// ブルートゥース接続の時だけ追加する
				if (service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
					adapter.add(service.getName());
				} */
			}
			adapter.notifyDataSetChanged();
		}
	};

}