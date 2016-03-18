package com.serenegiant.mediaeffect;

public class MediaEffectKernel extends MediaEffectGLESBase {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectKernel";

//	public static final int KERNEL_SIZE = 9;
//	public static final float[] KERNEL_NULL = { 0f, 0f, 0f,  0f, 1f, 0f,  0f, 0f, 0f};
//	public static final float[] KERNEL_SOBEL_H = { 1f, 0f, -1f, 2f, 0f, -2f, 1f, 0f, -1f, };	// ソーベル(1次微分)
//	public static final float[] KERNEL_SOBEL_V = { 1f, 2f, 1f, 0f, 0f, 0f, -1f, -2f, -1f, };
//	public static final float[] KERNEL_SOBEL2_H = { 3f, 0f, -3f, 10f, 0f, -10f, 3f, 0f, -3f, };
//	public static final float[] KERNEL_SOBEL2_V = { 3f, 10f, 3f, 0f, 0f, 0f, -3f, -10f, -3f, };
//	public static final float[] KERNEL_SHARPNESS = { 0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f,};	// シャープネス
//	public static final float[] KERNEL_EDGE_DETECT = { -1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f, }; // エッジ検出
//	public static final float[] KERNEL_EMBOSS = { 2f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f };	// エンボス, オフセット0.5f
//	public static final float[] KERNEL_SMOOTH = { 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, };	// 移動平均
//	public static final float[] KERNEL_GAUSSIAN = { 1/16f, 2/16f, 1/16f, 2/16f, 4/16f, 2/16f, 1/16f, 2/16f, 1/16f, };	// ガウシアン(ノイズ除去/)
//	public static final float[] KERNEL_BRIGHTEN = { 1f, 1f, 1f, 1f, 2f, 1f, 1f, 1f, 1f, };
//	public static final float[] KERNEL_LAPLACIAN = { 1f, 1f, 1f, 1f, -8f, 1f, 1f, 1f, 1f, };	// ラプラシアン(2次微分)
//
//	private final int muKernelLoc;		// カーネル行列(float配列)
//	private final int muTexOffsetLoc;	// テクスチャオフセット(カーネル行列用)
//	private int muColorAdjustLoc;		// 色調整
//	private final float[] mKernel = new float[KERNEL_SIZE * 2];	// Inputs for convolution filter based shaders
//	private final float[] mTexOffset = new float[KERNEL_SIZE * 2];
//	private float mColorAdjust;
//	private float mTexWidth;
//	private float mTexHeight;

	public MediaEffectKernel() {
		super(new MediaEffectKernelDrawer(false, MediaEffectDrawer.VERTEX_SHADER, MediaEffectDrawer.FRAGMENT_SHADER_2D));
//		muKernelLoc = GLES20.glGetUniformLocation(getProgram(), "uKernel");
//		if (muKernelLoc < 0) {
//			// no kernel in this one
//			muTexOffsetLoc = -1;
//		} else {
//			// has kernel, must also have tex offset and color adj
//			muTexOffsetLoc = GLES20.glGetUniformLocation(getProgram(), "uTexOffset");
//			GLHelper.checkLocation(muTexOffsetLoc, "uTexOffset");	// 未使用だと削除されてしまうのでチェックしない
//
//			setKernel(KERNEL_NULL, 0f);
//			setTexSize(256, 256);
//
//			muColorAdjustLoc = GLES20.glGetUniformLocation(getProgram(), "uColorAdjust");
//			if (muColorAdjustLoc < 0) {
//				muColorAdjustLoc = -1;
//			}
//			GLHelper.checkLocation(muColorAdjustLoc, "uColorAdjust");	// 未使用だと削除されてしまうのでチェックしない
//		}
	}

	public MediaEffectKernel(final float[] kernel) {
		this();
		setParameter(kernel, 0.0f);
	}

	public MediaEffectKernel(final float[] kernel, final float color_adjust) {
		this();
		setParameter(kernel, color_adjust);
	}

	@Override
	public MediaEffectGLESBase resize(final int width, final int height) {
		super.resize(width, height);
		setTexSize(width, height);
		return this;
	}

//	@Override
//	protected void preDraw(final MediaEffectDrawer drawer, final int tex_id, final float[] tex_matrix, final int offset) {
//		super.preDraw(drawer, tex_id, tex_matrix, offset);
//		// カーネル関数(行列)
//		if (muKernelLoc >= 0) {
//			GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0);
//			GLHelper.checkGlError("set kernel");
//		}
//		// テクセルオフセット
//		if ((muTexOffsetLoc >= 0) && (mTexOffset != null)) {
//			GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0);
//		}
//		// 色調整オフセット
//		if (muColorAdjustLoc >= 0) {
//			GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust);
//		}
//	}

	public void setKernel(final float[] values, final float colorAdj) {
//		if ((values == null) || (values.length < KERNEL_SIZE)) {
//			throw new IllegalArgumentException("Kernel size is " + (values != null ? values.length : 0) + " vs. " + KERNEL_SIZE);
//		}
//		synchronized (mSync) {
//			System.arraycopy(values, 0, mKernel, 0, KERNEL_SIZE);
//			mColorAdjust = colorAdj;
//		}
		synchronized (mSync) {
			((MediaEffectKernelDrawer)mDrawer).setKernel(values, colorAdj);
		}
	}

	public void setColorAdjust(final float adjust) {
//		synchronized (mSync) {
//			mColorAdjust = adjust;
//		}
		synchronized (mSync) {
			((MediaEffectKernelDrawer)mDrawer).setColorAdjust(adjust);
		}
	}

	/**
	 * Sets the size of the texture.  This is used to find adjacent texels when filtering.
	 */
	public void setTexSize(final int width, final int height) {
//		mTexHeight = height;
//		mTexWidth = width;
//		final float rw = 1.0f / width;
//		final float rh = 1.0f / height;
//
//		// Don't need to create a new array here, but it's syntactically convenient.
//		synchronized (mSync) {
//			mTexOffset[0] = -rw;	mTexOffset[1] = -rh;
//			mTexOffset[2] = 0f;		mTexOffset[3] = -rh;
//			mTexOffset[4] = rw;		mTexOffset[5] = -rh;
//
//			mTexOffset[6] = -rw;	mTexOffset[7] = 0f;
//			mTexOffset[8] = 0f;		mTexOffset[9] = 0f;
//			mTexOffset[10] = rw;	mTexOffset[11] = 0f;
//
//			mTexOffset[12] = -rw;	mTexOffset[13] = rh;
//			mTexOffset[14] = 0f;	mTexOffset[15] = rh;
//			mTexOffset[16] = rw;	mTexOffset[17] = rh;
//
///*			mTexOffset = new float[] {
//				-rw, -rh,   0f, -rh,    rw, -rh,
//				-rw, 0f,    0f, 0f,     rw, 0f,
//				-rw, rh,    0f, rh,     rw, rh
//			}; */
//		}
		synchronized (mSync) {
			((MediaEffectKernelDrawer)mDrawer).setTexSize(width, height);
		}
	}

	/**
	 * synonym of setKernel
	 * @param kernel
	 * @param color_adjust
	 * @return
	 */
	public MediaEffectKernel setParameter(final float[] kernel, final float color_adjust) {
		setKernel(kernel, color_adjust);
		return this;
	}
}
