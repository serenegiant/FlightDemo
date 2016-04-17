package com.serenegiant.arflight;

/**
 * Created by saki on 16/02/07.
 */
public interface CameraControllerListener {
	public void onCameraOrientationChanged(final IDeviceController controller, final int pan, final int tilt);
}
