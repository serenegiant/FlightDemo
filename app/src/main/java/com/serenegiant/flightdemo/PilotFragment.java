package com.serenegiant.flightdemo;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceControllerMiniDrone;
import com.serenegiant.arflight.FlightRecorder;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.dialog.SelectFileDialogFragment;
import com.serenegiant.utils.FileUtils;
import com.serenegiant.widget.StickView;
import com.serenegiant.widget.StickView.OnStickMoveListener;

import java.io.File;

public class PilotFragment extends ControlFragment implements SelectFileDialogFragment.OnFileSelectListener {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();

	static {
		FileUtils.DIR_NAME = "FlightDemo";
	}

	public static PilotFragment newInstance(final ARDiscoveryDeviceService device) {
		final PilotFragment fragment = new PilotFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private View mControllerView;	// 操作パネル
	// 上パネル
	private TextView mBatteryLabel;
	private ImageButton mFlatTrimBtn;	// フラットトリム
	private TextView mAlertMessage;
	// 下パネル
	private ImageButton mEmergencyBtn;	// 非常停止ボタン
	private ImageButton mTakeOnOffBtn;	// 離陸/着陸ボタン
	private ImageButton mRecordBtn;		// 記録ボタン
	private ImageButton mPlayBtn;		// 再生ボタン
	private ImageButton mLoadBtn;		// 読み込みボタン
	private ImageButton mConfigShowBtn;	// 設定パネル表示ボタン
	// 右サイドパネル
	private View mRightSidePanel;
	// 左サイドパネル
	private View mLeftSidePanel;
	// 右スティックパネル
	private StickView mRightStickPanel;
	// 左スティックパネル
	private StickView mLeftStickPanel;

	private final FlightRecorder mFlightRecorder = new FlightRecorder();

	public PilotFragment() {
		// デフォルトコンストラクタが必要
		mFlightRecorder.setPlaybackListener(mPlaybackListener);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_pilot_minidrone, container, false);

		mControllerView = rootView.findViewById(R.id.controller_frame);

		mEmergencyBtn = (ImageButton)rootView.findViewById(R.id.emergency_btn);
		mEmergencyBtn.setOnClickListener(mOnClickListener);

		mTakeOnOffBtn = (ImageButton)rootView.findViewById(R.id.take_onoff_btn);
		mTakeOnOffBtn.setOnClickListener(mOnClickListener);

		mRecordBtn = (ImageButton)rootView.findViewById(R.id.record_btn);
		mRecordBtn.setOnClickListener(mOnClickListener);
		mRecordBtn.setOnLongClickListener(mOnLongClickListener);

		mPlayBtn = (ImageButton)rootView.findViewById(R.id.play_btn);
		mPlayBtn.setOnClickListener(mOnClickListener);
		mPlayBtn.setOnLongClickListener(mOnLongClickListener);

		mLoadBtn = (ImageButton)rootView.findViewById(R.id.load_btn);
		mLoadBtn.setOnClickListener(mOnClickListener);
		mLoadBtn.setOnLongClickListener(mOnLongClickListener);

		mFlatTrimBtn = (ImageButton)rootView.findViewById(R.id.flat_trim_btn);
		mFlatTrimBtn.setOnLongClickListener(mOnLongClickListener);

		mConfigShowBtn = (ImageButton)rootView.findViewById(R.id.config_show_btn);
		mConfigShowBtn.setOnClickListener(mOnClickListener);

		ImageButton button;
		// 右サイドパネル
		mRightSidePanel = rootView.findViewById(R.id.right_side_panel);
		button = (ImageButton)rootView.findViewById(R.id.cap_p15_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.cap_p45_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.cap_m15_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.cap_m45_btn);
		button.setOnClickListener(mOnClickListener);
		// 左サイドパネル
		mLeftSidePanel = rootView.findViewById(R.id.left_side_panel);
		button = (ImageButton)rootView.findViewById(R.id.flip_right_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.flip_left_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.flip_front_btn);
		button.setOnClickListener(mOnClickListener);

		button = (ImageButton)rootView.findViewById(R.id.flip_back_btn);
		button.setOnClickListener(mOnClickListener);

		// 右スティックパネル
		mRightStickPanel = (StickView)rootView.findViewById(R.id.stick_view_right);
		mRightStickPanel.setOnStickMoveListener(mOnStickMoveListener);

		// 左スティックパネル
		mLeftStickPanel = (StickView)rootView.findViewById(R.id.stick_view_left);
		mLeftStickPanel.setOnStickMoveListener(mOnStickMoveListener);

		mBatteryLabel = (TextView)rootView.findViewById(R.id.batteryLabel);
		mAlertMessage = (TextView)rootView.findViewById(R.id.alert_message);
		mAlertMessage.setVisibility(View.INVISIBLE);

		return rootView;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		stopDeviceController(false);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		startDeviceController();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		stopRecord();
		super.onPause();
	}

	@Override
	public void onFileSelect(File[] files) {
		if (DEBUG) Log.v(TAG, "onFileSelect:");
		if ((files != null) && (files.length > 0)
			&& !mFlightRecorder.isPlaying() && !mFlightRecorder.isRecording() ) {
			mFlightRecorder.load(files[0]);
		}
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			if (DEBUG) Log.v(TAG, "onClick:" + view);
			switch (view.getId()) {
			case R.id.load_btn:
				// 読み込みボタンの処理
				final File root = FileUtils.getCaptureDir(getActivity(), "Documents", false);
				SelectFileDialogFragment.showDialog(PilotFragment.this, root.getAbsolutePath(), false, "fcr");
				break;
			case R.id.record_btn:
				// 記録ボタンの処理
				if (!mFlightRecorder.isRecording()) {
					startRecord(true);
				} else {
					stopRecord();
				}
				break;
			case R.id.play_btn:
				// 再生ボタンの処理
				stopMove();
				if (!mFlightRecorder.isPlaying()) {
					startPlay();
				} else {
					stopPlay();
				}
				break;
			case R.id.config_show_btn:
				// 設定パネル表示処理
				if (mIsConnected) {
					if (mFlyingState == 0) {
						final ConfigFragment fragment = ConfigFragment.newInstance(getDevice());
						getFragmentManager().beginTransaction()
							.addToBackStack(null)
							.replace(R.id.container, fragment)
							.commit();
					} else {
						landing();
					}
				}
				break;
			case R.id.emergency_btn:
				// 非常停止指示ボタンの処理
				emergencyStop();
				break;
			case R.id.take_onoff_btn:
				// 離陸指示/着陸指示ボタンの処理
				mIsFlying = !mIsFlying;
				if (mIsFlying) {
					takeOff();
				} else {
					landing();
				}
				updateButtons();
				break;
			case R.id.flip_front_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsFlip(DeviceControllerMiniDrone.FLIP_FRONT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, DeviceControllerMiniDrone.FLIP_FRONT);
				}
				break;
			case R.id.flip_back_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsFlip(DeviceControllerMiniDrone.FLIP_BACK);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, DeviceControllerMiniDrone.FLIP_BACK);
				}
				break;
			case R.id.flip_right_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsFlip(DeviceControllerMiniDrone.FLIP_RIGHT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, DeviceControllerMiniDrone.FLIP_RIGHT);
				}
				break;
			case R.id.flip_left_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsFlip(DeviceControllerMiniDrone.FLIP_LEFT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, DeviceControllerMiniDrone.FLIP_LEFT);
				}
				break;
			case R.id.cap_p15_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsCap(15);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, 15);
				}
				break;
			case R.id.cap_p45_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsCap(45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, 45);
				}
				break;
			case R.id.cap_m15_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsCap(-15);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, -15);
				}
				break;
			case R.id.cap_m45_btn:
				if (deviceController != null) {
					((DeviceControllerMiniDrone)deviceController).sendAnimationsCap(-45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, -45);
				}
				break;
/*			case R.id.north_btn:
				if (deviceController != null) {
					deviceController.setHeading(0);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 0);
				}
				break;
			case R.id.south_btn:
				if (deviceController != null) {
					deviceController.setHeading(180);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 180);
				}
				break;
			case R.id.west_btn:
				if (deviceController != null) {
					deviceController.setHeading(-90);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, -90);
				}
				break;
			case R.id.east_btn:
				if (deviceController != null) {
					deviceController.setHeading(90);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 90);
				}
				break; */
			}
		}
	};

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			if (DEBUG) Log.v(TAG, "onLongClick:" + view);
			switch (view.getId()) {
			case R.id.record_btn:
				if (!mFlightRecorder.isRecording()) {
					startRecord(false);
				} else {
					stopRecord();
				}
				return true;
			case R.id.flat_trim_btn:
				if ((deviceController != null) && (mFlyingState == 0)) {
					deviceController.sendFlatTrim();
					return true;
				}
				break;
			}
			return false;
		}
	};

	private static final int CTRL_STEP = 5;
	private float mFirstPtRightX, mFirstPtRightY;
	private int mPrevRightMX, mPrevRightMY;
	private float mFirstPtLeftX, mFirstPtLeftY;
	private int mPrevLeftMX, mPrevLeftMY;
	private final OnStickMoveListener mOnStickMoveListener = new OnStickMoveListener() {
		@Override
		public void onStickMove(final View view, final float dx, final float dy) {
			int mx = (int) (dx * 100);
			if (mx < -100) mx = -100;
			else if (mx > 100) mx = 100;
			mx = (mx / CTRL_STEP) * CTRL_STEP;
			int my = (int) (dy * 100);
			if (my < -100) my = -100;
			else if (my > 100) my = 100;
			my = (my / CTRL_STEP) * CTRL_STEP;
			switch (view.getId()) {
			case R.id.stick_view_right: {
				if (mx != mPrevRightMX) {
					mPrevRightMX = mx;
					if (deviceController != null) {
						deviceController.setRoll((byte) mx);
						deviceController.setFlag((byte) (mx != 0 ? 1 : 0));
						mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, mx);
					}
				}
				if (my != mPrevRightMY) {
					mPrevRightMY = my;
					if (deviceController != null) {
						deviceController.setPitch((byte) -my);
						mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, -my);
					}
				}
				break;
			}
			case R.id.stick_view_left: {
				if ((Math.abs(mx) < 20)) mx = 0;
				if (mx != mPrevLeftMX) {
					mPrevLeftMX = mx;
					if (deviceController != null) {
						deviceController.setYaw((byte) mx);
						mFlightRecorder.record(FlightRecorder.CMD_TURN, mx);
					}
				}
				if (my != mPrevLeftMY) {
					mPrevLeftMY = my;
					if (deviceController != null) {
						deviceController.setGaz((byte) -my);
						mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, -my);
					}
				}
				break;
			}
			}
		}
	};

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "mDeviceControllerListener#onDisconnect");
		stopRecord();
		super.onDisconnect(controller);
	}

	@Override
	protected void updateFlyingState(final int state) {
		updateButtons();
	}

	@Override
	protected void updateAlertState(final int alert_state) {
		runOnUiThread(mUpdateAlertMessageTask);
		updateButtons();
	}

	@Override
	protected void updateBattery(final int battery) {
		runOnUiThread(mUpdateBatteryTask);
	}

	/**
	 * 離陸指示
	 */
	private void takeOff() {
		// 離陸指示
		if (deviceController != null) {
			deviceController.sendTakeoff();
			mFlightRecorder.record(FlightRecorder.CMD_TAKEOFF);
		} else {
			mIsFlying = false;
		}
	}

	/**
	 * 着陸指示
	 */
	private void landing() {
		// 着陸指示
		stopMove();
		if (deviceController != null) {
			deviceController.sendLanding();
			mFlightRecorder.record(FlightRecorder.CMD_LANDING);
		}
		mIsFlying = false;
	}

	/**
	 * 移動停止
	 */
	private void stopMove() {
		if (DEBUG) Log.v(TAG, "stopMove:");
		if (deviceController != null) {
			// 上下移動量をクリア, 正:上, 負:下
			deviceController.setGaz((byte) 0);
			mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, 0);
			// 回転量をクリア, 正:右回り, 負:左回り
			deviceController.setYaw((byte) 0);
			mFlightRecorder.record(FlightRecorder.CMD_TURN, 0);
			// 前後移動量をクリア, 正:前, 負:後
			deviceController.setPitch((byte) 0);
			mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, 0);
			// 左右移動量をクリア, 正:右, 負:左
			deviceController.setRoll((byte) 0);
			mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, 0);
			deviceController.setFlag((byte) 0);	// pitch/roll移動フラグをクリア
		}
	}

	/**
	 * 記録開始
	 * @param needClear 既存の記録を破棄してから記録開始するかどうか
	 */
	private void startRecord(final boolean needClear) {
		if (DEBUG) Log.v(TAG, "startRecord:");
		if (!mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			if (needClear) {
				mFlightRecorder.clear();
			}
			mFlightRecorder.start();
			updateButtons();
		}
	}

	/**
	 * 記録終了
	 */
	private void stopRecord() {
		if (DEBUG) Log.v(TAG, "stopRecord:");
		if (mFlightRecorder.isRecording()) {
			mFlightRecorder.stop();
			// ファイルへ保存
			final String path = FileUtils.getCaptureFile(getActivity(), "Documents", ".fcr", false).getAbsolutePath();
			mFlightRecorder.save(path);
			updateButtons();
		}
	}

	/**
	 * 再生開始
	 */
	private void startPlay() {
		if (DEBUG) Log.v(TAG, "startPlay:");
		if (!mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying() && (mFlightRecorder.size() > 0)) {
			mFlightRecorder.pos(0);
			mFlightRecorder.play();
			updateButtons();
		}
	}

	/**
	 * 再生終了
	 */
	private void stopPlay() {
		if (DEBUG) Log.v(TAG, "stopPlay:");
		if (mFlightRecorder.isPlaying()) {
			mFlightRecorder.stop();
			updateButtons();
		}
	}

	/**
	 * 飛行記録再生時のコールバックリスナー
	 */
	private final FlightRecorder.PlaybackListener mPlaybackListener = new FlightRecorder.PlaybackListener() {
		@Override
		public void onStart() {
			if (DEBUG) Log.v(TAG, "mPlaybackListener#onStart:");
			updateButtons();
		}

		@Override
		public boolean onStep(final int cmd, final int value, final long t) {
			if (DEBUG) Log.v(TAG, String.format("mPlaybackListener#onStep:cmd=%d,v=%d,t=%d", cmd, value, t));
			if (deviceController != null) {
				switch (cmd) {
				case FlightRecorder.CMD_EMERGENCY:		// 非常停止
					deviceController.sendEmergency();
					break;
				case FlightRecorder.CMD_TAKEOFF:		// 離陸
					deviceController.sendTakeoff();
					break;
				case FlightRecorder.CMD_LANDING:		// 着陸
					deviceController.sendLanding();
					break;
				case FlightRecorder.CMD_UP_DOWN:		// 上昇:gaz>0, 下降: gaz<0
					deviceController.setGaz((byte) value);
					break;
				case FlightRecorder.CMD_RIGHT_LEFT:		// 右: roll>0,flag=1 左: roll<0,flag=1
					deviceController.setRoll((byte) value);
					deviceController.setFlag((byte) (value != 0 ? 1 : 0));
					break;
				case FlightRecorder.CMD_FORWARD_BACK:	// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
					deviceController.setPitch((byte) value);
					break;
				case FlightRecorder.CMD_TURN:			// 右回転: yaw>0, 左回転: ywa<0
					deviceController.setYaw((byte) value);
					break;
				case FlightRecorder.CMD_COMPASS:		// 北磁極に対する角度 -360〜360度
					deviceController.setHeading(value);		// 実際は浮動小数点だけど
					break;
				case FlightRecorder.CMD_FLIP:			// フリップ
					((DeviceControllerMiniDrone)deviceController).sendAnimationsFlip(value);
					break;
				case FlightRecorder.CMD_CAP:			// キャップ(指定角度水平回転)
					((DeviceControllerMiniDrone)deviceController).sendAnimationsCap(value);
					break;
				}
				return false;
			} else {
				return true;
			}
		}

		@Override
		public void onStop() {
			if (DEBUG) Log.v(TAG, "mPlaybackListener#onStop:");
			updateButtons();
		}
	};

	/**
	 * ボタン表示の更新(UIスレッドで処理)
	 */
	private void updateButtons() {
		runOnUiThread(mUpdateButtonsTask);
	}

	/**
	 * アラート表示の更新処理をUIスレッドで実行するためのRunnable
	 */
	private final Runnable mUpdateAlertMessageTask = new Runnable() {
		@Override
		public void run() {
			switch (mAlertState) {
			case 0:	// No alert
				break;
			case 1:	// User emergency alert
				mAlertMessage.setText(R.string.user_emergency);
				break;
			case 2:	// Cut out alert
				mAlertMessage.setText(R.string.motor_cut_out);
				break;
			case 3:	// Critical battery alert
				mAlertMessage.setText(R.string.low_battery_critical);
				break;
			case 4:	// Low battery alert
				mAlertMessage.setText(R.string.low_battery);
				break;
			}
			mAlertMessage.setVisibility(mAlertState != 0 ? View.INVISIBLE : View.VISIBLE);
		}
	};

	/**
	 * バッテリー残量表示の更新処理をUIスレッドでするためのRunnable
	 */
	private final Runnable mUpdateBatteryTask = new Runnable() {
		@Override
		public void run() {
			if (mBatteryState >= 0) {
				mBatteryLabel.setText(String.format("%d%%", mBatteryState));
			} else {
				mBatteryLabel.setText("---");
			}
		}
	};

	/**
	 *　ボタンの表示更新をUIスレッドで行うためのRunnable
	 */
	private final Runnable mUpdateButtonsTask = new Runnable() {
		@Override
		public void run() {
			final int state = mFlyingState;
			final int alert_state = mAlertState;
			final boolean is_connected = mIsConnected;
			final boolean is_recording = mFlightRecorder.isRecording();
			final boolean is_playing = mFlightRecorder.isPlaying();
			final boolean can_play = is_connected && !is_recording && (state != 5) && (mFlightRecorder.size() > 0);
			final boolean can_record = is_connected && !is_playing;
			final boolean can_load = is_connected && !is_playing && !is_recording;
			final boolean can_fly = can_record && (state != 5);
			final boolean can_flattrim = can_fly && (state == 0);
/*				switch (state) {
			case 0: // Landed state
			case 1:	// Taking off state
			case 2:	// Hovering state
			case 3:	// Flying state
			case 4:	// Landing state
			case 5:	// Emergency state
			case 6: // Rolling state
				break;
			} */
/*				switch (alert_state) {
			case 0:	// No alert
			case 1:	// User emergency alert
			case 2:	// Cut out alert
			case 3:	// Critical battery alert
			case 4:	// Low battery alert
				break;
			} */

			// 上パネル
			mFlatTrimBtn.setEnabled(can_flattrim);	// フラットトリム
			mBatteryLabel.setTextColor((alert_state == 3) || (alert_state == 4) ? 0xffff0000 : 0xff000000);
			// 下パネル
			mEmergencyBtn.setEnabled(is_connected);	// 非常停止
			mTakeOnOffBtn.setEnabled(can_fly);		// 離陸/着陸
			mLoadBtn.setEnabled(can_load);			// 読み込み
			mPlayBtn.setEnabled(can_play);			// 再生
			mRecordBtn.setEnabled(can_record);		// 記録
			if (is_recording) {
				mRecordBtn.setImageResource(R.drawable.rec);
			} else {
				mRecordBtn.setImageResource(R.drawable.rec);
			}
			if (mIsFlying || (state != 0)) {
//				mTakeOnOffBtn.setText(R.string.button_text_landing);
				mTakeOnOffBtn.setImageResource(R.drawable.landing72x72);
			} else {
//				mTakeOnOffBtn.setText(R.string.button_text_takeoff);
				mTakeOnOffBtn.setImageResource(R.drawable.takeoff72x72);
			}

			// 右サイドパネル(とmCapXXXBtn等)
			mRightSidePanel.setEnabled(can_fly);
			// 左サイドパネル(とmFlipXXXBtn等)
			mLeftSidePanel.setEnabled(can_fly);
			// 右スティックパネル(東/西ボタン)
			mRightStickPanel.setEnabled(can_fly);
			// 左スティックパネル(北/南ボタン)
			mLeftStickPanel.setEnabled(can_fly);

		}
	};

}
