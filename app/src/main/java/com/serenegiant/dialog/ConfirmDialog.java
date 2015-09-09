package com.serenegiant.dialog;

import android.app.Activity;
import android.app.Fragment;

public class ConfirmDialog extends BaseDialogFragment {
	private static final String TAG = "ConfirmDialog";

	public static ConfirmDialog showDialog(final Activity parent) {
		ConfirmDialog fragemnt = newInstance();
		try {
  			fragemnt.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragemnt = null;
		}
		return fragemnt;
	}

	public static ConfirmDialog showDialog(final Fragment parent) {
		ConfirmDialog fragment = newInstance();
		fragment.setTargetFragment(parent, parent.getId());
		try {
  			fragment.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static ConfirmDialog newInstance() {
		final ConfirmDialog fragment = new ConfirmDialog();
		return fragment;
	}
}
