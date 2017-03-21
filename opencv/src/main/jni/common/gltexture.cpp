/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
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

#define LOG_TAG "GLTexture"

#if 1	// デバッグ情報を出さない時は1
	#ifndef LOG_NDEBUG
		#define	LOG_NDEBUG		// LOGV/LOGD/MARKを出力しない時
	#endif
	#undef USE_LOGALL			// 指定したLOGxだけを出力
#else
	#define USE_LOGALL
	#undef LOG_NDEBUG
	#undef NDEBUG
//	#define DEBUG_GL_CHECK			// GL関数のデバッグメッセージを表示する時
#endif

#include "utilbase.h"
#include "gltexture.h"

GLTexture::GLTexture(const GLint &width, const GLint &height, const GLint &internal_pixel_format, const GLint &pixel_format)
:	mTextureTarget(GL_TEXTURE_2D),
	mInternalPixelFormat(internal_pixel_format),
	mPixelFormat(pixel_format),
	mTexture(0),
	mTexWidth(0), mTexHeight(0),
	mImageWidth(width), mImageHeight(height) {
	ENTER();

	MARK("size(%d,%d)", width, height);
	init(width, height);

	EXIT();
}

GLTexture::~GLTexture() {
	ENTER();

	if (mTexture) {
		glDeleteTextures(1, &mTexture);
		GLCHECK("glDeleteTextures");
		mTexture = 0;
	}
	EXIT();
}

void GLTexture::init(const GLint &width, const GLint &height) {
	ENTER();

    if (!mTexture) {
		// テクスチャを生成
		glActiveTexture(GL_TEXTURE0);	// テクスチャユニット0を選択
		GLCHECK("glActiveTexture");
		mTexture = createTexture(mTextureTarget,
			mInternalPixelFormat == GL_RGBA ? 4 : 1);	// RGBAのテクスチャを使う時は4バイト境界,それ以外は1バイト境界でアクセスする
    }
	// 2の乗数にする
	int w = 32;
	for (; w < width; w <<= 1);
	int h = 32;
	for (; h < height; h <<= 1);
	if (mTexWidth != w || mTexHeight != h) {
		mTexWidth = w;
		mTexHeight = h;
	}
	bind();
	// テクスチャのメモリ領域を確保する
	glTexImage2D(mTextureTarget,
		0,							// ミップマップレベル
		mInternalPixelFormat,		// 内部フォーマット
		mTexWidth, mTexHeight,		// サイズ
		0,							// 境界幅
		mPixelFormat,				// 引き渡すデータのフォーマット
		GL_UNSIGNED_BYTE,			// データの型
		NULL);						// ピクセルデータ
	GLCHECK("glTexImage2D");
    // テクスチャ変換行列を単位行列に初期化
	setIdentityMatrix(mTexMatrix, 0);
	// テクスチャ変換行列を設定
	mTexMatrix[0] = width / (float)mTexWidth;
	mTexMatrix[5] = height / (float)mTexHeight;
	MARK("tex(%d,%d),request(%d,%d)", mTexWidth, mTexHeight, width, height);
//	printMatrix(mTexMatrix);

    EXIT();
}

int GLTexture::assignTexture(const uint8_t *src, const int view_width, const int view_height) {
	ENTER();

	glActiveTexture(GL_TEXTURE0);	// テクスチャユニット0を選択
    GLCHECK("glActiveTexture");
	glBindTexture(mTextureTarget, mTexture);
    GLCHECK("glBindTexture");
    glTexSubImage2D(mTextureTarget,
    	0,								// ミップマップレベル
    	0, 0,							// オフセットx,y
    	view_width, view_height,		// 上書きするサイズ
		mPixelFormat,					// 引き渡すデータのフォーマット
    	GL_UNSIGNED_BYTE,				// データの型・・・1ピクセル=2バイト✕2ピクセル=1テクセル
    	src);							// ピクセルデータ
    GLCHECK("glTexSubImage2D");

    RETURN(0, int);
}

int GLTexture::bind() {
	ENTER();

	glActiveTexture(GL_TEXTURE0);	// テクスチャユニット0を選択
	GLCHECK("glActiveTexture");
	glBindTexture(mTextureTarget, mTexture);
	GLCHECK("glBindTexture");

	RETURN(0, int);
}

int GLTexture::unbind() {
	ENTER();

    glBindTexture(mTextureTarget, 0);
	GLCHECK("glBindTexture");

	RETURN(0, int);
}

int GLTexture::setFilter(const GLint &min_filter, const GLint &max_filter) {
	ENTER();

	// テクスチャの拡大・縮小方法を指定GL_NEARESTにすると補間無し
	glTexParameteri(mTextureTarget, GL_TEXTURE_MIN_FILTER, min_filter);
	GLCHECK("glTexParameteri");
	glTexParameteri(mTextureTarget, GL_TEXTURE_MAG_FILTER, max_filter);
	GLCHECK("glTexParameteri");

	RETURN(0, int);
}
