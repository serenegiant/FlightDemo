package com.serenegiant.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class ComponentUtils {
	public static void disable(final Context context, final Class<?> clazz) {
		setComponentState(context, clazz, false);
	}

	public static void enable(final Context context, final Class<?> clazz) {
		setComponentState(context, clazz, true);
	}

	public static void setComponentState(final Context context, final Class<?> clazz, final boolean enabled) {
		final int newState = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		final ComponentName componentName = new ComponentName(context, clazz);
		final PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
	}
}
