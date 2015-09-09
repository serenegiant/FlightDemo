package com.serenegiant.dialog;

import android.app.Activity;
import android.app.Fragment;

public class ConfirmDialog extends BaseDialogFragment {
	private static final String TAG = "ConfirmDialog";

	public static ConfirmDialog showDialog(final Activity parent) {
		ConfirmDialog dialog = newInstance();
		try {
  			dialog.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			dialog = null;
		}
		return dialog;
	}

	public static ConfirmDialog showDialog(final Fragment parent) {
		ConfirmDialog dialog = newInstance();
		dialog.setTargetFragment(parent, parent.getId());
		try {
  			dialog.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			dialog = null;
		}
		return dialog;
	}

	public static ConfirmDialog newInstance() {
		final ConfirmDialog dialog = new ConfirmDialog();
		return dialog;
	}
}
