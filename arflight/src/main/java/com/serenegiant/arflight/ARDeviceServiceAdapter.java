package com.serenegiant.arflight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ARDeviceServiceAdapter extends ArrayAdapter<ARDiscoveryDeviceService> {

	private final LayoutInflater mInflater;
	private final int mLayoutId;

	public ARDeviceServiceAdapter(final Context context, final int resource) {
		super(context, resource, new ArrayList<ARDiscoveryDeviceService>());
		mInflater = LayoutInflater.from(context);
		mLayoutId = resource;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View rootView = convertView;
		if (rootView == null) {
			rootView = mInflater.inflate(mLayoutId, parent, false);
		}
		ViewHolder holder = (ViewHolder)rootView.getTag(R.id.ardeviceserviceadapter);
		if (holder == null) {
			holder = new ViewHolder();
			holder.title = (TextView)rootView.findViewById(R.id.title);
			holder.state = (TextView)rootView.findViewById(R.id.state);
			holder.thumbnail = (ImageView)rootView.findViewById(R.id.thumbnail);
		}
		final ARDiscoveryDeviceService device = getItem(position);
		if (holder.title != null) {
			holder.title.setText(device.getName());
		}
		if (holder.state != null) {
			// FIXME 接続状態の更新処理
		}
		if (holder.thumbnail != null) {
			// FIXME 機体アイコンの更新処理
		}
		return rootView;
	}

	public String getItemName(final int position) {
		final ARDiscoveryDeviceService device = getItem(position);
		return device != null ? device.getName() : null;
	}

	private static final class ViewHolder {
		TextView title;
		TextView state;
		ImageView thumbnail;
	}
}
