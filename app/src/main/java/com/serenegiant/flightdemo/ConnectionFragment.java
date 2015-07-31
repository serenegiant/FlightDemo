package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.ArrayList;
import java.util.List;

public class ConnectionFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
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
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	private ListView listView;
	private List<ARDiscoveryDeviceService> deviceList;
	private String[] deviceNameList;

	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private ServiceConnection ardiscoveryServiceConnection;
	public IBinder discoveryServiceBinder;
	private BroadcastReceiver mDevicesListUpdatedReceiver;

	public ConnectionFragment() {
		// Required empty public constructor
	}

/*	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	} */

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
		initView(rootView);
		return rootView;
	}

/*	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
											 + " must implement OnFragmentInteractionListener");
		}
	} */

/*	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	} */

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "onResume ...");

		mDeviceListUpdatedReceiverDelegate.onServicesDevicesListUpdated();

		registerReceivers();

		initServices();

	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause ...");

		unregisterReceivers();
		closeServices();

		super.onPause();
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {
		startServices();
		initBroadcastReceiver();
		initServiceConnection();

		listView = (ListView)rootView.findViewById(R.id.list);
		final View empty_view = rootView.findViewById(R.id.empty_view);
		listView.setEmptyView(empty_view);

		deviceList = new ArrayList<ARDiscoveryDeviceService>();
		deviceNameList = new String[] {};

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
			android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);


		// Assign adapter to ListView
		listView.setAdapter(adapter);

		// ListView Item Click Listener
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// ListView Clicked item value
				final String itemValue = (String) listView.getItemAtPosition(position);

				final ARDiscoveryDeviceService service = deviceList.get(position);

/*				final Intent intent = new Intent(getActivity(), PilotingActivity.class);
				intent.putExtra(PilotingActivity.EXTRA_DEVICE_SERVICE, service);
				startActivity(intent);	// FIXME これはフラグメントに変える */

				final Fragment fragment = PilotFragment.newInstance(service);
				getFragmentManager().beginTransaction()
					.addToBackStack(null)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.container, fragment).commit();
			}

		});
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
			new Thread(new Runnable() {
				@Override
				public void run() {
					ardiscoveryService.stop();
					getActivity().getApplicationContext().unbindService(ardiscoveryServiceConnection);
					ardiscoveryServiceBound = false;
					discoveryServiceBinder = null;
					ardiscoveryService = null;
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
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
		localBroadcastMgr.registerReceiver(mDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));

	}

	private void unregisterReceivers() {
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
		localBroadcastMgr.unregisterReceiver(mDevicesListUpdatedReceiver);
	}

	private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDeviceListUpdatedReceiverDelegate
		= new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
		@Override
		public void onServicesDevicesListUpdated() {
			Log.d(TAG, "onServicesDevicesListUpdated ...");

			List<ARDiscoveryDeviceService> list;

			if (ardiscoveryService != null) {
				list = ardiscoveryService.getDeviceServicesArray();

				deviceList = new ArrayList<ARDiscoveryDeviceService>();
				final List<String> deviceNames = new ArrayList<String>();

				if (list != null) {
					for (ARDiscoveryDeviceService service : list) {
						Log.d(TAG, "service :  " + service);
						if (service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
							deviceList.add(service);
							deviceNames.add(service.getName());
						}
					}
				}

				deviceNameList = deviceNames.toArray(new String[deviceNames.size()]);

				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
																				 android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);

				// Assign adapter to ListView
				listView.setAdapter(adapter);
			}

		}
	};

}