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
#ifndef GLUT_STUFF2_H
#define GLUT_STUFF2_H

#include "glutils.h"
#include "Matrix.h"
#include <assets.h>

class GLUStuff {
private:
	void init(void);
protected:
	GLuint mShaderProgram;
	GLuint mVertexShader;
	GLuint mFragmentShader;
	GLuint loc_u_projection_m;	// 射影行列のlocation
	GLuint loc_u_modelview_m;	// モデルビュー行列のlocation
	GLuint loc_u_normal_m;		// モデル行列の逆転置行列のlocation
	Matrix mProjMatrix;			// 射影行列
	Matrix mViewMatrix;			// ビュー行列
	Matrix mModelMatrix;		// モデル行列
public:
	GLUStuff(const char* pVertexSource, const char* pFragmentSource);
	GLUStuff(Assets &assets, const char *vertexfile, const char *fragmentfile);
	~GLUStuff();
	inline const GLuint getProgram(void) const { return mShaderProgram; }
	void select(void);
	// 射影行列を設定する
	void setProjectionMatrix(Matrix &projMatrix);
	// ビュー行列を設定する
	void setViewMatrix(Matrix &ViewMatrix);
	// モデル行列を設定する
	void setModelMatrix(Matrix &ViewMatrix);
	// 視錐台設定(平行投影)
	void ortho(
		const float &left, const float &right,
		const float &bottom, const float &top,
		const float &near, const float &far);
	// 視錐台設定
	void frustum(
			const float &left, const float &right,
			const float &bottom, const float &top,
			const float &near, const float &far);
	// 視錐台設定(透視投影)
	void perspective(
		const float &fovy, const float &aspect,
		const float &zNear, const float &zFar);
	// 視点設定
	void lookAt(const Vector3 &eye, const Vector3 &at, const Vector3 &up);
};

#endif //GLUT_STUFF2_H
