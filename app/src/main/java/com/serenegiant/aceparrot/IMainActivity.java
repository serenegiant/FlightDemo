package com.serenegiant.aceparrot;

import android.view.View;

import com.serenegiant.gamepad.Joystick;

public interface IMainActivity {
	public Joystick getJoystick();
	public void setSideMenuEnable(final boolean enable);
	public void setSideMenuView(final View side_menu_view);
	public void removeSideMenuView(final View side_menu_view);
	public void closeSideMenu();
}
