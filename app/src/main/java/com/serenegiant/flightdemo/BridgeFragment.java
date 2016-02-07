package com.serenegiant.flightdemo;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

/**
 * Created by saki on 16/02/07.
 */
public class BridgeFragment extends ControlBaseFragment {

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device);
		return fragment;
	}

	public BridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
	}


}
