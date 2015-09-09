package com.serenegiant.flightdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parrot.arsdk.armedia.ARMediaObject;

import java.util.List;

public class MediaListAdapter extends ArrayAdapter<ARMediaObject> {

	private final LayoutInflater mInflater;
	private final int itemLayoutId;

	public MediaListAdapter(final Context context, final int itemResId) {
		super(context, itemResId);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public MediaListAdapter(final Context context, final int resource, final int itemResId) {
		super(context, resource, itemResId);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public MediaListAdapter(final Context context, final int itemResId, final ARMediaObject[] objects) {
		super(context, itemResId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public MediaListAdapter(final Context context, final int itemResId,
			final List<ARMediaObject> objects) {
		super(context, itemResId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public MediaListAdapter(final Context context, final int itemResId,
			final int textViewResourceId, final ARMediaObject[] objects) {
		super(context, itemResId, textViewResourceId, objects);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = itemResId;
	}

	public MediaListAdapter(final Context context, final int itemResId,
			final int textViewResourceId, final List<ARMediaObject> objects) {
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


		return convertView;
	}

	private static class ViewHolder {
        TextView titleTv;
    }

}
