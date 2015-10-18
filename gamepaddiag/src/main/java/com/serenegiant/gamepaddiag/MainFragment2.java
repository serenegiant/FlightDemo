package com.serenegiant.gamepaddiag;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.serenegiant.gamepad.GamePadConst;

import java.util.ArrayList;
import java.util.List;

public class MainFragment2 extends BaseFragment {
//	private static final boolean DEBUG = false;
//	private static final String TAG = MainFragment2.class.getSimpleName();

	protected static List<KeyPosition> sPositions = new ArrayList<KeyPosition>();
	static {
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_2, 290, 125, 120, 25));		// 左上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_1, 290, 155, 120, 25));		// 左上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_CENTER, 430, 430, 100, 100));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_UP, 290, 258, 80, 90));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_RIGHT, 351, 310, 90, 80));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_DOWN, 290, 375, 80, 90));
		sPositions.add(new KeyPosition(GamePadConst.KEY_LEFT_LEFT, 230, 310, 90, 80));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_2, 906, 125, 120, 25));		// 右上後
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_1, 906, 155, 120, 25));		// 右上前
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_CENTER, 764, 430, 100, 100));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_UP, 905, 238, 60, 60));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_RIGHT, 980, 310, 60, 60));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_DOWN, 905, 380, 60, 60));
		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_LEFT, 835, 310, 60, 60));
		//
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_LEFT, 490, 260, 70, 40));	// 中央左
		sPositions.add(new KeyPosition(GamePadConst.KEY_CENTER_RIGHT, 705, 260, 70, 40));	// 中央右
		//
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_A, 414, 88, 37, 37));			// アナログモードの時の右キーパッド上
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_B, 449, 123, 37, 37));		// アナログモードの時の右キーパッド右
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_C, 414, 156, 37, 37));		// アナログモードの時の右キーパッド下
//		sPositions.add(new KeyPosition(GamePadConst.KEY_RIGHT_D, 380, 123, 37, 37));		// アナログモードの時の右キーパッド左
	}

	private static final int[] sStickPos = new int[] {
		430, 430, 80,	// 左アナログスティック
		764, 430, 80,	// 右アナログスティック
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
		mNameTv = (TextView)rootView.findViewById(R.id.name_textview);
	}
}
