package com.serenegiant.gamepaddiag;


public class MainFragment extends BaseFragment {

	static {
		sPosition = new KeyPositions(548, 340, new int[] {
			193, 207, 64, 64,	// KEY_LEFT_CENTER = 0;
			140, 93, 37, 37,	// KEY_LEFT_UP = 1;
			168, 122, 37, 37,	// KEY_LEFT_RIGHT = 2;
			140, 150, 37, 37,	// KEY_LEFT_DOWN = 3;
			112, 122, 37, 37,	// KEY_LEFT_LEFT = 4;
			//
			360, 207, 64, 64,	// KEY_RIGHT_CENTER = 5;
			421, 89, 37, 37,	// KEY_RIGHT_UP = 6;
			455, 122, 37, 37,	// KEY_RIGHT_RIGHT = 7;
			421, 157, 37, 37,	// KEY_RIGHT_DOWN = 8;
			386, 122, 37, 37,	// KEY_RIGHT_LEFT = 9;
			//
			126, 53, 75, 75,	// KEY_LEFT_1 = 10;    		// 左上前
			116, 50, 112, 112,	// KEY_LEFT_2 = 11;    		// 左上後
			//
			222, 119, 32, 32,	// KEY_CENTER_LEFT = 12;	// 中央左
			//
			418, 53, 75, 75,	// KEY_RIGHT_1 = 13;    	// 右上前
			407, 50, 112, 112,	// KEY_RIGHT_2 = 14;   		// 右上後
			//
			335, 119, 32, 32,	// KEY_CENTER_RIGHT = 15;	// 中央右
			//
			414, 88, 37, 37,	// KEY_RIGHT_A = 16;		// アナログモードの時の右キーパッド上
			449, 123, 37, 37,	// KEY_RIGHT_B = 17;		// アナログモードの時の右キーパッド右
			414, 156, 37, 37,	// KEY_RIGHT_C = 18;		// アナログモードの時の右キーパッド下
			380, 123, 37, 37,	// KEY_RIGHT_D = 19;		// アナログモードの時の右キーパッド左
		});
	}

	public MainFragment() {
		super();
		// デフォルトコンストラクタが必要
	}
}
