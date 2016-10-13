package com.serenegiant.gameengine.v1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class StringTextureBuilder {
	protected String TAG = getClass().getSimpleName();
	
	private final Paint paint = new Paint();
	private final FontMetrics mFont;
	private final boolean mForceCreate;
	public final float glyphWidth;			// 半角1文字の幅
	public final float glyphHeight;			// 文字高さ
	
	public float[] glyphWidths;				// 各行の描画幅
	public int glyphsPerRow = 0;			// 最大文字数
	public float colWidth;
	public int colCount = 1;				// 列数

	public boolean isSquare = true;			// テクスチャサイズを正方形にするかどうか
	public boolean isSaveExternal = false;	// 外部ストレージに保存するかどうか
	public int quality = 90;
	
	/**
	 * コンストラクタ
	 * @param textSize		描画文字サイズ
	 * @param forceCreate
	 */
	public StringTextureBuilder(final int textSize, final boolean forceCreate) {
		// 等幅フォントにする・・・今はglyphWidths[]に各文字列毎の幅を保持しているので等幅でなくても行ける気がする
		paint.setTypeface(Typeface.MONOSPACE);
		paint.setTextSize(textSize); 
		paint.setAntiAlias(true);
		mFont = paint.getFontMetrics();
		glyphHeight = (float)Math.floor(Math.abs(mFont.top) + Math.abs(mFont.bottom) + 0.5f);
		glyphWidth = paint.measureText("W");	// ASCII文字の幅(ユニコード文字だとこの値よりも大きくなるので使えない)
		mForceCreate = forceCreate;
	}

	/**
	 * 指定した文字列配列を描画した場合のそれぞれの描画幅、最大幅と最大文字数を計算
	 * @param texts 文字列配列
	 * @return 最大幅
	 */
	private float calcTextWidths(final String[] texts) {
		final int nn = texts.length;
		glyphWidths = new float[nn];
		// 文字列の幅を計算&分割
		int m = 0;
		float w = 0f;
		for (int i = 0; i < nn; i++) {
			m = Math.max(m, texts[i].length());				// 最も長い文字列の長さ
			glyphWidths[i] = paint.measureText(texts[i]);	// 描画幅
			w = Math.max(w, glyphWidths[i]);				// 最も広い文字列描画幅
//			Log.w(TAG, "text=" + texts[i] + " width=" + glyphWidths[i]);
		}
//		Log.w(TAG, "maxWidth=" + w);
		glyphsPerRow = m;									// 1行あたりの最大文字数
		colWidth = w;										// 1列の幅
		return w;
	}
	
	/**
	 * 文字列配列を指定してテクスチャを生成
	 * @param game
	 * @param fileName
	 * @param texts
	 * @return テクスチャ
	 */
	public StaticTexture generate(final IModelView game, final String fileName, final String[] texts, final boolean mipmapped) {
		generateOnly(game, fileName, texts);
        return new StaticTexture(game, fileName, mipmapped);
	}
	
	private int roundUp2(float v) {
		int vv = 32;
		while (vv < v) vv <<= 1;		// 幅は2の乗数にする
//		Log.w(TAG, "roundUp2(" + v + ")=" + vv);
		return vv;
	}
	
	/**
	 * 文字列配列を指定してテクスチャ用の画像を生成
	 * @param game		GLGameインスタンス
	 * @param fileName	ファイル名
	 * @param texts		文字列配列
	 */
	public void generateOnly(final IModelView game, final String fileName, final String[] texts) {
		// 指定した文字列配列を描画した場合の各行の描画幅、最大幅と最大文字数を計算
		final float w = calcTextWidths(texts);				// w=最大幅		
		final int nn = texts.length;						// 行数
		
		int width = roundUp2(w);			// 幅：2の乗数に切り上げる
		final int hh = Math.round(glyphHeight * nn);
		int height = roundUp2(hh);			// 高さ：2の乗数に切り上げる
//		Log.w(TAG, "initialHeight=" + height + "(" + hh + ")");
		if (height > width) {
			if (height < 1024)
				width = height;
			else {
				int n;
				for (int c = 1; c < 10; c++) {		// 列数を1から9まで順に増やしていく
					n = Math.round((nn * 1f) / c);	// 1列あたりの行数
					height = roundUp2(Math.round(glyphHeight * n));
					width = roundUp2(w * c);
//					Log.w(TAG, "列=" + c + " width=" + width + " height=" + height);
					if (height <= width) {	// 2013/06/18 "=="から"<="に変更
						colCount = c;
						break;
					}
				}
			}
		}
		// 内部ファイルは見れないのでデバッグ時はSDカードへ保存する。WRITE_EXTERNAL_STORAGEパーミッションを追加すること。
		FileIO fileIO;
		if (isSaveExternal)
			fileIO = game.getExtFileIO();	// SDカードへ(Nexsusなら保存されるpathが違うだけで同じこと)
		else
			fileIO = game.getFileIO();		// 内部ファイルへ
		// 同名のファイルが存在しないとき
		if (mForceCreate || (!fileIO.fileExists(fileName))) {	
			System.gc();	// 2013/06/18		
			Bitmap bitmap = generateBitmap(texts, width, height);
			// 内部ファイルを生成する
			OutputStream out;
			try {
				out = fileIO.writeFile(fileName);
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
		        out.flush();
		        out.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			bitmap.recycle();
			bitmap = null;
			System.gc();	// 2013/05/27
		}
	}
	
	/**
	 * 文字列を指定してテクスチャを生成
	 * @param game		GLGameインスタンス
	 * @param name		ファイル名
	 * @param text		文字列
	 * @param perRow	1行あたりの文字数
	 * @return			テクスチャ
	 */
	public StaticTexture generate(final IModelView game, final String name, final String text, final int perRow, final boolean mipmapped) {
		generateOnly(game, name, text, perRow);
        return new StaticTexture(game, name, mipmapped);
	}
	
	/**
	 * 文字列を指定してテクスチャ用のビットマップを生成
	 * @param game		GLGameインスタンス
	 * @param fileName	ファイル名
	 * @param text		文字列
	 * @param perRow	1行あたりの文字数
	 */
	public void generateOnly(final IModelView game, final String fileName, final String text, final int perRow) {
		final int n = text.length();	// 文字数
		int nn = n / perRow;			// 行数=文字数÷1行あたりの文字数
		while (nn * perRow < n) nn++;
	
		// 文字列を分割
		final String[] texts = new String[nn];		
		for (int i = 0; i < nn; i++) {
			final int l = (i + 1) * perRow;	// 次の行の先頭の文字位置
			texts[i] = text.substring(i * perRow, l < n ? l : n);
		}
		// 指定した文字列配列を描画した場合の各行の描画幅、最大幅と最大文字数を計算
		final float w = calcTextWidths(texts);

		int width = 32;
		while (width < w) width <<= 1;		// 幅は2の乗数にする
		final int hh = Math.round(glyphHeight * nn);
		int height = 32;
		while (height < hh) height <<= 1;	// 高さは2の乗数にする
//		glyphsPerRow = perRow;

		// 内部ファイルは見れないのでデバッグ用にはSDカードへ保存する
		FileIO fileIO;
		if (isSaveExternal)					// WRITE_EXTERNAL_STORAGEパーミッションが必要
			fileIO = game.getExtFileIO();	// SDカードへ(Nexusなら保存されるpathが違うだけで同じこと)
		else
			fileIO = game.getFileIO();		// 内部ファイルへ
		// 同名のファイルが存在しないとき
		if (mForceCreate || (!fileIO.fileExists(fileName))) {	
			Bitmap bitmap = generateBitmap(texts, width, height);

			// 内部ファイルを生成する
			try {
				final OutputStream out = fileIO.writeFile(fileName);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		        out.flush();
		        out.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			bitmap.recycle();
			bitmap = null;
			System.gc();	// 2013/05/27
		}
	}
		
	/**
	 * テクスチャ用の文字列ビットマップ生成
	 * @param texts		文字列配列
	 * @param width		ビットマップの幅
	 * @param height	ビットマップの高さ
	 * @return			生成したビットマップ
	 */
	private Bitmap generateBitmap(final String[] texts, int width, int height) {
//		Log.w(TAG, "generateBitmap:width=" + width + " height=" + height);
		// ミップマップする時は2の乗数で正方形でないとダメ
		if (isSquare) {
			if (width != height) {
				// 大きい方に揃える
				final int w = Math.max(width, height);
				if (w != width)
					width = w;
				else
					height = w;
				Log.v(TAG, "The size is not square. Modified automatically. :width=" + width + " height=" + height);
			}
		}

		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);	// ARGB_8888
		final Canvas canvas = new Canvas(bitmap);
		
		// 文字列をキャンバスに描画
		paint.setARGB(0, 0, 0, 0);			// 透明色
		canvas.drawPaint(paint);			// 全面塗りつぶし(全面を透過させる)
		paint.setARGB(255, 255, 255, 255);	// 白色
		final int nn = texts.length;		// 行数
		int c = 0, r = 0;
		for (int i = 0; i < nn; i++) {
			if (texts[i] != null) {
				canvas.drawText(texts[i], colWidth * c, Math.abs(mFont.ascent) + r * glyphHeight, paint);
				c++;						// 列位置を加算
				if (c >= colCount) {		// 列位置が列数を超えたら
					c = 0;					// 列位置を0に
					r++;					// 行数を加算
				}
			}
		}
		return bitmap;
	}
	
	public static final String ascii = " !\"#$%&'()*+,-./"
		+ "0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
}
