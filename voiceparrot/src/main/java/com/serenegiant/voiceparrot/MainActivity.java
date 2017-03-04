package com.serenegiant.voiceparrot;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.serenegiant.aceparrot.AbstractMainActivity;
import com.serenegiant.gamepad.IGamePad;
import com.serenegiant.gamepad.RemoteJoystick;
import com.serenegiant.net.UdpBeacon;

import java.util.UUID;

public class MainActivity extends AbstractMainActivity {
	// ActionBarActivityを継承するとPilotFragmentから戻る際にクラッシュする
	// Fragmentが切り替わらずに処理中にもかかわらずActivityが破棄されてしまう
	private static final boolean DEBUG = true;    // FIXME 実働時はfalseにすること
	private static String TAG = MainActivity.class.getSimpleName();

	private UdpBeacon mUdpBeacon;
	private RemoteJoystick mRemoteJoystick;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VoiceFeedbackSettings.init();
		mUdpBeacon = new UdpBeacon(mUdpBeaconCallback);
		mUdpBeacon.setReceiveOnly(true);
	}

	@Override
	protected void internalOnResume() {
		if (mUdpBeacon != null) {
			mUdpBeacon.start();
		}
		super.internalOnResume();
	}

	@Override
	protected void internalOnPause() {
		super.internalOnPause();
		if (mUdpBeacon != null) {
			mUdpBeacon.stop();
		}
	}

	@Override
	protected void onDestroy() {
		releaseJoystick();
		if (mUdpBeacon != null) {
			mUdpBeacon.release();
			mUdpBeacon = null;
		}
		super.onDestroy();
	}

	@Override
	protected Fragment createConnectionFragment() {
		return MyInstructionsFragment.newInstance();
	}

	@Override
	public IGamePad getRemoteJoystick() {
		return mRemoteJoystick;
	}

	private void releaseJoystick() {
		if (mRemoteJoystick != null) {
			mRemoteJoystick.release();
			mRemoteJoystick = null;
		}
	}

	private final UdpBeacon.UdpBeaconCallback
		mUdpBeaconCallback = new UdpBeacon.UdpBeaconCallback() {
		@Override
		public void onReceiveBeacon(final UUID uuid, final String remote, final int remote_port) {
			if (DEBUG) Log.v(TAG, String.format("onReceiveBeacon:%s:%d", remote, remote_port));
			if (mRemoteJoystick == null) {
				try {
					mRemoteJoystick = new RemoteJoystick(remote, 9876, mRemoteJoystickListener);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}

		@Override
		public void onError(final Exception e) {

		}
	};

	private final RemoteJoystick.RemoteJoystickListener
		mRemoteJoystickListener = new RemoteJoystick.RemoteJoystickListener() {
		@Override
		public void onConnect(final RemoteJoystick joystick) {
			if (DEBUG) Log.v(TAG, "onConnect:");
			if (mUdpBeacon != null) {
				mUdpBeacon.stop();
			}
		}

		@Override
		public void onDisconnect(final RemoteJoystick joystick) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			releaseJoystick();
			if (mUdpBeacon != null) {
				mUdpBeacon.start();
			}
		}

		@Override
		public void onUpdate(final RemoteJoystick joystick) {
			if (DEBUG) Log.v(TAG, "onUpdate:");
		}

		@Override
		public void onError(final RemoteJoystick joystick, final Exception e) {
			if (DEBUG) Log.v(TAG, "onError:");
			releaseJoystick();
		}
	};
}
