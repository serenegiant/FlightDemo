package com.serenegiant.mediaeffect;

/** Dilation(膨張)フィルタ */
public class MediaEffectDilation extends MediaEffectGLESBase {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectDilation";

	public static final String VERTEX_SHADER_1 =
		"uniform mat4 uMVPMatrix;\n" +		// モデルビュー変換行列
		"uniform mat4 uTexMatrix;\n" +		// テクスチャ変換行列
		"attribute vec4 aPosition;\n" +		// 頂点座標
		"attribute vec4 aTextureCoord;\n" +	// テクスチャ情報
		"\n" +
		"uniform float texelWidthOffset; \n" +
		"uniform float texelHeightOffset; \n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"gl_Position = uMVPMatrix * aPosition;\n" +
			"vec2 tex = (uTexMatrix * aTextureCoord).xy;\n" +
			"\n" +
			"vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n" +
			"\n" +
			"centerTextureCoordinate = tex;\n" +
			"oneStepNegativeTextureCoordinate = tex - offset;\n" +
			"oneStepPositiveTextureCoordinate = tex + offset;\n" +
		"}\n";

	public static final String VERTEX_SHADER_2 =
		"uniform mat4 uMVPMatrix;\n" +		// モデルビュー変換行列
		"uniform mat4 uTexMatrix;\n" +		// テクスチャ変換行列
		"attribute vec4 aPosition;\n" +		// 頂点座標
		"attribute vec4 aTextureCoord;\n" +	// テクスチャ情報
		"\n" +
		"uniform float texelWidthOffset;\n" +
		"uniform float texelHeightOffset;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"gl_Position = uMVPMatrix * aPosition;\n" +
			"vec2 tex = (uTexMatrix * aTextureCoord).xy;\n" +
			"\n" +
			"vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n" +
			"\n" +
			"centerTextureCoordinate = tex;\n" +
			"oneStepNegativeTextureCoordinate = tex - offset;\n" +
			"oneStepPositiveTextureCoordinate = tex + offset;\n" +
			"twoStepsNegativeTextureCoordinate = tex - (offset * 2.0);\n" +
			"twoStepsPositiveTextureCoordinate = tex + (offset * 2.0);\n" +
		"}\n";

	public static final String VERTEX_SHADER_3 =
		"uniform mat4 uMVPMatrix;\n" +		// モデルビュー変換行列
		"uniform mat4 uTexMatrix;\n" +		// テクスチャ変換行列
		"attribute vec4 aPosition;\n" +		// 頂点座標
		"attribute vec4 aTextureCoord;\n" +	// テクスチャ情報
		"\n" +
		"uniform float texelWidthOffset;\n" +
		"uniform float texelHeightOffset;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"varying vec2 threeStepsPositiveTextureCoordinate;\n" +
		"varying vec2 threeStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"gl_Position = uMVPMatrix * aPosition;\n" +
			"vec2 tex = (uTexMatrix * aTextureCoord).xy;\n" +
			"\n" +
			"vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n" +
			"\n" +
			"centerTextureCoordinate = tex;\n" +
			"oneStepNegativeTextureCoordinate = tex - offset;\n" +
			"oneStepPositiveTextureCoordinate = tex + offset;\n" +
			"twoStepsNegativeTextureCoordinate = tex - (offset * 2.0);\n" +
			"twoStepsPositiveTextureCoordinate = tex + (offset * 2.0);\n" +
			"threeStepsNegativeTextureCoordinate = tex - (offset * 3.0);\n" +
			"threeStepsPositiveTextureCoordinate = tex + (offset * 3.0);\n" +
		"}\n";

	public static final String VERTEX_SHADER_4 =
		"uniform mat4 uMVPMatrix;\n" +		// モデルビュー変換行列
		"uniform mat4 uTexMatrix;\n" +		// テクスチャ変換行列
		"attribute vec4 aPosition;\n" +		// 頂点座標
		"attribute vec4 aTextureCoord;\n" +	// テクスチャ情報
		"\n" +
		"uniform float texelWidthOffset;\n" +
		"uniform float texelHeightOffset;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"varying vec2 threeStepsPositiveTextureCoordinate;\n" +
		"varying vec2 threeStepsNegativeTextureCoordinate;\n" +
		"varying vec2 fourStepsPositiveTextureCoordinate;\n" +
		"varying vec2 fourStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"gl_Position = uMVPMatrix * aPosition;\n" +
			"vec2 tex = (uTexMatrix * aTextureCoord).xy;\n" +
			"\n" +
			"vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n" +
			"\n" +
			"centerTextureCoordinate = tex;\n" +
			"oneStepNegativeTextureCoordinate = tex - offset;\n" +
			"oneStepPositiveTextureCoordinate = tex + offset;\n" +
			"twoStepsNegativeTextureCoordinate = tex - (offset * 2.0);\n" +
			"twoStepsPositiveTextureCoordinate = tex + (offset * 2.0);\n" +
			"threeStepsNegativeTextureCoordinate = tex - (offset * 3.0);\n" +
			"threeStepsPositiveTextureCoordinate = tex + (offset * 3.0);\n" +
			"fourStepsNegativeTextureCoordinate = tex - (offset * 4.0);\n" +
			"fourStepsPositiveTextureCoordinate = tex + (offset * 4.0);\n" +
		"}\n";


	public static final String FRAGMENT_SHADER_1 =
		"precision lowp float;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"\n" +
		"uniform sampler2D sTexture;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"float centerIntensity = texture2D(sTexture, centerTextureCoordinate).r;\n" +
			"float oneStepPositiveIntensity = texture2D(sTexture, oneStepPositiveTextureCoordinate).r;\n" +
			"float oneStepNegativeIntensity = texture2D(sTexture, oneStepNegativeTextureCoordinate).r;\n" +
			"\n" +
			"lowp float maxValue = max(centerIntensity, oneStepPositiveIntensity);\n" +
			"maxValue = max(maxValue, oneStepNegativeIntensity);\n" +
			"\n" +
			"gl_FragColor = vec4(vec3(maxValue), 1.0);\n" +
		"}\n";

	public static final String FRAGMENT_SHADER_2 =
		"precision lowp float;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"uniform sampler2D sTexture;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"float centerIntensity = texture2D(sTexture, centerTextureCoordinate).r;\n" +
			"float oneStepPositiveIntensity = texture2D(sTexture, oneStepPositiveTextureCoordinate).r;\n" +
			"float oneStepNegativeIntensity = texture2D(sTexture, oneStepNegativeTextureCoordinate).r;\n" +
			"float twoStepsPositiveIntensity = texture2D(sTexture, twoStepsPositiveTextureCoordinate).r;\n" +
			"float twoStepsNegativeIntensity = texture2D(sTexture, twoStepsNegativeTextureCoordinate).r;\n" +
			"\n" +
			"lowp float maxValue = max(centerIntensity, oneStepPositiveIntensity);\n" +
			"maxValue = max(maxValue, oneStepNegativeIntensity);\n" +
			"maxValue = max(maxValue, twoStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, twoStepsNegativeIntensity);\n" +
			"\n" +
			"gl_FragColor = vec4(vec3(maxValue), 1.0);\n" +
		"}\n";

	public static final String FRAGMENT_SHADER_3 =
		"precision lowp float;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"varying vec2 threeStepsPositiveTextureCoordinate;\n" +
		"varying vec2 threeStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"uniform sampler2D sTexture;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"float centerIntensity = texture2D(sTexture, centerTextureCoordinate).r;\n" +
			"float oneStepPositiveIntensity = texture2D(sTexture, oneStepPositiveTextureCoordinate).r;\n" +
			"float oneStepNegativeIntensity = texture2D(sTexture, oneStepNegativeTextureCoordinate).r;\n" +
			"float twoStepsPositiveIntensity = texture2D(sTexture, twoStepsPositiveTextureCoordinate).r;\n" +
			"float twoStepsNegativeIntensity = texture2D(sTexture, twoStepsNegativeTextureCoordinate).r;\n" +
			"float threeStepsPositiveIntensity = texture2D(sTexture, threeStepsPositiveTextureCoordinate).r;\n" +
			"float threeStepsNegativeIntensity = texture2D(sTexture, threeStepsNegativeTextureCoordinate).r;\n" +
			"\n" +
			"lowp float maxValue = max(centerIntensity, oneStepPositiveIntensity);\n" +
			"maxValue = max(maxValue, oneStepNegativeIntensity);\n" +
			"maxValue = max(maxValue, twoStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, twoStepsNegativeIntensity);\n" +
			"maxValue = max(maxValue, threeStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, threeStepsNegativeIntensity);\n" +
			"\n" +
			"gl_FragColor = vec4(vec3(maxValue), 1.0);\n" +
		"}\n";

	public static final String FRAGMENT_SHADER_4 =
		"precision lowp float;\n" +
		"\n" +
		"varying vec2 centerTextureCoordinate;\n" +
		"varying vec2 oneStepPositiveTextureCoordinate;\n" +
		"varying vec2 oneStepNegativeTextureCoordinate;\n" +
		"varying vec2 twoStepsPositiveTextureCoordinate;\n" +
		"varying vec2 twoStepsNegativeTextureCoordinate;\n" +
		"varying vec2 threeStepsPositiveTextureCoordinate;\n" +
		"varying vec2 threeStepsNegativeTextureCoordinate;\n" +
		"varying vec2 fourStepsPositiveTextureCoordinate;\n" +
		"varying vec2 fourStepsNegativeTextureCoordinate;\n" +
		"\n" +
		"uniform sampler2D sTexture;\n" +
		"\n" +
		"void main()\n" +
		"{\n" +
			"float centerIntensity = texture2D(sTexture, centerTextureCoordinate).r;\n" +
			"float oneStepPositiveIntensity = texture2D(sTexture, oneStepPositiveTextureCoordinate).r;\n" +
			"float oneStepNegativeIntensity = texture2D(sTexture, oneStepNegativeTextureCoordinate).r;\n" +
			"float twoStepsPositiveIntensity = texture2D(sTexture, twoStepsPositiveTextureCoordinate).r;\n" +
			"float twoStepsNegativeIntensity = texture2D(sTexture, twoStepsNegativeTextureCoordinate).r;\n" +
			"float threeStepsPositiveIntensity = texture2D(sTexture, threeStepsPositiveTextureCoordinate).r;\n" +
			"float threeStepsNegativeIntensity = texture2D(sTexture, threeStepsNegativeTextureCoordinate).r;\n" +
			"float fourStepsPositiveIntensity = texture2D(sTexture, fourStepsPositiveTextureCoordinate).r;\n" +
			"float fourStepsNegativeIntensity = texture2D(sTexture, fourStepsNegativeTextureCoordinate).r;\n" +
			"\n" +
			"lowp float maxValue = max(centerIntensity, oneStepPositiveIntensity);\n" +
			"maxValue = max(maxValue, oneStepNegativeIntensity);\n" +
			"maxValue = max(maxValue, twoStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, twoStepsNegativeIntensity);\n" +
			"maxValue = max(maxValue, threeStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, threeStepsNegativeIntensity);\n" +
			"maxValue = max(maxValue, fourStepsPositiveIntensity);\n" +
			"maxValue = max(maxValue, fourStepsNegativeIntensity);\n" +
			"\n" +
			"gl_FragColor = vec4(vec3(maxValue), 1.0);\n" +
		"}\n";

	private static String getVertexShader(final int radius) {
		switch (radius) {
		case 0:
		case 1:
			return VERTEX_SHADER_1;
		case 2:
			return VERTEX_SHADER_2;
		case 3:
			return VERTEX_SHADER_3;
		default:
			return VERTEX_SHADER_4;
		}
	}

	private static String getFragmentShader(final int radius) {
		switch (radius) {
		case 0:
		case 1:
			return FRAGMENT_SHADER_1;
		case 2:
			return FRAGMENT_SHADER_2;
		case 3:
			return FRAGMENT_SHADER_3;
		default:
			return FRAGMENT_SHADER_4;
		}
	}

	public MediaEffectDilation() {
		this(1);
	}

	/**
	 * 膨張範囲を指定して生成
	 * @param radius 1, 2, 3, 4
	 */
	public MediaEffectDilation(final int radius) {
		super(false, getVertexShader(radius), getFragmentShader(radius));
	}

}
