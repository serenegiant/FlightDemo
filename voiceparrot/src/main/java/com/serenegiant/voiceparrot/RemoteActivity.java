package com.serenegiant.voiceparrot;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.net.UdpBeacon;
import com.serenegiant.remotegamepad.RemoteJoystickSrv;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.widget.GamepadView;
import com.serenegiant.widget.KeyPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoteActivity extends Activity {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteActivity.class.getSimpleName();

	protected static List<KeyPosition> sPositions = new ArrayList<KeyPosition>();
	static {
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_2, 128, 62, 115, 120));		// 左上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_1, 128, 53, 75, 75));			// 左上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_CENTER, 191, 206, 64, 64));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_UP, 134, 86, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_RIGHT, 160, 114, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_DOWN, 134, 143, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_LEFT, 105, 114, 38, 38));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_2, 420, 62, 115, 120));		// 右上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_1, 420, 53, 75, 75));			// 右上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_CENTER, 359, 206, 64, 64));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_UP, 416, 80, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_RIGHT, 449, 114, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_DOWN, 415, 147, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_LEFT, 380, 114, 38, 38));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_LEFT, 217, 113, 32, 32));	// 中央左
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_RIGHT, 330, 113, 32, 32));	// 中央右
		//
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_A, 414, 88, 37, 37));			// アナログモードの時の右キーパッド上
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_B, 449, 123, 37, 37));		// アナログモードの時の右キーパッド右
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_C, 414, 156, 37, 37));		// アナログモードの時の右キーパッド下
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_D, 380, 123, 37, 37));		// アナログモードの時の右キーパッド左
	}

	private static final int[] sStickPos = new int[] {
		191, 206, 25,	// 左アナログスティック
		359, 206, 25,	// 右アナログスティック
	};

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private UdpBeacon mUdpBeacon;
	private RemoteJoystickSrv mRemoteJoystickSrv;
	private ScrollView mScrollView;
	private TextView mMessageTv;
	private GamepadView mGamepadView;
	private TextView mNameTv;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);

		mScrollView = (ScrollView)findViewById(R.id.scroll_view);
		mMessageTv = (TextView)findViewById(R.id.message_textview);
		mGamepadView = (GamepadView)findViewById(R.id.gamepad_view);
		mGamepadView.setKeys(sPositions);
		mGamepadView.setSticks(sStickPos);
		mNameTv = (TextView)findViewById(R.id.name_textview);

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
		mUIHandler.post(mKeyUpdateTask);
		mUIHandler.postDelayed(mUpdateNameTask, 0);
	}

	protected void internalOnPause() {
		mUIHandler.removeCallbacks(mUpdateNameTask);
		mUIHandler.removeCallbacks(mKeyUpdateTask);
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

	private void updateButtons(final boolean[] downs, final long[] counts, final int[] analog_sticks) {
		mGamepadView.setKeyState(downs, analog_sticks);
	}

	private String getGamepadName() {
		return mRemoteJoystickSrv != null ? mRemoteJoystickSrv.getName() : null;
	}

	private final boolean[] mDowns = new boolean[GamePadConst.KEY_NUMS];
	private final long[] mCounts = new long[GamePadConst.KEY_NUMS];
	private final int[] mAnalogSticks = new int[4];

	private final Runnable mKeyUpdateTask = new Runnable() {
		@Override
		public void run() {
			mUIHandler.removeCallbacks(this);
			final long current = System.currentTimeMillis();
			if (mRemoteJoystickSrv != null) {
				mRemoteJoystickSrv.updateState(mDowns, mCounts, mAnalogSticks, false);
			}
			updateButtons(mDowns, mCounts, mAnalogSticks);
			mUIHandler.postDelayed(this, 50);
		}
	};

	private final Runnable mUpdateNameTask = new Runnable() {
		@Override
		public void run() {
			mUIHandler.removeCallbacks(this);
			if (mNameTv != null) {
				final String name = getGamepadName();
				mNameTv.setText(name);
				mUIHandler.postDelayed(this, 1000);
			}
		}
	};
}
