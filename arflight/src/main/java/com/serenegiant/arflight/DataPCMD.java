package com.serenegiant.arflight;

public class DataPCMD {
	public int flag;
	public float roll;
	public float pitch;
	public float yaw;
	public float gaz;
	public float heading;
	public boolean requestSend;

	public DataPCMD() {
		flag = 0;
		roll = pitch = yaw = gaz = heading = 0;
	}

	public void set(final DataPCMD other) {
		flag = other.flag;
		roll = other.roll;
		pitch = other.pitch;
		yaw = other.yaw;
		gaz = other.gaz;
		heading = other.heading;
	}
}
