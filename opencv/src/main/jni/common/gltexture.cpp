/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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
