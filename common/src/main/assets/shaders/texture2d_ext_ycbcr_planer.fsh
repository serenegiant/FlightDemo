#version 100
#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform vec2 uTextureSz;
uniform vec2 uFrameSz;
uniform float uBrightness;

/*const mat3 MAT_YUV2RGB = mat3 (
//	r			g			b			// こっちの方が少し色調が暗い
	1.0,		1.0,		1.0,		// y
	0.0,		-0.337633,	1.732446,	// u
	1.370705,	-0.698001,	0.0			// v
); */
const mat3 MAT_YUV2RGB = mat3 (
//	r			g			b
	1.0,		1.0,		1.0,		// y
	0.0,		-0.39173,	2.017,		// u
	1.5958,		-0.81290,	0.0			// v
);

const vec3 YUV_OFFSET = vec3(0.0625, 0.5, 0.5);

#define EPS 0.00001

// YCbCr semi planerの3プレーンのデータをGL_LUMINANCEの1つのテクスチャとしてまとめて渡して換算する場合(元の1ピクセル分のデータがテクセル1個に対応)
void main() {
	vec2 tex = vTextureCoord / 2.0;
	vec2 uv_sz = (uFrameSz / uTextureSz) / 2.0;
	float y, u, v;
	y = texture2D(sTexture, vec2(vTextureCoord.x, tex.y)).r;
	float u1 = texture2D(sTexture, vec2(tex.x,            tex.y / 2.0 + uv_sz.y)).r;
	float u2 = texture2D(sTexture, vec2(tex.x + uv_sz.x,  tex.y / 2.0 + uv_sz.y)).r;
	u = (u1 + u2) / 2.0;
	float v1 = texture2D(sTexture, vec2(tex.x,           (tex.y + uv_sz.y * 3.0) / 2.0)).r;
	float v2 = texture2D(sTexture, vec2(tex.x + uv_sz.x, (tex.y + uv_sz.y * 3.0) / 2.0)).r;
	v = (v1 + v2) / 2.0;
	// 輝度がEPS未満なら黒を返す(プレビューを停止した時に画面が緑になるのを防ぐため)
	if (y > EPS) {
		vec3 yuv = vec3(clamp(y + uBrightness, 0.0, 1.0), u, v) - YUV_OFFSET;
		vec3 rgb = clamp((MAT_YUV2RGB * yuv), 0.0, 1.0);
		gl_FragColor = vec4(rgb, 1.0);
	} else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
}
