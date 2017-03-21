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

#define LOG_TAG "GLOffScreen"
#if 1	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
		#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
	#define USE_LOGALL
	#undef LOG_NDEBUG
	#undef NDEBUG
	#define DEBUG_GL_CHECK			// GL関数のデバッグメッセージを表示する時
#endif

#include "utilbase.h"
#include "gloffscreen.h"

GLOffScreen::GLOffScreen(GLint width, GLint height, bool use_depth_buffer)
:	mWidth(width),
 	mHeight(height),
	mFrameBufferObj(0),
 	mDepthBufferObj(0) {

	MARK("size(%d,%d)", width, height);
	mFBOTexture = new GLTexture(width, height);

	if (use_depth_buffer) {
		// デプスバッファが必要な場合は、レンダーバッファオブジェクトを生成・初期化する
		glGenRenderbuffers(1, &mDepthBufferObj);
		GLCHECK("glGenRenderbuffers");
		glBindRenderbuffer(GL_RENDERBUFFER, mDepthBufferObj);
		GLCHECK("glBindRenderbuffer");
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, mFBOTexture->getTexWidth(), mFBOTexture->getTexHeight());	// 16ビット
		GLCHECK("glRenderbufferStorage");
	}
	// フレームバッファーオブジェクトを生成・初期化
	glGenFramebuffers(1, &mFrameBufferObj);
	GLCHECK("glGenFramebuffers");
    glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObj);
    GLCHECK("GLOffScreen:glBindFramebuffer");

    // フレームバッファにテクスチャ(カラーバッファ)を接続する
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mFBOTexture->getTexture(), 0);
    GLCHECK("GLOffScreen:glFramebufferTexture2D");
	if (use_depth_buffer) {
		// フレームバッファにデプスバッファを接続する
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mDepthBufferObj);
		GLCHECK("glFramebufferRenderbuffer");
	}
    // 正常に終了したかどうかを確認する
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
	GLCHECK("glCheckFramebufferStatus");
    if UNLIKELY(status != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Framebuffer not complete, status=%d", status);
    }
    // デフォルトのフレームバッファに戻す
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
	GLCHECK("glBindFramebuffer");
    // GLRendererは上限反転させて描画するので上下を入れ替える
	setIdentityMatrix(mMvpMatrix, 0);
	mMvpMatrix[5] *= -1.f;			// 上下反転
}

GLOffScreen::~GLOffScreen() {
	// フレームバッファオブジェクトを破棄
	if (mFrameBufferObj > 0) {
		glDeleteFramebuffers(1, &mFrameBufferObj);
		GLCHECK("glDeleteFramebuffers");
		mFrameBufferObj = 0;
	}
	// デプスバッファがある時はデプスバッファを破棄
	if (mDepthBufferObj > 0) {
		glDeleteRenderbuffers(1, &mDepthBufferObj);
		GLCHECK("glDeleteRenderbuffers");
	}
	// テクスチャを破棄
	if (mFBOTexture) {
		SAFE_DELETE(mFBOTexture);
		mFBOTexture = NULL;
	}
}

// オフスクリーンへの描画に切り替える
// Viewportも変更になるので必要であればunbind後にViewportの設定をすること
int GLOffScreen::bind() {
	ENTER();
    glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObj);
    GLCHECK("glBindFramebuffer");
    glViewport(0, 0, mWidth, mHeight);
	GLCHECK("glViewport");
	RETURN(0, int);
}

// デフォルトのレンダリングバッファに戻す
int GLOffScreen::unbind() {
	ENTER();
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
	GLCHECK("glBindFramebuffer");
	RETURN(0, int);
}

// オフスクリーンテクスチャを指定したGLRendererで描画する(ヘルパーメソッド)
int GLOffScreen::draw(GLRenderer *renderer) {
	ENTER();
	if LIKELY(renderer) {
		renderer->draw(mFBOTexture, mFBOTexture->getTexMatrix(), mMvpMatrix);
		RETURN(0, int);
	}
	RETURN(-1, int);
}

