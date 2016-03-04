package com.serenegiant.flightdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.ICameraController;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.VideoStream;
import com.serenegiant.opencv.ImageProcessor;

import java.nio.ByteBuffer;

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

	protected SurfaceView mDetectView;
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

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView =super.onCreateView(inflater, container, savedInstanceState);
		mDetectView = (SurfaceView)rootView.findViewById(R.id.detect_view);
		mDetectView.setVisibility(View.VISIBLE);
		return rootView;
	}

	private int mImageProcessorSurfaceId;
	@Override
	protected void onConnect(final IDeviceController controller) {
		super.onConnect(controller);
		if (DEBUG) Log.v(TAG, "onConnect");
		if ((mController instanceof IVideoStreamController) && (mVideoStream != null)) {
			mImageProcessor = new ImageProcessor(mImageProcessorCallback);
			final Surface surface = mImageProcessor.getSurface();
			mImageProcessorSurfaceId = surface != null ? surface.hashCode() : 0;
			if (mImageProcessorSurfaceId != 0) {
				mVideoStream.addSurface(mImageProcessorSurfaceId, surface);
			}
		}
		if (mController instanceof ICameraController) {
			((ICameraController)mController).sendExposure(3);
			((ICameraController)mController).sendCameraOrientation(-100, 0);
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

	protected boolean onClick(final View view) {
		switch (view.getId()) {
		case R.id.top_panel:
			if (mImageProcessor != null) {
				mImageProcessor.setResultFrameType(mImageProcessor.getResultFrameType() + 1);
			}
			break;
		}
		return false;
	}

	private Bitmap mFrame;
	private final ImageProcessor.ImageProcessorCallback mImageProcessorCallback
		= new ImageProcessor.ImageProcessorCallback() {
		private final Matrix matrix = new Matrix();
		@Override
		public void onFrame(final ByteBuffer frame) {
			if (mDetectView != null) {
				final SurfaceHolder holder = mDetectView.getHolder();
				if ((holder == null) || (holder.getSurface() == null)) return;
				if (mFrame == null) {
					mFrame = Bitmap.createBitmap(VideoStream.VIDEO_WIDTH, VideoStream.VIDEO_HEIGHT, Bitmap.Config.ARGB_8888);
					final float scaleX = mDetectView.getWidth() / (float)VideoStream.VIDEO_WIDTH;
					final float scaleY = mDetectView.getHeight() / (float)VideoStream.VIDEO_HEIGHT;
					matrix.reset();
					matrix.postScale(scaleX, scaleY);
				}
				frame.clear();
//				if (DEBUG) Log.v(TAG, "frame=" + frame);
				mFrame.copyPixelsFromBuffer(frame);
				final Canvas canvas = holder.lockCanvas();
				if (canvas != null) {
					try {
						canvas.drawBitmap(mFrame, matrix, null);
					} catch (final Exception e) {
						Log.w(TAG, e);
					} finally {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	};
}
