package com.serenegiant.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.sql.Date;

/**
 * Created by saki on 15/10/31.
 */
public class SkyController implements IDeviceController {
	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getProductName() {
		return null;
	}

	@Override
	public int getProductId() {
		return 0;
	}

	@Override
	public String getSoftwareVersion() {
		return null;
	}

	@Override
	public String getHardwareVersion() {
		return null;
	}

	@Override
	public String getSerial() {
		return null;
	}

	@Override
	public ARDiscoveryDeviceService getDeviceService() {
		return null;
	}

	@Override
	public ARNetworkConfig getNetConfig() {
		return null;
	}

	@Override
	public int getBattery() {
		return 0;
	}

	@Override
	public void addListener(DeviceConnectionListener mListener) {

	}

	@Override
	public void removeListener(DeviceConnectionListener mListener) {

	}

	@Override
	public int getState() {
		return 0;
	}

	@Override
	public boolean start() {
		return false;
	}

	@Override
	public void cancelStart() {

	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isStarted() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean sendDate(Date currentDate) {
		return false;
	}

	@Override
	public boolean sendTime(Date currentDate) {
		return false;
	}

	@Override
	public boolean sendAllSettings() {
		return false;
	}

	@Override
	public boolean sendAllStates() {
		return false;
	}
}
