package com.serenegiant.arflight;

public interface IWiFiController {
	/**
	 * 室外モードか室内モードかを設定
	 * @param is_outdoor
	 * @return
	 */
	public boolean sendSettingsOutdoor(final boolean is_outdoor);
	public boolean isOutdoor();
}
