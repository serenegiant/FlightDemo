package com.serenegiant.arflight;

/**
 * Created by saki on 16/02/07.
 */
public interface ICameraController extends IVideoStreamController {

	public void setCameraControllerListener(final CameraControllerListener listener);

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera, 3:take jpeg fisheye image only
	 * @return
	 */
	public boolean sendPictureFormat(final int pictureFormat);

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id);

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start);

	/**
	 * カメラの方向を設定, コールバックの返り値から推測すると設定可能なのは[-100;100]<br>
	 * Tilt and pan value is saturated by the drone.<br>
	 * Saturation value is sent by the drone through CameraSettingsChanged command.
	 * @param tilt Tilt camera consign for the drone (in degree).
	 * @param pan Pan camera consign for the drone (in degree)
	 * @return
	 */
	public boolean sendCameraOrientation(final int tilt, final int pan);

	/**
	 * オートホワイトバランス設定
	 * @param auto_white_balance<br>
	 * -1: 手動
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 * @return
	 */
	public boolean sendAutoWhiteBalance(final int auto_white_balance);

	/**
	 * オートホワイトバランス設定を取得
	 * @return
	 */
	public int autoWhiteBalance();

	/**
	 * 露出設定
	 * @param exposure Exposure value (bounds given by ExpositionChanged arg min and max, by default [-3:3])
	 * @return
	 */
	public boolean sendExposure(final float exposure);

	public float exposure();
	/**
	 * 彩度設定
	 * @param saturation Saturation value (bounds given by SaturationChanged arg min and max, by default [-100:100])
	 * @return
	 */
	public boolean sendSaturation(final float saturation);

	public float saturation();

	/**
	 * タイムラプス設定(一定時間毎に自動撮影)
	 * @param enabled
	 * @param interval 撮影間隔[秒]
	 * @return
	 */
	public boolean sendTimelapseSelection(final boolean enabled, final float interval);

	/**
	 * 自動録画設定
	 * @param enabled
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id);

	/**
	 * 映像のブレ補正設定
	 * @param enabled
	 * @return
	 */
	public boolean sendWobbleCancellation(final boolean enabled);

	/**
	 * 映像のブレ補正用のジャイロ設定
	 * @param anglesDelay_s Shift by x seconds angles (video stabilization)
	 * @param gyrosDelay_s Shift by x seconds t gyros (wobble cancellation
	 * @return
	 */
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s);

	public int getPan();
	public int getTilt();

}
