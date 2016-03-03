#version 100
attribute vec4 aPosition;
attribute vec4 aTextureCoord;

varying vec2 vTextureCoord;

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;

void main() {
	vTextureCoord = (uTexMatrix * aTextureCoord).xy;
	gl_Position = uMVPMatrix * aPosition;
}
