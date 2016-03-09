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

import android.opengl.GLES20;
import android.util.Log;

import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.glutils.TextureOffscreen;

/** 色抽出フィルタ */
public class MediaEffectExtraction implements IEffect {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectExtraction";

	private FullFrameRect mDrawer;
	private TextureOffscreen mOutputOffscreen;
	private final float[] mLimit = new float[Texture2dProgram.KERNEL_SIZE];
	private boolean mEnabled = true;

	private static final String FRAGMENT_SHADER_BASE = Texture2dProgram.SHADER_VERSION +
		"%s" +
		"#define KERNEL_SIZE " + Texture2dProgram.KERNEL_SIZE + "\n" +
		"precision highp float;\n" +
		"varying       vec2 vTextureCoord;\n" +
		"uniform %s    sTexture;\n" +
		"uniform float uKernel[18];\n" +
		"uniform vec2  uTexOffset[KERNEL_SIZE];\n" +
		"uniform float uColorAdjust;\n" +
//		"vec3 rgb2hsv(vec3 rgb) {\n" +
//		"    float min = min(min(rgb.r, rgb.g), rgb.b);\n" +
//		"    float max = max(max(rgb.r, rgb.g), rgb.b);\n" +
//		"    float h = 0.0;\n" +
//		"    float s = 0.0;\n" +
//		"    if (min != max) {\n" +
//		"        s = max - min;\n" +
//		"        h = 60.0 / (max - min);\n" +
//		"        if (min == rgb.r) {\n" +
//		"            h = h * (rgb.b - rgb.g) + 180.0;\n" +
//		"        } else if (min == rgb.g) {\n" +
//		"            h = h * (rgb.r - rgb.b) + 300.0;\n" +
//		"        } else if (min == rgb.b) {\n" +
//		"            h = h * (rgb.g - rgb.r) + 60.0;\n" +
//		"        }\n" +
//		"    }\n" +
//		"    return clamp(vec3(mod(h, 360.0) / 360.0, s, max), 0.0, 1.0);\n" +	// hsv全成分とも[0.0-1.0]
//		"}\n" +
		"vec3 rgb2hsv(vec3 c) {\n" +
		"    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
		"    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
		"    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
		"    float d = q.x - min(q.w, q.y);\n" +
		"    float e = 1.0e-10;\n" +
		"    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
		"}\n" +
//		"vec3 hue(float hue) {\n" +
//		"    vec3 rgb = fract(hue + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0));\n" +
//		"    rgb = abs(rgb * 2.0 - 1.0);\n" +
//		"    return clamp(rgb * 3.0 - 1.0, 0.0, 1.0);\n" +
//		"}\n" +
//		"vec3 hsv2rgb(vec3 hsv) {\n" +
//		"    return ((hue(hsv.x) - 1.0) * hsv.y + 1.0) * hsv.z;\n" +
//		"}\n" +
		"vec3 hsv2rgb(vec3 c) {\n" +
		"    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
		"    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
		"    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
		"}\n" +
		"void main() {\n" +
		"    vec3 hsv = rgb2hsv(texture2D(sTexture, vTextureCoord).rgb);\n" +	// RGBをHSVに変換
		"    vec3 min = vec3(uKernel[0], uKernel[2], uKernel[4]);\n" +	// HSV下限
		"    vec3 max = vec3(uKernel[1], uKernel[3], uKernel[5]);\n" +	// HSV上限
		"    vec3 add = vec3(uKernel[6], uKernel[7], uKernel[8]);\n" +	// HSV加算
		"    float e = 1e-10;\n" +
		"    vec3 eps = vec3(e, e, e);\n" +
//		"    vec3 v = hsv * step(min - eps, hsv);\n" +	// 元の色がminより小さければ0
//		"    v = v * step(-max - eps, -hsv);\n" +		// 元の色がmaxより大きければ0
//		"    if ((v.x * v.y * v.z == 0.0) && (hsv.x * hsv.y * hsv.z != 0.0)) {\n" +
//		"    	hsv = vec3(0.0, 0.0, 0.0);\n" +
//		"    }\n" +
		"    vec3 v = hsv;\n" +
		"    if (hsv.r < min.r || hsv.r > max.r || hsv.g < min.g || hsv.g > max.g || hsv.b < min.b || hsv.b > max.b) {\n" +
		"        v = vec3(0.0);\n" +
		"    }\n" +
		"    hsv = v + add;\n" +
		"    if (uColorAdjust > 0.0) {\n" +
		"        hsv = step(vec3(uColorAdjust), hsv);\n" +	// 2値化
		"    }\n" +
		"    gl_FragColor = vec4(hsv2rgb(clamp(hsv, 0.0, 1.0)), 1.0);\n" +
		"}\n";
	private static final String FRAGMENT_SHADER
		= String.format(FRAGMENT_SHADER_BASE, Texture2dProgram.HEADER_2D, Texture2dProgram.SAMPLER_2D);
	private static final String FRAGMENT_SHADER_EXT
		= String.format(FRAGMENT_SHADER_BASE, Texture2dProgram.HEADER_OES, Texture2dProgram.SAMPLER_OES);

	public MediaEffectExtraction() {
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
		mDrawer = new FullFrameRect(new Texture2dProgram(GLES20.GL_TEXTURE_2D, FRAGMENT_SHADER));
		mLimit[0] = 0.0f;	mLimit[1] = 1.0f;	// H上下限
		mLimit[2] = 0.0f;	mLimit[3] = 1.0f;	// S上下限
		mLimit[4] = 0.0f; 	mLimit[5] = 1.0f;	// V上下限
		mLimit[6] = 0.0f; 	mLimit[7] = 0.0f;	mLimit[8] = 0.0f;	// 抽出後加算値HSV
		mDrawer.getProgram().setKernel(mLimit, 0.0f);	// デフォルトは2値化しないのでcolorAdjは0
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
		mDrawer.draw(src_tex_ids[0], mOutputOffscreen.getTexMatrix(), 0);
		mOutputOffscreen.unbind();
	}

	@Override
	public void apply(final ISource src) {
		if (!mEnabled) return;
		if (src instanceof MediaSource) {
			final TextureOffscreen output_tex = ((MediaSource)src).getOutputTexture();
			final int[] src_tex_ids = src.getSourceTexId();
			output_tex.bind();
			mDrawer.draw(src_tex_ids[0], output_tex.getTexMatrix(), 0);
			output_tex.unbind();
		} else {
			apply(src.getSourceTexId(), src.getWidth(), src.getHeight(), src.getOutputTexId());
		}
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		if (mDrawer != null) {
			mDrawer.release();
			mDrawer = null;
		}
		if (mOutputOffscreen != null) {
			mOutputOffscreen.release();
			mOutputOffscreen = null;
		}
	}

	/**
	 * 色抽出の上下限をHSVで設定
	 * @param lowerH [0.0, 1.0]
	 * @param upperH [0.0, 1.0]
	 * @param lowerS [0.0, 1.0]
	 * @param upperS [0.0, 1.0]
	 * @param lowerV [0.0, 1.0]
	 * @param upperV [0.0, 1.0]
	 * @param color_adjust 0より大きければ2値化時のしきい値, 0以下なら2値化なし
	 * @return
	 */
	public MediaEffectExtraction setParameter(
		final float lowerH, final float upperH,
		final float lowerS, final float upperS,
		final float lowerV, final float upperV,
		final float color_adjust) {
		return setParameter(lowerH, upperH, lowerS, upperS, lowerV, upperV, 0.0f, 0.0f, 0.0f, color_adjust);
	}

	public MediaEffectExtraction setParameter(
		final float lowerH, final float upperH,
		final float lowerS, final float upperS,
		final float lowerV, final float upperV,
		final float addH, final float addS, final float addV,
		final float color_adjust) {

		mLimit[0] = Math.min(lowerH, upperH);
		mLimit[1] = Math.max(lowerH, upperH);
		mLimit[2] = Math.min(lowerS, upperS);
		mLimit[3] = Math.max(lowerS, upperS);
		mLimit[4] = Math.min(lowerV, upperV);
		mLimit[5] = Math.max(lowerV, upperV);
		mLimit[6] = addH;
		mLimit[7] = addS;
		mLimit[8] = addV;
		mDrawer.getProgram().setKernel(mLimit, color_adjust);
		return this;
	}

	/**
	 * 色抽出の上下限をHSVで設定, 0,1がHの下限上限, 2,3がSの下限上限, 4,5がVの下限上限
	 * @param limit
	 * @param color_adjust 0より大きければ2値化時のしきい値, 0以下なら2値化なし
	 * @return
	 */
	public MediaEffectExtraction setParameter(final float[] limit, final float color_adjust) {
		if ((limit == null) || (limit.length < 6)) {
			throw new IllegalArgumentException("limit is null or short");
		}
		System.arraycopy(limit, 0, mLimit, 0, 6);
		mDrawer.getProgram().setKernel(mLimit, color_adjust);
		return this;
	}

	@Override
	public MediaEffectExtraction resize(final int width, final int height) {
		if ((mOutputOffscreen == null) || (width != mOutputOffscreen.getWidth())
			|| (height != mOutputOffscreen.getHeight())) {
			if (mOutputOffscreen != null)
				mOutputOffscreen.release();
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		mDrawer.getProgram().setTexSize(width, height);
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
}