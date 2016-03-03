#version 100
#extension GL_OES_EGL_image_external : require

precision highp float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform vec2 uTextureSz;
uniform vec2 uFrameSz;

const vec3 bw = vec3(0.3, 0.59, 0.11);

void main() {
	vec3 tc = texture2D(sTexture, vTextureCoord).rgb;
	float color = dot(tc, bw);
	gl_FragColor = vec4(color, color, color, 1.0);
}
