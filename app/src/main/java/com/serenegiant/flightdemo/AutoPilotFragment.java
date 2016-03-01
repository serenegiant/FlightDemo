package com.serenegiant.flightdemo;

import android.app.Activity;
import android.util.Log;
import android.view.Surface;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.opencv.ImageProcessor;

public class AutoPilotFragment extends PilotFragment {
	private static final boolean DEBUG = true; // FIXME 実働時はfalseにすること
	private static final String TAG = AutoPilotFragment.class.getSimpleName();

	public static AutoPilotFragment newInstance(final ARDiscoveryDeviceService device) {
		final AutoPilotFragment fragment = new AutoPilotFragment();
		fragment.setDevice(device);
		return fragment;
	}

	public static AutoPilotFragment newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info) {
		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		final AutoPilotFragment fragment = new AutoPilotFragment();
		fragment.setBridge(device, info);
		return fragment;
	}

	protected ImageProcessor mImageProcessor;
	public AutoPilotFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach");
		if (mImageProcessor != null) {
			mImageProcessor.release();
			mImageProcessor = null;
		}
		super.onDetach();
	}

	private int mImageProcessorSurfaceId;
	@Override
	protected void onConnect(final IDeviceController controller) {
		super.onConnect(controller);
		if (DEBUG) Log.v(TAG, "onConnect");
		if ((mController instanceof IVideoStreamController) && (mVideoStream != null)) {
			mImageProcessor = new ImageProcessor();
			final Surface surface = mImageProcessor.getSurface();
			mImageProcessorSurfaceId = surface != null ? surface.hashCode() : 0;
			if (mImageProcessorSurfaceId != 0) {
				mVideoStream.addSurface(mImageProcessorSurfaceId, surface);
			}
		}
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect");
		if ((mVideoStream != null) && (mImageProcessorSurfaceId != 0)) {
			mVideoStream.removeSurface(mImageProcessorSurfaceId);
			mImageProcessorSurfaceId = 0;
		}
		if (mImageProcessor != null) {
			mImageProcessor.release();
			mImageProcessor = null;
		}
		super.onDisconnect(controller);
	}
}
