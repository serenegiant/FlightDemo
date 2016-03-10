package com.serenegiant.mediaeffect;
/*
 * Copyright (c) 2015 saki t_saki@serenegiant.com
 *
 * File name: MediaEffectKernel.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import com.serenegiant.glutils.Texture2dProgram;

public class MediaEffectKernel extends MediaEffectGLESBase {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectKernel";

	public MediaEffectKernel() {
		super(Texture2dProgram.ProgramType.TEXTURE_FILT3x3);
	}

	public MediaEffectKernel(final float[] kernel) {
		this();
		setParameter(kernel, 0.0f);
	}

	public MediaEffectKernel(final float[] kernel, final float color_adjust) {
		this();
		setParameter(kernel, color_adjust);
	}

	public MediaEffectKernel setParameter(final float[] kernel, final float color_adjust) {
		if ((kernel == null) || (kernel.length < 9))
			throw new IllegalArgumentException("kernel should be 3x3");
		mDrawer.getProgram().setKernel(kernel, color_adjust);
		return this;
	}
}
