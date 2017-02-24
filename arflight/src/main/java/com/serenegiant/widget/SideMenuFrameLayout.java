package com.serenegiant.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SideMenuFrameLayout extends FrameLayout implements ISideMenuView {

	private OnSidleMenuListener mOnSidleMenuListener;

	public SideMenuFrameLayout(final Context context) {
		super(context);
	}

	public SideMenuFrameLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SideMenuFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void hide() {
		final int n = getChildCount();
		int m = 0;
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).hide();
				m++;
			}
		}
		if ((m == 0) && (mOnSidleMenuListener != null)) {
			// 子ViewにISideMenuViewインスタンスが無い時はここでコールバックを呼び出す
			// 子Viewが無い時orISideMenuViewインスタンスが無い時に
			// onSideMenuHideが呼び出されないのを防ぐため
			mOnSidleMenuListener.onSideMenuHide(this);
		}
	}

	@Override
	public void reset() {
		final int n = getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).reset();
			}
		}
	}

	@Override
	public void setOnSidleMenuListener(final OnSidleMenuListener listener) {
		mOnSidleMenuListener = listener;
		final int n = getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).setOnSidleMenuListener(listener);
			}
		}
	}

	@Override
	public OnSidleMenuListener getOnSidleMenuListener() {
		return mOnSidleMenuListener;
	}

}
