package com.serenegiant.mediaeffect;

public class MediaEffectEmboss extends MediaEffectKernel {

	private float mIntensity;
	public MediaEffectEmboss() {
		this(1.0f);
	}

	public MediaEffectEmboss(final float intensity) {
		super(new float[] {
				intensity * (-2.0f), -intensity, 0.0f,
				-intensity, 1.0f, intensity,
				0.0f, intensity, intensity * 2.0f,
			});
		mIntensity = intensity;
	}

	public MediaEffectEmboss setParameter(final float intensity) {
		if (mIntensity != intensity) {
			mIntensity = intensity;
			setParameter(new float[] {
				intensity * (-2.0f), -intensity, 0.0f,
				-intensity, 1.0f, intensity,
				0.0f, intensity, intensity * 2.0f,
			}, 0.0f);
		}
		return this;
	}
}
