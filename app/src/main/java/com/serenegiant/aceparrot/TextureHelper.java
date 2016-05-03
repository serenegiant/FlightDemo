package com.serenegiant.aceparrot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.serenegiant.gameengine1.IModelView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextureHelper {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = TextureHelper.class.getSimpleName();

	private static final String[][] PATH = {
		// 偶数位置が出力ファイル名, 奇数位置がマスクファイルパス
		{	// ローリングスパイダー用
			"minidrone_tex.png", "model/minidrone_tex_mask.png",
		},
		{	// Bebop2用
			"bebop_drone2_body_tex.png", "model/bebop_drone2_body_tex_mask.png",
			"bebop_drone2_rotor_front_tex.png", "model/bebop_drone2_rotor_front_tex_mask.png",
		},
		{	// Bebop用
			"bebop_drone_body_tex.png", "model/bebop_drone_body_tex_mask.png",
			"bebop_drone_rotor_front_tex.png", "model/bebop_drone_rotor_front_tex_mask.png",
			"bebop_drone_bumper_tex.png", "model/bebop_drone_bumper_tex_mask.png",
		},
		{	// Cargodrone用
			"cargo_drone_tex.png", "model/cargo_drone_tex_mask.png",
		},
	};

	public static void genTexture(final Context context, final int model, final int color) {
		if (DEBUG) Log.v(TAG, "genTexture:color=" + color);
		switch (model % IModelView.MODEL_NUM) {
		case IModelView.MODEL_JUMPINGSUMO:
			break;
		case IModelView.MODEL_MINIDRONE:
			// model/minidrone.png
			genTexture(context, PATH[0][0], PATH[0][1], color);
			break;
		case IModelView.MODEL_CARGO:
			// model/cargo_drone_tex_blue.png
			genTexture(context, PATH[3][0], PATH[3][1], color);
			break;
		case IModelView.MODEL_BEBOP2:
			// model/bebop_drone2_body_tex.png
			genTexture(context, PATH[1][0], PATH[1][1], color);
			// model/bebop_drone2_rotor_front_tex.png
			genTexture(context, PATH[1][2], PATH[1][3], color);
			// model/bebop_drone2_rotor_rear_tex.png
			// これはいつも黒だから不要
			break;
		case IModelView.MODEL_BEBOP:
		default:
			// model/bebop_drone_body_tex.png
			genTexture(context, PATH[2][0], PATH[2][1], color);
			// model/bebop_drone_rotor_front_tex.png
			genTexture(context, PATH[2][2], PATH[2][3], color);
			// model/bebop_drone_bumper_tex.png
			genTexture(context, PATH[2][4], PATH[2][5], color);
			// model/bebop_drone_rotor_rear_tex.png
			// これはいつも黒だから不要
			break;
		}
	}

	public static void clearTexture(final Context context) {
		for (final String[] p: PATH) {
			final int n = p.length;
			// 偶数位置が出力ファイル名
			for (int i = 0; i < n; i += 2) {
				final File dest = context.getFileStreamPath(p[i]);
				dest.delete();
			}
		}
	}

	/**
	 * 指定した出力先ファイル名が存在しなければmaskと指定した色でテスクチャを生成して保存する
	 * @param dest_path 出力先内部ストレージファイル名, セパレータは使用不可
	 * @param mask_path マスク用Bitmapのassets名
	 * @param color 背景の色(機体色)
	 */
	private static void genTexture(final Context context, final String dest_path, final String mask_path, final int color) {
		if (DEBUG) Log.v(TAG, "genTexture:dest_path=" + dest_path + ", mask_path=" + mask_path + ", color=" + color);
		final File dest = context.getFileStreamPath(dest_path);
		if (!dest.exists()) {
			// 存在しない時
			try {
				if (DEBUG) Log.v(TAG, "assetsからマスクを読み込む");
				final InputStream in = context.getAssets().open(mask_path);
				final Bitmap mask = BitmapFactory.decodeStream(in);
				in.close();
				final int w = mask.getWidth();
				final int h = mask.getHeight();
				final Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				if (DEBUG) Log.v(TAG, "オフスクリーン描画用のCanvasを生成");
				final Canvas canvas = new Canvas(result);
				final Paint paint = new Paint();
				if (DEBUG) Log.v(TAG, "オフスクリーンへ描画");
				canvas.drawColor(color);
				canvas.drawBitmap(mask, 0, 0, paint);
				mask.recycle();
				if (DEBUG) Log.v(TAG, "ファイルへ出力");
				final OutputStream out = new BufferedOutputStream(context.openFileOutput(dest_path, 0));
				result.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				result.recycle();
				System.gc();
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		} else if (DEBUG) {
			Log.v(TAG, "既に存在している " + dest_path);
		}
	}
}
