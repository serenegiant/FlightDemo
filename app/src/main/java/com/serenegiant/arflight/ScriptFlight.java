package com.serenegiant.arflight;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.serenegiant.lang.script.ASTParse;
import com.serenegiant.lang.script.Script;
import com.serenegiant.lang.script.ScriptVisitorImpl;

import java.io.InputStream;
import java.util.List;

public class ScriptFlight implements IAutoFlight {
	private static final boolean DEBUG = true;
	private static final String TAG = "ScriptFlight";

	private final Object mSync = new Object();
	private final AutoFlightListener mAutoFlightListener;
	private final Handler mHandler;	// プライベートスレッドでの実行用

	private volatile boolean mIsPlayback;	// 再生中
	private ASTParse mASTParse;
	private ScriptVisitorImpl mVisitor;
	private int prevRoll, prevPitch, prevGaz, prevYaw;

	public ScriptFlight(final AutoFlightListener listener) {
		if (listener == null) {
			throw new NullPointerException("AutoFlightListenerコールバックリスナーが設定されてない");
		}
		mAutoFlightListener = listener;
		final HandlerThread thread = new HandlerThread("ScriptFlight");
		thread.start();
		mHandler = new Handler(thread.getLooper());

		ScriptVisitorImpl.resetPresetFunc();
		// プリセット関数を追加する
		// 非常停止
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("emergency", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute emergency:");
					if (mAutoFlightListener.onStep(CMD_EMERGENCY, 0, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 離陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("takeoff", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute takeoff:");
					if (mAutoFlightListener.onStep(CMD_TAKEOFF, 0, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 着陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("landing", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute landing:");
					if (mAutoFlightListener.onStep(CMD_LANDING, 0, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 移動
		// roll, pitch, gaz, yaw
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("move", "iiii") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute move:" + args + ",size=" + args.size());
					final int roll = args.get(0).intValue();
					final int pitch = args.get(1).intValue();
					final int gaz = args.get(2).intValue();
					final int yaw = args.get(3).intValue();
					if (mIsPlayback && (prevRoll != roll)) {
						prevRoll = roll;
						if (mAutoFlightListener.onStep(CMD_RIGHT_LEFT, roll, getCurrentTime())) {
							stop();
						}
					}
					if (mIsPlayback && (prevPitch != pitch)) {
						prevPitch = pitch;
						if (mAutoFlightListener.onStep(CMD_FORWARD_BACK, pitch, getCurrentTime())) {
							stop();
						}
					}
					if (mIsPlayback && (prevGaz != gaz)) {
						prevGaz = gaz;
						if (mAutoFlightListener.onStep(CMD_UP_DOWN, gaz, getCurrentTime())) {
							stop();
						}
					}
					if (mIsPlayback && (prevYaw != yaw)) {
						prevYaw = yaw;
						if (mAutoFlightListener.onStep(CMD_TURN, yaw, getCurrentTime())) {
							stop();
						}
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 上下移動
		// 上昇:gaz>0, 下降: gaz<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("updown", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute updown:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_UP_DOWN, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 左右移動
		// 右: roll>0,flag=1 左: roll<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("rightleft", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute rightleft:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_RIGHT_LEFT, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 前後移動
		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("frontback", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute frontback:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_FORWARD_BACK, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 左右回転
		// 右回転: yaw>0, 左回転: ywa<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("turn", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute turn:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_TURN, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 北磁極に対する角度を指定-360-360度 ローリングスパイダーは動かない
		// compass
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("compass", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute compass:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_COMPASS, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// flip
		// 1:前, 2:後, 3:右, 4:左
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("flip", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute flip:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_FLIP, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// cap
		// -180〜180度
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("cap", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG) Log.v(TAG, "execute cap:" + args + ",size=" + args.size());
					if (mAutoFlightListener.onStep(CMD_CAP, args.get(0).intValue(), getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
	}

	/**
	 * 自動フライトの準備
	 * @param args
	 * @throws RuntimeException
	 */
	@Override
	public void prepare(Object...args) throws RuntimeException {
		final InputStream in = (args != null) && (args.length > 0) && (args[0] instanceof InputStream) ? (InputStream)args[0] : null;
		if (in == null) throw new IllegalArgumentException("InputStreamがセットされていない");

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				final Script script = new Script(in);
				try {
					final ASTParse parse = script.Parse();
					synchronized (mSync) {
						mASTParse = parse;
						// FIXME ビジターの引数を変更できるようにする
						mVisitor = new ScriptVisitorImpl(100, 1.0, 1.0);
					}
					try {
						mAutoFlightListener.onPrepared();
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				} catch (final Exception e) {
					synchronized (mSync) {
						mASTParse = null;
						mVisitor = null;
					}
					mAutoFlightListener.onError(e);
				}
			}
		});
	}

	@Override
	public void play() throws IllegalStateException {
		if (mIsPlayback) {
			throw new IllegalStateException("既に実行中");
		}
		synchronized (mSync) {
			if ((mASTParse == null) || (mVisitor == null)) {
				throw new IllegalStateException("prepareが呼ばれてない");
			}
			prevRoll = prevPitch = prevGaz = prevYaw = 0;
			mIsPlayback = true;
		}
		mHandler.post(mPlaybackRunnable);
	}

	@Override
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
		synchronized (mSync) {
			if (mVisitor != null) {
				mVisitor.requestStop();
			}
			mIsPlayback = false;
			mSync.notifyAll();
		}
		mHandler.removeCallbacks(mPlaybackRunnable);
	}

	@Override
	public boolean isPrepared() {
		synchronized (mSync) {
			return !mIsPlayback && (mASTParse == null) && (mVisitor == null);
		}
	}

	@Override
	public boolean isPlaying() {
		return mIsPlayback;
	}

	/**
	 * 関係するリソースを破棄する
	 */
	@Override
	public void release() {
		stop();
		mHandler.getLooper().quit();
	}

	private long mStartTime;
	private long getCurrentTime() {
		return System.currentTimeMillis() - mStartTime;
	}

	/**
	 * コマンド再生スレッドの実行用Runnable
	 */
	private final Runnable mPlaybackRunnable = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run");
			try {
				mAutoFlightListener.onStart();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			mStartTime = System.currentTimeMillis();
			try {
				// スクリプト実行
				mASTParse.jjtAccept(mVisitor, null);
			} finally {
				stop();
			}
			synchronized (mSync) {
				mVisitor = null;
			}
			try {
				mAutoFlightListener.onStop();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run:終了");
		}
	};
}
