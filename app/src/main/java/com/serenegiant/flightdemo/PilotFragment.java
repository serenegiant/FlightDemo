package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.AutoFlightListener;
import com.serenegiant.arflight.FlightRecorder;
import com.serenegiant.arflight.GamePad;
import com.serenegiant.arflight.IAutoFlight;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.ScriptFlight;
import com.serenegiant.arflight.TouchFlight;
import com.serenegiant.arflight.VideoStream;
import com.serenegiant.dialog.SelectFileDialogFragment;
import com.serenegiant.math.Vector;
import com.serenegiant.utils.FileUtils;
import com.serenegiant.widget.SideMenuListView;
import com.serenegiant.widget.StickView;
import com.serenegiant.widget.StickView.OnStickMoveListener;
import com.serenegiant.widget.TouchPilotView;
import com.serenegiant.widget.gl.AttitudeView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PilotFragment extends ControlFragment implements SelectFileDialogFragment.OnFileSelectListener {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();

	static {
		FileUtils.DIR_NAME = "FlightDemo";
	}

	public static PilotFragment newInstance(final ARDiscoveryDeviceService device) {
		final PilotFragment fragment = new PilotFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private ViewGroup mControllerView;		// 操作パネル全体
	// 上パネル
	private View mTopPanel;
	private TextView mBatteryLabel;			// バッテリー残量表示
	private ImageButton mFlatTrimBtn;		// フラットトリム
	private TextView mAlertMessage;			// 非常メッセージ
	// 下パネル
	private View mBottomPanel;
	private ImageButton mEmergencyBtn;		// 非常停止ボタン
	private ImageButton mTakeOnOffBtn;		// 離陸/着陸ボタン
	private ImageButton mRecordBtn;			// 記録ボタン
	private TextView mRecordLabel;
	private ImageButton mPlayBtn;			// 再生ボタン
	private TextView mPlayLabel;
	private ImageButton mLoadBtn;			// 読み込みボタン
	private ImageButton mConfigShowBtn;		// 設定パネル表示ボタン
	private TextView mTimeLabelTv;
	private ImageButton mClearButton;		// クリアボタン(タッチ描画操縦)
	private ImageButton mMoveButton;		// 移動ボタン(タッチ描画操縦)
	// 右サイドパネル
	private View mRightSidePanel;
	// 左サイドパネル
	private View mLeftSidePanel;
	// 操縦用
	private StickView mRightStickPanel;		// 右スティックパネル
	private StickView mLeftStickPanel;		// 左スティックパネル
	private TouchPilotView mTouchPilotView;	// タッチ描画パネル
	// ビデオストリーミング用
	private TextureView mVideoTextureView;	// ビデオストリーミング表示用
	// サイドメニュー
	private SideMenuListView mSideMenuListView;
	/** 操縦に使用するボタン等の一括変更用。操作可・不可に応じてenable/disableを切り替える */
	private final List<View> mActionViews = new ArrayList<View>();
	/** 飛行記録 */
	private final FlightRecorder mFlightRecorder;
	/** スクリプト操縦 */
	private final ScriptFlight mScriptFlight;
	private boolean mScriptRunning;
	/** タッチ描画操縦 */
	private final TouchFlight mTouchFlight;
	private boolean mTouchMoveRunning;
	/** モデル表示 */
	private AttitudeView mModelView;

	private VideoStream mVideoStream;

	public PilotFragment() {
		super();
		// デフォルトコンストラクタが必要
		mFlightRecorder = new FlightRecorder(mAutoFlightListener);
		mScriptFlight = new ScriptFlight(mAutoFlightListener);
		mTouchFlight = new TouchFlight(mAutoFlightListener);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		final MainActivity activity = (MainActivity) getActivity();
		activity.setSideMenuEnable(false);
		activity.removeSideMenuView(mSideMenuListView);
		super.onDetach();
	}

	private double mMaxControlValue = 100.0;
	private double mScaleX, mScaleY, mScaleZ, mScaleR;
	private float mGamepadSensitivity = 1.0f;
	private float mGamepadScaleX, mGamepadScaleY, mGamepadScaleZ, mGamepadScaleR;
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final SharedPreferences pref = getActivity().getPreferences(0);
		final int operation_type = pref.getInt(ConfigFragment.KEY_OPERATION_TYPE, 0);
		mMaxControlValue = pref.getFloat(ConfigFragment.KEY_AUTOPILOT_MAX_CONTROL_VALUE, 100.0f);
		mScaleX = pref.getFloat(ConfigFragment.KEY_AUTOPILOT_SCALE_X, 1.0f);
		mScaleY = pref.getFloat(ConfigFragment.KEY_AUTOPILOT_SCALE_Y, 1.0f);
		mScaleZ = pref.getFloat(ConfigFragment.KEY_AUTOPILOT_SCALE_Z, 1.0f);
		mScaleR = pref.getFloat(ConfigFragment.KEY_AUTOPILOT_SCALE_R, 1.0f);
		mGamepadSensitivity = pref.getFloat(ConfigFragment.KEY_GAMEPAD_SENSITIVITY, 1.0f);
		mGamepadScaleX = pref.getFloat(ConfigFragment.KEY_GAMEPAD_SCALE_X, 1.0f);
		mGamepadScaleY = pref.getFloat(ConfigFragment.KEY_GAMEPAD_SCALE_Y, 1.0f);
		mGamepadScaleZ = pref.getFloat(ConfigFragment.KEY_GAMEPAD_SCALE_Z, 1.0f);
		mGamepadScaleR = pref.getFloat(ConfigFragment.KEY_GAMEPAD_SCALE_R, 1.0f);

		final ViewGroup rootView = (ViewGroup)inflater.inflate(operation_type == 1 ?
			R.layout.fragment_pilot_reverse
			: (operation_type == 2 ? R.layout.fragment_pilot_touch
			: R.layout.fragment_pilot),
			container, false);

//		rootView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//		rootView.setOnKeyListener(mOnKeyListener);
//		rootView.setFocusable(true);

		mControllerView = (ViewGroup)rootView.findViewById(R.id.controller_frame);
//		mControllerView.setFocusable(true);
//		mControllerView.requestFocus();

		mActionViews.clear();
		// 上パネル
		mTopPanel = rootView.findViewById(R.id.top_panel);
		mActionViews.add(mTopPanel);

		mFlatTrimBtn = (ImageButton)rootView.findViewById(R.id.flat_trim_btn);
		mFlatTrimBtn.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mFlatTrimBtn);

		mConfigShowBtn = (ImageButton)rootView.findViewById(R.id.config_show_btn);
		mConfigShowBtn.setOnClickListener(mOnClickListener);

		// 下パネル
		mBottomPanel = rootView.findViewById(R.id.bottom_panel);
		mEmergencyBtn = (ImageButton)rootView.findViewById(R.id.emergency_btn);
		mEmergencyBtn.setOnClickListener(mOnClickListener);

		mTakeOnOffBtn = (ImageButton)rootView.findViewById(R.id.take_onoff_btn);
		mTakeOnOffBtn.setOnClickListener(mOnClickListener);
		mActionViews.add(mTakeOnOffBtn);

		mRecordBtn = (ImageButton)rootView.findViewById(R.id.record_btn);
		mRecordBtn.setOnClickListener(mOnClickListener);
		mRecordBtn.setOnLongClickListener(mOnLongClickListener);

		mRecordLabel = (TextView)rootView.findViewById(R.id.record_label);

		mPlayBtn = (ImageButton)rootView.findViewById(R.id.play_btn);
		mPlayBtn.setOnClickListener(mOnClickListener);
		mPlayBtn.setOnLongClickListener(mOnLongClickListener);

		mPlayLabel = (TextView)rootView.findViewById(R.id.play_label);

		mLoadBtn = (ImageButton)rootView.findViewById(R.id.load_btn);
		mLoadBtn.setOnClickListener(mOnClickListener);
		mLoadBtn.setOnLongClickListener(mOnLongClickListener);

		mTimeLabelTv = (TextView)rootView.findViewById(R.id.time_label);
		mTimeLabelTv.setVisibility(View.INVISIBLE);

		// クリアボタン(タッチ描画操縦)
		mClearButton = (ImageButton)rootView.findViewById(R.id.clear_btn);
		if (mClearButton != null) {
			mClearButton.setOnClickListener(mOnClickListener);
		}
		// 移動ボタン(タッチ描画操縦)
		mMoveButton = (ImageButton)rootView.findViewById(R.id.move_btn);
		if (mMoveButton != null) {
			mMoveButton.setOnClickListener(mOnClickListener);
		}

		ImageButton button;
		// 右サイドパネル
		mRightSidePanel = rootView.findViewById(R.id.right_side_panel);
		mActionViews.add(mRightSidePanel);

		button = (ImageButton)rootView.findViewById(R.id.cap_p15_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.cap_p45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.cap_m15_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.cap_m45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		// 左サイドパネル
		mLeftSidePanel = rootView.findViewById(R.id.left_side_panel);
		mActionViews.add(mLeftSidePanel);

		button = (ImageButton)rootView.findViewById(R.id.flip_right_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.flip_left_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.flip_front_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton)rootView.findViewById(R.id.flip_back_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		// 右スティックパネル
		mRightStickPanel = (StickView)rootView.findViewById(R.id.stick_view_right);
		if (mRightStickPanel != null) {
			mRightStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		// 左スティックパネル
		mLeftStickPanel = (StickView)rootView.findViewById(R.id.stick_view_left);
		if (mLeftStickPanel != null) {
			mLeftStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		// タッチパイロットView(タッチ描画操縦)
		mTouchPilotView = (TouchPilotView)rootView.findViewById(R.id.touch_pilot_view);
		if (mTouchPilotView != null) {
			mTouchPilotView.setTouchPilotListener(mTouchPilotListener);
			mActionViews.add(mTouchPilotView);
		}

		// ビデオストリーミング用
		mVideoTextureView = (TextureView)rootView.findViewById(R.id.video_textureview);

		mBatteryLabel = (TextView)rootView.findViewById(R.id.batteryLabel);
		mAlertMessage = (TextView)rootView.findViewById(R.id.alert_message);
		mAlertMessage.setVisibility(View.INVISIBLE);

		// 機体モデル表示
		mModelView = (AttitudeView)rootView.findViewById(R.id.drone_view);
		return rootView;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		stopDeviceController(false);
		mFlightRecorder.release();
		mScriptFlight.release();
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
		stopVideoStreaming();
		stopRecord();
		stopPlay();
		stopScript();
		stopTouchMove();
		removeSideMenu();
		remove(mGamePadTask);
		remove(mUpdateStatusTask);
		removeFromUIThread(mPopBackStackTask);
		mResetColorFilterTasks.clear();
		super.onPause();
	}

	@Override
	public void onFileSelect(File[] files) {
		if (DEBUG) Log.v(TAG, "onFileSelect:");
		if ((files != null) && (files.length > 0)
			&& !mFlightRecorder.isPlaying() && !mFlightRecorder.isRecording() ) {
			mFlightRecorder.load(files[0]);
			updateButtons();
		}
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			if (DEBUG) Log.v(TAG, "onClick:" + view);
			switch (view.getId()) {
			case R.id.load_btn:
				// 読み込みボタンの処理
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
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
				updateButtons();
				break;
			case R.id.play_btn:
				// 再生ボタンの処理
				PilotFragment.super.stopMove();
				if (!mFlightRecorder.isPlaying()) {
					startPlay();
				} else {
					stopPlay();
				}
				break;
			case R.id.config_show_btn:
				// 設定パネル表示処理
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (isConnected()) {
					if ((mController.getState() & IDeviceController.STATE_MASK_FLYING) == DroneStatus.STATE_FLYING_LANDED) {
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
			case R.id.clear_btn:
				// タッチ描画データの消去
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mTouchPilotView != null) {
					mTouchPilotView.clear();
				}
				updateButtons();
				break;
			case R.id.move_btn:
				// タッチ描画で操縦開始
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				// 再生ボタンの処理
				PilotFragment.super.stopMove();
				if (!mTouchFlight.isPlaying()) {
					startTouchMove();
				} else {
					stopTouchMove();
				}
				break;
			case R.id.emergency_btn:
				// 非常停止指示ボタンの処理
				setColorFilter((ImageView) view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				emergencyStop();
				break;
			case R.id.take_onoff_btn:
				// 離陸指示/着陸指示ボタンの処理
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				mIsFlying = !mIsFlying;
				if (mIsFlying) {
					takeOff();
				} else {
					landing();
				}
				updateButtons();
				break;
			case R.id.flip_front_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_FRONT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IDeviceController.FLIP_FRONT);
				}
				break;
			case R.id.flip_back_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_BACK);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IDeviceController.FLIP_BACK);
				}
				break;
			case R.id.flip_right_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_RIGHT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IDeviceController.FLIP_RIGHT);
				}
				break;
			case R.id.flip_left_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_LEFT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IDeviceController.FLIP_LEFT);
				}
				break;
			case R.id.cap_p15_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsCap(15);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, 15);
				}
				break;
			case R.id.cap_p45_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsCap(45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, 45);
				}
				break;
			case R.id.cap_m15_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsCap(-15);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, -15);
				}
				break;
			case R.id.cap_m45_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.sendAnimationsCap(-45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, -45);
				}
				break;
/*			case R.id.north_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.setHeading(0);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 0);
				}
				break;
			case R.id.south_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.setHeading(180);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 180);
				}
				break;
			case R.id.west_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.setHeading(-90);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, -90);
				}
				break;
			case R.id.east_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mController != null) {
					mController.setHeading(90);
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
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if ((mController != null) && (mController.getState() == IDeviceController.STATE_STARTED)) {
					mController.sendFlatTrim();
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
				if ((mx != mPrevRightMX) || ((my != mPrevRightMY))) {
					mPrevRightMX = mx;
					mPrevRightMY = my;
					if (mController != null) {
						mController.setMove(mx, -my);
						mFlightRecorder.record(FlightRecorder.CMD_MOVE2, mx, -my);
					}
				}
				break;
			}
			case R.id.stick_view_left: {
				if ((Math.abs(mx) < 20)) mx = 0;
				if (mx != mPrevLeftMX) {
					mPrevLeftMX = mx;
					if (mController != null) {
						mController.setYaw(mx);
						mFlightRecorder.record(FlightRecorder.CMD_TURN, mx);
					}
				}
				if (my != mPrevLeftMY) {
					mPrevLeftMY = my;
					if (mController != null) {
						mController.setGaz(-my);
						mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, -my);
					}
				}
				break;
			}
			}
		}
	};

	private final TouchPilotView.TouchPilotListener mTouchPilotListener = new  TouchPilotView.TouchPilotListener() {
		@Override
		public void onDrawFinish(final TouchPilotView view,
			final float min_x, final float max_x,
			final float min_y, final float max_y,
			final float min_z, final float max_z,
			final int num_points, final float[] points) {

			if (DEBUG) Log.v(TAG, "onDrawFinish:" + num_points);
			mTouchFlight.prepare(view.getWidth(), view.getHeight(), min_x, max_x, min_y, max_y, min_z, max_z, num_points, points);
			updateButtons();
		}
	};

	@Override
	protected void startVideoStreaming() {
		if (DEBUG) Log.v(TAG, "startVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			if (mVideoStream == null) {
				mVideoStream = new VideoStream();
			}
			((IVideoStreamController)mController).setVideoStream(mVideoStream);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mVideoTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
					mVideoTextureView.setVisibility(View.VISIBLE);
				}
			});
		}
		super.startVideoStreaming();
	}

	@Override
	protected void stopVideoStreaming() {
		if (DEBUG) Log.v(TAG, "stopVideoStreaming:");
		super.stopVideoStreaming();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mVideoTextureView.setVisibility(View.GONE);
				mVideoTextureView.setSurfaceTextureListener(null);
			}
		});
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).setVideoStream(null);
		}
		if (mVideoStream != null) {
			mVideoStream.release();
			mVideoStream = null;
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onConnect");
		super.onConnect(controller);
		setSideMenu();
		startGamePadTask();
		post(mUpdateStatusTask, 100);
		updateButtons();
	}

	/** 切断された時に前のフラグメントに戻るまでの遅延時間[ミリ秒] */
	private static final long POP_BACK_STACK_DELAY = 2000;
	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onDisconnect");
		stopRecord();
		stopPlay();
		removeSideMenu();
		stopGamePadTask();
		remove(mUpdateStatusTask);
		removeFromUIThread(mPopBackStackTask);
		postUIThread(mPopBackStackTask, POP_BACK_STACK_DELAY);	// UIスレッド上で遅延実行
		super.onDisconnect(controller);
	}

	@Override
	protected void updateFlyingState(final int state) {
		updateButtons();
	}

	@Override
	protected void updateAlarmState(final int alert_state) {
		runOnUiThread(mUpdateAlarmMessageTask);
		updateButtons();
	}

	@Override
	protected void updateBattery() {
		runOnUiThread(mUpdateBatteryTask);
	}

	/**
	 * 移動停止
	 */
	@Override
	protected void stopMove() {
		if (DEBUG) Log.v(TAG, "stopMove:");
		super.stopMove();
		if (mController != null) {
			mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, 0);
			mFlightRecorder.record(FlightRecorder.CMD_TURN, 0);
			mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, 0);
			mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, 0);
		}
	}

	/**
	 * 非常停止
	 */
	@Override
	protected void emergencyStop() {
		super.emergencyStop();
		stopPlay();
		stopScript();
		stopTouchMove();
	}

	/**
	 * 離陸指示
	 */
	private void takeOff() {
		// 離陸指示
		if (mController != null) {
			mController.sendTakeoff();
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
		if (mController != null) {
			mController.sendLanding();
			mFlightRecorder.record(FlightRecorder.CMD_LANDING);
		}
		mIsFlying = false;
	}

	/**
	 * 記録開始
	 * @param needClear 既存の記録を破棄してから記録開始するかどうか
	 */
	private void startRecord(final boolean needClear) {
		if (DEBUG) Log.v(TAG, "startRecord:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			if (needClear) {
				mFlightRecorder.clear();
			}
			mFlightRecorder.start();
			updateTime(0);
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
			if (!TextUtils.isEmpty(path)) {
				mFlightRecorder.save(path);
				updateButtons();
			}
			updateTime(-1);
		}
	}

	/**
	 * 再生開始
	 */
	private void startPlay() {
		if (DEBUG) Log.v(TAG, "startPlay:");
		if (!mScriptRunning && !mTouchMoveRunning && mFlightRecorder.isPrepared()) {
			try {
				mFlightRecorder.prepare();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
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

	private static final String[] SCRIPTS = {
		"circle_xy",
		"circle_xz",
		"revolution_xr",
		"revolution_yr",
	};

	/**
	 * スクリプト実行開始
	 */
	private void startScript(final int index) {
		if (DEBUG) Log.v(TAG, "startScript:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			mScriptRunning = true;
			try {
				switch (index) {
				case 0:
					mScriptFlight.prepare(getResources().getAssets().open("circle_xy2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 1:
					mScriptFlight.prepare(getResources().getAssets().open("circle_xz.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 2:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_xr.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 3:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_yr.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				default:
					throw new IOException("スクリプトファイルが見つからない(範囲外)");
				}
			} catch (final IOException e) {
				mScriptRunning = false;
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * スクリプト実行終了
 	 */
	private void stopScript() {
		if (DEBUG) Log.v(TAG, "stopScript:");
		mScriptRunning = false;
		if (mScriptFlight.isPlaying()) {
			mScriptFlight.stop();
			updateButtons();
		}
	}

	/**
	 * タッチ描画での操縦開始
	 */
	private void startTouchMove() {
		if (DEBUG) Log.v(TAG, "startTouchMove:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			mTouchMoveRunning = true;
			try {
				mTouchFlight.prepare((float)mMaxControlValue, (float)mScaleX, (float)mScaleY, (float)mScaleZ, (float)mScaleR);
			} catch (final Exception e) {
				mTouchMoveRunning = false;
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * タッチ描画での操縦終了
	 */
	private void stopTouchMove() {
		if (DEBUG) Log.v(TAG, "stopTouchMove:");
		mTouchMoveRunning = false;
		if (mTouchFlight.isPlaying()) {
			mTouchFlight.stop();
			updateButtons();
		}
	}

	private static final String asString(final int[] values) {
		final int n = values != null ? values.length : 0;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(values[i]).append(",");
		}
		return sb.toString();
	}


	/**
	 * 自動フライト実行時のコールバックリスナー
	 */
	private final AutoFlightListener mAutoFlightListener = new AutoFlightListener() {

		@Override
		public void onPrepared() {
			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onPrepared:");
			if (mScriptRunning) {
				if (mScriptFlight.isPrepared()) {
					mScriptFlight.play();
				} else {
					mScriptRunning = false;
				}
			} else if (mTouchMoveRunning) {
				if (mTouchFlight.isPrepared()) {
					mTouchFlight.play();
				} else {
					mTouchMoveRunning = false;
				}
			} else if (mFlightRecorder.isPrepared()) {
				mFlightRecorder.pos(0);
				mFlightRecorder.play();
			}

			updateButtons();
		}

		@Override
		public void onStart() {
			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onStart:");
			updateTime(0);
			updateButtons();
		}

		@Override
		public boolean onStep(final int cmd, final int[] values, final long t) {
			if (DEBUG) Log.v(TAG, String.format("mAutoFlightListener#onStep:cmd=%d,t=%d,v=[%s]" , cmd, t, asString(values)));
			updateTime(t);
			if (mController != null) {
				switch (cmd) {
				case IAutoFlight.CMD_EMERGENCY:			// 非常停止
					mController.sendEmergency();
					break;
				case IAutoFlight.CMD_TAKEOFF:			// 離陸
					mController.sendTakeoff();
					break;
				case IAutoFlight.CMD_LANDING:			// 着陸
					mController.sendLanding();
					break;
				case IAutoFlight.CMD_UP_DOWN:			// 上昇:gaz>0, 下降: gaz<0
					mController.setGaz(values[0]);
					break;
				case IAutoFlight.CMD_RIGHT_LEFT:		// 右: roll>0,flag=1 左: roll<0,flag=1
					mController.setRoll(values[0]);
					mController.setFlag((values[0] != 0 ? 1 : 0));
					break;
				case IAutoFlight.CMD_FORWARD_BACK:		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
					mController.setPitch(values[0]);
					break;
				case IAutoFlight.CMD_TURN:				// 右回転: yaw>0, 左回転: ywa<0
					mController.setYaw(values[0]);
					break;
				case IAutoFlight.CMD_COMPASS:			// 北磁極に対する角度 -360〜360度
					mController.setHeading(values[0]);	// 実際は浮動小数点だけど
					break;
				case IAutoFlight.CMD_MOVE5:
					mController.setMove(values[0], values[1], values[2], values[3], values[4]);
					break;
				case IAutoFlight.CMD_MOVE4:
					mController.setMove(values[0], values[1], values[2], values[3]);
					break;
				case IAutoFlight.CMD_MOVE3:
					mController.setMove(values[0], values[1], values[2]);
					break;
				case IAutoFlight.CMD_MOVE2:
					mController.setMove(values[0], values[1]);
					break;
				case IAutoFlight.CMD_FLIP:				// フリップ
					mController.sendAnimationsFlip(values[0]);
					break;
				case IAutoFlight.CMD_CAP:				// キャップ(指定角度水平回転)
					mController.sendAnimationsCap(values[0]);
					break;
				}
				if (mTouchMoveRunning && mFlightRecorder.isRecording()) {
					mFlightRecorder.record(cmd, values);
				}
				return false;
			} else {
				return true;
			}
		}

		@Override
		public void onStop() {
			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onStop:");
			stopPlay();
			stopScript();
			stopTouchMove();
			PilotFragment.super.stopMove();
			updateTime(-1);
			updateButtons();
		}

		@Override
		public void onError(final Exception e) {
			stopPlay();
			stopScript();
			stopTouchMove();
			Log.w(TAG, e);
			updateButtons();
		}

	};

	// 機体姿勢と高度
	private float mCurrentRoll = 0;
	private float mCurrentPitch = 0;
	private float mCurrentYaw = 0;
	private float mCurrentAltitude = 0;
	/**
	 * 定期的にステータスをポーリングして処理するスレッドの実行部
 	 */
	private final Runnable mUpdateStatusTask = new Runnable() {
		private final Vector mAttitude = new Vector();
		@Override
		public void run() {
			if ((mController != null) && mController.canGetAttitude()) {
				mAttitude.set(mController.getAttitude());
				mAttitude.toDegree();	// ラジアンを度に変換
				final float altitude = mController.getAltitude();
				if ((mCurrentRoll != mAttitude.x())
					|| (mCurrentPitch != mAttitude.y()
					|| (mCurrentYaw != mAttitude.z()))
					|| (mCurrentAltitude != altitude)) {

					synchronized (mUpdateStatusUITask) {
						mCurrentRoll = mAttitude.x();
						mCurrentPitch = mAttitude.y();
						mCurrentYaw = mAttitude.z();
						mCurrentAltitude = altitude;
						postUIThread(mUpdateStatusUITask, 1);
					}
				}
			}
			post(this, 200);	// 200ミリ秒=1秒間に最大で約5回更新
		}
	};

	/**
	 * ポーリングによるステータス更新処理のUIスレッドでの実行部
	 */
	private final Runnable mUpdateStatusUITask = new Runnable() {
		@Override
		public synchronized void run() {
			final String s = String.format("%5.1f,%5.1f,%5.1f/%5.1f", mCurrentRoll, mCurrentPitch, mCurrentYaw, mCurrentAltitude);
			if (DEBUG) Log.v(TAG, "Attitude:" + s);
		}
	};

	/**
	 * アラート表示の更新処理をUIスレッドで実行するためのRunnable
	 */
	private final Runnable mUpdateAlarmMessageTask = new Runnable() {
		@Override
		public void run() {
			final int alarm = getAlarm();
			if (DEBUG) Log.w(TAG, "mUpdateAlarmMessageTask:alarm=" + alarm);
			switch (alarm) {
			case DroneStatus.ALARM_NON:					// No alert
				break;
			case DroneStatus.ALARM_USER_EMERGENCY:		// User emergency alert
				mAlertMessage.setText(R.string.alarm_user_emergency);
				break;
			case DroneStatus.ALARM_CUTOUT:				// Cut out alert
				mAlertMessage.setText(R.string.alarm_motor_cut_out);
				break;
			case DroneStatus.ALARM_BATTERY_CRITICAL:	// Critical battery alert
				mAlertMessage.setText(R.string.alarm_low_battery_critical);
				break;
			case DroneStatus.ALARM_BATTERY:				// Low battery alert
				mAlertMessage.setText(R.string.alarm_low_battery);
				break;
			case DroneStatus.ALARM_DISCONNECTED:		// 切断された
				mAlertMessage.setText(R.string.alarm_disconnected);
				break;
			default:
				Log.w(TAG, "unexpected alarm state:" + alarm);
				break;
			}
			mAlertMessage.setVisibility(alarm != 0 ? View.VISIBLE : View.INVISIBLE);
		}
	};

	/**
	 * バッテリー残量表示の更新処理をUIスレッドでするためのRunnable
	 */
	private final Runnable mUpdateBatteryTask = new Runnable() {
		@Override
		public void run() {
			final int battery = mController != null ? mController.getBattery() : -1;
			if (battery >= 0) {
				mBatteryLabel.setText(String.format("%d%%", battery));
			} else {
				mBatteryLabel.setText("---");
			}
		}
	};

	private volatile long lastCall = -1;
	private void updateTime(final long t) {
//		if (DEBUG) Log.v(TAG, "updateTime:" + t);
		mCurrentTime = t;
		lastCall = System.currentTimeMillis();
		runOnUiThread(mUpdateTimeTask);
	}

	private volatile long mCurrentTime;	// 現在の経過時間[ミリ秒]
	private final Runnable mIntervalUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			if (mCurrentTime >= 0) {
				runOnUiThread(mUpdateTimeTask);
			}
		}
	};

	private final Runnable mUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			remove(mIntervalUpdateTimeTask);
			long t = mCurrentTime;
			if (t >= 0) {
				t +=  System.currentTimeMillis() - lastCall;
				final int m = (int)(t / 60000);
				final int s = (int)(t - m * 60000) / 1000;
				mTimeLabelTv.setText(String.format("%3d:%02d", m, s));
				post(mIntervalUpdateTimeTask, 500);	// プライベートスレッド上で遅延実行
			}
		}
	};

	/**
	 * ボタン表示の更新(UIスレッドで処理)
	 */
	private void updateButtons() {
		runOnUiThread(mUpdateButtonsTask);
	}

	private static final int DISABLE_COLOR = 0xcf777777;
	/**
	 *　ボタンの表示更新をUIスレッドで行うためのRunnable
	 */
	private final Runnable mUpdateButtonsTask = new Runnable() {
		@Override
		public void run() {
			final int state = getState();
			final int alarm_state = getAlarm();
			final boolean is_connected = isConnected();
			final boolean is_recording = mFlightRecorder.isRecording();
			final boolean is_playing = mFlightRecorder.isPlaying();
			final boolean can_play = is_connected && !is_recording && !mScriptRunning && !mTouchMoveRunning && (alarm_state == DroneStatus.ALARM_NON) && (mFlightRecorder.size() > 0);
			final boolean can_record = is_connected && !is_playing && !mScriptRunning;
			final boolean can_load = is_connected && !is_playing && !is_recording && !mTouchMoveRunning;
			final boolean can_fly = can_record && (alarm_state == DroneStatus.ALARM_NON);
			final boolean can_flattrim = can_fly && (state == IDeviceController.STATE_STARTED);
			final boolean can_config = can_flattrim;
			final boolean can_clear = is_connected && !is_recording && !is_playing && !mScriptRunning && !mTouchMoveRunning && mTouchFlight.isPrepared();
			final boolean can_move = is_connected && !is_recording && !is_playing && !mScriptRunning && (mTouchFlight.isPrepared() || mTouchFlight.isPlaying()) && (alarm_state == DroneStatus.ALARM_NON);
			final boolean is_battery_alarm
				= (alarm_state == DroneStatus.ALARM_BATTERY) || (alarm_state == DroneStatus.ALARM_BATTERY_CRITICAL);

			// 上パネル
			mTopPanel.setEnabled(is_connected);
			mFlatTrimBtn.setEnabled(can_flattrim);	// フラットトリム
			mBatteryLabel.setTextColor(is_battery_alarm ? 0xffff0000 : 0xff9400d3);
			mConfigShowBtn.setEnabled(can_config);
			mConfigShowBtn.setColorFilter(can_config ? 0: DISABLE_COLOR);

			// 下パネル
			mBottomPanel.setEnabled(is_connected);
			mEmergencyBtn.setEnabled(is_connected);	// 非常停止
			mTimeLabelTv.setVisibility(is_recording || is_playing ? View.VISIBLE : View.INVISIBLE);
			mLoadBtn.setEnabled(can_load);            // 読み込み
			mPlayBtn.setEnabled(can_play);            // 再生
			mPlayBtn.setColorFilter(can_play ? (mFlightRecorder.isPlaying() ? 0xffff0000 : 0) : DISABLE_COLOR);
			mPlayLabel.setText(is_recording ? R.string.action_stop : R.string.action_play);
			mRecordBtn.setEnabled(can_record);        // 記録
			mRecordBtn.setColorFilter(can_record ? (is_recording ? 0xffff0000 : 0) : DISABLE_COLOR);
			mRecordLabel.setText(is_recording ? R.string.action_stop : R.string.action_record);
			if (mClearButton != null) {
				mClearButton.setEnabled(can_clear);
				mClearButton.setColorFilter(can_clear ? (mTouchMoveRunning ? 0xffff0000 : 0) : DISABLE_COLOR);
			}
			if (mMoveButton != null) {
				mMoveButton.setEnabled(can_move);
				mMoveButton.setColorFilter(can_move ? (mTouchMoveRunning || mTouchFlight.isPlaying() ? 0xffff0000 : 0) : DISABLE_COLOR);
			}

			// 離陸/着陸
			switch (state & IDeviceController.STATE_MASK_FLYING) {
			case DroneStatus.STATE_FLYING_LANDED:	// 0x0000;		// FlyingState=0
			case DroneStatus.STATE_FLYING_LANDING:	// 0x0400;		// FlyingState=4
				mTakeOnOffBtn.setImageResource(R.drawable.takeoff72x72);
				break;
			case DroneStatus.STATE_FLYING_TAKEOFF:	// 0x0100;		// FlyingState=1
			case DroneStatus.STATE_FLYING_HOVERING:	// 0x0200;		// FlyingState=2
			case DroneStatus.STATE_FLYING_FLYING:	// 0x0300;		// FlyingState=3
			case DroneStatus.STATE_FLYING_ROLLING:	// 0x0600;		// FlyingState=6
				mTakeOnOffBtn.setImageResource(R.drawable.landing72x72);
				break;
			case DroneStatus.STATE_FLYING_EMERGENCY:	// 0x0500;		// FlyingState=5
				break;
			}

			// 右サイドパネル(とmCapXXXBtn等)
			mRightSidePanel.setEnabled(can_fly);
			// 左サイドパネル(とmFlipXXXBtn等)
			mLeftSidePanel.setEnabled(can_fly);
			// 右スティックパネル(東/西ボタン)
			if (mRightStickPanel != null) {
				mRightStickPanel.setEnabled(can_fly);
			}
			// 左スティックパネル(北/南ボタン)
			if (mLeftStickPanel != null) {
				mLeftStickPanel.setEnabled(can_fly);
			}
			// タッチパイロットView
			if (mTouchPilotView != null) {
				mTouchPilotView.setEnabled(can_fly);
			}

			for (final View view: mActionViews) {
				view.setEnabled(can_fly);
				if (view instanceof ImageView) {
					((ImageView)view).setColorFilter(can_fly ? 0 : DISABLE_COLOR);
				}
			}
		}
	};

	/**
	 * 一定時間後にフラグメントを終了するためのRunnable
	 * 切断された時に使用
	 */
	private final Runnable mPopBackStackTask = new Runnable() {
		@Override
		public void run() {
			try {
				getFragmentManager().popBackStack();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	/** ゲームパッド読み取りスレッド操作用Handler */
	private Handler mGamePadHandler;
	/**
	 *　ゲームパッド読み取りスレッド開始
	 */
	private void startGamePadTask() {
		if (mGamePadHandler == null) {
			final HandlerThread thread = new HandlerThread("GamePadThread");
			thread.start();
			mGamePadHandler = new Handler(thread.getLooper());
		}
		mGamePadHandler.removeCallbacks(mGamePadTask);
		mGamePadHandler.postDelayed(mGamePadTask, 100);
	}

	/**
	 * ゲームパッド読み取りスレッド終了
	 */
	private void stopGamePadTask() {
		if (mGamePadHandler != null) {
			final Handler handler = mGamePadHandler;
			mGamePadHandler = null;
			handler.removeCallbacks(mGamePadTask);
			handler.getLooper().quit();
		}
	}

	private static final long YAW_LIMIT = 200;
	private boolean[] downs = new boolean[GamePad.KEY_NUMS];
	private long[] down_times = new long[GamePad.KEY_NUMS];
	boolean moved;
	/**
	 * ゲームパッド読み取りスレッドの実行部
	 */
	private final Runnable mGamePadTask = new Runnable() {
		@Override
		public void run() {
			final Handler handler = mGamePadHandler;
			if (handler == null) return;	// 既に終了指示が出てる

			handler.removeCallbacks(this);
			GamePad.updateState(downs, down_times, true);

			// 左右の上端ボタン(手前側)を同時押しすると非常停止
			if (((downs[GamePad.KEY_RIGHT_RIGHT] || downs[GamePad.KEY_RIGHT_1]))
				&& (downs[GamePad.KEY_RIGHT_LEFT] || downs[GamePad.KEY_LEFT_1]) ) {
				emergencyStop();
				handler.postDelayed(this, 50);
				return;
			}

			// 飛行していない時にL2/R2同時押しするとフラットトリム実行
			if ((getState() == IDeviceController.STATE_STARTED)
				&& (getAlarm() == DroneStatus.ALARM_NON)
				&& downs[GamePad.KEY_LEFT_2] && downs[GamePad.KEY_RIGHT_2]) {

				mController.sendFlatTrim();
				handler.postDelayed(this, 50);
				return;
			}

			// R2押しながら左スティックでフリップ
			if (downs[GamePad.KEY_RIGHT_2]) {
				if (downs[GamePad.KEY_LEFT_LEFT]) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_LEFT);
					handler.postDelayed(this, 50);
					return;
				} if (downs[GamePad.KEY_LEFT_RIGHT]) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_RIGHT);
					handler.postDelayed(this, 50);
					return;
				} if (downs[GamePad.KEY_LEFT_UP]) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_FRONT);
					handler.postDelayed(this, 50);
					return;
				} if (downs[GamePad.KEY_LEFT_DOWN]) {
					mController.sendAnimationsFlip(IDeviceController.FLIP_BACK);
					handler.postDelayed(this, 50);
					return;
				}
			}

			// 中央の右側ボタン[12]=着陸
			if (downs[GamePad.KEY_CENTER_RIGHT]) {
				landing();
				handler.postDelayed(this, 50);
				return;
			}
			// 中央の左側ボタン[11]=離陸
			if (downs[GamePad.KEY_CENTER_LEFT]) {
				takeOff();
				handler.postDelayed(this, 50);
				return;
			}

			// 左側十字キーまたは左側アナログスティックの左右
			final float roll = mGamepadSensitivity * mGamepadScaleX * (downs[GamePad.KEY_LEFT_RIGHT]
				? down_times[GamePad.KEY_LEFT_RIGHT]
				: (downs[GamePad.KEY_LEFT_LEFT]
					? -down_times[GamePad.KEY_LEFT_LEFT]
					: 0)
			);
			// 左側十字キーまたは左側アナログスティックの上下
			final float pitch = mGamepadSensitivity * mGamepadScaleY * (downs[GamePad.KEY_LEFT_UP]
				? down_times[GamePad.KEY_LEFT_UP]
				: (downs[GamePad.KEY_LEFT_DOWN]
					? -down_times[GamePad.KEY_LEFT_DOWN]
					: 0)
			);
			// 右側アナログスティックの上下
			final float gaz = mGamepadSensitivity * mGamepadScaleZ * (downs[GamePad.KEY_RIGHT_UP]
				? down_times[GamePad.KEY_RIGHT_UP]
				: (downs[GamePad.KEY_RIGHT_DOWN]
					? -down_times[GamePad.KEY_RIGHT_DOWN]
					: 0)
			);
			// 右側アナログスティックの左右または上端ボタン(手前側)
			final float yaw = mGamepadSensitivity * (downs[GamePad.KEY_RIGHT_RIGHT] && (down_times[GamePad.KEY_RIGHT_RIGHT] > YAW_LIMIT)
				? down_times[GamePad.KEY_RIGHT_RIGHT] - YAW_LIMIT
				: (
					downs[GamePad.KEY_RIGHT_1]
					? down_times[GamePad.KEY_RIGHT_1]
					: (
						downs[GamePad.KEY_RIGHT_LEFT] && (down_times[GamePad.KEY_RIGHT_LEFT] > YAW_LIMIT)
						? -down_times[GamePad.KEY_RIGHT_LEFT] + YAW_LIMIT
						: (
							downs[GamePad.KEY_LEFT_1]
							? -down_times[GamePad.KEY_LEFT_1]
							: 0
						)
					)
				)
			);
//			GamePad.KEY_LEFT_CENTER:	// = 0;
//			GamePad.KEY_LEFT_UP:		// = 1;
//			GamePad.KEY_LEFT_RIGHT:		// = 2;
//			GamePad.KEY_LEFT_DOWN:		// = 3;
//			GamePad.KEY_LEFT_LEFT:		// = 4;
//			GamePad.KEY_RIGHT_CENTER:	// = 5;
//			GamePad.KEY_RIGHT_UP:		// = 6;
//			GamePad.KEY_RIGHT_RIGHT:	// = 7;
//			GamePad.KEY_RIGHT_DOWN:		// = 8;
//			GamePad.KEY_RIGHT_LEFT:		// = 9;
//			GamePad.KEY_LEFT_1:			// = 10;	// 左上前
//			GamePad.KEY_LEFT_2:			// = 11;	// 左上後
//			GamePad.KEY_CENTER_LEFT:	// = 12;	// 中央左
//			GamePad.KEY_RIGHT_1:		// = 13;	// 右上前
//			GamePad.KEY_RIGHT_2:		// = 14;	// 右上後
//			GamePad.KEY_CENTER_RIGHT:	// = 15;	// 中央右
			if ((roll != 0) || (pitch != 0) || (gaz != 0) || (yaw != 0)) {
				if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", roll, pitch, gaz, yaw));
				if (mController != null) {
					moved = true;
					mController.setMove((int) roll, (int) pitch, (int) gaz, (int) yaw);
					mFlightRecorder.record(FlightRecorder.CMD_MOVE4, (int)roll, (int)pitch, (int)gaz, (int)yaw);
				}
			} else if (moved) {
				if (mController != null) {
					mController.setMove(0, 0, 0, 0, 0);
					mFlightRecorder.record(FlightRecorder.CMD_MOVE4, (int) 0, (int) 0, (int) 0, (int) 0);
				}
				moved = false;
//				if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", 0f, 0f, 0f, 0f));
			}
			handler.postDelayed(this, 50);
		}
	};

	/**
	 * タッチレスポンス用にカラーフィルターを適用する時間
	 */
	private static final long TOUCH_RESPONSE_TIME_MS = 100;	// 200ミリ秒
	/**
	 * タッチレスポンス時のカラーフィルター色
	 */
	private static final int TOUCH_RESPONSE_COLOR = 0x7f331133;

	/**
	 * カラーフィルタクリア用のRunnableのキャッシュ
	 */
	private final Map<ImageView, ResetColorFilterTask> mResetColorFilterTasks = new HashMap<ImageView, ResetColorFilterTask>();

	/**
	 * 指定したImageViewに指定した色でカラーフィルターを適用する。
	 * reset_delayが0より大きければその時間経過後にカラーフィルターをクリアする
	 * @param image
	 * @param color
	 * @param reset_delay ミリ秒
	 */
	private void setColorFilter(final ImageView image, final int color, final long reset_delay) {
		if (image != null) {
			image.setColorFilter(color);
			if (reset_delay > 0) {
				ResetColorFilterTask task = mResetColorFilterTasks.get(image);
				if (task == null) {
					task = new ResetColorFilterTask(image);
				}
				removeFromUIThread(task);
				postUIThread(task, reset_delay);	// UIスレッド上で遅延実行
			}
		}
	}

	/**
	 * 一定時間後にImageView(とImageButton)のカラーフィルターをクリアするためのRunnable
	 */
	private static class ResetColorFilterTask implements Runnable {
		private final ImageView mImage;
		public ResetColorFilterTask(final ImageView image) {
			mImage = image;
		}
		@Override
		public void run() {
			mImage.setColorFilter(0);
		}
	}

	private void setSideMenu() {
		if (DEBUG) Log.v(TAG, "setSideMenu:");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final MainActivity activity = (MainActivity) getActivity();
				if (activity == null || activity.isFinishing()) return;

				if (mSideMenuListView == null) {
					mSideMenuListView = new SideMenuListView(activity);
					activity.setSideMenuView(mSideMenuListView);
					mSideMenuListView.setOnItemClickListener(mOnItemClickListener);
				}
				final List<String> labelList = new ArrayList<String>();
				for (int i = 0; i < SCRIPTS.length; i++) {
					labelList.add(SCRIPTS[i]);
				}
				ListAdapter adapter = mSideMenuListView.getAdapter();
				if (adapter instanceof SideMenuAdapter) {
					((SideMenuAdapter) adapter).clear();
					if (labelList.size() > 0) {
						((SideMenuAdapter) adapter).addAll(labelList);
					}
				} else {
					mSideMenuListView.setAdapter(null);
					if (labelList.size() > 0) {
						adapter = new SideMenuAdapter(getActivity(), R.layout.item_sidemenu, labelList);
						mSideMenuListView.setAdapter(adapter);
					}
				}
				activity.setSideMenuEnable(labelList.size() > 0);
			}
		});
	}

	private void removeSideMenu() {
		if (DEBUG) Log.v(TAG, "removeSideMenu:");
		if (mSideMenuListView != null) {
			final SideMenuListView v = mSideMenuListView;
			mSideMenuListView = null;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final MainActivity activity = (MainActivity) getActivity();
					activity.setSideMenuEnable(false);
					activity.removeSideMenuView(v);
				}
			});
		}
	}

	/**
	 * サイドメニューの項目をクリックした時の処理
	 */
	private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			if (DEBUG) Log.v(TAG, "onItemClick:" + position);
			final MainActivity activity = (MainActivity)getActivity();
			activity.closeSideMenu();
			startScript(position);
		}
	};

	private int mSurfaceId = 0;
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:");
			if ((mVideoStream != null) && (mSurfaceId == 0)) {
				final Surface _surface = new Surface(surface);
				mSurfaceId = _surface.hashCode();
				mVideoStream.addSurface(mSurfaceId, _surface);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:");
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:");
			if (mVideoStream != null) {
				mVideoStream.removeSurface(mSurfaceId);
			}
			mSurfaceId = 0;
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};
}
