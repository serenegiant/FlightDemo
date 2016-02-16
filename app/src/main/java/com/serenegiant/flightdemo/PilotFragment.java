package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.AutoFlightListener;
import com.serenegiant.arflight.CameraControllerListener;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.FlightControllerBebop;
import com.serenegiant.arflight.FlightControllerBebop2;
import com.serenegiant.arflight.FlightControllerMiniDrone;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.FlightRecorder;
import com.serenegiant.arflight.IBridgeController;
import com.serenegiant.arflight.ICameraController;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;
import com.serenegiant.arflight.IAutoFlight;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.ScriptFlight;
import com.serenegiant.arflight.TouchFlight;
import com.serenegiant.arflight.VideoStream;
import com.serenegiant.dialog.SelectFileDialogFragment;
import com.serenegiant.gl.IVideoScreen;
import com.serenegiant.math.Vector;
import com.serenegiant.utils.FileUtils;
import com.serenegiant.widget.OrientationView;
import com.serenegiant.widget.SideMenuListView;
import com.serenegiant.widget.StickView;
import com.serenegiant.widget.StickView.OnStickMoveListener;
import com.serenegiant.widget.TouchPilotView;
import com.serenegiant.gl.AttitudeScreenBase;
import com.serenegiant.gl.IModelView;
import com.serenegiant.gl.IScreen;
import com.serenegiant.widget.TouchableLinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PilotFragment extends ControlFragment implements SelectFileDialogFragment.OnFileSelectListener {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();

	static {
		FileUtils.DIR_NAME = "FlightDemo";
	}

	public static PilotFragment newInstance(final ARDiscoveryDeviceService device) {
		final PilotFragment fragment = new PilotFragment();
		fragment.setDevice(device);
		return fragment;
	}

	public static PilotFragment newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info) {
		final PilotFragment fragment = new PilotFragment();
		fragment.setBridge(device, info);
		return fragment;
	}

	private ViewGroup mControllerFrame;			// 操作パネル全体
	private TouchableLinearLayout mPilotFrame;	// 操縦パネル
	private OrientationView mCameraView;

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
	private ImageButton mStillCaptureBtn;
	private ImageButton mVideoRecordingBtn;
	// 左サイドパネル
	private View mLeftSidePanel;
	// 操縦用
	private int mOperationType;				// 操縦スティックのモード
	private boolean mOperationTouch;		// タッチ描画で操縦モードかどうか

	private StickView mRightStickPanel;		// 右スティックパネル
	private StickView mLeftStickPanel;		// 左スティックパネル
	private TouchPilotView mTouchPilotView;	// タッチ描画パネル
	// ビデオストリーミング用
//	private TextureView mVideoTextureView;	// ビデオストリーミング表示用
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
	private IModelView mModelView;

	private VideoStream mVideoStream;
	private boolean mVideoRecording;

	public PilotFragment() {
		super();
		// デフォルトコンストラクタが必要
		mFlightRecorder = new FlightRecorder(mAutoFlightListener);
		mScriptFlight = new ScriptFlight(mAutoFlightListener);
		mTouchFlight = new TouchFlight(mAutoFlightListener);
	}

//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		if (DEBUG) Log.v(TAG, "onAttach:");
//	}

	@Override
	public void onDetach() {
//		if (DEBUG) Log.v(TAG, "onDetach:");
		mJoystick = null;
		stopDeviceController(false);
//		releaseUsbDriver();
		mFlightRecorder.release();
		mScriptFlight.release();
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
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final SharedPreferences pref = getActivity().getPreferences(0);
		mOperationTouch = pref.getBoolean(ConfigFragment.KEY_OPERATION_TOUCH, false);
		mOperationType = pref.getInt(ConfigFragment.KEY_OPERATION_TYPE, 0);
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
		int layout_id;
		if (mOperationTouch) {
			layout_id = R.layout.fragment_pilot_touch;
		} else {
			switch (mOperationType) {
			case 1:
				layout_id = R.layout.fragment_pilot_reverse;
				break;
			case 2:
				layout_id = R.layout.fragment_pilot_mode1;
				break;
			case 3:
				layout_id = R.layout.fragment_pilot_mode2;
				break;
//			case 0:
			default:
				layout_id = R.layout.fragment_pilot;
			}
		}
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final ViewGroup rootView = (ViewGroup) local_inflater.inflate(layout_id, container, false);

//		rootView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//		rootView.setOnKeyListener(mOnKeyListener);
//		rootView.setFocusable(true);

		mControllerFrame = (ViewGroup) rootView.findViewById(R.id.controller_frame);
//		mControllerFrame.setFocusable(true);
//		mControllerFrame.requestFocus();

		mPilotFrame = (TouchableLinearLayout)rootView.findViewById(R.id.pilot_frame);
		mPilotFrame.setOnTouchableListener(mOnTouchableListener);

		mCameraView = (OrientationView)rootView.findViewById(R.id.camera_view);
		if (mCameraView != null) {
			mCameraView.setPanTilt(0, 0);
		}

		mActionViews.clear();
		// 上パネル
		mTopPanel = rootView.findViewById(R.id.top_panel);
		mActionViews.add(mTopPanel);

		mFlatTrimBtn = (ImageButton) rootView.findViewById(R.id.flat_trim_btn);
		mFlatTrimBtn.setOnClickListener(mOnClickListener);
		mFlatTrimBtn.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mFlatTrimBtn);

		mConfigShowBtn = (ImageButton) rootView.findViewById(R.id.config_show_btn);
		mConfigShowBtn.setOnClickListener(mOnClickListener);

		// 下パネル
		mBottomPanel = rootView.findViewById(R.id.bottom_panel);
		mEmergencyBtn = (ImageButton) rootView.findViewById(R.id.emergency_btn);
		mEmergencyBtn.setOnClickListener(mOnClickListener);

		mTakeOnOffBtn = (ImageButton) rootView.findViewById(R.id.take_onoff_btn);
		mTakeOnOffBtn.setOnClickListener(mOnClickListener);
		mTakeOnOffBtn.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mTakeOnOffBtn);

		mRecordBtn = (ImageButton) rootView.findViewById(R.id.record_btn);
		mRecordBtn.setOnClickListener(mOnClickListener);
		mRecordBtn.setOnLongClickListener(mOnLongClickListener);

		mRecordLabel = (TextView) rootView.findViewById(R.id.record_label);

		mPlayBtn = (ImageButton) rootView.findViewById(R.id.play_btn);
		mPlayBtn.setOnClickListener(mOnClickListener);
		mPlayBtn.setOnLongClickListener(mOnLongClickListener);

		mPlayLabel = (TextView) rootView.findViewById(R.id.play_label);

		mLoadBtn = (ImageButton) rootView.findViewById(R.id.load_btn);
		mLoadBtn.setOnClickListener(mOnClickListener);
		mLoadBtn.setOnLongClickListener(mOnLongClickListener);

		mTimeLabelTv = (TextView) rootView.findViewById(R.id.time_label);
		mTimeLabelTv.setVisibility(View.INVISIBLE);

		// クリアボタン(タッチ描画操縦)
		mClearButton = (ImageButton) rootView.findViewById(R.id.clear_btn);
		if (mClearButton != null) {
			mClearButton.setOnClickListener(mOnClickListener);
		}
		// 移動ボタン(タッチ描画操縦)
		mMoveButton = (ImageButton) rootView.findViewById(R.id.move_btn);
		if (mMoveButton != null) {
			mMoveButton.setOnClickListener(mOnClickListener);
		}

		ImageButton button;
		// 右サイドパネル
		mRightSidePanel = rootView.findViewById(R.id.right_side_panel);
		mActionViews.add(mRightSidePanel);

		// 静止画撮影
		mStillCaptureBtn = (ImageButton) rootView.findViewById(R.id.still_capture_btn);
		mStillCaptureBtn.setOnClickListener(mOnClickListener);

		// 動画撮影
		mVideoRecordingBtn = (ImageButton) rootView.findViewById(R.id.video_capture_btn);
		mVideoRecordingBtn.setOnClickListener(mOnClickListener);

		button = (ImageButton) rootView.findViewById(R.id.cap_p45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton) rootView.findViewById(R.id.cap_m45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		// 左サイドパネル
		mLeftSidePanel = rootView.findViewById(R.id.left_side_panel);
		mActionViews.add(mLeftSidePanel);

		button = (ImageButton) rootView.findViewById(R.id.flip_right_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton) rootView.findViewById(R.id.flip_left_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton) rootView.findViewById(R.id.flip_front_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = (ImageButton) rootView.findViewById(R.id.flip_back_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		// 右スティックパネル
		mRightStickPanel = (StickView) rootView.findViewById(R.id.stick_view_right);
		if (mRightStickPanel != null) {
			mRightStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		// 左スティックパネル
		mLeftStickPanel = (StickView) rootView.findViewById(R.id.stick_view_left);
		if (mLeftStickPanel != null) {
			mLeftStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		// タッチパイロットView(タッチ描画操縦)
		mTouchPilotView = (TouchPilotView) rootView.findViewById(R.id.touch_pilot_view);
		if (mTouchPilotView != null) {
			mTouchPilotView.setTouchPilotListener(mTouchPilotListener);
			mActionViews.add(mTouchPilotView);
		}

		mBatteryLabel = (TextView) rootView.findViewById(R.id.batteryLabel);
		mAlertMessage = (TextView) rootView.findViewById(R.id.alert_message);
		mAlertMessage.setVisibility(View.INVISIBLE);

		// 機体モデル表示
		final int model;
		final int ctrl;
		if (mController instanceof FlightControllerMiniDrone) {
			model = IModelView.MODEL_MINIDRONE;
			ctrl = AttitudeScreenBase.CTRL_PILOT;
		} else if (mController instanceof FlightControllerBebop2) {
			model = IModelView.MODEL_BEBOP2;
			ctrl = AttitudeScreenBase.CTRL_ATTITUDE;
		} else if (mController instanceof FlightControllerBebop) {
			model = IModelView.MODEL_BEBOP;
			ctrl = AttitudeScreenBase.CTRL_ATTITUDE;
		} else {
			model = IModelView.MODEL_BEBOP;
			ctrl = AttitudeScreenBase.CTRL_ATTITUDE;
		}
		if (mController instanceof ICameraController) {
			((ICameraController)mController).setCameraControllerListener(mCameraControllerListener);
			((ICameraController)mController).sendCameraOrientation(0, 0);
		}
		final int color = pref.getInt(ConfigFragment.KEY_COLOR, getResources().getColor(R.color.RED));
		TextureHelper.genTexture(getActivity(), model, color);
		mModelView = (IModelView)rootView.findViewById(R.id.drone_view);
		mModelView.setModel(model, ctrl);

//		final boolean use_gamepad_driver = pref.getBoolean(ConfigFragment.KEY_GAMEPAD_USE_DRIVER, false);
//		if (use_gamepad_driver) {
//			startUsbDriver();
//		}
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
//		if (DEBUG) Log.v(TAG, "onResume:");
		final Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			mJoystick = ((MainActivity)activity).mJoystick;
		}
		mControllerFrame.setKeepScreenOn(true);
		startDeviceController();
		startSensor();
		if ((mFlightController != null) && isConnected()) {
			mModelView.hasGuard(mFlightController.hasGuard());
		}
		mModelView.onResume();
//		synchronized (mUsbSync) {
//			if (mUSBMonitor != null) {
//				mUSBMonitor.register();
//			}
//		}
	}

	@Override
	public void onPause() {
//		if (DEBUG) Log.v(TAG, "onPause:");
		mJoystick = null;
//		synchronized (mUsbSync) {
//			releaseGamepad();
//			if (mUSBMonitor != null) {
//				mUSBMonitor.unregister();
//			}
//		}
		mModelView.onPause();
		if (mController instanceof ICameraController) {
			((ICameraController)mController).sendVideoRecording(false);
		}
		stopVideoStreaming();
		stopRecord();
		stopPlay();
		stopScript();
		stopTouchMove();
		stopSensor();
		removeSideMenu();
		remove(mGamePadTask);
		remove(mUpdateStatusTask);
		mControllerFrame.setKeepScreenOn(false);
		super.onPause();
	}

//	@Override
//	public void onDestroy() {
//		if (DEBUG) Log.v(TAG, "onDestroy:");
//		super.onDestroy();
//	}

	@Override
	public void onFileSelect(File[] files) {
//		if (DEBUG) Log.v(TAG, "onFileSelect:");
		if ((files != null) && (files.length > 0)
			&& !mFlightRecorder.isPlaying() && !mFlightRecorder.isRecording() ) {
			mFlightRecorder.load(files[0]);
			updateButtons();
		}
	}

	private final TouchableLinearLayout.OnTouchableListener mOnTouchableListener
		= new TouchableLinearLayout.OnTouchableListener() {

		/** minimum distance between touch positions*/
		private static final float MIN_DISTANCE = 15.f;
		private static final float MIN_DISTANCE_SQUARE = MIN_DISTANCE * MIN_DISTANCE;
		/** コマンドを送る最小間隔[ミリ秒]  */
		private static final long MIN_CMD_INTERVALS_MS = 50;	// 50ミリ秒
		/** pan/tiltをリセットするための長押し時間 */
		private static final long RESET_DURATION_MS = 2000;	// 2秒

		private boolean inited;
		/** マルチタッチ開始時のタッチポインタのインデックス */
		private int mPrimaryId, mSecondaryId;
		/** マルチタッチ開始時のタッチ位置 */
		private float mPrimaryX, mPrimaryY, mSecondX, mSecondY;
		/** マルチタッチ開始時のタッチ中点 */
		private float mPivotX, mPivotY;
		/** マルチタッチ開始時のタッチ距離 */
		private float mTouchDistance;

		private float mPanLen, mTiltLen;
		private int mPan, mTilt;
		private long prevTime;

		@Override
		public boolean onInterceptTouchEvent(final MotionEvent event) {
			final boolean intercept = (mController instanceof ICameraController) && (event.getPointerCount() > 1);	// マルチタッチした時は横取りする
			if (intercept) {
				// マルチタッチ開始時のタッチ位置等を保存
				initTouch(event);
			}
			return intercept;
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			final int action = event.getActionMasked();
			final int n = event.getPointerCount();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
//				if (DEBUG) Log.v(TAG, "ACTION_DOWN:");
				// シングルタッチ
				return n > 1;	// 多分ここにはこない
			case MotionEvent.ACTION_POINTER_DOWN:
//				if (DEBUG) Log.v(TAG, "ACTION_POINTER_DOWN:");
				return true;
			case MotionEvent.ACTION_MOVE:
//				if (DEBUG) Log.v(TAG, "ACTION_MOVE:");
				if ((n > 1) && (System.currentTimeMillis() - prevTime > MIN_CMD_INTERVALS_MS) && checkTouchMoved(event)) {
					prevTime = System.currentTimeMillis();
					remove(mResetRunnable);
					if (!inited) {
						initTouch(event);
					}
					// 現在のタッチ座標
					final float x0 = event.getX(0);
					final float y0 = event.getY(0);
					final float x1 = event.getX(1);
					final float y1 = event.getY(1);
					// 現在の中点座標
					final float cx = (x0 + x1) / 2.0f;
					final float cy = (y0 + y1) / 2.0f;
					// 最初のタッチ中点との距離を計算
					final float dx = (mPivotX - cx) * mPanLen + mPan;
					final float dy = (cy - mPivotY) * mTiltLen + mTilt;
					final int pan = dx < -100 ? -100 : (dx > 100 ? 100 : (int)dx);
					final int tilt = dy < -100 ? -100 : (dy > 100 ? 100 : (int)dy);
//					if (DEBUG) Log.v(TAG, String.format("ACTION_MOVE:dx=%5.2f,dy=%5.2f,pan=%d,tilt=%d", dx, dy, pan, tilt));
					if (mController instanceof ICameraController) {
						((ICameraController)mController).sendCameraOrientation(tilt, pan);
					}
				}
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				break;
			}
			if (n == 0) {
				inited = false;
				remove(mResetRunnable);
			}
			return false;
		}

		/** Pan/TiltをリセットするためのRunnable */
		private Runnable mResetRunnable = new Runnable() {
			@Override
			public void run() {
				inited = false;
				if (mController instanceof ICameraController) {
					((ICameraController)mController).sendCameraOrientation(0, 0);
					if (mCameraView != null) {
						mCameraView.setPanTilt(0, 0);
					}
				}
			}
		};

		private void initTouch(final MotionEvent event) {
			// primary touch
			mPrimaryId = event.getPointerId(0);
			mPrimaryX = event.getX(0);
			mPrimaryY = event.getY(0);
			// secondary touch
			mSecondaryId = event.getPointerId(1);
			mSecondX = event.getX(1);
			mSecondY = event.getY(1);
			// calculate the distance between first and second touch
			final float dx = mSecondX - mPrimaryX;
			final float dy = mSecondY - mPrimaryY;
			mTouchDistance = (float)Math.hypot(dx, dy);
			// set pivot position to the middle coordinate
			mPivotX = (mPrimaryX + mSecondX) / 2.0f;
			mPivotY = (mPrimaryY + mSecondY) / 2.0f;
			prevTime = System.currentTimeMillis() - MIN_CMD_INTERVALS_MS;
			mPanLen = 80.0f / mPilotFrame.getWidth();
			mTiltLen = 80.0f / mPilotFrame.getHeight();
			if (mController instanceof ICameraController) {
				mPan = ((ICameraController)mController).getPan();
				mTilt = ((ICameraController)mController).getTilt();
			}
			inited = true;
			remove(mResetRunnable);
			post(mResetRunnable, RESET_DURATION_MS);
		}

		/** タッチ位置を動かしたかどうかを取得 */
		private final boolean checkTouchMoved(final MotionEvent event) {
			final int ix0 = event.findPointerIndex(mPrimaryId);
			final int ix1 = event.findPointerIndex(mSecondaryId);
			if (ix0 >= 0) {
				// check primary touch
				float x = event.getX(ix0) - mPrimaryX;
				float y = event.getY(ix0) - mPrimaryY;
				if (x * x + y * y < MIN_DISTANCE_SQUARE) {
					// primary touch is at the almost same position
					if (ix1 >= 0) {
						// check secondary touch
						x = event.getX(ix1) - mSecondX;
						y = event.getY(ix1) - mSecondY;
						if (x * x + y * y < MIN_DISTANCE_SQUARE) {
							// secondary touch is also at the almost same position.
							return false;
						}
					} else {
						return false;
					}
				}
			}
			return true;
		}

	};

	private final CameraControllerListener mCameraControllerListener = new CameraControllerListener() {
		@Override
		public void onCameraOrientationChanged(final int pan, final int tilt) {
			if (mCameraView != null) {
				mCameraView.setPanTilt(-pan, tilt);
			}
		}
	};

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
//			if (DEBUG) Log.v(TAG, "onClick:" + view);
			switch (view.getId()) {
			case R.id.flat_trim_btn:
				// フラットトリム
				setColorFilter((ImageView)view);
				if ((mFlightController != null) && (getState() == IFlightController.STATE_STARTED)) {
					mFlightController.requestFlatTrim();
				}
				break;
			case R.id.load_btn:
				// 読み込みボタンの処理
				setColorFilter((ImageView)view);
				final File root = FileUtils.getCaptureDir(getActivity(), "Documents", false);
				SelectFileDialogFragment.showDialog(PilotFragment.this, root.getAbsolutePath(), false, "fcr");
				break;
			case R.id.record_btn:
				// 操縦記録ボタンの処理
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
				setColorFilter((ImageView)view);
				if (isConnected()) {
					if ((getState() & IFlightController.STATE_MASK_FLYING) == DroneStatus.STATE_FLYING_LANDED) {
						replace(ConfigFragment.newInstance(getDevice()));
					} else {
						landing();
					}
				}
				break;
			case R.id.clear_btn:
				// タッチ描画データの消去
				setColorFilter((ImageView)view);
				if (mTouchPilotView != null) {
					mTouchPilotView.clear();
				}
				updateButtons();
				break;
			case R.id.move_btn:
				// タッチ描画で操縦開始
				setColorFilter((ImageView)view);
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
				setColorFilter((ImageView) view);
				emergencyStop();
				break;
			case R.id.take_onoff_btn:
				// 離陸指示/着陸指示ボタンの処理
				setColorFilter((ImageView)view);
				if (!isFlying()) {
//					takeOff();
					Toast.makeText(getActivity(), R.string.notify_takeoff, Toast.LENGTH_SHORT).show();
				} else {
					landing();
				}
				updateButtons();
				break;
			case R.id.flip_front_btn:
				setColorFilter((ImageView) view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_FRONT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IFlightController.FLIP_FRONT);
				}
				break;
			case R.id.flip_back_btn:
				setColorFilter((ImageView)view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_BACK);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IFlightController.FLIP_BACK);
				}
				break;
			case R.id.flip_right_btn:
				setColorFilter((ImageView)view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_RIGHT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IFlightController.FLIP_RIGHT);
				}
				break;
			case R.id.flip_left_btn:
				setColorFilter((ImageView)view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_LEFT);
					mFlightRecorder.record(FlightRecorder.CMD_FLIP, IFlightController.FLIP_LEFT);
				}
				break;
			case R.id.still_capture_btn:
				// 静止画撮影ボタンの処理
				if (getStillCaptureState() == DroneStatus.MEDIA_READY) {
					setColorFilter((ImageView) view);
					if (mFlightController != null) {
						mFlightController.requestTakePicture();
					}
				}
				break;
			case R.id.video_capture_btn:
				// 動画撮影ボタンの処理
				setColorFilter((ImageView)view);
				if (mController instanceof ICameraController) {
					mVideoRecording = !mVideoRecording;
					((ICameraController)mController).sendVideoRecording(mVideoRecording);
				}
				break;
			case R.id.cap_p45_btn:
				setColorFilter((ImageView)view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsCap(45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, 45);
				}
				break;
			case R.id.cap_m45_btn:
				setColorFilter((ImageView)view);
				if (mFlightController != null) {
					mFlightController.requestAnimationsCap(-45);
					mFlightRecorder.record(FlightRecorder.CMD_CAP, -45);
				}
				break;
/*			case R.id.north_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mFlightController != null) {
					mFlightController.setHeading(0);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 0);
				}
				break;
			case R.id.south_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mFlightController != null) {
					mFlightController.setHeading(180);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 180);
				}
				break;
			case R.id.west_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mFlightController != null) {
					mFlightController.setHeading(-90);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, -90);
				}
				break;
			case R.id.east_btn:
				setColorFilter((ImageView)view, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
				if (mFlightController != null) {
					mFlightController.setHeading(90);
					mFlightRecorder.record(FlightRecorder.CMD_COMPASS, 90);
				}
				break; */
			}
		}
	};

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
//			if (DEBUG) Log.v(TAG, "onLongClick:" + view);
			switch (view.getId()) {
			case R.id.record_btn:
				if (!mFlightRecorder.isRecording()) {
					startRecord(false);
				} else {
					stopRecord();
				}
				return true;
			case R.id.flat_trim_btn:
				setColorFilter((ImageView)view);
				if ((mFlightController != null) && (getState() == IFlightController.STATE_STARTED)) {
					replace(CalibrationFragment.newInstance(getDevice()));
					return true;
				}
				break;
			case R.id.take_onoff_btn:
				// 離陸/着陸ボタンを長押しした時の処理
				setColorFilter((ImageView)view);
				if (!isFlying()) {
					takeOff();
					return true;
				}
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
			switch (mOperationType) {
			case 0:	// 通常
			case 1:	// 左右反転
				// レイアウト上でView自体を左右入れ替えているので左右反転モードも通常操作モードと同じ
				stick_normal(view.getId(), mx, my);
				break;
			case 2:	// mode1
				stick_mode1(view.getId(), mx, my);
				break;
			case 3:	// mode2
				stick_mode2(view.getId(), mx, my);
				break;
			}
		}
	};

	private void stick_normal(final int id, final int _mx, final int _my) {
		switch (id) {
		case R.id.stick_view_right: {	// 前後左右移動
			if ((_mx != mPrevRightMX) || ((_my != mPrevRightMY))) {
				mPrevRightMX = _mx;
				mPrevRightMY = _my;
				if (mFlightController != null) {
					mFlightController.setMove(_mx, -_my);
					mFlightRecorder.record(FlightRecorder.CMD_MOVE2, _mx, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_normal:LR%d,FB%d", _mx, -_my));
				}
			}
			break;
		}
		case R.id.stick_view_left: {
			int mx = _mx;
			if ((Math.abs(_mx) < 20)) mx = 0;
			if (mx != mPrevLeftMX) {	// 左右回転
				mPrevLeftMX = mx;
				if (mFlightController != null) {
					mFlightController.setYaw(mx);
					mFlightRecorder.record(FlightRecorder.CMD_TURN, mx);
//					if (DEBUG) Log.v(TAG, String.format("stick_normal:T%d", _mx));
				}
			}
			if (_my != mPrevLeftMY) {	// 上昇下降
				mPrevLeftMY = _my;
				if (mFlightController != null) {
					mFlightController.setGaz(-_my);
					mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_normal:UD%d", -_my));
				}
			}
			break;
		}
		}
	}

	private void stick_mode1(final int id, final int _mx, final int _my) {
		// モード1
		// 右スティック: 左右=左右移動, 上下=上昇下降
		// 左スティック: 左右=左右回転, 上下=前後移動
		switch (id) {
		case R.id.stick_view_right: {
			if (_my != mPrevRightMY) {	// 上昇下降
				mPrevRightMY = _my;
				if (mFlightController != null) {
					mFlightController.setGaz(-_my);
					mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode1:UD%d", -_my));
				}
			}
			int mx = _mx;
			if ((Math.abs(_mx) < 20)) mx = 0;
			if (mx != mPrevRightMX) {	// 左右移動
				mPrevRightMX = mx;
				if (mFlightController != null) {
					mFlightController.setRoll(mx, true);
					mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, mx);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode1:LR%d", mx));
				}
			}
			break;
		}
		case R.id.stick_view_left: {
			if (_mx != mPrevLeftMX) {	// 左右回転
				mPrevLeftMX = _mx;
				if (mFlightController != null) {
					mFlightController.setYaw(_mx);
					mFlightRecorder.record(FlightRecorder.CMD_TURN, _mx);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode1:T%d", _mx));
				}
			}
			if (_my != mPrevLeftMY) {	// 前後移動
				mPrevLeftMY = _my;
				if (mFlightController != null) {
					mFlightController.setPitch(-_my, true);
					mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode1:FB%d", -_my));
				}
			}
			break;
		}
		}
	}

	private void stick_mode2(final int id, final int _mx, final int _my) {
		// モード2
		// 右スティック: 左右=左右移動, 上下=前後移動
		// 左スティック: 左右=左右回転, 上下=上昇下降
		switch (id) {
		case R.id.stick_view_right: {
			if (_my != mPrevRightMY) {	// 前後移動
				mPrevRightMY = _my;
				if (mFlightController != null) {
					mFlightController.setPitch(-_my, true);
					mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode2:FB%d", -_my));
				}
			}
			if (_mx != mPrevRightMX) {	// 左右移動
				mPrevRightMX = _mx;
				if (mFlightController != null) {
					mFlightController.setRoll(_mx, true);
					mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, _mx);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode2:LR%d", _mx));
				}
			}
			break;
		}
		case R.id.stick_view_left: {
			int mx = _mx;
			if ((Math.abs(_mx) < 20)) mx = 0;
			if (mx != mPrevLeftMX) {	// 左右回転
				mPrevLeftMX = mx;
				if (mFlightController != null) {
					mFlightController.setYaw(mx);
					mFlightRecorder.record(FlightRecorder.CMD_TURN, mx);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode2:T%d", _mx));
				}
			}
			if (_my != mPrevLeftMY) {	// 上昇下降
				mPrevLeftMY = _my;
				if (mFlightController != null) {
					mFlightController.setGaz(-_my);
					mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, -_my);
//					if (DEBUG) Log.v(TAG, String.format("stick_mode2:UD%d", -_my));
				}
			}
			break;
		}
		}
	}

	private final TouchPilotView.TouchPilotListener mTouchPilotListener = new  TouchPilotView.TouchPilotListener() {
		@Override
		public void onDrawFinish(final TouchPilotView view,
			final float min_x, final float max_x,
			final float min_y, final float max_y,
			final float min_z, final float max_z,
			final int num_points, final float[] points) {

//			if (DEBUG) Log.v(TAG, "onDrawFinish:" + num_points);
			mTouchFlight.prepare(view.getWidth(), view.getHeight(), min_x, max_x, min_y, max_y, min_z, max_z, num_points, points);
			updateButtons();
		}
	};

	@Override
	protected void startVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "startVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			if (mVideoStream == null) {
				mVideoStream = new VideoStream();
			}
			((IVideoStreamController)mController).setVideoStream(mVideoStream);
			post(new Runnable() {
				@Override
				public void run() {
					if (mVideoStream == null) return;
					final IScreen screen = mModelView.getCurrentScreen();
//					if (DEBUG) Log.v(TAG, "startVideoStreaming:screen=" + screen);
					if (screen instanceof IVideoScreen) {
						try {
							final SurfaceTexture surface = ((IVideoScreen) screen).getVideoTexture();
							if ((surface != null) && (mSurfaceId == 0)) {
								final Surface _surface = new Surface(surface);
								mSurfaceId = _surface.hashCode();
								mVideoStream.addSurface(mSurfaceId, _surface);
								((IVideoScreen) screen).setEnableVideo(true);
							}
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					} else {
						post(this, 300);
					}
				}
			}, 100);
		}
		super.startVideoStreaming();
	}

	@Override
	protected void stopVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "stopVideoStreaming:");
		super.stopVideoStreaming();
		final IScreen screen = mModelView.getCurrentScreen();
		if (screen instanceof IVideoScreen) {
			((IVideoScreen) screen).setEnableVideo(false);
		}
		mSurfaceId = 0;
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
//		if (DEBUG) Log.v(TAG, "onConnect:");
		super.onConnect(controller);
		mVideoRecording = false;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mVideoRecordingBtn.setVisibility(controller instanceof IVideoStreamController ? View.VISIBLE : View.INVISIBLE);
			}
		});
		mModelView.hasGuard((controller instanceof IFlightController) && ((IFlightController)controller).hasGuard());
		setSideMenu();
		startGamePadTask();
		post(mUpdateStatusTask, 100);
		updateButtons();
		// キャリブレーションが必要ならCalibrationFragmentへ遷移させる
		if ((controller instanceof IFlightController) && ((IFlightController)controller).needCalibration()) {
			replace(CalibrationFragment.newInstance(getDevice()));
		}
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
//		if (DEBUG) Log.v(TAG, "#onDisconnect");
		mVideoRecording = false;
		stopRecord();
		stopPlay();
		removeSideMenu();
		stopGamePadTask();
		remove(mUpdateStatusTask);
		requestPopBackStack(POP_BACK_STACK_DELAY);
		super.onDisconnect(controller);
	}

	/**
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	@Override
	protected void updateFlyingState(final int state) {
		updateButtons();
	}

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param alert_state
	 */
	@Override
	protected void updateAlarmState(final int alert_state) {
		runOnUiThread(mUpdateAlarmMessageTask);
		updateButtons();
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	@Override
	protected void updateBattery() {
		runOnUiThread(mUpdateBatteryTask);
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param picture_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updatePictureCaptureState(final int picture_state) {
		switch (picture_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_success, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case DroneStatus.MEDIA_ERROR:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_error, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		}
		updateButtons();
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバック
	 * @param video_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updateVideoRecordingState(final int video_state) {
		switch (video_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_success, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		case DroneStatus.MEDIA_ERROR:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_error, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		}
		updateButtons();
	}

	/**
	 * 移動停止
	 */
	@Override
	protected void stopMove() {
//		if (DEBUG) Log.v(TAG, "stopMove:");
		super.stopMove();
		if (mFlightController != null) {
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
		super.emergencyStop();	// stopMoveはこの中から呼び出されるので自前では呼ばない
		stopPlay();
		stopScript();
		stopTouchMove();
		stopAnimationActionAll();
	}

	private static final int[] SENSOR_TYPES = {
		Sensor.TYPE_MAGNETIC_FIELD,
		Sensor.TYPE_GRAVITY,
		Sensor.TYPE_ACCELEROMETER,
//		Sensor.TYPE_GYROSCOPE,
	};
	private SensorManager mSensorManager;
	private int mRotation;									// 画面の向き
	private final Object mSensorSync = new Object();		// センサー値同期用
	private final float[] mMagnetValues = new float[3];		// 磁気[μT]
	private final float[] mGravityValues = new float[3];	// 重力[m/s^2]
	private final float[] mAzimuthValues = new float[3];	// 方位[-180,+180][度]
//	private final float[] mAccelValues = new float[3];		// 加速度[m/s^2]
//	private final float[] mGyroValues = new float[3];		// ジャイロ[radian/s]

	/**
	 * 磁気センサー・加速度センサー等を読み取り開始
	 * (MiniDroneの時は何もしない)
	 */
	private void startSensor() {
		for (int i = 0; i < 3; i++) {
			mMagnetValues[i] = mGravityValues[i] = mAzimuthValues[i] = 0;
//			mAccelValues[i] = mGyroValues[i] = 0;
		}
		if (mController instanceof FlightControllerMiniDrone) return;
		final Display display = getActivity().getWindowManager().getDefaultDisplay();
		mRotation = display.getRotation();
		// 重力センサーがあればそれを使う。なければ加速度センサーで代用する
		boolean hasGravity = false;
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			for (final int sensor_type : SENSOR_TYPES) {
				final List<Sensor> sensors = mSensorManager.getSensorList(sensor_type);
				if ((sensors != null) && (sensors.size() > 0)) {
					if (sensor_type == Sensor.TYPE_GRAVITY)
						hasGravity = true;
					if (!hasGravity || (sensor_type != Sensor.TYPE_ACCELEROMETER)) {
						mSensorManager.registerListener(mSensorEventListener, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
					}
				}
			}
		}
	}

	/**
	 * 磁気センサー・加速度センサー等からの読み取り終了
	 */
	private void stopSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorEventListener);
			mSensorManager = null;
		}
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		/**
		 * ハイパスフィルターを通して値をセットする
		 * @param values 値を保持するためのfloat配列
		 * @param new_values 新しい値を渡すためのfloat配列
		 * @param alpha フィルター定数(alpha=t/(t+dt)
		 */
		private void highPassFilter(final float[] values, final float[] new_values, final float alpha) {
			values[0] = alpha * values[0] + (1 - alpha) * new_values[0];
			values[1] = alpha * values[1] + (1 - alpha) * new_values[1];
			values[2] = alpha * values[2] + (1 - alpha) * new_values[2];
		}

		private final float[] outR = new float[16];
		private final float[] outR2 = new float[16];
		private void getOrientation(final float[] rotateMatrix, final float[] result) {

			switch (mRotation) {
			case Surface.ROTATION_0:
				SensorManager.getOrientation(rotateMatrix, result);
				return;
			case Surface.ROTATION_90:
				SensorManager.remapCoordinateSystem(
					rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_180:
				SensorManager.remapCoordinateSystem(
					rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR2);
				SensorManager.remapCoordinateSystem(
					outR2, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_270:
				SensorManager.remapCoordinateSystem(
					outR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			}
			SensorManager.getOrientation(outR, result);
		}

		private static final float TO_DEGREE = (float)(180 / Math.PI);
		private final float[] mRotateMatrix = new float[16];			// 回転行列
		private final float[] mInclinationMatrix = new float[16];    	// 傾斜行列

		/**
		 * センサーの値が変化した時のコールバック
		 * @param event
		 */
		@Override
		public void onSensorChanged(final SensorEvent event) {
			final float[] values = event.values;
			final int type = event.sensor.getType();
			switch (type) {
			case Sensor.TYPE_MAGNETIC_FIELD:	// 磁気センサー
				synchronized (mSensorSync) {
					// ハイパスフィルターを通して取得
					// alpha=t/(t+dt), dt≒20msec@SENSOR_DELAY_GAME, tはローパスフィルタの時定数(t=80)
					highPassFilter(mMagnetValues, values, 0.8f);
//					System.arraycopy(values, 0, mMagnetValues, 0, 3);
					// 磁気センサーの値と重力センサーの値から
					SensorManager.getRotationMatrix(mRotateMatrix, mInclinationMatrix, mGravityValues, mMagnetValues);
					getOrientation(mRotateMatrix, mAzimuthValues);
					mAzimuthValues[0] *= TO_DEGREE;
					mAzimuthValues[1] *= TO_DEGREE;
					mAzimuthValues[2] *= TO_DEGREE;
				}
				break;
			case Sensor.TYPE_GRAVITY:			// 重力センサー
				synchronized (mSensorSync) {
					System.arraycopy(values, 0, mGravityValues, 0, 3);
				}
				break;
			case Sensor.TYPE_ACCELEROMETER:		// 加速度センサー
				synchronized (mSensorSync) {
//					System.arraycopy(values, 0, mAccelValues, 0, 3);
					System.arraycopy(values, 0, mGravityValues, 0, 3);	// 重力センサーが無い時は加速度センサーで代用
				}
				break;
//			case Sensor.TYPE_GYROSCOPE:			// ジャイロセンサー
//				synchronized (mSensorSync) {
//					System.arraycopy(values, 0, mGyroValues, 0, 3);
//				}
//				break;
			default:
//				if (DEBUG) Log.v(TAG, "onSensorChanged:" + String.format("その他%d(%f,%f,%f)", type, values[0], values[1], values[2]));
				break;
			}
		}

		/**
		 * センサーの精度が変更された時のコールバック
		 * @param sensor
		 * @param accuracy
		 */
		@Override
		public void onAccuracyChanged(final Sensor sensor, int accuracy) {
		}
	};


	/**
	 * 離陸指示
	 */
	private void takeOff() {
		// 離陸指示
		if (mFlightController != null) {
			mFlightController.requestTakeoff();
			mFlightRecorder.record(FlightRecorder.CMD_TAKEOFF);
		}
	}

	/**
	 * 着陸指示
	 */
	private void landing() {
		// 着陸指示
		stopMove();
		if (mFlightController != null) {
			mFlightController.requestLanding();
			mFlightRecorder.record(FlightRecorder.CMD_LANDING);
		}
	}

	/**
	 * 記録開始
	 * @param needClear 既存の記録を破棄してから記録開始するかどうか
	 */
	private void startRecord(final boolean needClear) {
//		if (DEBUG) Log.v(TAG, "startRecord:");
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
//		if (DEBUG) Log.v(TAG, "stopRecord:");
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
//		if (DEBUG) Log.v(TAG, "startPlay:");
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
//		if (DEBUG) Log.v(TAG, "stopPlay:");
		if (mFlightRecorder.isPlaying()) {
			mFlightRecorder.stop();
			updateButtons();
		}
	}

//	private static final int SCRIPT_NUM = 6;
	/**
	 * 左サイドメニューの項目
	 */
/*	private static final int[] SIDE_MENU_ITEMS = {
		R.string.script_circle_xy,				// スクリプト
		R.string.script_circle_xz,				// スクリプト
		R.string.script_revolution_xr,			// スクリプト
		R.string.script_revolution_yr,			// スクリプト
		R.string.script_revolution_xr2,			// スクリプト
		R.string.script_revolution_yr2,			// スクリプト
		// 以下アニメーション動作。動かないのでコメントアウト実質jumpingsumo専用なのかも
//		R.string.anim_headlights_flash,
//		R.string.anim_headlights_blink,
//		R.string.anim_headlights_oscillation,
//		R.string.anim_spin,
//		R.string.anim_tap,
//		R.string.anim_slow_shake,
//		R.string.anim_metronome,
//		R.string.anim_ondulation,
//		R.string.anim_spin_jump,
//		R.string.anim_spin_to_posture,
//		R.string.anim_spiral,
//		R.string.anim_slalome,
//		R.string.anim_boost,
	}; */

	private final List<ScriptHelper.ScriptRec> mScripts = new ArrayList<ScriptHelper.ScriptRec>();

	private List<String> setupScript() {
		final List<String> result = new ArrayList<String>();

		final SharedPreferences pref = getActivity().getPreferences(0);
		try {
			ScriptHelper.loadScripts(pref, mScripts);
			final int n = mScripts.size();
			for (int i = 0; i < n; i++) {
				result.add(mScripts.get(i).name);
			}
/*			for (int i = 0; i < SIDE_MENU_ITEMS.length; i++) {
				result.add(getString(SIDE_MENU_ITEMS[i]));
			} */

		} catch (final IOException e) {
			Log.w(TAG, e);
		}
		return result;
	}

	/**
	 * スクリプト実行開始
	 */
	private void startScript(final int index) {
//		if (DEBUG) Log.v(TAG, "startScript:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			mScriptRunning = true;
			try {
				final int n = mScripts.size();
				if ((index >= 0) && (index < n)) {
					mScriptFlight.prepare(new FileInputStream(mScripts.get(index).path), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
				} else {
					throw new IOException("スクリプトファイルが見つからない(範囲外):" + index);
				}
/*				switch (index) {
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
				case 4:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_xr2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 5:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_yr2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				default:
					throw new IOException("スクリプトファイルが見つからない(範囲外):" + index);
				} */
			} catch (final IOException e) {
				mScriptRunning = false;
				ScriptHelper.appendLog(getActivity(), e.getMessage());
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * スクリプト実行終了
 	 */
	private void stopScript() {
//		if (DEBUG) Log.v(TAG, "stopScript:");
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
//		if (DEBUG) Log.v(TAG, "startTouchMove:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isPlaying()) {
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
//		if (DEBUG) Log.v(TAG, "stopTouchMove:");
		mTouchMoveRunning = false;
		if (mTouchFlight.isPlaying()) {
			mTouchFlight.stop();
			updateButtons();
		}
	}

	/**
	 * アニメーション動作開始指示
	 * @param anim
	 */
	private void startAnimationAction(final int anim) {
		if (!mFlightRecorder.isPlaying()) {
			mFlightController.startAnimation(anim);
			updateButtons();
		}
	}

	/**
	 * アニメーション動作停止指示
	 * @param anim
	 */
	private void stopAnimationAction(final int anim) {
		if (mFlightController != null) {
			mFlightController.stopAnimation(anim);
		}
	}

	/**
	 * アニメーション動作を全て停止
	 */
	private void stopAnimationActionAll() {
		if (mFlightController != null) {
			mFlightController.stopAllAnimation();
		}
		updateButtons();
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
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onPrepared:");
			if (mScriptRunning) {
				// スクリプト操縦
				if (mScriptFlight.isPrepared()) {
					mScriptFlight.play();
				} else {
					mScriptRunning = false;
				}
			} else if (mTouchMoveRunning) {
				// タッチ描画操縦
				if (mTouchFlight.isPrepared()) {
					mTouchFlight.play();
				} else {
					mTouchMoveRunning = false;
				}
			} else if (mFlightRecorder.isPrepared()) {
				// 飛行記録を再生
				mFlightRecorder.pos(0);
				mFlightRecorder.play();
			}

			updateButtons();
		}

		@Override
		public void onStart() {
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onStart:");
			updateTime(0);
			updateButtons();
		}

		@Override
		public boolean onStep(final int cmd, final int[] values, final long t) {
//			if (DEBUG) Log.v(TAG, String.format("mAutoFlightListener#onStep:cmd=%d,t=%d,v=[%s]" , cmd, t, asString(values)));
			updateTime(t);
			if (mFlightController != null) {
				switch (cmd) {
				case IAutoFlight.CMD_EMERGENCY:			// 非常停止
					mFlightController.requestEmergencyStop();
					updateButtons();
					break;
				case IAutoFlight.CMD_TAKEOFF:			// 離陸
					mFlightController.requestTakeoff();
					updateButtons();
					break;
				case IAutoFlight.CMD_LANDING:			// 着陸
					mFlightController.requestLanding();
					updateButtons();
					break;
				case IAutoFlight.CMD_UP_DOWN:			// 上昇:gaz>0, 下降: gaz<0
					mFlightController.setGaz(values[0]);
					break;
				case IAutoFlight.CMD_RIGHT_LEFT:		// 右: roll>0,flag=1 左: roll<0,flag=1
					mFlightController.setRoll(values[0]);
					mFlightController.setFlag((values[0] != 0 ? 1 : 0));
					break;
				case IAutoFlight.CMD_FORWARD_BACK:		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
					mFlightController.setPitch(values[0]);
					break;
				case IAutoFlight.CMD_TURN:				// 右回転: yaw>0, 左回転: ywa<0
					mFlightController.setYaw(values[0]);
					break;
				case IAutoFlight.CMD_COMPASS:			// 北磁極に対する角度 -360〜360度
					mFlightController.setHeading(values[0]);	// 実際は浮動小数点だけど
					break;
				case IAutoFlight.CMD_MOVE5:
					mFlightController.setMove(values[0], values[1], values[2], values[3], values[4]);
					break;
				case IAutoFlight.CMD_MOVE4:
					mFlightController.setMove(values[0], values[1], values[2], values[3]);
					break;
				case IAutoFlight.CMD_MOVE3:
					mFlightController.setMove(values[0], values[1], values[2]);
					break;
				case IAutoFlight.CMD_MOVE2:
					mFlightController.setMove(values[0], values[1]);
					break;
				case IAutoFlight.CMD_FLIP:				// フリップ
					mFlightController.requestAnimationsFlip(values[0]);
					break;
				case IAutoFlight.CMD_CAP:				// キャップ(指定角度水平回転)
					mFlightController.requestAnimationsCap(values[0]);
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
		public float getValues(int axis) {
			if (axis >= 0 && axis < 3) {
				final Vector attitude = new Vector(mFlightController.getAttitude());
				attitude.toDegree();
				switch (axis) {
				case 0:    // roll
					return attitude.x;
				case 1:    // pitch
					return attitude.y;
				case 2:    // yaw
					return attitude.z;
				}
			} else {
				switch (axis) {
				case 3:    // gaz
					return mFlightController.getAltitude();
				case 4:    // FIXME compass
					return 0;
				case 5:		// max_tilt
					return mFlightController.getMaxTilt().current();
				case 6:		// max_rotation_speed
					return mFlightController.getMaxRotationSpeed().current();
				case 7:		// max_vertical_speed
					return mFlightController.getMaxVerticalSpeed().current();

				}
			}
			return 0;
		}

		@Override
		public void onStop() {
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onStop:");
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
			updateButtons();
			Log.w(TAG, e);
			ScriptHelper.appendLog(getActivity(), e.getMessage());
		}

	};

	// 機体姿勢と高度
	private float mCurrentRoll = 0;
	private float mCurrentPitch = 0;
	private float mCurrentYaw = 0;
	private float mCurrentAltitude = 0;
	/** 定期的にステータスをポーリングして処理するスレッドの実行部 */
	private final Runnable mUpdateStatusTask = new Runnable() {
		private final Vector mAttitude = new Vector();
		private int prevState;
		@Override
		public void run() {
			if (mFlightController != null) {
				if (mFlightController.canGetAttitude()) {
					// 機体姿勢を取得できる時
					mAttitude.set(mFlightController.getAttitude());
					mAttitude.toDegree();	// ラジアンを度に変換
					final float altitude = mFlightController.getAltitude();
					float yaw = mAzimuthValues[0] - mAttitude.z();
					if ((mCurrentRoll != mAttitude.x())
						|| (mCurrentPitch != mAttitude.y())
						|| (mCurrentYaw != yaw)
						|| (mCurrentAltitude != altitude)) {

							mCurrentRoll = mAttitude.x();
							mCurrentPitch = mAttitude.y();
							mCurrentYaw = yaw;
							mCurrentAltitude = altitude;
							mModelView.setAttitude(mCurrentRoll, mCurrentPitch, yaw, altitude);
					}
				}
			}
			if (mController instanceof ICameraController) {
				// FIXME カメラの方向を更新する
			}
			if (prevState != getState()) {
				prevState = getState();
				updateButtons();
			}
			post(this, 50);	// 50ミリ秒=1秒間に最大で約20回更新
		}
	};

	/** アラート表示の更新処理をUIスレッドで実行するためのRunnable */
	private final Runnable mUpdateAlarmMessageTask = new Runnable() {
		@Override
		public void run() {
			final int alarm = getAlarm();
//			if (DEBUG) Log.w(TAG, "mUpdateAlarmMessageTask:alarm=" + alarm);
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
			final int battery = mFlightController != null ? mFlightController.getBattery() : -1;
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
			final int still_capture_state = getStillCaptureState();
			final int video_recording_state = getVideoRecordingState();
			final boolean is_connected = isConnected();
			final boolean is_recording = mFlightRecorder.isRecording();
			final boolean is_playing = mFlightRecorder.isPlaying();
			final boolean can_play = is_connected && !is_recording && !mScriptRunning && !mTouchMoveRunning && (alarm_state == DroneStatus.ALARM_NON) && (mFlightRecorder.size() > 0);
			final boolean can_record = is_connected && !is_playing && !mScriptRunning;
			final boolean can_load = is_connected && !is_playing && !is_recording && !mTouchMoveRunning;
			final boolean can_fly = can_record && (alarm_state == DroneStatus.ALARM_NON);
			final boolean can_flattrim = can_fly && (state == IFlightController.STATE_STARTED);
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
			switch (state & IFlightController.STATE_MASK_FLYING) {
			case DroneStatus.STATE_FLYING_LANDED:		// 0x0000;		// FlyingState=0
				mModelView.stopEngine();
			case DroneStatus.STATE_FLYING_LANDING:		// 0x0400;		// FlyingState=4
				mTakeOnOffBtn.setImageResource(R.mipmap.ic_takeoff);
				break;
			case DroneStatus.STATE_FLYING_TAKEOFF:		// 0x0100;		// FlyingState=1
			case DroneStatus.STATE_FLYING_HOVERING:		// 0x0200;		// FlyingState=2
			case DroneStatus.STATE_FLYING_FLYING:		// 0x0300;		// FlyingState=3
			case DroneStatus.STATE_FLYING_ROLLING:		// 0x0600;		// FlyingState=6
				mTakeOnOffBtn.setImageResource(R.mipmap.ic_landing);
				mModelView.startEngine();
				break;
			case DroneStatus.STATE_FLYING_EMERGENCY:	// 0x0500;		// FlyingState=5
				mModelView.stopEngine();
				break;
			}

			// 右サイドパネル(とmCapXXXBtn等)
			mRightSidePanel.setEnabled(can_fly);

			mStillCaptureBtn.setEnabled(still_capture_state == DroneStatus.MEDIA_READY);
			mStillCaptureBtn.setVisibility(still_capture_state != DroneStatus.MEDIA_UNAVAILABLE ? View.VISIBLE : View.INVISIBLE);

			mVideoRecordingBtn.setEnabled((video_recording_state == DroneStatus.MEDIA_READY) || (video_recording_state == DroneStatus.MEDIA_BUSY));
			mStillCaptureBtn.setVisibility(video_recording_state != DroneStatus.MEDIA_UNAVAILABLE ? View.VISIBLE : View.INVISIBLE);
			mVideoRecordingBtn.setColorFilter(mVideoRecording ? 0x7fff0000 : 0);
//			mVideoRecordingBtn.setImageResource(mVideoRecording ? android.R.drawable.presence_video_busy : android.R.drawable.presence_video_online);

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

//	private void startUsbDriver() {
//		synchronized (mUsbSync) {
//			if (mUSBMonitor == null) {
//				final Activity activity = getActivity();
//				final Context context = (activity != null && !activity.isFinishing()) ? activity.getApplicationContext() : null;
//				if (context != null) {
//					mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
//					// こっちのデバイスフィルター定義はHIDと対応するゲームパッドのすべてを選択可能
//					final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(context, R.xml.device_filter_hid_all);
//					mUSBMonitor.setDeviceFilter(filters);
//				}
//			}
//		}
//	}

//	private void releaseUsbDriver() {
//		synchronized (mUsbSync) {
//			releaseGamepad();
//			if (mUSBMonitor != null) {
//				mUSBMonitor.unregister();
//				mUSBMonitor.destroy();
//				mUSBMonitor = null;
//			}
//		}
//	}

//	private void releaseGamepad() {
//		synchronized (mUsbSync) {
//			if (mHIDGamepad != null) {
//				mHIDGamepad.release();
//				mHIDGamepad = null;
//			}
//		}
//	}

//	private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
//		@Override
//		public void onAttach(final UsbDevice device) {
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
//			synchronized (mUsbSync) {
//				if (mUSBMonitor != null) {
//					UsbDevice _device = device;
//					if ((_device == null) && (mUSBMonitor.getDeviceCount() > 0)) {
//						_device = mUSBMonitor.getDeviceList().get(0);
//					}
//					if (mHIDGamepad == null) {
//						mUSBMonitor.requestPermission(_device);
//					}
//				}
//			}
//		}
//
//		@Override
//		public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
//			synchronized (mUsbSync) {
//				if (mHIDGamepad == null) {
//					mHIDGamepad = new HIDGamepad(/*mHIDGamepadCallback*/);
//					mHIDGamepad.open(ctrlBlock);
//				}
//			}
//		}
//
//		@Override
//		public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
//			releaseGamepad();
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:finished");
//		}
//
//		@Override
//		public void onDettach(final UsbDevice device) {
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
//			releaseGamepad();
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:finished");
//		}
//
//		@Override
//		public void onCancel() {
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
//			releaseGamepad();
//			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:finished");
//		}
//	};

	/** ゲームパッド読み取りスレッド操作用Handler */
	private Handler mGamePadHandler;
	/** ゲームパッド読み取りスレッド開始 */
	private void startGamePadTask() {
		if (mGamePadHandler == null) {
			final HandlerThread thread = new HandlerThread("GamePadThread");
			thread.start();
			mGamePadHandler = new Handler(thread.getLooper());
		}
		mGamePadHandler.removeCallbacks(mGamePadTask);
		mGamePadHandler.postDelayed(mGamePadTask, 100);
	}

	/** ゲームパッド読み取りスレッド終了 */
	private void stopGamePadTask() {
		if (mGamePadHandler != null) {
			final Handler handler = mGamePadHandler;
			mGamePadHandler = null;
			handler.removeCallbacks(mGamePadTask);
			handler.getLooper().quit();
		}
	}

	private static final long YAW_LIMIT = 200;
//	private final Object mUsbSync = new Object();
//	private USBMonitor mUSBMonitor;
//	private HIDGamepad mHIDGamepad;
//	private final KeyGamePad mKeyGamePad = KeyGamePad.getInstance();
	private Joystick mJoystick;
	private final boolean[] downs = new boolean[GamePadConst.KEY_NUMS];
	private final long[] down_times = new long[GamePadConst.KEY_NUMS];
	private final int[] analogSticks = new int[4];
	boolean moved;
	/** ゲームパッド読み取りスレッドの実行部 */
	private final Runnable mGamePadTask = new Runnable() {
		private int mCurrentPan = Integer.MAX_VALUE, mCurrentTilt = Integer.MAX_VALUE;

		@Override
		public void run() {
			final Handler handler = mGamePadHandler;
			if (handler == null) return;	// 既に終了指示が出てる

			long interval = 50;
			handler.removeCallbacks(this);
			try {
				if (mJoystick != null) {
					mJoystick.updateState(downs, down_times, analogSticks, false);
				}
/*				if ((mUSBMonitor != null) && (mHIDGamepad != null)) {
					mHIDGamepad.updateState(downs, down_times, analogSticks, false);
				} else if (mJoystick != null) {
					mJoystick.updateState(downs, down_times, analogSticks, false);
				} else {
//					mKeyGamePad.updateState(downs, down_times, analogSticks, false);
				} */

				// 左右の上端ボタン(手前側)を同時押しすると非常停止
				if (((downs[GamePadConst.KEY_RIGHT_RIGHT] || downs[GamePadConst.KEY_RIGHT_1]))
					&& (downs[GamePadConst.KEY_RIGHT_LEFT] || downs[GamePadConst.KEY_LEFT_1]) ) {
					emergencyStop();
					return;
				}

				// 飛行していない時にL2/R2同時押しするとフラットトリム実行
				if ((getState() == IFlightController.STATE_STARTED)
					&& (getAlarm() == DroneStatus.ALARM_NON)
					&& downs[GamePadConst.KEY_LEFT_2] && downs[GamePadConst.KEY_RIGHT_2]) {

					mFlightController.requestFlatTrim();
					return;
				}

				// L2押しながら左アナログスティックでカメラのpan/tilt
				if (downs[GamePadConst.KEY_LEFT_2] && (mController instanceof ICameraController)) {
					if (mCurrentPan > 100) {
						mCurrentPan = ((ICameraController)mController).getPan();
						mCurrentTilt = ((ICameraController)mController).getTilt();
					}
					final int pan = mCurrentPan;
					final int tilt = mCurrentTilt;
					// 左アナログスティックの左右=左右移動
					int p = pan + (int)(analogSticks[0] / 51.2f);
					if (p < -100) {
						p = -100;
					} else if (p > 100) {
						p = 100;
					}
					// 左アナログスティックの上下=前後移動
					int t = tilt + (int)(analogSticks[1] / 51.2f);
					if (t < -100) {
						t = -100;
					} else if (t > 100) {
						t = 100;
					}
//					if (DEBUG) Log.d(TAG, String.format("(%d,%d),pan=%d/%d,tilt=%d/%d", analogSticks[0], analogSticks[1], pan, p, tilt, t));
					if ((p != pan) || (t != tilt)) {
						((ICameraController)mController).sendCameraOrientation(t, p);
						mCurrentPan = p;
						mCurrentTilt = t;
						return;
					}
					interval = 20;
				} else {
					mCurrentPan = mCurrentTilt = Integer.MAX_VALUE;
				}

				// R2押しながら左スティックでフリップ
				if (downs[GamePadConst.KEY_RIGHT_2]) {
					if (downs[GamePadConst.KEY_LEFT_LEFT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_LEFT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_RIGHT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_RIGHT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_UP]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_FRONT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_DOWN]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_BACK);
						return;
					}
				}

				// 中央の右側ボタン[12]=着陸
				if (downs[GamePadConst.KEY_CENTER_RIGHT]) {
					landing();
					return;
				}
				// 中央の左側ボタン[11]=離陸
				if (downs[GamePadConst.KEY_CENTER_LEFT]) {
					takeOff();
					return;
				}
				// ここまでは共通操作の処理

				// 操作モード毎の処理
				switch (mOperationType) {
				case 0:	// 通常
					gamepad_normal();
					break;
				case 1:	// 左右反転
					gamepad_reverse();
					break;
				case 2:	// mode1
					gamepad_mode1();
					break;
				case 3:	// mode2
					gamepad_mode2();
					break;
				default:
					gamepad_normal();
					break;
				}
	//			KeyGamePad.KEY_LEFT_CENTER:		// = 0;
	//			KeyGamePad.KEY_LEFT_UP:			// = 1;
	//			KeyGamePad.KEY_LEFT_RIGHT:		// = 2;
	//			KeyGamePad.KEY_LEFT_DOWN:		// = 3;
	//			KeyGamePad.KEY_LEFT_LEFT:		// = 4;
	//			KeyGamePad.KEY_RIGHT_CENTER:	// = 5;
	//			KeyGamePad.KEY_RIGHT_UP:		// = 6;
	//			KeyGamePad.KEY_RIGHT_RIGHT:		// = 7;
	//			KeyGamePad.KEY_RIGHT_DOWN:		// = 8;
	//			KeyGamePad.KEY_RIGHT_LEFT:		// = 9;
	//			KeyGamePad.KEY_LEFT_1:			// = 10;	// 左上前
	//			KeyGamePad.KEY_LEFT_2:			// = 11;	// 左上後
	//			KeyGamePad.KEY_CENTER_LEFT:		// = 12;	// 中央左
	//			KeyGamePad.KEY_RIGHT_1:			// = 13;	// 右上前
	//			KeyGamePad.KEY_RIGHT_2:			// = 14;	// 右上後
	//			KeyGamePad.KEY_CENTER_RIGHT:	// = 15;	// 中央右
			} finally {
				handler.postDelayed(this, interval);
			}
		}
	};

	/**
	 * ゲームパッド操作時の実際の移動コマンド発行処理
	 * @param roll
	 * @param pitch
	 * @param gaz
	 * @param yaw
	 */
	private void gamepad_move(final float roll, final float pitch, final float gaz, final float yaw) {
		if ((roll != 0) || (pitch != 0) || (gaz != 0) || (yaw != 0)) {
//			if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", roll, pitch, gaz, yaw));
			if (mFlightController != null) {
				moved = true;
				mFlightController.setMove((int) roll, (int) pitch, (int) gaz, (int) yaw);
				mFlightRecorder.record(FlightRecorder.CMD_MOVE4, (int)roll, (int)pitch, (int)gaz, (int)yaw);
			}
		} else if (moved) {
			if (mFlightController != null) {
				mFlightController.setMove(0, 0, 0, 0, 0);
				mFlightRecorder.record(FlightRecorder.CMD_MOVE4, (int) 0, (int) 0, (int) 0, (int) 0);
			}
			moved = false;
//			if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", 0f, 0f, 0f, 0f));
		}
	}

	/** 通常操作モードでのゲームパッド入力処理 */
	private void gamepad_normal() {
/*
		// 右側十字キーまたは右側アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * (downs[GamePadConst.KEY_RIGHT_RIGHT]
			? down_times[GamePadConst.KEY_RIGHT_RIGHT]
			: (downs[GamePadConst.KEY_RIGHT_LEFT]
				? -down_times[GamePadConst.KEY_RIGHT_LEFT]
				: 0)
		);
		// 右側十字キーまたは右側アナログスティックの上下=前後移動
		final float pitch = mGamepadSensitivity * mGamepadScaleY * (downs[GamePadConst.KEY_RIGHT_UP]
			? down_times[GamePadConst.KEY_RIGHT_UP]
			: (downs[GamePadConst.KEY_RIGHT_DOWN]
				? -down_times[GamePadConst.KEY_RIGHT_DOWN]
				: 0)
		);
		// 左側アナログスティックの上下=上昇下降
		final float gaz = mGamepadSensitivity * mGamepadScaleZ * (downs[GamePadConst.KEY_LEFT_UP]
			? down_times[GamePadConst.KEY_LEFT_UP]
			: (downs[GamePadConst.KEY_LEFT_DOWN]
				? -down_times[GamePadConst.KEY_LEFT_DOWN]
				: 0)
		);
		// 左側アナログスティックの左右または上端ボタン(手前側)=左右回転
		final float yaw = mGamepadSensitivity * (downs[GamePadConst.KEY_LEFT_RIGHT] && (down_times[GamePadConst.KEY_LEFT_RIGHT] > YAW_LIMIT)
			? down_times[GamePadConst.KEY_LEFT_RIGHT] - YAW_LIMIT
			: (
				downs[GamePadConst.KEY_RIGHT_1]
				? down_times[GamePadConst.KEY_RIGHT_1]
				: (
					downs[GamePadConst.KEY_LEFT_LEFT] && (down_times[GamePadConst.KEY_LEFT_LEFT] > YAW_LIMIT)
					? -down_times[GamePadConst.KEY_LEFT_LEFT] + YAW_LIMIT
					: (
						downs[GamePadConst.KEY_LEFT_1]
						? -down_times[GamePadConst.KEY_LEFT_1]
						: 0
					)
				)
			)
		);
*/
		// 右アナログスティックの左右=左右移動
		final float roll = analogSticks[2] * mGamepadSensitivity * mGamepadScaleX;
		// 右アナログスティックの上下=前後移動
		final float pitch = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleY;
		// 左アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleZ;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** 左右反転操作モードでのゲームパッド入力処理 */
	private void gamepad_reverse() {
/*
		// 左側十字キーまたは左側アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * (downs[GamePadConst.KEY_LEFT_RIGHT]
			? down_times[GamePadConst.KEY_LEFT_RIGHT]
			: (downs[GamePadConst.KEY_LEFT_LEFT]
				? -down_times[GamePadConst.KEY_LEFT_LEFT]
				: 0)
		);
		// 左側十字キーまたは左側アナログスティックの上下=前後移動
		final float pitch = mGamepadSensitivity * mGamepadScaleY * (downs[GamePadConst.KEY_LEFT_UP]
			? down_times[GamePadConst.KEY_LEFT_UP]
			: (downs[GamePadConst.KEY_LEFT_DOWN]
				? -down_times[GamePadConst.KEY_LEFT_DOWN]
				: 0)
		);
		// 右側アナログスティックの上下=上昇下降
		final float gaz = mGamepadSensitivity * mGamepadScaleZ * (downs[GamePadConst.KEY_RIGHT_UP]
			? down_times[GamePadConst.KEY_RIGHT_UP]
			: (downs[GamePadConst.KEY_RIGHT_DOWN]
				? -down_times[GamePadConst.KEY_RIGHT_DOWN]
				: 0)
		);
		// 右側アナログスティックの左右または上端ボタン(手前側)=左右回転
		final float yaw = mGamepadSensitivity * (downs[GamePadConst.KEY_RIGHT_RIGHT] && (down_times[GamePadConst.KEY_RIGHT_RIGHT] > YAW_LIMIT)
			? down_times[GamePadConst.KEY_RIGHT_RIGHT] - YAW_LIMIT
			: (
				downs[GamePadConst.KEY_RIGHT_1]
				? down_times[GamePadConst.KEY_RIGHT_1]
				: (
					downs[GamePadConst.KEY_RIGHT_LEFT] && (down_times[GamePadConst.KEY_RIGHT_LEFT] > YAW_LIMIT)
					? -down_times[GamePadConst.KEY_RIGHT_LEFT] + YAW_LIMIT
					: (
						downs[GamePadConst.KEY_LEFT_1]
						? -down_times[GamePadConst.KEY_LEFT_1]
						: 0
					)
				)
			)
		);
*/
		// 左アナログスティックの左右=左右移動
		final float roll = analogSticks[0] * mGamepadSensitivity * mGamepadScaleX;
		// 左アナログスティックの上下=前後移動
		final float pitch = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleY;
		// 右アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleZ;
		// 右アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[2])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** モード1でのゲームパッド入力処理 */
	private void gamepad_mode1() {
		// モード1
		// 右スティック: 左右=左右移動, 上下=上昇下降
		// 左スティック: 左右=左右回転, 上下=前後移動
/*
		// 右側アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * (downs[GamePadConst.KEY_RIGHT_RIGHT]
			? down_times[GamePadConst.KEY_RIGHT_RIGHT]
			: (downs[GamePadConst.KEY_RIGHT_LEFT]
				? -down_times[GamePadConst.KEY_RIGHT_LEFT]
				: 0)
		);
		// 左側十字キーまたは左側アナログスティックの上下=前後移動
		final float pitch = mGamepadSensitivity * mGamepadScaleY * (downs[GamePadConst.KEY_LEFT_UP]
			? down_times[GamePadConst.KEY_LEFT_UP]
			: (downs[GamePadConst.KEY_LEFT_DOWN]
				? -down_times[GamePadConst.KEY_LEFT_DOWN]
				: 0)
		);
		// 右側アナログスティックの上下=上昇下降
		final float gaz = mGamepadSensitivity * mGamepadScaleZ * (downs[GamePadConst.KEY_RIGHT_UP]
			? down_times[GamePadConst.KEY_RIGHT_UP]
			: (downs[GamePadConst.KEY_RIGHT_DOWN]
				? -down_times[GamePadConst.KEY_RIGHT_DOWN]
				: 0)
		);
		// 左側十字キーまたは左側アナログスティックの左右=左右回転
		final float yaw = mGamepadSensitivity * (downs[GamePadConst.KEY_LEFT_RIGHT] && (down_times[GamePadConst.KEY_LEFT_RIGHT] > YAW_LIMIT)
			? down_times[GamePadConst.KEY_LEFT_RIGHT] - YAW_LIMIT
			: (
				downs[GamePadConst.KEY_RIGHT_1]
				? down_times[GamePadConst.KEY_RIGHT_1]
				: (
					downs[GamePadConst.KEY_LEFT_LEFT] && (down_times[GamePadConst.KEY_LEFT_LEFT] > YAW_LIMIT)
					? -down_times[GamePadConst.KEY_LEFT_LEFT] + YAW_LIMIT
					: (
						downs[GamePadConst.KEY_LEFT_1]
						? -down_times[GamePadConst.KEY_LEFT_1]
						: 0
					)
				)
			)
		);
*/
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=前後移動
		final float pitch = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleY;
		// 右アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleZ;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** モード2でのゲームパッド入力処理 */
	private void gamepad_mode2() {
		// モード2
		// 右スティック: 左右=左右移動, 上下=前後移動
		// 左スティック: 左右=左右回転, 上下=上昇下降
/*
		// 右側アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * (downs[GamePadConst.KEY_RIGHT_RIGHT]
			? down_times[GamePadConst.KEY_RIGHT_RIGHT]
			: (downs[GamePadConst.KEY_RIGHT_LEFT]
				? -down_times[GamePadConst.KEY_RIGHT_LEFT]
				: 0)
		);
		// 左側十字キーまたは左側アナログスティックの上下=上昇下降
		final float gaz = mGamepadSensitivity * mGamepadScaleY * (downs[GamePadConst.KEY_LEFT_UP]
			? down_times[GamePadConst.KEY_LEFT_UP]
			: (downs[GamePadConst.KEY_LEFT_DOWN]
				? -down_times[GamePadConst.KEY_LEFT_DOWN]
				: 0)
		);
		// 右側アナログスティックの上下=前後移動
		final float pitch = mGamepadSensitivity * mGamepadScaleZ * (downs[GamePadConst.KEY_RIGHT_UP]
			? down_times[GamePadConst.KEY_RIGHT_UP]
			: (downs[GamePadConst.KEY_RIGHT_DOWN]
				? -down_times[GamePadConst.KEY_RIGHT_DOWN]
				: 0)
		);
		// 左側十字キーまたは左側アナログスティックの左右または上端ボタン(手前側)=左右回転
		final float yaw = mGamepadSensitivity * (downs[GamePadConst.KEY_LEFT_RIGHT] && (down_times[GamePadConst.KEY_LEFT_RIGHT] > YAW_LIMIT)
			? down_times[GamePadConst.KEY_LEFT_RIGHT] - YAW_LIMIT
			: (
				downs[GamePadConst.KEY_RIGHT_1]
				? down_times[GamePadConst.KEY_RIGHT_1]
				: (
					downs[GamePadConst.KEY_LEFT_LEFT] && (down_times[GamePadConst.KEY_LEFT_LEFT] > YAW_LIMIT)
					? -down_times[GamePadConst.KEY_LEFT_LEFT] + YAW_LIMIT
					: (
						downs[GamePadConst.KEY_LEFT_1]
						? -down_times[GamePadConst.KEY_LEFT_1]
						: 0
					)
				)
			)
		);
*/
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleZ;
		// 右アナログスティックの上下=前後移動
		final float pitch = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleY;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	private void setSideMenu() {
//		if (DEBUG) Log.v(TAG, "setSideMenu:");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final MainActivity activity = (MainActivity)getActivity();
				if (activity == null || activity.isFinishing()) return;

				final List<String> labelList = setupScript();
				if (labelList.size() > 0) {
					if (mSideMenuListView == null) {
						mSideMenuListView = new SideMenuListView(activity);
						activity.setSideMenuView(mSideMenuListView);
						mSideMenuListView.setOnItemClickListener(mOnItemClickListener);
					}
					ListAdapter adapter = mSideMenuListView.getAdapter();
					if (adapter instanceof SideMenuAdapter) {
						((SideMenuAdapter)adapter).clear();
						if (labelList.size() > 0) {
							((SideMenuAdapter)adapter).addAll(labelList);
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
			}
		});
	}

	private void removeSideMenu() {
//		if (DEBUG) Log.v(TAG, "removeSideMenu:");
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
//			if (DEBUG) Log.v(TAG, "onItemClick:" + position);
			final MainActivity activity = (MainActivity)getActivity();
			activity.closeSideMenu();
			startScript(position);
/*			if ((position >= 0) && (position < SCRIPT_NUM)) {
				startScript(position);
			} else {
				startAnimationAction(position - SCRIPT_NUM);	// これは新minidrone以外だと動かないのでコメントアウト
			} */
		}
	};

	private int mSurfaceId = 0;
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:");
			if ((mVideoStream != null) && (mSurfaceId == 0)) {
				final Surface _surface = new Surface(surface);
				mSurfaceId = _surface.hashCode();
				mVideoStream.addSurface(mSurfaceId, _surface);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:");
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:");
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
