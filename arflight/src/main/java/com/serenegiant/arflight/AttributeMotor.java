package com.serenegiant.arflight;

/**
 * モーターの種類とバージョン
 */
public class AttributeMotor extends AttributeVersion {
	public static final int ERR_MOTOR_NON			= 0x000000000;
	public static final int ERR_MOTOR_EEPROM		= 0x000000001;	// 1 EEPROM access failure
	public static final int ERR_MOTOR_STALLED		= 0x000000002;	// 2: Motor stalled
	public static final int ERR_MOTOR_CUTOUT		= 0x000000004;	// 3: Propeller cutout security triggered
	public static final int ERR_MOTOR_COMM			= 0x000000008;	// 4: Communication with motor failed by timeout
	public static final int ERR_MOTOR_RC_EMERGENCY	= 0x000000010;	// 5: RC emergency stop
	public static final int ERR_MOTOR_REALTIME		= 0x000000020;	// 6: Motor controller scheduler real-time out of bounds
	public static final int ERR_MOTOR_SETTING		= 0x000000040;	// 7: One or several incorrect values in motor settings
	public static final int ERR_MOTOR_THERMAL		= 0x000000080;	// 8: Too hot or too cold Cypress temperature
	public static final int ERR_MOTOR_VOLTAGE		= 0x000000100;	// 9: Battery voltage out of bounds
	public static final int ERR_MOTOR_LIPO			= 0x000000200;	//10: Incorrect number of LIPO cells
	public static final int ERR_MOTOR_MOSFET		= 0x000000400;	//11: Defectuous MOSFET or broken motor phases
	public static final int ERR_MOTOR_BOOTLOADER	= 0x000000800;	//12: Not use for BLDC but useful for HAL
	public static final int ERR_MOTOR_ASSERT		= 0x000001000;	//13: Error Made by BLDC_ASSERT()

	protected String mType;
	protected int mError;

	public AttributeMotor() {
		mError = ERR_MOTOR_NON;
	}

	public void set(final String type, final String software, final String hardware) {
		set(software, hardware);
		mType = type;
	}

	public void setError(final int err) {
		mError |= err;
	}

	public int clearError() {
		final int err = mError;
		mError = ERR_MOTOR_NON;
		return err;
	}

	public int error() {
		return mError;
	}
}
