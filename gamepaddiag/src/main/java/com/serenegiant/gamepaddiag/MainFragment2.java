package com.serenegiant.gamepaddiag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.serenegiant.gamepad.GamePadConst;

import java.util.ArrayList;
import java.util.List;

public class MainFragment2 extends BaseFragment {

	protected static List<KeyPosition> sPositions = new ArrayList<KeyPosition>();
	static {
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_2, 128, 62, 115, 120));		// 左上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_1, 128, 53, 75, 75));			// 左上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_CENTER, 191, 206, 64, 64));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_UP, 134, 86, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_RIGHT, 160, 114, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_DOWN, 134, 143, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_LEFT, 105, 114, 38, 38));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_2, 420, 62, 115, 120));		// 右上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_1, 420, 53, 75, 75));			// 右上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_CENTER, 359, 206, 64, 64));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_UP, 416, 80, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_RIGHT, 449, 114, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_DOWN, 415, 147, 38, 38));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_LEFT, 380, 114, 38, 38));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_LEFT, 217, 113, 32, 32));	// 中央左
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_RIGHT, 330, 113, 32, 32));	// 中央右
		//
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_A, 414, 88, 37, 37));			// アナログモードの時の右キーパッド上
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_B, 449, 123, 37, 37));		// アナログモードの時の右キーパッド右
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_C, 414, 156, 37, 37));		// アナログモードの時の右キーパッド下
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_D, 380, 123, 37, 37));		// アナログモードの時の右キーパッド左
	}

	private static final int[] sStickPos = new int[] {
		191, 206, 25,	// 左アナログスティック
		359, 206, 25,	// 右アナログスティック
	};

	private GamepadView mGamepadView;

	public MainFragment2() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	protected void updateButtons(final boolean[] downs, final long[] counts, final int[] analog_sticks) {
		mGamepadView.setKeyState(downs, analog_sticks);
	}

	private void initView(final View rootView) {
		mGamepadView = (GamepadView)rootView.findViewById(R.id.gamepad_view);
		mGamepadView.setKeys(sPositions);
		mGamepadView.setSticks(sStickPos);
	}
}
