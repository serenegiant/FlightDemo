package com.serenegiant.aceparrot;

public class PagerAdapterConfig {
	public final int title_id;
	public final int layout_id;
	public final PagerAdapterItemHandler handler;

	public PagerAdapterConfig(final int _title_id, final int _layout_id, final PagerAdapterItemHandler _handler) {
		title_id = _title_id;
		layout_id = _layout_id;
		handler = _handler;
	}
}
