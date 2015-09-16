package com.serenegiant.flightdemo;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parrot.arsdk.armedia.ARMediaObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ARMediaObjectListAdapter extends ArrayAdapter<ARMediaObject> {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = ARMediaObjectListAdapter.class.getSimpleName();

	private final SimpleDateFormat mDurationFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
	private final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HHmmss", Locale.getDefault());
	private final LayoutInflater mInflater;
	private final int itemLayoutId;

	public ARMediaObjectListAdapter(final Context context, final int resource) {
		super(context, resource);
		mInflater = LayoutInflater.from(context);
		itemLayoutId = resource;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			final TextView label;
			convertView = mInflater.inflate(itemLayoutId, null);
		}
		ViewHolder holder = (ViewHolder)convertView.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
			holder.size = (TextView) convertView.findViewById(R.id.size);
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
			holder.playable = (ImageView) convertView.findViewById(R.id.playable_imageview);
			convertView.setTag(holder);
		}
		final ARMediaObject mediaObject = getItem(position);
		if (mediaObject != null) {
			// FIXME 非同期でセットするようにした方がいいかも
			holder.mediaObject = mediaObject;
			if (holder.title != null) {
				holder.title.setText(mediaObject.getName());
			}
			if (holder.datetime != null) {
				final String dateStr = mediaObject.getDate();
				try {
					final Date date = mDateTimeFormat.parse(dateStr);
					holder.datetime.setText(DateUtils.formatDateTime(getContext(), date.getTime(),
						DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));
				} catch (ParseException e) {
					if (DEBUG) Log.w(TAG, "dateStr:" + dateStr, e);
					holder.datetime.setText(dateStr);
				}
			}
			if (holder.size != null) {
				final float size = mediaObject.getSize();
				final String s;
				if (size > 1024 * 1024 * 0.5f) {
					s = String.format("%5.1fMB", size / 1024 / 1024);
				} else {
					s = String.format("%5.1fkB", size / 1024);
				}
				holder.size.setText(s);
			}
			if (holder.thumbnail != null) {
				holder.thumbnail.setImageDrawable(mediaObject.getThumbnail());
			}
			holder.isPlayable = false;
			if (holder.playable != null) {
				try {
					final File file = new File(mediaObject.getFilePath());
					holder.isPlayable = file.exists() && (file.length() == mediaObject.getSize());
				} catch (final Exception e) {
				}
				holder.playable.setVisibility(holder.isPlayable ? View.VISIBLE : View.INVISIBLE);
			}
		}
		return convertView;
	}

	private static class ViewHolder {
		ARMediaObject mediaObject;
        TextView title;
        TextView datetime;
        TextView size;
		ImageView thumbnail;
        ImageView playable;
        boolean isPlayable;
    }

}
