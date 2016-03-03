#version 100

precision highp float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
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

#define EPS 0.00001

// yuyvのデータをrgb565のテクスチャとして渡して換算する場合
vec2 getYuv(vec2 tex_coord) {
	// rgb565として変換させてしまった値を元のyuyvに戻す
	vec4 chroma = texture2D(sTexture, clamp(tex_coord / uTextureSz, 0.0, 1.0));
	if ((chroma.r < EPS) && (chroma.g < EPS) && (chroma.b < EPS))
		return vec2(0.0, 0.0);
	float fyuv = floor(chroma.r * 250.0) / 8.0 * 2048.0
				+ floor(chroma.g * 250.0) / 4.0 * 32.0
				+ floor(chroma.b * 250.0) / 8.0;
	float hi = clamp(fyuv / 256.0, 0.0, 255.0);
	float lo = mod(fyuv, 256.0);
	return vec2(lo - 16.0 + uBrightness, hi - 128.0); 
}

void main() {
	vec2 even, odd;
	vec2 center = floor(vTextureCoord * uTextureSz);
	even.y = odd.y = center.y;
	even.x = odd.x = floor(center.x * 0.5) * 2.0 + 0.5;
	odd.x += 1.0;
	vec3 yuv = vec3(getYuv(center).x, getYuv(even).y, getYuv(odd).y) / 255.0;
	vec3 rgb = clamp((MAT_YUV2RGB * yuv), 0.0, 1.0);
	
	gl_FragColor = vec4(rgb, 1.0);
}