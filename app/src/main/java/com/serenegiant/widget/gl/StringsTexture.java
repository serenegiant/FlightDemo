package com.serenegiant.widget.gl;

public class StringsTexture extends Texture {
	protected final float mGlyphWidth;		// 半角1文字の幅
	protected final float mGlyphHeight;		// 1文字(1行)の高さ
	protected final float[] mGlyphWidths;	// 各行の描画幅
	protected final float mColWidth;		// 列幅
	protected final int mColCount;			// 列数
	protected final int mTextCount;			// 文字列数

	public StringsTexture(final IModelView glGame, final String fileName, final String[] strings, final int textSize) {
		this(glGame, fileName, strings, textSize, true, false);
	}
	
	public StringsTexture(final IModelView glGame, final String fileName, final String[] strings, final int textSize, final boolean mipmapped) {
		this(glGame, fileName, strings, textSize, mipmapped, false);
	}
	
	public StringsTexture(final IModelView glGame, final String fileName, final String[] strings, final int textSize, final boolean mipmapped, final boolean forceCreate) {
		super(glGame, null, mipmapped);
		final StringTextureBuilder builder = new StringTextureBuilder(textSize, forceCreate);
//		builder.isSaveExternal = true;
		builder.generateOnly(glGame, fileName, strings);
		mTextCount = strings.length;
		mGlyphWidth = builder.glyphWidth;
		mGlyphHeight = builder.glyphHeight;
		mGlyphWidths = new float[mTextCount];
		mColCount = builder.colCount;
		mColWidth = builder.colWidth;
		System.arraycopy(builder.glyphWidths, 0, mGlyphWidths, 0, mTextCount);
		load(fileName);
	}
	
	/**
	 * 指定した行のTextureRegionを返す
	 * @param index
	 * @return TextureRegion(indexが0未満またはmTextCount以上ならnull)
	 */
	public TextureRegion getTextureRegion(final int index) {
		if ((index < 0) || (index >= mTextCount)) return null;
		final int r = (int)(index / mColCount);	// 行番号
		final int c = index - r * mColCount;	// 列番号
		return new TextureRegion(this, c * mColWidth, r * mGlyphHeight, mGlyphWidths[index], mGlyphHeight);
	}
	
}
