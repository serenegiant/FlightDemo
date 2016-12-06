/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef GLOFFSCREEN_H_
#define GLOFFSCREEN_H_

#pragma interface

#include "glutils.h"
#include "gltexture.h"
#include "glrenderer.h"

/**
 * フレームバッファオブジェクト(FBO)を使ってオフスクリーン描画するためのクラス
 */
class GLOffScreen {
private:
	const GLint mWidth;
	const GLint mHeight;
	GLuint mFrameBufferObj;
	GLuint mDepthBufferObj;
	GLTexture *mFBOTexture;
	float mMvpMatrix[16];
public:
	GLOffScreen(GLint width, GLint height, bool use_depth_buffer = 0);
	~GLOffScreen();
	// オフスクリーンへの描画に切り替える
	// Viewportも変更になるので必要であればunbind後にViewportの設定をすること
	int bind();
	// デフォルトのレンダリングバッファへ戻す
	int unbind();
	// オフスクリーンテクスチャを指定したGLRendererで描画する(ヘルパーメソッド)
	int draw(GLRenderer *renderer);
	inline GLTexture *getOffscreen() { return mFBOTexture; }
	inline const GLfloat *getMatrix() const { return mMvpMatrix; }
};


#endif /* GLOFFSCREEN_H_ */
