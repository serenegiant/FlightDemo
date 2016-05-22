package com.serenegiant.autoparrot;

import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.aceparrot.LineRec;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.VideoStream;
import com.serenegiant.math.Vector;
import com.serenegiant.opencv.ImageProcessor;

import static com.serenegiant.autoparrot.AutoPilotConst.*;

public class AutoPilotFragment2 extends BaseAutoPilotFragment {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = AutoPilotFragment2.class.getSimpleName();

	public static AutoPilotFragment2 newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info, final String pref_name, final int mode, final boolean newAPI) {

		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		final AutoPilotFragment2 fragment = new AutoPilotFragment2();
		final Bundle args = fragment.setDevice(device, info, newAPI);
		fragment.mPrefName =  TextUtils.isEmpty(pref_name) ? TAG : pref_name;
		fragment.mMode = mode;
		args.putString(KEY_PREF_NAME_AUTOPILOT, fragment.mPrefName);
		args.putInt(KEY_AUTOPILOT_MODE, fragment.mMode);
		return fragment;
	}

	protected TraceTask mTraceTask;
	private int mImageProcessorSurfaceId;

	public AutoPilotFragment2() {
		super();
		// デフォルトコンストラクタが必要
	}

	protected void startImageProcessor(final int processing_width, final int processing_height) {
		if (DEBUG) Log.v(TAG, "startImageProcessor:");
		super.startImageProcessor(processing_width, processing_height);
		if (mTraceTask == null) {
			mTraceTask = new TraceTask(processing_width, processing_height);
			new Thread(mTraceTask, "Trace").start();
		}
		if (mImageProcessor == null) {
			mImageProcessor = new ImageProcessor(VideoStream.VIDEO_WIDTH, VideoStream.VIDEO_HEIGHT,	// こっちは元映像のサイズ
				new MyImageProcessorCallback(processing_width, processing_height));	// こっちは処理サイズ
			mImageProcessor.enableAutoFix(!isNewAPI());
			mImageProcessor.setExposure(mExposure);
			mImageProcessor.setSaturation(mSaturation);
			mImageProcessor.setBrightness(mBrightness);
			applyExtractRange(mExtractRangeH, mExtractRangeS, mExtractRangeV);
			mImageProcessor.enableExtraction(mEnableGLESExtraction);
//			mImageProcessor.enableNativeExtract(mEnableNativeExtraction);
//			mImageProcessor.enableNativeCanny(mEnableNativeCanny);
			mImageProcessor.trapeziumRate(mTrapeziumRate);
			mImageProcessor.setAreaLimit(mAreaLimitMin, AREA_LIMIT_MAX);
			mImageProcessor.setAreaErrLimit(mAreaErrLimit1, mAreaErrLimit2);
			mImageProcessor.setAspectLimit(mAspectLimitMin);
			mImageProcessor.setMaxThinningLoop(mMaxThinningLoop);
			mImageProcessor.setFillInnerContour(mFillContour);
			mImageProcessor.start(processing_width, processing_height);	// これも処理サイズ
			final Surface surface = mImageProcessor.getSurface();
			mImageProcessorSurfaceId = surface != null ? surface.hashCode() : 0;
			if (mImageProcessorSurfaceId != 0) {
				addSurface(mImageProcessorSurfaceId, surface);
			}
		}
		updateButtons();
	}

	protected void stopImageProcessor() {
		removeSurface(mImageProcessorSurfaceId);
		if (mImageProcessor != null) {
			mImageProcessor.release();
			mImageProcessor = null;
		}
		mTraceTask = null;
		super.stopImageProcessor();
	}

	/** トレース飛行タスク */
	private class TraceTask extends AbstractTraceTask {
		private PilotVector calcValue = new PilotVector();
		private final Vector offset = new Vector();
		private final Vector work = new Vector();
		private final Vector work2 = new Vector();
		float flightAngleYaw = mTraceAttitudeYaw;										// カメラの上方向に対する移動方向の角度
		float flightSpeed = mTraceSpeed / 2.0f * (float)(mMaxControlValue / 100.0);		// 前進速度の1/2(負なら後進)
		final Vector scale = new Vector((float)mScaleX, (float)mScaleY, (float)mScaleZ);
		float scaleR = (float)mScaleR;
		float directionalReverseBias = mTraceDirectionalReverseBias;
//		float curvature = 0.0f; // mTraceCurvature;
		float sensitivity = mTraceSensitivity;
		//
		final Vector dir = new Vector(0.0f, flightSpeed, 0.0f).rotate(0.0f, 0.0f, flightAngleYaw);
		final Vector prevOffset = new Vector();

		public TraceTask(final int processing_width, final int processing_height) {
			super(processing_width, processing_height);
		}

		@Override
		protected void onUpdateParams() {
			flightAngleYaw = mTraceAttitudeYaw;
			// scaleが最大で±2になるのでmFlightSpeedは[-100,+100]なのを[-50,+50]にする
			flightSpeed = mTraceSpeed / 2.0f * (float)(mMaxControlValue / 100.0);
			scale.set((float)mScaleX, (float)mScaleY, (float)mScaleZ);
			scaleR = (float)mScaleR;
			dir.set(0.0f, flightSpeed, 0.0f).rotateXY(flightAngleYaw);
			directionalReverseBias = mTraceDirectionalReverseBias;
//			curvature = mTraceCurvature;
			sensitivity = mTraceSensitivity;
			if (mMovingAveTap != mTraceMovingAveTap) {
				createMovingAve(mTraceMovingAveTap);
			}
		}

		@Override
		protected PilotVector onCalc(final LineRec rec) {
			// ラインを検出出来た時
			//--------------------------------------------------------------------------------
			// 制御量を計算
			// 機体からの角度はカメラ映像の真上が0で反時計回りが負、時計回りが正(Bebopのyaw軸回転角と同じ)
			// 解析画像のラインに対する角度は機体が時計回りすれば正
			// この時機体自体のラインに対する角度は符号反転
			// mCurvatureがゼロでない時にmAngleが正ならラインは左へ曲がっている、mAngleが負なら右へ曲がっている
			// Vectorクラスは反時計回りが正, 時計回りが負
			//--------------------------------------------------------------------------------
			// ライン角に機体の進行方向の傾きを補正
			final float theta = rec.angle - flightAngleYaw;
			float line_angle = -theta;
			if ((line_angle > 90.0f) || (line_angle < -90.0f)) {
				if (theta < 0.0f) {
					line_angle -= 180.0f;
				} else {
					line_angle += 180.0f;
				}
			}
			//--------------------------------------------------------------------------------
			// 画像中心からライン最小矩形の中心へのオフセットを計算
			offset.set(CX, CY, flightAltitude).sub(rec.linePos);
			// 解析データ
			msg1 = String.format("%d,v(%3.0f,%3.0f,%5.1f,%5.2f),θ=%5.2f)",
				rec.type, offset.x, offset.y, offset.z, rec.angle, line_angle);
			//--------------------------------------------------------------------------------
			// 画面の端が-1または+1になるように変換する
			offset.div(CX, CY, flightAltitude);	// [-320,+320][-184,+184][z] => [-1,+1][-1,+1][0,1]
			offset.set(updateMovingAve(offset));	// オフセットの移動平均を取得
			// 移動方向, 前回と同じ方向なら1, 逆なら-1
			work.set(offset).sub(prevOffset).sign();
			// オフセットを保存
			prevOffset.set(offset);
			calcValue.set(offset);	// これは画面座標での画面中央とライン重心のオフセット値
			// オフセットの移動平均の符号を取得
			offset.sign();
			// 移動方向が変わってなければバイアス加算, 変わってればバイアス減算
			if (offset.x != 0.0f) { if (offset.x == work.x) { work.x = directionalReverseBias; } else { work.x = -directionalReverseBias; } } else { offset.x = 0.0f; }
			if (offset.y != 0.0f) { if (offset.y == work.y) { work.y = directionalReverseBias; } else { work.y = -directionalReverseBias; } } else { offset.y = 0.0f; }
			if (offset.z != 0.0f) { if (offset.z == work.z) { work.z = directionalReverseBias; } else { work.z = -directionalReverseBias; } } else { offset.z = 0.0f; }
			work.add(1.0f, 1.0f, 1.0f);	// この時点でworkの各成分は1.0f±directionalReverseBias
			// 機体のオフセットと反対向き動かすので-1倍, ±1を±sensitivityに換算するのでsensitivity倍, 前進速度を加算
			// オフセットy(ピッチ, 前後方向)はラインの中心点が中央より前だと負、中央より後ろだと正なので符号反転はしない
			calcValue.mult(work).mult(-sensitivity, sensitivity, sensitivity);
			// 実際の機体の進行方向に合わせて回転, これで機体の実際の進行方向に対する制御量になる
			// でも角度の変えながらなのでとりあえず半分だけ回転させる
			calcValue.rotateXY(line_angle / 2.0f);
			// FIXME 高度に応じてスケールを変えないとだめかも
			// 自動操縦スケールを適用
			calcValue.mult(scale);
			// 飛行速度を加算
			switch (mMode) {
			case MODE_TRACE:	// 通常(トレース)
				calcValue.add(dir);
				break;
			case MODE_TRACKING:	// トラッキング
				// 飛行速度の加算なし
				break;
			}
			// 最大最小値を制限
			calcValue.limit(-100.0f, +100.0f);
			//--------------------------------------------------------------------------------
			// 機体のyaw角を計算
			switch (rec.type) {
			case 0: // TYPE_LINE
			{
				calcValue.angle = line_angle;
				msg2 = null;
				break;
			}
			case 1:	// TYPE_CIRCLE
			{
				// 楕円の中心とライン中心を通る線分と楕円の交点座標での接線の傾きを求める
				final float ellipse_angle = rec.ellipseAngle <= 90.0f ? rec.ellipseAngle : -180.0f + rec.ellipseAngle;
				// 楕円の中心からライン最小句形の中旬へ向かうベクトルを計算
				offset.set(rec.linePos).sub(rec.ellipsePos);
				// 楕円の回転角を補正, 楕円の回転角はline_angleと大体同じみたい,範囲が違うけど, [0-180]
				offset.rotateXY(-ellipse_angle);
				// 長軸半径・短軸半径
				final float a = rec.ellipseA;
				final float b = rec.ellipseB;
				final float c;	// 楕円の中心とライン重心を通る線分の傾き
				// 楕円の中心とライン重心を通る線分の傾きを取得
				final float slope, slope_angle;
				if (offset.x != 0) {
					c = offset.y / offset.x;
					//  楕円: x^2 / a^2 + y^2 / b^2 = 1との交点を計算
					final float w = (a * a * b * b) / (b * b + a * a * c * c);
					final float x1 = (float)Math.sqrt(w);
					work.set(x1, c * x1);
					final float d = Math.abs(work.getAngle(offset));
					if (d > 5) {
						// ライン重心と反対側の交点だったので符号を反転
						work.mult(-1.0f);
					}
					// この時点でworkには楕円の中心とライン重心を通る線分と楕円の交点座標が入っている
					//  楕円: x^2 / a^2 + y^2 / b^2 = 1上の点(x0,y0)の接線の方程式は
					// x0・x / a^2 + y0・y / b^2 = 1, 式変形してy = b^2 / y - (x0・b^2) / (a^2・y0)・x
					// なので傾きは -(x0・b^2) / (a^2・y0)
					slope = - work.x * b * b / (a * a * work.y);
					// 接線がx軸となす角を計算, 楕円の傾きを加算
					slope_angle = (float)Math.toDegrees(Math.atan(slope)) + ellipse_angle;
				} else {
					c = slope = 0.0f;
					slope_angle = ellipse_angle;
					if (DEBUG) Log.v(TAG, "offset.x == 0");
				}

				msg2 = String.format("e(%5.2f,%5.2f,%5.2f),θ=%5.2f,s=%5.2f",
					offset.x, offset.y, rec.ellipseAngle, ellipse_angle, slope_angle);
				calcValue.angle = slope_angle;
				if (calcValue.angle < -90.0f) calcValue.angle += 90.0f;
				if (calcValue.angle > +90.0f) calcValue.angle -= 90.0f;
				if (Math.abs(calcValue.angle - ellipse_angle) > 10.0f) {
					calcValue.angle = ellipse_angle;
				}
				break;
			}
			case 2: // TYPE_CORNER
			{
				break;
			}
			}	// switch (rec.type)
			// 自動操縦スケールを適用
			calcValue.angle *= scaleR;
			// 一定角度以下は0に丸める
			calcValue.angle = (calcValue.angle < -MIN_PILOT_ANGLE) || (calcValue.angle > MIN_PILOT_ANGLE) ? calcValue.angle : 0.0f;
			return calcValue;
		}
	}

}
