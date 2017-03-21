/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2014-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

#define LOG_TAG "glustuff"
#define LOG_NDEBUG
#undef USE_LOGALL

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <android/native_window.h>

#include "utilbase.h"
#include "glutils.h"
#include "glProgram.h"
#include "glutstuff.h"

GLUStuff::GLUStuff(const char* pVertexSource, const char* pFragmentSource)
: mShaderProgram(createShaderProgram(
	pVertexSource, pFragmentSource, &mVertexShader, &mFragmentShader)) {
	init();
}

GLUStuff::GLUStuff(Assets &assets, const char *vertexfile, const char *fragmentfile)
: mShaderProgram(createShaderProgram(
	assets, vertexfile, fragmentfile, &mVertexShader, &mFragmentShader)) {
	init();
}

GLUStuff::~GLUStuff() {
	disposeProgram(mShaderProgram, mVertexShader, mFragmentShader);
}

// private
void GLUStuff::init(void) {
	// 射影行列のlocation取得
	loc_u_projection_m = glGetUniformLocation(mShaderProgram, "uProjection");
	if (!loc_u_projection_m) {
		LOGE("GLUStuff:Vertex_uniform:uProjection not found");
	}
	// モデルビュー行列のlocation取得
	loc_u_modelview_m = glGetUniformLocation(mShaderProgram, "uMVPMatrix");
	if (!loc_u_modelview_m) {
		LOGE("GLUStuff:Vertex_uniform:'uMVPMatrix' not found");
	}
	// モデル行列の逆転置行列のlocation取得
	loc_u_normal_m = glGetUniformLocation(mShaderProgram, "uNormalMatrix");
	if (!loc_u_normal_m) {
		LOGE("GLUStuff:Vertex_uniform:'uNormalMatrix' not found");
	}
}

// シェーダーを有効にする
void GLUStuff::select(void) {
	glUseProgram(mShaderProgram);
}

// 射影行列を設定する
void GLUStuff::setProjectionMatrix(Matrix &projMatrix) {
	glUniformMatrix4fv(loc_u_projection_m, 1, GL_FALSE, projMatrix);
}

// ビュー行列を設定する
void GLUStuff::setViewMatrix(Matrix &viewMatrix) {
	mViewMatrix = viewMatrix;
	glUniformMatrix4fv(loc_u_modelview_m, 1, GL_FALSE, mModelMatrix * mViewMatrix);
}

// モデル行列を設定する
void GLUStuff::setModelMatrix(Matrix &modelMatrix) {
	mModelMatrix = modelMatrix;
	glUniformMatrix4fv(loc_u_modelview_m, 1, GL_FALSE, mModelMatrix * mViewMatrix);
	if LIKELY(loc_u_normal_m) {
		Matrix t = modelMatrix.inverse().transpose();		// モデル行列の逆転置行列
		glUniformMatrix4fv(loc_u_normal_m, 1,  GL_FALSE, t);
	}
}

// 視錐台設定(平行投影)
void GLUStuff::ortho(
	const float &left, const float &right,
	const float &bottom, const float &top,
	const float &near, const float &far) {

	Matrix proj;
	proj.ortho(left, right, bottom, top, near, far);
	setProjectionMatrix(proj);
}

// 視錐台設定
void GLUStuff::frustum(
		const float &left, const float &right,
		const float &bottom, const float &top,
		const float &near, const float &far) {

	Matrix proj;
	proj.frustum(left, right, bottom, top, near, far);
	setProjectionMatrix(proj);
}

// 視錐台設定(透視投影)
void GLUStuff::perspective(
	const float &fovy, const float &aspect,
	const float &zNear, const float &zFar) {

	Matrix proj;
	proj.perspective(fovy, aspect, zNear, zFar);
	setProjectionMatrix(proj);
}

// 視点設定
void GLUStuff::lookAt(const Vector3 &eye, const Vector3 &at, const Vector3 &up) {
	setViewMatrix(mViewMatrix.lookAt(eye, at, up));
}
