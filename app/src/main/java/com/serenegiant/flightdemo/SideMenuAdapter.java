package com.serenegiant.flightdemo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SideMenuAdapter extends ArrayAdapter<String> {

	private final LayoutInflater mInflater;
	private final int itemLayoutId;

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
		// リストアイテムのアニメーションを開始
		anim.setStartOffset(position * 100);
		convertView.startAnimation(anim);
		return convertView;
	}

	private static class ViewHolder {
        TextView titleTv;
    }
}
