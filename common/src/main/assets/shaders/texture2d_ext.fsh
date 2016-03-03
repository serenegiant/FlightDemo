#version 100
#extension GL_OES_EGL_image_external : require
precision highp float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
uniform vec2 uTextureSz;
uniform vec2 uFrameSz;

void main() {
	gl_FragColor = texture2D(sTexture, vTextureCoord);
}
