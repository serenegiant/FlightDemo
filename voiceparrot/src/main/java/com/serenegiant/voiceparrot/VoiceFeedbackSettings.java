package com.serenegiant.voiceparrot;

import android.util.Log;

import com.serenegiant.aceparrot.VoiceFeedback;
import com.serenegiant.utils.CollectionMap;

import static com.serenegiant.aceparrot.VoiceConst.*;

/**
 * Created by saki on 2017/02/25.
 *
 */
public class VoiceFeedbackSettings {
	private static final boolean DEBUG = true;	// 実働時はfalseにすること
	private static final String TAG = VoiceFeedbackSettings.class.getSimpleName();

	public static void init() {
		if (DEBUG) Log.v(TAG, "init:");
		final CollectionMap<Long, Integer> ids = VoiceFeedback.ID_MAP;
		ids.clear();

// CMD_STOP
//		ids.add((long)CMD_STOP, R.raw.stop_001);
//		ids.add((long)CMD_STOP, R.raw.stop_002);
//		ids.add((long)CMD_STOP, R.raw.stop_003);
//		ids.add((long)CMD_STOP, R.raw.stop_004);
//		ids.add((long)CMD_STOP, R.raw.stop_005);
//		ids.add((long)CMD_STOP, R.raw.stop_006);
//		ids.add((long)CMD_STOP, R.raw.stop_007);
//		ids.add((long)CMD_STOP, R.raw.stop_008);
//		ids.add((long)CMD_STOP, R.raw.stop_009);
		ids.add((long)CMD_STOP, R.raw.stop_010);
		ids.add((long)CMD_STOP, R.raw.stop_011);
//		ids.add((long)CMD_STOP, R.raw.stop_012);
//		ids.add((long)CMD_STOP, R.raw.stop_013);
		ids.add((long)CMD_STOP, R.raw.stop_014);
//		ids.add((long)CMD_STOP, R.raw.stop_015);
//		ids.add((long)CMD_STOP, R.raw.stop_016);

		ids.add((long)CMD_TAKEOFF, R.raw.takeoff_001);
//		ids.add((long)CMD_TAKEOFF, R.raw.tekeoff_002);
//		ids.add((long)CMD_TAKEOFF, R.raw.tekeoff_003);
//		ids.add((long)CMD_TAKEOFF, R.raw.tekeoff_004);
//		ids.add((long)CMD_TAKEOFF, R.raw.tekeoff_005);
//		ids.add((long)CMD_TAKEOFF, R.raw.tekeoff_006);

//		ids.add((long)CMD_LANDING, R.raw.landing_001);
		ids.add((long)CMD_LANDING, R.raw.landing_002);
		ids.add((long)CMD_LANDING, R.raw.landing_003);
//		ids.add((long)CMD_LANDING, R.raw.landing_004);
//		ids.add((long)CMD_LANDING, R.raw.landing_005);

		ids.add((long)CMD_FLIP | DIR_BACKWARD, R.raw.flip_back_001);
//		ids.add((long)CMD_FLIP | DIR_BACKWARD, R.raw.flip_back_002);

		ids.add((long)CMD_FLIP | DIR_FORWARD, R.raw.flip_front_001);
//		ids.add((long)CMD_FLIP | DIR_FORWARD, R.raw.flip_front_002);

		ids.add((long)CMD_FLIP | DIR_LEFT, R.raw.flip_left_001);
//		ids.add((long)CMD_FLIP | DIR_LEFT, R.raw.flip_left_002);

		ids.add((long)CMD_FLIP | DIR_RIGHT, R.raw.flip_right_001);
//		ids.add((long)CMD_FLIP | DIR_RIGHT, R.raw.flip_right_002);

//		ids.add((long)CMD_FLIP , R.raw.flip_001);
//		ids.add((long)CMD_FLIP , R.raw.flip_002);
		ids.add((long)CMD_FLIP, R.raw.flip_003);
//		ids.add((long)CMD_FLIP , R.raw.flip_004);
//		ids.add((long)CMD_FLIP , R.raw.flip_005);
//		ids.add((long)CMD_FLIP , R.raw.flip_006);
		ids.add((long)CMD_FLIP , R.raw.flip_007);
//		ids.add((long)CMD_FLIP , R.raw.flip_008);


//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_001);
//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_002);
//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_003);
// 		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_004);
//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_005);
//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_006);
		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_007);
		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_008);
//		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_009);
		ids.add((long)CMD_MOVE | DIR_FORWARD, R.raw.move_front_010);

//		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_001);
		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_002);
//		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_003);
		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_004);
		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_005);
//		ids.add((long)CMD_MOVE | DIR_BACKWARD, R.raw.move_back_006);

//		ids.add((long)CMD_MOVE | DIR_DOWN, R.raw.move_down_001);
		ids.add((long)CMD_MOVE | DIR_DOWN, R.raw.move_down_002);
//		ids.add((long)CMD_MOVE | DIR_DOWN, R.raw.move_down_003);
//		ids.add((long)CMD_MOVE | DIR_DOWN, R.raw.move_down_004);
//		ids.add((long)CMD_MOVE | DIR_DOWN, R.raw.move_down_005);

//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_001);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_002);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_003);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_004);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_005);
		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_006);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_007);
		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_008);
		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_left_009);
//		ids.add((long)CMD_MOVE | DIR_LEFT, R.raw.move_right_001);

//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_002);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_003);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_004);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_005);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_006);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_007);
		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_008);
//		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_009);
		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_010);
		ids.add((long)CMD_MOVE | DIR_RIGHT, R.raw.move_right_011);

//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_001);
//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_002);
//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_003);
//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_004);
//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_005);
		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_006);
//		ids.add((long)CMD_MOVE | DIR_UP, R.raw.move_up_007);
//
//		ids.add((long)CMD_MOVE, R.raw.move_001);
//		ids.add((long)CMD_MOVE, R.raw.move_002);
//		ids.add((long)CMD_MOVE, R.raw.move_003);
//		ids.add((long)CMD_MOVE R.raw.move_004);
		ids.add((long)CMD_MOVE, R.raw.move_005);

		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_001);
		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_002);
//		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_003);
//		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_004);
//		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_005);
		ids.add((long)CMD_TURN | DIR_LEFT, R.raw.turn_left_006);
		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_001);
		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_002);
//		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_003);
//		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_004);
//		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_005);
		ids.add((long)CMD_TURN | DIR_RIGHT, R.raw.turn_right_006);

//		ids.add((long)CMD_TURN, R.raw.turn_001);
//		ids.add((long)CMD_TURN, R.raw.turn_002);
//		ids.add((long)CMD_TURN, R.raw.turn_003);
//		ids.add((long)CMD_TURN, R.raw.turn_004);
		ids.add((long)CMD_TURN, R.raw.turn_005);

//		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_close_001);
//		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_close_002);
		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_close_003);
//		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_close_004);
//		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_close_005);
		ids.add((long)CMD_CLAW_CLOSE, R.raw.claw_open_005);	// ファイル名間違えたこれはclose
//
//		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_001);
//		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_002);
//		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_003);
		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_004);
//		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_006);
		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_007);
//		ids.add((long)CMD_CLAW_OPEN, R.raw.claw_open_008);
//
//		ids.add((long)CMD_CLAW_TOGGLE, R.raw.claw_toggle_001);
		ids.add((long)CMD_CLAW_TOGGLE, R.raw.claw_toggle_002);
//		ids.add((long)CMD_CLAW_TOGGLE, R.raw.claw_toggle_003);
//		ids.add((long)CMD_CLAW_TOGGLE, R.raw.claw_toggle_004);

		ids.add((long)CMD_ERROR_BATTERY_LOW_CRITICAL, R.raw.alarm_battery_critical_001);
//		ids.add((long)CMD_ERROR_BATTERY_LOW_CRITICAL, R.raw.alarm_battery_critical_002);
//		ids.add((long)CMD_ERROR_BATTERY_LOW_CRITICAL, R.raw.alarm_battery_critical_003);
//		ids.add((long)CMD_ERROR_BATTERY_LOW_CRITICAL, R.raw.alarm_battery_critical_004);

		ids.add((long)CMD_ERROR_BATTERY_LOW , R.raw.alarm_battery_low_001);
//		ids.add((long)CMD_ERROR_BATTERY_LOW, R.raw.alarm_battery_low_002);
//		ids.add((long)CMD_ERROR_BATTERY_LOW, R.raw.alarm_battery_low_003);
//
		ids.add((long)CMD_ERROR_MOTOR, R.raw.alarm_error_001);
//		ids.add((long)CMD_ERROR_MOTOR, R.raw.alarm_error_002);
//		ids.add((long)CMD_ERROR_MOTOR, R.raw.alarm_error_003);
//
//		ids.add((long) , R.raw.destroy_001);
//		ids.add((long) , R.raw.destroy_002);
//		ids.add((long) , R.raw.destroy_003);
//		ids.add((long) , R.raw.destroy_004);
//
//		ids.add((long) , R.raw.greets_001);
		ids.add((long)CMD_GREETINGS_HELLO, R.raw.greets_002);
		ids.add((long)CMD_GREETINGS_HELLO, R.raw.greets_003);
//		ids.add((long)CMD_GREETINGS_HELLO, R.raw.greets_004);
//
//		ids.add((long)CMD_FIRE, R.raw.gun_001);
//		ids.add((long)CMD_FIRE, R.raw.gun_002);
//		ids.add((long)CMD_FIRE, R.raw.gun_003);
//		ids.add((long)CMD_FIRE, R.raw.gun_004);
//		ids.add((long)CMD_FIRE, R.raw.gun_005);
//		ids.add((long)CMD_FIRE, R.raw.gun_006);
//		ids.add((long)CMD_FIRE, R.raw.gun_007);
//		ids.add((long)CMD_FIRE, R.raw.gun_008);
//		ids.add((long)CMD_FIRE, R.raw.gun_009);
//		ids.add((long)CMD_FIRE, R.raw.gun_010);
//		ids.add((long)CMD_FIRE, R.raw.gun_011);
//		ids.add((long)CMD_FIRE, R.raw.gun_012);
//		ids.add((long)CMD_FIRE, R.raw.gun_013);
//		ids.add((long)CMD_FIRE, R.raw.gun_014);
//		ids.add((long)CMD_FIRE, R.raw.gun_015);
//		ids.add((long)CMD_FIRE, R.raw.gun_016);
//		ids.add((long)CMD_FIRE, R.raw.gun_017);
//		ids.add((long)CMD_FIRE, R.raw.gun_018);
		ids.add((long)CMD_FIRE, R.raw.gun_019);
		ids.add((long)CMD_FIRE, R.raw.gun_020);
//		ids.add((long)CMD_FIRE, R.raw.gun_021);
//		ids.add((long)CMD_FIRE, R.raw.gun_022);
//		ids.add((long)CMD_FIRE, R.raw.gun_023);
//		ids.add((long)CMD_FIRE, R.raw.gun_024);

		ids.add((long)CMD_SPIN | DIR_LEFT, R.raw.spin_left_001);
		ids.add((long)CMD_SPIN | DIR_LEFT, R.raw.spin_left_002);
//		ids.add((long)CMD_SPIN | DIR_LEFT, R.raw.spin_left_003);
		ids.add((long)CMD_SPIN | DIR_LEFT, R.raw.spin_left_004);

		ids.add((long)CMD_SPIN | DIR_RIGHT, R.raw.spin_right_001);
		ids.add((long)CMD_SPIN | DIR_RIGHT, R.raw.spin_right_002);
//		ids.add((long)CMD_SPIN | DIR_RIGHT, R.raw.spin_right_003);
		ids.add((long)CMD_SPIN | DIR_RIGHT, R.raw.spin_right_004);

//		ids.add((long)CMD_SPIN, R.raw.spin_001);
		ids.add((long)CMD_SPIN, R.raw.spin_002);
//		ids.add((long)CMD_SPIN, R.raw.spin_003);
//		ids.add((long)CMD_SPIN, R.raw.spin_004);
//		ids.add((long)CMD_SPIN, R.raw.spin_005);
//		ids.add((long)CMD_SPIN, R.raw.spin_006);
//		ids.add((long)CMD_SPIN, R.raw.spin_007);
//		ids.add((long)CMD_SPIN, R.raw.spin_008);
//		ids.add((long)CMD_SPIN, R.raw.spin_009);
//		ids.add((long)CMD_SPIN, R.raw.spin_010);
//		ids.add((long)CMD_SPIN, R.raw.spin_011);
//		ids.add((long)CMD_SPIN, R.raw.spin_012);

//		ids.add((long) , R.raw.ok_001);
//		ids.add((long) , R.raw.ok_002);
//		ids.add((long) , R.raw.ok_003);
//
//		ids.add((long) , R.raw.script_0_001);
//		ids.add((long) , R.raw.script_1_001);
//		ids.add((long) , R.raw.script_1_002);
//		ids.add((long) , R.raw.script_2_001);
//		ids.add((long) , R.raw.script_2_002);
//		ids.add((long) , R.raw.script_3_001);
//		ids.add((long) , R.raw.script_3_002);
//
//		ids.add((long) , R.raw.script_circle_001);
//		ids.add((long) , R.raw.script_circle_002);
//		ids.add((long) , R.raw.script_circle_003);
//		ids.add((long) , R.raw.script_circle_004);
//		ids.add((long) , R.raw.script_circle_005);
//		ids.add((long) , R.raw.script_circle_006);
//		ids.add((long) , R.raw.script_circle_007);
//		ids.add((long) , R.raw.script_circle_008);
//		ids.add((long) , R.raw.script_circle_009);
//
		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_001);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_002);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_003);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_004);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_005);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_006);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_007);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_008);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_009);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_010);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_011);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_012);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_013);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_014);
//		ids.add((long)CMD_SR_ERROR_SPEECH_TIMEOUT, R.raw.waiting_015);
	}
}
