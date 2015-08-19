package com.serenegiant.arflight;

import android.util.Log;

import com.serenegiant.lang.script.Script;
import com.serenegiant.lang.script.ScriptVisitorImpl;

import java.io.InputStream;
import java.util.List;

/**
 * Created by saki on 2015/08/20.
 */
public class ScriptFlight implements IAutoFlight {
	private static final boolean DEBUG = true;
	private static final String TAG = "ScriptFlight";

	private final Object mSync = new Object();
	private final Object mPlaybackSync = new Object();	// PlaybackListener同期用
	private AutoFlightListener mAutoFlightListener;
	private volatile boolean mIsPlayback;	// 再生中
	private Script mScript;

	private int prevRoll, prevPitch, prevGaz, prevYaw;

	public ScriptFlight() {
		ScriptVisitorImpl.resetPresetFunc();
		// プリセット関数を追加する
		// 非常停止
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("emergency", "") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_EMERGENCY, 0, getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 離陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("takeoff", "") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_TAKEOFF, 0, getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 着陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("landing", "") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_LANDING, 0, getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 移動
		// roll, pitch, gaz, yaw
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("move", "iiii") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
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
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 上下移動
		// 上昇:gaz>0, 下降: gaz<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("updown", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_UP_DOWN, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 左右移動
		// 右: roll>0,flag=1 左: roll<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("rightleft", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_RIGHT_LEFT, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 前後移動
		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("frontback", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_FORWARD_BACK, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 左右回転
		// 右回転: yaw>0, 左回転: ywa<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("turn", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_TURN, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// 北磁極に対する角度を指定-360-360度 ローリングスパイダーは動かない
		// compass
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("compass", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_COMPASS, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// flip
		// 1:前, 2:後, 3:右, 4:左
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("flip", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_FLIP, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
		// cap
		// -180〜180度
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("cap", "i") {
			@Override
			protected Object execute(List<Number> args) {
				synchronized (mPlaybackSync) {
					if (mAutoFlightListener != null) {
						try {
							if (mAutoFlightListener.onStep(CMD_CAP, args.get(0).intValue(), getCurrentTime())) {
								stop();
							}
						} catch (Exception e) {
							Log.w(TAG, e);
						}
					}
				}
				return null;
			}
		});
	}

	public void setAutoFlightListener(final AutoFlightListener listener) {
		synchronized (mPlaybackSync) {
			mAutoFlightListener = listener;
		}
	}

	public AutoFlightListener getAutoFlightListener() {
		synchronized (mPlaybackSync) {
			return mAutoFlightListener;
		}
	}

	public void prepare(final InputStream in) {
		mScript = new Script(in);
	}

	@Override
	public void play() throws IllegalStateException {
		if (mIsPlayback) {
			throw new IllegalStateException("既に実行中");
		}
		if (mScript == null) {
			throw new IllegalStateException("prepareが呼ばれてない");
		}
		prevRoll = prevPitch = prevGaz = prevYaw = 0;
		mIsPlayback = true;
		new Thread(mPlaybackRunnable, "ScriptFlight").start();
	}

	@Override
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
		synchronized (mSync) {
			mIsPlayback = false;
			mSync.notifyAll();
		}
	}

	@Override
	public boolean isPlaying() {
		return false;
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
			synchronized (mPlaybackSync) {
				if (mAutoFlightListener != null) {
					try {
						mAutoFlightListener.onStart();
					} catch (Exception e) {
						Log.w(TAG, e);
					}
				}
			}
			mStartTime = System.currentTimeMillis();

			mIsPlayback = false;
			synchronized (mPlaybackSync) {
				if (mAutoFlightListener != null) {
					try {
						mAutoFlightListener.onStop();
					} catch (Exception e) {
						Log.w(TAG, e);
					}
				}
			}
		}
	};
}
