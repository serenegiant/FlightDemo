package com.serenegiant.aceparrot;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SideMenuAdapter extends ArrayAdapter<String> {
	private static final String TAG = SideMenuAdapter.class.getSimpleName();

	public interface SideMenuAdapterListener {
		public void onAnimationFinished(SideMenuAdapter adapter);
	}

	private final LayoutInflater mInflater;
	private final int itemLayoutId;
	private SideMenuAdapterListener mListener;

	public SideMenuAdapter(final Context context, final int itemResId) {
		super(context, itemResId);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public SideMenuAdapter(final Context context, final int resource, final int itemResId) {
		super(context, resource, itemResId);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public SideMenuAdapter(final Context context, final int itemResId, final String[] objects) {
		super(context, itemResId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public SideMenuAdapter(final Context context, final int itemResId,
			final List<String> objects) {
		super(context, itemResId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public SideMenuAdapter(final Context context, final int itemResId,
			final int textViewResourceId, final String[] objects) {
		super(context, itemResId, textViewResourceId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public SideMenuAdapter(final Context context, final int itemResId,
			final int textViewResourceId, final List<String> objects) {
		super(context, itemResId, textViewResourceId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			final TextView label;
			convertView = mInflater.inflate(itemLayoutId, null);
			if (convertView instanceof TextView)
				label = (TextView)convertView;
			else if (convertView instanceof ViewGroup) {
				label = (TextView) convertView.findViewById(R.id.title);
			} else
				throw new RuntimeException("title view not found");
			final ViewHolder holder = new ViewHolder();
			holder.titleTv = label;
			convertView.setTag(holder);
		}
		final ViewHolder holder = (ViewHolder)convertView.getTag();
		// 特定の行のデータを取得
		final String str = getItem(position);

		if (!TextUtils.isEmpty(str)) {
			// テキストビューにラベルをセット
			holder.titleTv.setText(str);
		}

		// XMLで定義したアニメーションを読み込む
		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
		anim.setAnimationListener(mAnimationListener);
		// リストアイテムのアニメーションを開始
		anim.setStartOffset(position * 100);
		convertView.startAnimation(anim);
		return convertView;
	}

	private static class ViewHolder {
        TextView titleTv;
    }

	public void setSideMenuAdapterListener(final SideMenuAdapterListener listener) {
		mListener = listener;
	}

	public SideMenuAdapterListener getSideMenuAdapterListener() {
		return mListener;
	}

    private int mAnimationCount;
	private final Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(final Animation animation) {
			mAnimationCount++;
		}
		@Override
		public void onAnimationEnd(final Animation animation) {
			mAnimationCount--;
			if (mAnimationCount <= 0) {
				// 全ての表示項目のアニメーションが終わればコールバックを呼び出す
				if (mListener != null) {
					try {
						mListener.onAnimationFinished(SideMenuAdapter.this);
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}
		}
		@Override
		public void onAnimationRepeat(final Animation animation) {
		}
	};

}
