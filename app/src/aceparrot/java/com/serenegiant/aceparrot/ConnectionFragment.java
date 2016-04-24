package com.serenegiant.aceparrot;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

public class ConnectionFragment extends BaseConnectionFragment {

	public static ConnectionFragment newInstance(final boolean newAPI) {
		ConnectionFragment fragment = new ConnectionFragment();
		final Bundle args = fragment.setNewAPI(newAPI);
		return fragment;
	}

	public ConnectionFragment() {
		super();
	}

	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			fragment = getFragment(position, true);
			break;
		case R.id.download_button:
			fragment = getFragment(position, false);
			break;
		case R.id.gallery_button:
			fragment = GalleyFragment.newInstance();
			break;
		case R.id.script_button:
			fragment = ScriptFragment.newInstance();
			break;
		case R.id.config_show_btn:
			fragment = ConfigAppFragment.newInstance();
			break;
		}
		replace(fragment);
	}

	protected boolean onLongClick(final View view, final int position) {
		return false;
	}
}
