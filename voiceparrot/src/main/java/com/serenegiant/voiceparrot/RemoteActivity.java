package com.serenegiant.voiceparrot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;

import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.net.UdpBeacon;
import com.serenegiant.remotegamepad.RemoteJoystickSrv;
import com.serenegiant.utils.BuildCheck;

import java.util.UUID;

public class RemoteActivity extends Activity {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteActivity.class.getSimpleName();

	private UdpBeacon mUdpBeacon;
	private RemoteJoystickSrv mRemoteJoystickSrv;
	private ScrollView mScrollView;
	private TextView mMessageTv;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);

		mScrollView = (ScrollView)findViewById(R.id.scroll_view);
		mMessageTv = (TextView)findViewById(R.id.message_textview);

		NetworkChangedReceiver.enable(getApplicationContext());
		mUdpBeacon = new UdpBeacon(mUdpBeaconCallback);
		try {
			mRemoteJoystickSrv = new RemoteJoystickSrv(this, 9876, mRemoteJoystickSrvListener);
		} catch (final Exception e) {
			mRemoteJoystickSrv = null;
			Log.w(TAG, e);
		}
	}

	@Override
	protected final void onStart() {
		super.onStart();
		if (BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		if (!BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onPause() {
		if (!BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onPause();
	}

	@Override
	protected final void onStop() {
		if (BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		releaseJoystick();
		NetworkChangedReceiver.disable(getApplicationContext());
		if (mUdpBeacon != null) {
			mUdpBeacon.release();
			mUdpBeacon = null;
		}
		super.onDestroy();
	}

	protected void internalOnResume() {
		if (mUdpBeacon != null) {
			addMessage("start beacon");
			mUdpBeacon.start();
		}
	}

	protected void internalOnPause() {
		if (mUdpBeacon != null) {
			addMessage("stop beacon");
			mUdpBeacon.stop();
		}
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (mRemoteJoystickSrv != null) {
			if (mRemoteJoystickSrv.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		if (mRemoteJoystickSrv != null) {
			mRemoteJoystickSrv.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
	}

	private void releaseJoystick() {
		if (mRemoteJoystickSrv != null) {
			addMessage("release joystick");
			mRemoteJoystickSrv.release();
			mRemoteJoystickSrv = null;
		}
	}

	private final UdpBeacon.UdpBeaconCallback
		mUdpBeaconCallback = new UdpBeacon.UdpBeaconCallback() {
		@Override
		public void onReceiveBeacon(final UUID uuid, final String remote, final int remote_port) {
			addMessage(String.format("receive beacon from %s", remote));
		}

		@Override
		public void onError(final Exception e) {
			Log.w(TAG, e);
		}
	};

	private final RemoteJoystickSrv.RemoteJoystickSrvListener
		mRemoteJoystickSrvListener = new RemoteJoystickSrv.RemoteJoystickSrvListener() {
		@Override
		public void onConnect(final RemoteJoystickSrv srv, final String remote) {
			addMessage(String.format("%s connected", remote));
		}

		@Override
		public void onDisconnect(final RemoteJoystickSrv srv, final String remote) {
			addMessage(String.format("%s disconnected", remote));
		}

		@Override
		public void onError(final RemoteJoystickSrv srv, final Exception e) {
			addMessage("Exception:" + e.getMessage());
		}
	};

	private void addMessage(final String msg) {
		if (mMessageTv != null) {
			mMessageTv.post(new Runnable() {
				@Override
				public void run() {
					mMessageTv.append(msg + "\n");
					if (mScrollView != null) {
						mScrollView.scrollTo(0, mMessageTv.getBottom());
					}
				}
			});
		}
	}
}
