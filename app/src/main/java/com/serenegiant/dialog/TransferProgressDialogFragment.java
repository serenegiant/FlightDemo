package com.serenegiant.dialog;

import android.app.Activity;
import android.app.Fragment;

public class TransferProgressDialogFragment extends BaseDialogFragment {
	private static final String TAG = "TransferProgressDialogFragment";

	public static TransferProgressDialogFragment showDialog(final Activity parent) {
		TransferProgressDialogFragment fragment = newInstance();
		try {
			fragment.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static TransferProgressDialogFragment showDialog(final Fragment parent) {
		TransferProgressDialogFragment fragment = newInstance();
		fragment.setTargetFragment(parent, parent.getId());
		try {
			fragment.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static TransferProgressDialogFragment newInstance() {
		final TransferProgressDialogFragment fragment = new TransferProgressDialogFragment();
		return fragment;
	}
}
