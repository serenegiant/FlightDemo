#version 100
#extension GL_OES_EGL_image_external : require

precision highp float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform vec2 uTextureSz;
uniform vec2 uFrameSz;
#define KERNEL_SIZE 9
uniform float uKernel[KERNEL_SIZE];
uniform vec2 uTexOffset[KERNEL_SIZE];
uniform float uColorAdjust;

void main() {
	int i = 0;
	vec4 sum = vec4(0.0);
	if (vTextureCoord.x < vTextureCoord.y - 0.005) {
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[0]) * uKernel[0];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[1]) * uKernel[1];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[2]) * uKernel[2];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[3]) * uKernel[3];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[4]) * uKernel[4];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[5]) * uKernel[5];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[6]) * uKernel[6];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[7]) * uKernel[7];
		sum += texture2D(sTexture, vTextureCoord + uTexOffset[8]) * uKernel[8];
		sum += uColorAdjust;
	} else if (vTextureCoord.x > vTextureCoord.y + 0.005) {
		sum = texture2D(sTexture, vTextureCoord);
	} else {
		sum.r = 1.0;
	}
	gl_FragColor = sum;
}
