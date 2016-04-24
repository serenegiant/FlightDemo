package com.serenegiant.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.serenegiant.aceparrot.R;
import com.serenegiant.aceparrot.SideMenuAdapter;

public class SideMenuListView extends ListView implements ISideMenuView {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "SideMenuListView";

	private OnSidleMenuListener mOnSidleMenuListener;
	private ListAdapter mAdapter;

	public SideMenuListView(final Context context) {
		this(context, null, 0);
	}

	public SideMenuListView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SideMenuListView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private SideMenuAdapter.SideMenuAdapterListener mOrgSideMenuAdapterListener;
	@Override
	public void setAdapter(final ListAdapter adapter) {
		super.setAdapter(adapter);
		// リスト項目のアニメーションを簡単にリセットする為に保持しておく
		// 内部から呼ぶ時はsuper.setAdapterを呼び出すこと
		// でないとmAdapterの値が更新されてしまってリストの内容がわからなくなってしまう
		if (mAdapter != adapter) {
			mAdapter = adapter;
			if (adapter instanceof SideMenuAdapter) {
				final SideMenuAdapter.SideMenuAdapterListener listener = ((SideMenuAdapter) adapter).getSideMenuAdapterListener();
				if (listener != mSideMenuAdapterListener) {
					mOrgSideMenuAdapterListener = listener;
				}
				((SideMenuAdapter) adapter).setSideMenuAdapterListener(mSideMenuAdapterListener);
			}
		}
	}

	/**
	 * コールバックリスナーを取得
	 * @return
	 */
	public OnSidleMenuListener getOnSidleMenuListener() {
		return mOnSidleMenuListener;
	}

	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	public void setOnSidleMenuListener(final OnSidleMenuListener listener) {
		mOnSidleMenuListener = listener;
	}

	/**
	 * アニメーションをリセット
	 */
	@Override
	public void reset() {
		if (DEBUG) Log.v(TAG, "reset:");
		super.setAdapter(mAdapter);
	}

	/**
	 * 表示中の項目数
	 */
	private volatile int mVisibleCount;
	/**
	 * サイドメニュー項目を上から順にアニメーションでスライドアウトさせる
	 */
	@Override
	public void hide() {
		if (DEBUG) Log.v(TAG, "hide:");
		mVisibleCount = 0;
		final int first = getFirstVisiblePosition();
		final int last = getLastVisiblePosition();
		if ((first >= 0) && (last >= first)) {
			mVisibleCount = last - first + 1;
			for (int i = first; i <= last; i++) {
				final View item = getChildAt(i);
				if (item != null) {
					final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
					anim.setStartOffset((i - first) * 100);
					anim.setFillAfter(true);
					anim.setAnimationListener(mAnimationListener);
					item.startAnimation(anim);
				} else {
					mVisibleCount--;
				}
			}
		}
		if (mVisibleCount <= 0 && (mOnSidleMenuListener != null)) {
			mOnSidleMenuListener.onSideMenuHide(this);
		}
	}

	private final AnimationListener mAnimationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(final Animation animation) {
			if (DEBUG) Log.v(TAG, "onAnimationStart:mVisibleCount=" + mVisibleCount);
		}
		@Override
		public void onAnimationEnd(final Animation animation) {
			if (DEBUG) Log.v(TAG, "onAnimationEnd:mVisibleCount=" + mVisibleCount);
			mVisibleCount--;
			if (mVisibleCount <= 0) {
				// 全ての表示項目のアニメーションが終わればコールバックを呼び出す
				if (mOnSidleMenuListener != null) {
					mOnSidleMenuListener.onSideMenuHide(SideMenuListView.this);
				}
			}
		}
		@Override
		public void onAnimationRepeat(final Animation animation) {
		}
	};

	private final SideMenuAdapter.SideMenuAdapterListener mSideMenuAdapterListener = new SideMenuAdapter.SideMenuAdapterListener() {
		@Override
		public void onAnimationFinished(final SideMenuAdapter adapter) {
			if (DEBUG) Log.v(TAG, "onAnimationFinished:");
			if (mOnSidleMenuListener != null) {
				mOnSidleMenuListener.onSideMenuShow(SideMenuListView.this);
			}
			if (mOrgSideMenuAdapterListener != null) {
				mOrgSideMenuAdapterListener.onAnimationFinished(adapter);
			}
		}
	};
}
