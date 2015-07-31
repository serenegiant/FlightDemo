package com.serenegiant.flightdemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public class PilotNewFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotNewFragment.class.getSimpleName();
	private static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PilotFragment.
	 */
	public static PilotNewFragment newInstance(final ARDiscoveryDeviceService service) {
		final PilotNewFragment fragment = new PilotNewFragment();
		fragment.service = service;
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_DEVICE_SERVICE, service);
		fragment.setArguments(args);
		return fragment;
	}

	private final Handler mHandler = new Handler();
	private final long mUIThreadId = Thread.currentThread().getId();
	private DeviceController deviceController;
	private ARDiscoveryDeviceService service;

	private Button mEmergencyBtn;
	private Button mTakeOnOffBtn;
	private View mRightStickPanel;
	private View mLeftStickPanel;

	private TextView batteryLabel;

	private boolean mIsFlying;
	private boolean mIsConnected;
	// 画面座標値から移動量(±100)に変換するための係数
	private float mRightScaleX, mRightScaleY;
	private float mLeftScaleX, mLeftScaleY;
	private final FlightRecorder mFlightRecorder = new FlightRecorder();

	public PilotNewFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			service = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			deviceController = new DeviceController(getActivity(), service);
			deviceController.setListener(mDeviceControllerListener);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_pilot, container, false);
		mEmergencyBtn = (Button)rootView.findViewById(R.id.emergency_btn);
		mEmergencyBtn.setOnClickListener(mOnClickListener);
		mTakeOnOffBtn = (Button)rootView.findViewById(R.id.take_onoff_btn);
		mTakeOnOffBtn.setOnClickListener(mOnClickListener);

		mRightStickPanel = rootView.findViewById(R.id.right_panel);
		mRightStickPanel.setOnTouchListener(mOnTouchListener);

		mLeftStickPanel = rootView.findViewById(R.id.left_panel);
		mLeftStickPanel.setOnTouchListener(mOnTouchListener);

		batteryLabel = (TextView)rootView.findViewById(R.id.batteryLabel);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mRightScaleX = 250 / (float)mRightStickPanel.getWidth();
		mRightScaleY = 250 / (float)mRightStickPanel.getHeight();
		mLeftScaleX = 250 / (float)mLeftStickPanel.getWidth();
		mLeftScaleY = 250 / (float)mLeftStickPanel.getHeight();
		startDeviceController();
	}

	@Override
	public void onPause() {
		stopDeviceController();
		super.onPause();
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.emergency_btn:
				// 非常停止指示ボタンの処理
				stopMove();
				if (deviceController != null) {
					deviceController.sendEmergency();
					mIsFlying = false;
				}
				break;
			case R.id.take_onoff_btn:
				// 離陸指示/着陸指示ボタンの処理
				if (deviceController != null) {
					mIsFlying = !mIsFlying;
					if (mIsFlying) {
						// 離陸指示
						mFlightRecorder.record(FlightRecorder.CMD_TAKEOFF, 0);
						deviceController.sendTakeoff();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mTakeOnOffBtn.setText(R.string.button_text_landing);
							}
						});
					} else {
						// 着陸指示
						stopMove();
						mFlightRecorder.record(FlightRecorder.CMD_LANDING, 0);
						deviceController.sendLanding();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mTakeOnOffBtn.setText(R.string.button_text_takeoff);
							}
						});
					}
				} else {
					mFlightRecorder.record(FlightRecorder.CMD_LANDING, 0);
					mIsFlying = false;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mTakeOnOffBtn.setText(R.string.button_text_takeoff);
						}
					});
				}
				break;
			}
		}
	};

	private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(final View view, final MotionEvent event) {
			switch (view.getId()) {
			case R.id.right_panel:
				doRightStick(event);
				break;
			case R.id.left_panel:
				doLeftStick(event);
				break;
			default:
				return false;
			}
			return true;
		}
	};

	private final DeviceControllerListener mDeviceControllerListener
		= new DeviceControllerListener() {
		@Override
		public void onDisconnect() {
			stopDeviceController();
			mIsFlying = false;
		}

		@Override
		public void onUpdateBattery(final byte percent) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					batteryLabel.setText(String.format("%d%%", percent));
				}
			});
		}

		@Override
		public void onFlatTrimUpdate(boolean success) {
			if (DEBUG) Log.v(TAG, "onFlatTrimUpdate:success=" + success);
		}

		@Override
		public void onFlyingStateChangedUpdate(final int state) {
			if (DEBUG) Log.v(TAG, "onFlyingStateChangedUpdate:state=" + state);
			switch (state) {
			case 0: // Landed state
			case 1:	// Taking off state
			case 2:	// Hovering state
			case 3:	// Flying state
			case 4:	// Landing state
			case 5:	// Emergency state
			case 6: // Rolling state
				break;
			}
		}
	};

	private float mFirstPtRightX, mFirstPtRightY;
	private int mPrevRightMX, mPrevRightMY;
	private final void doRightStick(final MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mFirstPtRightX = event.getX();
			mFirstPtRightY = event.getY();
			mPrevRightMX = mPrevRightMY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			final float dx = event.getX() - mFirstPtRightX;
			int mx = (int) (dx * mRightScaleX);
			if (mx < -100) mx = -100;
			else if (mx > 100) mx = 100;
			if (mx != mPrevRightMX) {
				mPrevRightMX = mx;
				if (deviceController != null) {
					deviceController.setRoll((byte) mx);
					deviceController.setFlag((byte) (mx != 0 ? 1 : 0));
				}
			}
			final float dy = event.getY() - mFirstPtRightY;
			int my = (int) (dy * mRightScaleY);
			if (my < -50) my = 100;
			else if (my > 100) my = 100;
			if (my != mPrevRightMY) {
				mPrevRightMY = my;
				if (deviceController != null) {
					deviceController.setPitch((byte) -my);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (deviceController != null) {
				// 左右移動量をクリア, 正:右, 負:左
				deviceController.setRoll((byte) 0);
				deviceController.setFlag((byte) 0);
				// 前後移動量をクリア, 正:前, 負:後
				deviceController.setPitch((byte) 0);
				deviceController.setFlag((byte) 0);
			}
			break;
		}
	}

	private float mFirstPtLeftX, mFirstPtLeftY;
	private int mPrevLeftMX, mPrevLeftMY;
	private final void doLeftStick(final MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mFirstPtLeftX = event.getX();
			mFirstPtLeftY = event.getY();
			mPrevLeftMX = mPrevLeftMY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			final float dx = event.getX() - mFirstPtLeftX;
			int mx = (int) (dx * mLeftScaleX);
			if (mx < -100) mx = 100;
			else if (mx > 100) mx = 100;
			if ((Math.abs(mx) < 50)) mx = 0;
			if (mx != mPrevLeftMX) {
				mPrevLeftMX = mx;
				if (deviceController != null) {
					deviceController.setYaw((byte) mx);
				}
			}
			final float dy = event.getY() - mFirstPtLeftY;
			int my = (int) (dy * mLeftScaleY);
			if (my < -100) my = -100;
			else if (my > 100) my = 100;
			if (my != mPrevLeftMY) {
				mPrevLeftMY = my;
				if (deviceController != null) {
					deviceController.setPitch((byte) my);
					deviceController.setFlag((byte) (my != 0 ? 1 : 0));
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (deviceController != null) {
				// 上下移動量をクリア, 正:上, 負:下
				deviceController.setGaz((byte) 0);
				// 回転量をクリア, 正:右回り, 負:左回り
				deviceController.setYaw((byte) 0);
				mFlightRecorder.record(FlightRecorder.CMD_UP, 0);
				mFlightRecorder.record(FlightRecorder.CMD_ROLL_RIGHT, 0);
			}
			break;
		}
	}

	private void startDeviceController() {
		if ((deviceController != null) && !mIsConnected) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える?
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle(R.string.connecting);
			final AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					final boolean failed = deviceController.start();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							alertDialog.dismiss();
						}
					});

					mIsConnected = !failed;
					if (failed) {
						getFragmentManager().popBackStack();
					} else {
						//only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
						final Date currentDate = new Date(System.currentTimeMillis());
						deviceController.sendDate(currentDate);
						deviceController.sendTime(currentDate);
					}
				}
			}).start();

		}
	}

	private void stopMove() {
		mFlightRecorder.record(FlightRecorder.CMD_RIGHT, 0);
		mFlightRecorder.record(FlightRecorder.CMD_FORWARD, 0);
		mFlightRecorder.record(FlightRecorder.CMD_UP, 0);
		mFlightRecorder.record(FlightRecorder.CMD_ROLL_RIGHT, 0);
		if (deviceController != null) {
			// 上下移動量をクリア, 正:上, 負:下
			deviceController.setGaz((byte) 0);
			// 回転量をクリア, 正:右回り, 負:左回り
			deviceController.setYaw((byte) 0);
			// 前後移動量をクリア, 正:前, 負:後
			deviceController.setPitch((byte) 0);
			deviceController.setFlag((byte) 0);
			// 左右移動量をクリア, 正:右, 負:左
			deviceController.setRoll((byte) 0);
			deviceController.setFlag((byte) 0);
		}
	}

	private void stopDeviceController() {
		stopMove();
		if (deviceController != null) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える?
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle(R.string.disconnecting);
			final AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					mIsConnected = mIsFlying = false;
					deviceController.stop();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							alertDialog.dismiss();
							getFragmentManager().popBackStack();
						}
					});
				}
			}).start();
		}
	}

	private void runOnUiThread(final Runnable task) {
		if (task != null) {
			if (mUIThreadId != Thread.currentThread().getId()) {
				mHandler.post(task);
			} else {
				task.run();
			}
		}
	}
}
