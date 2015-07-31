package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public class PilotFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();
	private static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

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

	private AlertDialog alertDialog;

	private boolean mIsFlying;
	private boolean mIsConnected;
	// 画面座標値から移動量(±100)に変換するための係数
	private float mRightScaleX, mRightScaleY;
	private float mLeftScaleX, mLeftScaleY;

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
		super.onPause();
		stopDeviceController();
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.emergency_btn:
				if (deviceController != null) {
					// 非常停止指示
					deviceController.sendEmergency();
					mIsFlying = false;
				}
				break;
			case R.id.take_onoff_btn:
				if (deviceController != null) {
					mIsFlying = !mIsFlying;
					if (mIsFlying) {
						// 離陸指示
						deviceController.sendTakeoff();
					} else {
						// 着陸指示
						deviceController.sendLanding();
					}
				} else {
					mIsFlying = false;
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

	private final DeviceControllerListener mDeviceControllerListener = new DeviceControllerListener() {
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
				// 前後移動量をクリア, 正:前, 負:後
				deviceController.setPitch((byte) 0);
				deviceController.setFlag((byte) 0);
				// 左右移動量をクリア, 正:右, 負:左
				deviceController.setRoll((byte) 0);
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
			}
			break;
		}
	}

	private void startDeviceController() {
		if ((deviceController != null) && !mIsConnected) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える
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
						Date currentDate = new Date(System.currentTimeMillis());
						deviceController.sendDate(currentDate);
						deviceController.sendTime(currentDate);
					}
				}
			}).start();

		}
	}

	private void stopDeviceController() {
		if (deviceController != null) {
			// FIXME AlertDialogを使うのはあまり良くない ProgressDialogに変える
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle(R.string.disconnecting);
			alertDialog = alertDialogBuilder.create();
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
