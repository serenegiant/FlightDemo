/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef GLPROGRAM_H_
#define GLPROGRAM_H_

#include "assets.h"

// シェーダプログラムを設定する(createShaderProgramの下請け)
GLuint loadShader(GLenum shaderType, const char* pSource);
// シェーダプログラムをビルド・設定する
GLuint createShaderProgram(
	const char* pVertexSource,
	const char* pFragmentSource,
	GLuint *vertex_shader = NULL, GLuint *fragment_shader = NULL);
// assetsの指定したファイルから頂点シェーダプログラムとフラグメントシェーダプログラムを読み込んでコンパイル・設定する
GLuint createShaderProgram(
	Assets &assets,
	const char *vertexfile,
	const char *fragmentfile,
	GLuint *vertex_shader = NULL, GLuint *fragment_shader = NULL);
// シェーダプログラムを開放する
void disposeProgram(GLuint &shader_program, GLuint &vertex_shader, GLuint &fragment_shader);

#endif /* GLPROGRAM_H_ */
