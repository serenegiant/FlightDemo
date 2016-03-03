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

const vec3 YUV_OFFSET = vec3(0.0625, 0.5, 0.5);

#define EPS 0.00001

// yuyvのデータをrgbaのテクスチャとして渡して換算する場合(元の2ピクセル分のデータがテクセル1個に対応)
void main() {
	vec2 center = floor(vTextureCoord * uTextureSz);
	vec2 even = vec2(floor(center.x * 0.5) * 2.0 + 0.5, center.y);
	// 常に偶数位置のテクセルを読み込む(y,u,y,v)=(r,g,b,a)
//	vec4 yuyv = texture2D(sTexture, clamp(even / uTextureSz, 0.0, 1.0));
	vec4 yuyv = texture2D(sTexture, vTextureCoord);
	float y = (even.x == center.x) ? yuyv.r : yuyv.b;
	// 輝度がEPS未満なら黒を返す(プレビューを停止した時に画面が緑になるのを防ぐため)
	if (y > EPS) {
		vec3 yuv = vec3(clamp(y + uBrightness, 0.0, 1.0), yuyv.g, yuyv.a) - YUV_OFFSET;	
		vec3 rgb = clamp((MAT_YUV2RGB * yuv), 0.0, 1.0);
		gl_FragColor = vec4(rgb, 1.0);
	} else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
}
