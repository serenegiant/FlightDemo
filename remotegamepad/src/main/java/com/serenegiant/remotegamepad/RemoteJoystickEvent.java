package com.serenegiant.remotegamepad;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;

import java.io.Serializable;

/**
 * Created by saki on 2017/03/02.
 *
 */
public class RemoteJoystickEvent implements Parcelable, Serializable {

	public static final Creator<RemoteJoystickEvent> CREATOR = new Creator<RemoteJoystickEvent>() {
		@Override
		public RemoteJoystickEvent createFromParcel(Parcel in) {
			return new RemoteJoystickEvent(in);
		}

		@Override
		public RemoteJoystickEvent[] newArray(int size) {
			return new RemoteJoystickEvent[size];
		}
	};

	private final boolean[] downs = new boolean[GamePadConst.KEY_NUMS];
	private final long[] down_times = new long[GamePadConst.KEY_NUMS];
	private final int[] analog_sticks = new int[4];

	public RemoteJoystickEvent() {
	}

	public RemoteJoystickEvent(@NonNull final RemoteJoystickEvent other) {
		other.updateState(downs, down_times, analog_sticks, false);
	}

	protected RemoteJoystickEvent(final Parcel in) {
		in.readBooleanArray(downs);
		in.readLongArray(down_times);
		in.readIntArray(analog_sticks);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeBooleanArray(downs);
		dest.writeLongArray(down_times);
		dest.writeIntArray(analog_sticks);
	}

	public void set(@NonNull final RemoteJoystickEvent other) {
		other.updateState(downs, down_times, analog_sticks, false);
	}

	public void set(@NonNull final Joystick joystick) {
		joystick.updateState(downs, down_times, analog_sticks, false);
	}

	public void updateState(final boolean[] downs, final long[] down_times,
		final int[] analog_sticks, final boolean force) {

		int n = downs != null ? downs.length : 0;
		for (int i = 0, count = this.downs.length; (i < n) && (i < count); i++) {
			downs[i] = this.downs[i];
		}

		n = down_times != null ? down_times.length : 0;
		for (int i = 0, count = this.down_times.length; (i < n) && (i < count); i++) {
			down_times[i] = this.down_times[i];
		}

		n = analog_sticks != null ? analog_sticks.length : 0;
		for (int i = 0, count = this.analog_sticks.length; (i < n) && (i < count); i++) {
			analog_sticks[i] = this.analog_sticks[i];
		}
	}
}
