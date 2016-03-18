package com.serenegiant.mediaeffect;

import android.util.Log;

import com.serenegiant.glutils.TextureOffscreen;

/**
 * OpenGL|ES2のフラグメントシェーダーで映像効果を与える時の基本クラス
 */
public class MediaEffectGLESBase implements IEffect {
	private static final boolean DEBUG = false;
	private static final String TAG = "MediaEffectGLESBase";

	protected TextureOffscreen mOutputOffscreen;
	protected boolean mEnabled = true;
	/** 描画の排他制御用 */
	protected final Object mSync = new Object();

//	private final int mTexTarget;
//	private final FloatBuffer pVertex;
//	private final FloatBuffer pTexCoord;
//	private final int maPositionLoc;
//	private final int maTextureCoordLoc;
//	private final int muMVPMatrixLoc;
//	private final int muTexMatrixLoc;
//	private final float[] mMvpMatrix = new float[16];
//	private int hProgram;
	protected final MediaEffectDrawer mDrawer;

	/**
	 * フラグメントシェーダーを指定する場合のコンストラクタ(頂点シェーダーはデフォルトを使用)
	 * @param shader
	 */
	public MediaEffectGLESBase(final String shader) {
		this(new MediaEffectDrawer(false, MediaEffectDrawer.VERTEX_SHADER, shader));
	}

	/**
	 * フラグメントシェーダーを指定する場合のコンストラクタ(頂点シェーダーはデフォルトを使用)
	 * @param shader
	 */
	public MediaEffectGLESBase(final boolean isOES, final String shader) {
		this(new MediaEffectDrawer(isOES, MediaEffectDrawer.VERTEX_SHADER, shader));
	}

	/**
	 * 頂点シェーダーとフラグメントシェーダーを指定する場合のコンストラクタ
	 * @param vss
	 * @param fss
	 */
	public MediaEffectGLESBase(final boolean isOES, final String vss, final String fss) {
		this(new MediaEffectDrawer(isOES, vss, fss));
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
//		mTexTarget = isOES ? GL_TEXTURE_EXTERNAL_OES : GL_TEXTURE_2D;
//		final FloatBuffer pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();
//		pVertex.put(VERTICES);
//		pVertex.flip();
//		final FloatBuffer pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();
//		pTexCoord.put(TEXCOORD);
//		pTexCoord.flip();
//
//		hProgram = GLHelper.loadShader(vss, fss);
//		GLES20.glUseProgram(hProgram);
//		final int maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
//		final int maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
//      muMVPMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uMVPMatrix");
//		muTexMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uTexMatrix");
//        // モデルビュー変換行列を初期化
//		Matrix.setIdentityM(mMvpMatrix, 0);
//		//
//		if (muMVPMatrixLoc >= 0) {
//			GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
//		}
//		if (muTexMatrixLoc >= 0) {
//			// ここは単位行列に初期化するだけなのでmMvpMatrixを流用
//			GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
//		}
//		// 頂点座標配列を割り当てる
//		GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
//		GLES20.glEnableVertexAttribArray(maPositionLoc);
//		// テクスチャ座標配列を割り当てる
//		GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
//		GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
	}

	public MediaEffectGLESBase(final MediaEffectDrawer drawer) {
		mDrawer = drawer;
		resize(256, 256);
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		mDrawer.release();
//		GLES20.glUseProgram(0);
//		if (hProgram >= 0) {
//			GLES20.glDeleteProgram(hProgram);
//		}
//		hProgram = -1;
		if (mOutputOffscreen != null) {
			mOutputOffscreen.release();
			mOutputOffscreen = null;
		}
	}

	/**
	 * モデルビュー変換行列を取得(内部配列を直接返すので変更時は要注意)
	 * @return
	 */
	public float[] getMvpMatrix() {
		return mDrawer.getMvpMatrix();
	}

	/**
	 * モデルビュー変換行列に行列を割り当てる
	 * @param matrix 領域チェックしていないのでoffsetから16個以上必須
	 * @param offset
	 * @return
	 */
	public MediaEffectGLESBase setMvpMatrix(final float[] matrix, final int offset) {
		mDrawer.setMvpMatrix(matrix, offset);
//		System.arraycopy(matrix, offset, mMvpMatrix, 0, mMvpMatrix.length);
		return this;
	}

	/**
	 * モデルビュー変換行列のコピーを取得
	 * @param matrix 領域チェックしていないのでoffsetから16個以上必須
	 * @param offset
	 */
	public void getMvpMatrix(final float[] matrix, final int offset) {
		mDrawer.getMvpMatrix(matrix, offset);
//		System.arraycopy(mMvpMatrix, 0, matrix, offset, mMvpMatrix.length);
	}

	@Override
	public MediaEffectGLESBase resize(final int width, final int height) {
		if ((mOutputOffscreen == null) || (width != mOutputOffscreen.getWidth())
			|| (height != mOutputOffscreen.getHeight())) {
			if (mOutputOffscreen != null)
				mOutputOffscreen.release();
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		return this;
	}

	@Override
	public boolean enabled() {
		return mEnabled;
	}

	@Override
	public IEffect setEnable(final boolean enable) {
		mEnabled = enable;
		return this;
	}

	/**
	 * If you know the source texture came from MediaSource,
	 * using #apply(MediaSource) is much efficient instead of this
	 * @param src_tex_ids
	 * @param width
	 * @param height
	 * @param out_tex_id
	 */
	@Override
	public void apply(final int [] src_tex_ids, final int width, final int height, final int out_tex_id) {
		if (!mEnabled) return;
		if (mOutputOffscreen == null) {
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		if ((out_tex_id != mOutputOffscreen.getTexture())
			|| (width != mOutputOffscreen.getWidth())
			|| (height != mOutputOffscreen.getHeight())) {
			mOutputOffscreen.assignTexture(out_tex_id, width, height);
		}
		mOutputOffscreen.bind();
		synchronized (mSync) {
			internal_apply(mDrawer, src_tex_ids[0], mOutputOffscreen.getTexMatrix(), 0);
		}
		mOutputOffscreen.unbind();
	}

	@Override
	public void apply(final ISource src) {
		if (!mEnabled) return;
		if (src instanceof MediaSource) {
			final TextureOffscreen output_tex = ((MediaSource)src).getOutputTexture();
			final int[] src_tex_ids = src.getSourceTexId();
			output_tex.bind();
			synchronized (mSync) {
				internal_apply(mDrawer, src_tex_ids[0], output_tex.getTexMatrix(), 0);
			}
			output_tex.unbind();
		} else {
			apply(src.getSourceTexId(), src.getWidth(), src.getHeight(), src.getOutputTexId());
		}
	}

	protected int getProgram() {
		return mDrawer.getProgram();
	}

	/**
	 * preDraw => draw => postDrawを順に呼び出す
	 * mSyncはロックされて呼び出される
	 * @param tex_id texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void internal_apply(final MediaEffectDrawer drawer, final int tex_id, final float[] tex_matrix, final int offset) {
		preDraw(drawer, tex_id, tex_matrix, offset);
		draw(drawer, tex_id, tex_matrix, offset);
		postDraw(drawer);
	}

	/**
	 * 描画の前処理
	 * プログラムを使用可能にしてテクスチャ変換行列/モデルビュー変換行列を代入, テクスチャをbindする
	 * mSyncはロックされて呼び出される
	 * @param tex_id texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void preDraw(final MediaEffectDrawer drawer, final int tex_id, final float[] tex_matrix, final int offset) {
		drawer.preDraw(tex_id, tex_matrix, offset);
/*		GLES20.glUseProgram(hProgram);
		if ((muTexMatrixLoc >= 0) && (tex_matrix != null)) {
			GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, offset);
		}
		if (muMVPMatrixLoc >= 0) {
			GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
		}
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(mTexTarget, tex_id); */
	}

	/**
	 * 実際の描画実行, GLES20.glDrawArraysを呼び出すだけ
	 * mSyncはロックされて呼び出される
	 * @param tex_id texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void draw(final MediaEffectDrawer drawer, final int tex_id, final float[] tex_matrix, final int offset) {
		// これが実際の描画
		drawer.draw(tex_id, tex_matrix, offset);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
	}

	/**
	 * 描画後の後処理, テクスチャのunbind, プログラムをデフォルトに戻す
	 * mSyncはロックされて呼び出される
	 */
	protected void postDraw(final MediaEffectDrawer drawer) {
		drawer.postDraw();
//		GLES20.glBindTexture(mTexTarget, 0);
//		GLES20.glUseProgram(0);
	}
}
