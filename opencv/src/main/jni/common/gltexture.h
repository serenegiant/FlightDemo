/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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
