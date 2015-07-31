package com.serenegiant.flightdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.widget.MovableImageView;

import java.sql.Date;

public class PilotFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();
	private static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment PilotFragment.
	 */
	public static PilotFragment newInstance(final ARDiscoveryDeviceService service) {
		final PilotFragment fragment = new PilotFragment();
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

	public PilotFragment() {
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
		MovableImageView iv = (MovableImageView)rootView.findViewById(R.id.right_stick_image);
		iv.setResizable(false);
		iv.setMovable(false);

		mLeftStickPanel = rootView.findViewById(R.id.left_panel);
		mLeftStickPanel.setOnTouchListener(mOnTouchListener);
		iv = (MovableImageView)rootView.findViewById(R.id.left_stick_image);
		iv.setResizable(false);
		iv.setMovable(false);

		batteryLabel = (TextView)rootView.findViewById(R.id.batteryLabel);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mRightStickPanel.getWidth() != 0 && mRightStickPanel.getHeight() != 0) {
			mRightScaleX = 250f / (float) mRightStickPanel.getWidth();
			mRightScaleY = 250f / (float) mRightStickPanel.getHeight();
			mLeftScaleX = 250f / (float) mLeftStickPanel.getWidth();
			mLeftScaleY = 250f / (float) mLeftStickPanel.getHeight();
		} else {
			mRightScaleX = mRightScaleY = mLeftScaleX = mLeftScaleY = 0;
		}
		if (DEBUG) Log.w(TAG, String.format("scale:left(%f,%f)right(%f,%f)", mRightScaleX, mRightScaleY, mLeftScaleX, mLeftScaleY));
		if (DEBUG) Log.w(TAG, String.format("mRightStickPanel:(%d,%d)mLeftStickPanel(%d,%d)",
			mRightStickPanel.getWidth(), mRightStickPanel.getHeight(),
			mLeftStickPanel.getWidth(), mLeftStickPanel.getHeight()));
		final Rect r = new Rect();
		mRightStickPanel.getDrawingRect(r);
		if (DEBUG) Log.w(TAG, "mRightStickPanel:" + r);
		mLeftStickPanel.getDrawingRect(r);
		if (DEBUG) Log.w(TAG, "mLeftStickPanel:" + r);
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

	private static final int CTRL_STEP = 5;

	private float mFirstPtRightX, mFirstPtRightY;
	private int mPrevRightMX, mPrevRightMY;
	private final void doRightStick(final MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (DEBUG) Log.v(TAG, "doRightStick:ACTION_DOWN");
			if ((mRightScaleX == 0) || (mRightScaleY == 0)) {
				mRightScaleX = 250f / (float) mRightStickPanel.getWidth();
				mRightScaleY = 250f / (float) mRightStickPanel.getHeight();
			}

			mFirstPtRightX = event.getX();
			mFirstPtRightY = event.getY();
			mPrevRightMX = mPrevRightMY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			final float dx = event.getX() - mFirstPtRightX;
			final float dy = event.getY() - mFirstPtRightY;
//			if (DEBUG) Log.v(TAG, String.format("doRightStick:(%5.1f,%5.1f", dx, dy));

			int mx = (int) (dx * mRightScaleX);
			if (mx < -100) mx = -100;
			else if (mx > 100) mx = 100;
			mx = (mx / CTRL_STEP) * CTRL_STEP;
			if (mx != mPrevRightMX) {
				mPrevRightMX = mx;
				if (deviceController != null) {
					deviceController.setRoll((byte) mx);
					deviceController.setFlag((byte) (mx != 0 ? 1 : 0));
				}
			}
			int my = (int) (dy * mRightScaleY);
			if (my < -100) my = -100;
			else if (my > 100) my = 100;
			my = (my / CTRL_STEP) * CTRL_STEP;
			if (my != mPrevRightMY) {
				mPrevRightMY = my;
				if (deviceController != null) {
					deviceController.setPitch((byte) -my);
				}
			}
			if (DEBUG) Log.v(TAG, String.format("doRightStick:(%d,%d", mx, my));
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (DEBUG) Log.v(TAG, "doRightStick:ACTION_UP");
			if (deviceController != null) {
				// 左右移動量をクリア, 正:右, 負:左
				deviceController.setRoll((byte) 0);
				deviceController.setFlag((byte) 0);
				// 前後移動量をクリア, 正:前, 負:後
				deviceController.setPitch((byte) 0);
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
			if (DEBUG) Log.v(TAG, "doLeftStick:ACTION_DOWN");
			if ((mLeftScaleX == 0) || (mLeftScaleY == 0)) {
				mLeftScaleX = 250f / (float) mLeftStickPanel.getWidth();
				mLeftScaleY = 250f / (float) mLeftStickPanel.getHeight();
			}
			mFirstPtLeftX = event.getX();
			mFirstPtLeftY = event.getY();
			mPrevLeftMX = mPrevLeftMY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			final float dx = event.getX() - mFirstPtLeftX;
			final float dy = event.getY() - mFirstPtLeftY;
			if (DEBUG) Log.v(TAG, String.format("doLeftStick:(%5.1f,%5.1f", dx, dy));

			int mx = (int) (dx * mLeftScaleX);
			if (mx < -100) mx = -100;
			else if (mx > 100) mx = 100;
			if ((Math.abs(mx) < 50)) mx = 0;
			mx = (mx / CTRL_STEP) * CTRL_STEP;
			if (mx != mPrevLeftMX) {
				mPrevLeftMX = mx;
				if (deviceController != null) {
					deviceController.setYaw((byte) mx);
				}
			}
			int my = (int) (dy * mLeftScaleY);
			if (my < -100) my = -100;
			else if (my > 100) my = 100;
			my = (my / CTRL_STEP) * CTRL_STEP;
			if (my != mPrevLeftMY) {
				mPrevLeftMY = my;
				if (deviceController != null) {
					deviceController.setGaz((byte) -my);
				}
			}
			if (DEBUG) Log.v(TAG, String.format("doLeftStick:(%d,%d", mx, my));
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (DEBUG) Log.v(TAG, "doLeftStick:ACTION_DOWN");
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
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setTitle(R.string.connecting);
			dialog.setIndeterminate(true);
			dialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					final boolean failed = deviceController.start();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
						}
					});

					mIsConnected = !failed;
					if (failed) {
						try {
							getFragmentManager().popBackStack();
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
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
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setTitle(R.string.disconnecting);
			dialog.setIndeterminate(true);
			dialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					mIsConnected = mIsFlying = false;
					deviceController.stop();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
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
