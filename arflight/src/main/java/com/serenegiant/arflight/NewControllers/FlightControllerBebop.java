package com.serenegiant.arflight.NewControllers;

import android.content.Context;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.CameraControllerListener;
import com.serenegiant.arflight.ICameraController;
import com.serenegiant.arflight.IVideoStream;
import com.serenegiant.arflight.IWiFiController;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public class FlightControllerBebop extends FlightController implements ICameraController, IWiFiController {

	public FlightControllerBebop(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
	}

	@Override
	protected boolean startNetwork() {
		final boolean failed = super.startNetwork();
		if (!failed) {
			mARDeviceController.addStreamListener(mStreamListener);
		}
		return failed;
	}


	private final ARDeviceControllerStreamListener mStreamListener = new ARDeviceControllerStreamListener() {
		@Override
		public ARCONTROLLER_ERROR_ENUM configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
			FlightControllerBebop.this.configureDecoder(deviceController, codec);
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public ARCONTROLLER_ERROR_ENUM onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
			FlightControllerBebop.this.onFrameReceived(deviceController, frame);
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public void onFrameTimeout(final ARDeviceController deviceController) {
			FlightControllerBebop.this.onFrameTimeout(deviceController);
		}
	};

	protected void configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
	}

	protected void onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
	}

	protected void onFrameTimeout(final ARDeviceController deviceController) {
	}

	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerDictionary elementDictionary) {

		super.onCommandReceived(deviceController, commandKey, elementDictionary);
  		if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED) && (elementDictionary != null)) {
			// if event received is the flying state update
			final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
			if (args != null) {
				final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state
					= ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(
						(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));
				// 飛行状態が変化した時の処理 FIXME 未実装
			}
		} else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED) && (elementDictionary != null)){
			// if event received is the picture notification
			final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
			if (args != null) {
				final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error
					= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue(
						(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR));
				// 写真を撮影した時の処理 FIXME 未実装
			}
		}
	}

//--------------------------------------------------------------------------------
// ICameraControllerのメソッド
//--------------------------------------------------------------------------------
	private CameraControllerListener mCameraControllerListener;
	@Override
	public void setCameraControllerListener(final CameraControllerListener listener) {
		mCameraControllerListener = listener;
	}

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera, 3:take jpeg fisheye image only
	 * @return
	 */
	@Override
	public boolean sendPictureFormat(final int pictureFormat) {
		final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM type
			= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM.getFromValue(pictureFormat);
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_RAW (0, "Take raw image"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG (1, "Take a 4:3 jpeg photo"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_SNAPSHOT (2, "Take a 16:9 snapshot from camera"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG_FISHEYE (3, "Take jpeg fisheye image only"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsPictureFormatSelection(type);
		}
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordVideo(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_STOP,
				(byte)mass_storage_id);
		}
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordVideoV2(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_STOP);
		}
		return false;
	}

	@Override
	public boolean sendCameraOrientation(final int tilt, final int pan) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendCameraOrientation((byte)tilt, (byte)pan);
		}
		return false;
	}

	@Override
	public boolean sendAutoWhiteBalance(final int auto_white_balance) {
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_AUTO (0, "Auto guess of best white balance params"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_TUNGSTEN (1, "Tungsten white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_DAYLIGHT (2, "Daylight white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_CLOUDY (3, "Cloudy white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_COOL_WHITE (4, "White balance for a flash"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			if (auto_white_balance >= 0) {
				final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM awb
					= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM.getFromValue(auto_white_balance);
				mARDeviceController.getFeatureARDrone3().sendPictureSettingsAutoWhiteBalanceSelection(awb);
			} else {
				mARDeviceController.getFeatureARDrone3Debug().sendVideoManualWhiteBalance();
			}
		}
		return false;
	}

	@Override
	public int autoWhiteBalance() {
		return 0;
	}

	@Override
	public boolean sendExposure(final float exposure) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsExpositionSelection(exposure);
		}
		return false;
	}

	@Override
	public boolean sendSaturation(final float saturation) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsSaturationSelection(saturation);
		}
		return false;
	}

	@Override
	public boolean sendTimelapseSelection(final boolean enabled, final float interval) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsTimelapseSelection(enabled ? (byte)1 : (byte)0, interval);
		}
		return false;
	}

	@Override
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsVideoAutorecordSelection(enabled ? (byte)1 : (byte)0, (byte)mass_storage_id);
		}
		return false;
	}

	@Override
	public boolean sendWobbleCancellation(final boolean enabled) {
		return false;
	}

	@Override
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s) {
		return false;
	}

	@Override
	public int getPan() {
		return 0;
	}

	@Override
	public int getTilt() {
		return 0;
	}

	@Override
	public void setVideoStream(final IVideoStream video_stream) {

	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return false;
	}

	@Override
	public boolean enableVideoStreaming(final boolean enable) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable(enable ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean requestTakeoff() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingTakeOff();
		}
		return false;
	}

	@Override
	public boolean requestLanding() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingLanding();
		}
		return false;
	}

	@Override
	public boolean requestEmergencyStop() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingEmergency();
		}
		return false;
	}

	@Override
	public boolean requestFlatTrim() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingFlatTrim();
		}
		return false;
	}

	@Override
	public boolean startCalibration(final boolean start) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureCommon().sendCalibrationMagnetoCalibration(start ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean setMaxAltitude(final float altitude) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxAltitude(altitude);
		}
		return false;
	}

	@Override
	public boolean setMaxTilt(final float tilt) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxTilt(tilt);
		}
		return false;
	}

	@Override
	public boolean setMaxVerticalSpeed(final float speed) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxVerticalSpeed(speed);
		}
		return false;
	}

	@Override
	public boolean setMaxRotationSpeed(final float speed) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxRotationSpeed(speed);
		}
		return false;
	}

	@Override
	public boolean canGetAttitude() {
		return true;
	}

	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		// これは常時ONなのかも
		return false;
	}

	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingAutoTakeOffMode(enable ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean setHasGuard(final boolean has_guard) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsHullProtection(has_guard ? (byte)1 : (byte)0);
		}
		return false;
	}

	private int _timestampAndSeqNum;
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, _timestampAndSeqNum++);
		}
		return false;
	}

	/**
	 * 指定した方向にフリップ実行
	 @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * 	public static final int FLIP_FRONT = 1;
	 *	public static final int FLIP_BACK = 2;
	 *	public static final int FLIP_RIGHT = 3;
	 *	public static final int FLIP_LEFT = 4;
	 * @return
	 */
	@Override
	public boolean requestAnimationsFlip(final int direction) {
		final ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.getFromValue(direction - 1);
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_FRONT (0, "Flip direction front"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_BACK (1, "Flip direction back"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_RIGHT (2, "Flip direction right"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_LEFT (3, "Flip direction left"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendAnimationsFlip(dir);
		}
		return false;
	}

	/**
	 * 自動で指定した角度回転させる
	 * @param degree -180〜180度
	 * @return
	 */
	@Override
	public boolean requestAnimationsCap(final int degree) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;

		if (degree != 0) {

			final AttributeFloat rotation_speed = mSettings.maxRotationSpeed();    // 回転速度[度/秒]
			final float current = rotation_speed.current();
			try {
				try {
					if (current != rotation_speed.max()) {
						// 最大回転速度に変更する
						setMaxRotationSpeed(rotation_speed.max());
						Thread.sleep(5);
					}
					final long t = (long) Math.abs(degree / rotation_speed.max() * 1000);    // 回転時間[ミリ秒]を計算
					setYaw(degree > 0 ? 100 : -100);
					Thread.sleep(t + 5);
				} catch (InterruptedException e) {
				}
			} finally {
				// 元の回転速度設定に戻す
				setYaw(0);
				setMaxRotationSpeed(current);
			}
		}
		return sentStatus;
	}

	public boolean requestAnimationsCap(final int degree, final Object sync) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;

		if (degree != 0) {

			final AttributeFloat rotation_speed = mSettings.maxRotationSpeed();    // 回転速度[度/秒]
			final float current = rotation_speed.current();
			try {
				try {
					final long t = (long) Math.abs(degree / rotation_speed.max() * 1000);    // 回転時間[ミリ秒]を計算
					synchronized (sync) {
						if (current != rotation_speed.max()) {
							// 最大回転速度に変更する
							setMaxRotationSpeed(rotation_speed.max());
							sync.wait(5);
						}
						setYaw(degree > 0 ? 100 : -100);
						sync.wait(t);
					}
				} catch (InterruptedException e) {
				}
			} finally {
				// 元の回転速度設定に戻す
				setYaw(0);
				setMaxRotationSpeed(current);
			}
		}
		return sentStatus;
	}

	@Override
	public boolean requestTakePicture(final int mass_storage_id) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordPictureV2();
		}
		return false;
	}

	@Override
	public boolean requestTakePicture() {
		return requestTakePicture(0);
	}

	@Override
	public boolean setHeadlightsIntensity(final int left, final int right) {
		// これは無いみたい
		return false;
	}

	@Override
	public boolean startAnimation(final int animation) {
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAnimation(final int animation) {
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAllAnimation() {
		// これは無いみたい?
		return false;
	}
}
