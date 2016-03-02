/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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
