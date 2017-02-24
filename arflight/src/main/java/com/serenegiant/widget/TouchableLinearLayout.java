package com.serenegiant.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * コールバックリスナーでタッチイベント処理を横取りするかどうかを決めることができるLinearLayout
 */
public class TouchableLinearLayout extends LinearLayout {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = TouchableLinearLayout.class.getSimpleName();

	public interface OnTouchableListener {
		/**
		 * ViewGroupのonInterceptTouchEventが呼び出された時のコールバック
		 * @param event
		 * @return trueを返すと以降のTouchEventが横取りされてonTouchEventが呼ばれる
		 */
		public boolean onInterceptTouchEvent(final MotionEvent event);

		/**
		 * ViewGroupのonTouchEventが呼び出された時のコールバック
		 * @param event
		 * @return true:処理済み, false:上位に任せる
		 */
		public boolean onTouchEvent(final MotionEvent event);
	}

	private OnTouchableListener mOnTouchableListener;

	public TouchableLinearLayout(final Context context) {
		this(context, null, 0);
	}

	public TouchableLinearLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchableLinearLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public synchronized void setOnTouchableListener(final OnTouchableListener listener) {
		mOnTouchableListener = listener;
	}

	@Override
	public synchronized boolean onInterceptTouchEvent(final MotionEvent event) {
		if (mOnTouchableListener != null) {
			try {
				return mOnTouchableListener.onInterceptTouchEvent(event) || super.onInterceptTouchEvent(event);
			} catch (final Exception e) {
				// ignore
			}
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public synchronized boolean onTouchEvent(final MotionEvent event) {
		if (mOnTouchableListener != null) {
			try {
				return mOnTouchableListener.onTouchEvent(event) || super.onTouchEvent(event);
			} catch (final Exception e) {
				// ignore
			}
		}
		return super.onTouchEvent(event);
	}

}
