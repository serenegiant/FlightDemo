package com.serenegiant.gamepaddiag;

import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;

public abstract class BaseFragment extends Fragment {
	private static final boolean DEBUG = true;
	private static final String TAG = BaseFragment.class.getSimpleName();

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private Joystick mJoystick;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

//	@SuppressWarnings("deprecation")
//	@Override
//	public void onAttach(final Activity activity) {
//		super.onAttach(activity);
//	}

//	@Override
//	public void onCreate(final Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mJoystick = Joystick.getInstance(getActivity());
		mUIHandler.post(mKeyUpdateTask);
	}

	@Override
	public void onPause() {
		mUIHandler.removeCallbacks(mKeyUpdateTask);
		mJoystick = null;
		super.onPause();
	}

//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//	}

//	@Override
//	public void onDetach() {
//		super.onDetach();
//	}

	protected abstract void updateButtons(final boolean[] downs, final long[] counts, final int[] analog_sticks);

	private final boolean[] mDowns = new boolean[GamePadConst.KEY_NUMS];
	private final long[] mCounts = new long[GamePadConst.KEY_NUMS];
	private final int[] mAnalogSticks = new int[4];

	private final Runnable mKeyUpdateTask = new Runnable() {
		@Override
		public void run() {
			mUIHandler.removeCallbacks(this);
			final long current = System.currentTimeMillis();
			if (mJoystick != null) {
				mJoystick.updateState(mDowns, mCounts, mAnalogSticks, false);
			}
			updateButtons(mDowns, mCounts, mAnalogSticks);
			mUIHandler.postDelayed(this, 50);
		}
	};

}
