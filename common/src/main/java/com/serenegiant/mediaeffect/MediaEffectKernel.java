package com.serenegiant.mediaeffect;

public class MediaEffectKernel extends MediaEffectGLESBase {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectKernel";

	public MediaEffectKernel() {
		super(new MediaEffectKernelDrawer(false, MediaEffectDrawer.VERTEX_SHADER, MediaEffectDrawer.FRAGMENT_SHADER_2D));
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

	public void setKernel(final float[] values, final float colorAdj) {
		((MediaEffectKernelDrawer)mDrawer).setKernel(values, colorAdj);
	}

	public void setColorAdjust(final float adjust) {
		((MediaEffectKernelDrawer)mDrawer).setColorAdjust(adjust);
	}

	/**
	 * Sets the size of the texture.  This is used to find adjacent texels when filtering.
	 */
	public void setTexSize(final int width, final int height) {
		((MediaEffectKernelDrawer)mDrawer).setTexSize(width, height);
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
