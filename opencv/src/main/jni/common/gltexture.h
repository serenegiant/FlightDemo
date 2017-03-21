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

#ifndef GLTEXTURE_H_
#define GLTEXTURE_H_

#include "glutils.h"

class GLTexture {
private:
	const GLuint mTextureTarget;		// 使用するテクスチャの種類GL_TEXTURE_2D/GL_TEXTURE_EXTERNAL_OES
	const GLint mInternalPixelFormat;	// テクスチャの内部形式, デフォルトはGL_RGBA
	const GLint mPixelFormat;			// テクスチャをセットするときの形式, デフォルトはGL_RGBA
	// テクスチャのサイズ
	int mTexWidth, mTexHeight;
	// テクスチャに貼り付けるイメージのサイズ
	const int mImageWidth, mImageHeight;
	float mTexMatrix[16];
	GLuint mTexture;				// テクスチャ名(テクスチャID)
protected:
	void init(const GLint &width, const GLint &height);
public:
	GLTexture(const GLint &width, const GLint &height, const GLint &internal_format = GL_RGBA, const GLint &format = GL_RGBA);
	virtual ~GLTexture();
	int assignTexture(const uint8_t *src, const int view_width, const int view_height);
	int bind();
	int unbind();
	inline const GLuint getTexTarget() const { return mTextureTarget; }
	inline GLuint getTexture() const { return mTexture; }
	inline const GLfloat *getTexMatrix() const { return mTexMatrix; }
	inline const int getTexWidth() const { return mTexWidth; }
	inline const int getTexHeight() const { return mTexHeight; }
	inline const int getImageWidth() const { return mImageWidth; }
	inline const int getImageHeight() const { return mImageHeight; }

	/**
	 * min_filter/max_filterはそれぞれGL_NEARESTかGL_LINEAR
	 * GL_LINEARにすると補間なし
	 */
	int setFilter(const GLint &min_filter, const GLint &max_filter);
};

#endif /* GLTEXTURE_H_ */
