package com.serenegiant.arflight;

import android.os.Parcel;
import android.os.Parcelable;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM.*;

public class DeviceInfo implements Parcelable {
	public static final int CONNECT_STATE_DISCONNECT = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_NOTCONNECTED.getValue();	// 0
	public static final int CONNECT_STATE_CONNECTING = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTING.getValue();		// 1
	public static final int CONNECT_STATE_CONNECTED = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTED.getValue();		// 2
	public static final int CONNECT_STATE_DISCONNECTing = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_DISCONNECTING.getValue();// 3

	public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
		@Override
		public DeviceInfo createFromParcel(final Parcel in) {
			return new DeviceInfo(in);
		}

		@Override
		public DeviceInfo[] newArray(final int size) {
			return new DeviceInfo[size];
		}
	};

	private final Object mSync = new Object();
	private final String mName;
	private final int mProductId;
	private int connectionState;

	public DeviceInfo(final String name, final int product_id) {
		mName = name;
		mProductId = product_id;
		connectionState = CONNECT_STATE_DISCONNECT;
	}

	protected DeviceInfo(final Parcel in) {
		mName = in.readString();
		mProductId = in.readInt();
		connectionState = in.readInt();
	}

	public String name() {
		return mName;
	}

	public int productId() {
		return mProductId;
	}

	public void connectionState(final int connection_state) {
		synchronized (mSync) {
			if (connectionState != connection_state) {
				connectionState = connection_state;
			}
		}
	}

	public int connectionState() {
		synchronized (mSync) {
			return connectionState;
		}
	}

	public boolean isConnected() {
		synchronized (mSync) {
			return (connectionState == CONNECT_STATE_CONNECTING) || (connectionState == CONNECT_STATE_CONNECTED);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(mName);
		dest.writeInt(mProductId);
		dest.writeInt(connectionState);
	}
}
