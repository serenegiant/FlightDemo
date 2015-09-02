package com.serenegiant.utils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageTask implements Runnable {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
//	private static final String TAG = "MessageTask";

	protected static final class Request {
		int request;
		int arg1;
		int arg2;
		Object obj;

		/**
		 * @param _request minus value is reserved internal use
		 * @param _arg1
		 * @param _arg2
		 * @param _obj
		 */
		public Request(final int _request, final int _arg1, final int _arg2, final Object _obj) {
			request = _request;
			arg1 = _arg1;
			arg2 = _arg2;
			obj = _obj;
		}

		@Override
		public boolean equals(final Object o) {
			return (o instanceof Request)
				? (request == ((Request) o).request)
					&& (arg1 == ((Request) o).arg1)
					&& (arg2 == ((Request) o).arg2)
					&& (obj == ((Request) o).obj)
				: super.equals(o);
		}
	}

	// minus value is reserved for internal use
	protected static final int REQUEST_TASK_NON = 0;
	protected static final int REQUEST_TASK_RUN = -1;
	protected static final int REQUEST_TASK_START = -8;
	protected static final int REQUEST_TASK_QUIT = -9;

	private final Object mSync = new Object();
	private final LinkedBlockingQueue<Request> mRequestPool = new LinkedBlockingQueue<Request>();
	private final LinkedBlockingDeque<Request> mRequestQueue = new LinkedBlockingDeque<Request>();
	private boolean mIsRunning;

	public MessageTask() {
	}

	/** 初期化要求。コンストラクタから呼び出すこと */
	protected void init(final int arg1, final int arg2, final Object obj) {
		offer(REQUEST_TASK_START, arg1, arg2, obj);
	}

	/** 初期化処理 */
	protected abstract void onInit(final int request, final int arg1, final int arg2, final Object obj);
	/** 要求処理ループ開始直前に呼ばれる */
	protected abstract void onStart();
	/** onStopの直前に呼び出される */
	protected void onBeforeStop() {}
	/** 停止処理 */
	protected abstract void onStop();
	/** onStop後に呼び出される。onStopで例外発生しても呼ばれる */
	protected abstract void onRelease();
	/** エラー発生, trueを返すと要求メッセージ処理ループを終了する */
	protected boolean onError(final Exception e) {
//		if (DEBUG) Log.w(TAG, e);
		return true;
	};
	/** 要求メッセージの処理(内部メッセージは来ない)
	 * trueを返すと要求メッセージ処理ループを終了する */
	protected abstract boolean processRequest(final int request, final int arg1, final int arg2, final Object obj);
	/** 要求メッセージを取り出す処理(要求メッセージがなければブロックされる) */
	protected Request takeRequest() throws InterruptedException {
		return mRequestQueue.take();
	}

	public void waitReady() {
		synchronized (mSync) {
			if (!mIsRunning) {
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		}
	}

	@Override
	public void run() {
		Request request = null;
		try {
			request = mRequestQueue.take();
		} catch (final InterruptedException e) {
			// ignore
		}
		synchronized (mSync) {
			mIsRunning = true;
			try {
				onInit(request.request, request.arg1, request.arg2, request.obj);
			} catch (final Exception e) {
				mIsRunning = false;
			}
			mSync.notifyAll();
		}
		if (mIsRunning) {
			try {
				onStart();
			} catch (final Exception e) {
				if (callOnError(e))
					mIsRunning = false;
			}
		}
LOOP:	for (; mIsRunning; ) {
			try {
				request = takeRequest();
				boolean result = false;
				switch (request.request) {
				case REQUEST_TASK_NON:
					break;
				case REQUEST_TASK_QUIT:
					break LOOP;
				case REQUEST_TASK_RUN:
					if (request.obj instanceof Runnable)
					try {
						((Runnable)request.obj).run();
					} catch (final Exception e) {
						if (callOnError(e))
							break LOOP;
					}
					break;
				default:
					try {
						result = processRequest(request.request, request.arg1, request.arg2, request.obj);
					} catch (final Exception e) {
						if (callOnError(e))
							break LOOP;
					}
					if (result)
						break LOOP;
					break;
				}
				request.request = REQUEST_TASK_NON;
				mRequestPool.offer(request);
			} catch (final InterruptedException e) {
				break;
			}
		}
		try {
			onBeforeStop();
			onStop();
		} catch (final Exception e) {
			callOnError(e);
		}
		try {
			onRelease();
		} catch (final Exception e) {
			callOnError(e);
		}
		synchronized (mSync) {
			mIsRunning = false;
			mSync.notifyAll();
		}
	}
	/**
	 * エラー処理。onErrorを呼び出す。
	 * trueを返すと要求メッセージ処理ループを終了する
	 * @param e
	 * @return
	 */
	protected boolean callOnError(final Exception e) {
		try {
			return onError(e);
		} catch (final Exception e2) {
//			if (DEBUG) Log.e(TAG, "exception occurred in callOnError", e);
		}
		return true;
	}

	protected Request obtain(final int request, final int arg1, final int arg2, final Object obj) {
		Request req = mRequestPool.poll();
		if (req != null) {
			req.request = request;
			req.arg1 = arg1;
			req.arg2 = arg2;
			req.obj = obj;
		} else {
			req = new Request(request, arg1, arg2, obj);
		}
		return req;
	}

	public void offer(final int request, final int arg1, final int arg2, final Object obj) {
		mRequestQueue.offer(obtain(request, arg1, arg2, obj));
	}

	/**
	 * offer request to run on worker thread
	 * @param request minus values and zero are reserved
	 * @param arg1
	 * @param obj
	 */
	public void offer(final int request, final int arg1, final Object obj) {
		mRequestQueue.offer(obtain(request, arg1, 0, obj));
	}

	/**
	 *
	 * @param request
	 * @param arg1
	 * @param arg2
	 */
	public void offer(final int request, final int arg1, final int arg2) {
		mRequestQueue.offer(obtain(request, arg1, arg2, null));
	}

	/**
	 * offer request to run on worker thread
	 * @param request minus values and zero are reserved
	 * @param arg1
	 */
	public void offer(final int request, final int arg1) {
		mRequestQueue.offer(obtain(request, arg1, 0, null));
	}

	/**
	 * offer request to run on worker thread
	 * @param request minus values and zero are reserved
	 */
	public void offer(final int request) {
		mRequestQueue.offer(obtain(request, 0, 0, null));
	}

	/**
	 * offer request to run on worker thread
	 * @param request minus values and zero are reserved
	 * @param obj
	 */
	public void offer(final int request, final Object obj) {
		mRequestQueue.offer(obtain(request, 0, 0, obj));
	}

	/**
	 * offer request to run on worker thread on top of the request queue
	 * @param request minus values and zero are reserved
	 * @param arg1
	 * @param arg2
	 */
	public void offerFirst(final int request, final int arg1, final int arg2, final Object obj) {
		mRequestQueue.offerFirst(obtain(request, arg1, arg2, obj));
	}

	/**
	 * request to run on worker thread
	 * @param task
	 */
	public void queueEvent(final Runnable task) {
		if (task != null)
			offer(REQUEST_TASK_RUN, task);
	}

	public void removeRequest(final Request request) {
		for (; mRequestQueue.remove(request) && mIsRunning ;) {};
	}

	public void removeRequest(final int request) {
		final Request req = obtain(request, 0, 0, null);
		removeRequest(req);
	}

	/**
	 * request terminate worker thread and release all related resources
	 */
	public void release() {
		mRequestQueue.clear();
		synchronized (mSync) {
			if (mIsRunning) {
				offerFirst(REQUEST_TASK_QUIT, 0, 0, null);
				mIsRunning = false;
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		}
	}

	public void releaseSelf() {
		mRequestQueue.clear();
		synchronized (mSync) {
			if (mIsRunning) {
				offerFirst(REQUEST_TASK_QUIT, 0, 0, null);
				mIsRunning = false;
			}
		}
	}

}
