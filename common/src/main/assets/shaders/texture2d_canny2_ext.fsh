#version 120
#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2      vTextureCoord;
uniform samplerExternalOES sTexture;

uniform mediump float upperThreshold;
uniform mediump float lowerThreshold;

const float texelWidth = 1.0 / 640.0;
const float texelHeight = 1.0 / 368.0;

void main()
{
    vec3 currentGradientAndDirection = texture2D(sTexture, vTextureCoord).rgb;
    vec2 gradientDirection = ((currentGradientAndDirection.gb * 2.0) - 1.0) * vec2(texelWidth, texelHeight);

    float firstSampledGradientMagnitude = texture2D(sTexture, vTextureCoord + gradientDirection).r;
    float secondSampledGradientMagnitude = texture2D(sTexture, vTextureCoord - gradientDirection).r;

    float multiplier = step(firstSampledGradientMagnitude, currentGradientAndDirection.r);
    multiplier = multiplier * step(secondSampledGradientMagnitude, currentGradientAndDirection.r);

    float thresholdCompliance = smoothstep(lowerThreshold, upperThreshold, currentGradientAndDirection.r);
    multiplier = multiplier * thresholdCompliance;

    gl_FragColor = vec4(multiplier, multiplier, multiplier, 1.0);
}