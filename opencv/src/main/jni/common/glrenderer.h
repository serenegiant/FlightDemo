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

#ifndef GLRENDERER_H_
#define GLRENDERER_H_

#pragma interface

#include "assets.h"
#include "gltexture.h"

// 指定したイメージをview全面にOpenGL|ESで描画するクラス
class GLRenderer {
protected:
	GLuint mShaderProgram;
	GLuint mVertexShader;
	GLuint mFragmentShader;
    // uniform変数のロケーション
	GLint muMVPMatrixLoc;		// モデルビュー行列のロケーション
	GLint muTexMatrixLoc;		// テクスチャ行列のロケーション
	GLint muTextureSzLoc;		// テクスチャサイズ変数のロケーション
	GLint muFrameSzLoc;			// フレームサイズ変数のロケーション
	GLint muBrightnessLoc;		// 明るさのオフセット変数の
    // attribute変数のロケーション
	GLint maPositionLoc;		// 頂点情報配列のロケーション
	GLint maTextureCoordLoc;	// テクスチャ座標配列のロケーション
	//
	float mBrightness;
	void init();
public:
	GLRenderer(const char *pVertexSource, const char *pFragmentSource);
	GLRenderer(Assets &assets, const char *vertexfile, const char *fragmentfile);
	~GLRenderer();
	int draw(GLTexture *texture, const GLfloat *tex_matrix = NULL, const GLfloat *mv_matrix = IDENTITY_MATRIX);
};


#endif /* GLRENDERER_H_ */
