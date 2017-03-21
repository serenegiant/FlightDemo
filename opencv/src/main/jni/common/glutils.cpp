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

#define LOG_TAG "glutils"
#ifndef LOG_NDEBUG
#define LOG_NDEBUG				// LOGV/LOGD/MARKを出力しない時
#endif
#undef USE_LOGALL				// 指定したLOGxだけを出力

//#define DEBUG_GL_CHECK			// GL関数のデバッグメッセージを表示する時

#include <string.h>

#include "utilbase.h"
#include "glutils.h"

/**
 * 単位行列
 */
const GLfloat IDENTITY_MATRIX[] = {
	1.0f,	0.0f,	0.0f,	0.0f,
	0.0f,	1.0f,	0.0f,	0.0f,
	0.0f,	0.0f,	1.0f,	0.0f,
	0.0f,	0.0f,	0.0f,	1.0f,
};

// 単位行列にする
// 境界チェックしていないのでOffsetから16個必ず確保しておくこと
void setIdentityMatrix(GLfloat *m, int offset) {
	memcpy(&m[offset], IDENTITY_MATRIX, sizeof(IDENTITY_MATRIX));
}

// 境界チェックしていないので16個必ず確保しておくこと
void printMatrix(const GLfloat *m) {
	const GLfloat *p;
	for (int i = 0; i < 4; i++) {
		p = &m[i * 4];
		LOGI("%2d)%8.4f,%8.4f,%8.4f,%8.4f", i * 4, p[0], p[1], p[2], p[3]);
	}
}

void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    MARK("GL %s = %s", name, v);
}

void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGE("glError (0x%x) after %s()", error, op);
    }
}

// テクスチャを生成する
// targetはGL_TEXTURE_2Dとか
// alignmentは1, 2, 4, 8のいずれか
GLuint createTexture(GLenum target, int alignment) {
	// テクスチャはバイト単位でつめ込まれている
	/* glPixelStorei() は，コンピュータ側のメモリと OpenGL 側のメモリとの間でデータをやり取りする際に，
	 * 画像（この場合はテクスチャ）がメモリにどのように格納されているかを OpenGL に伝えるために用います．
	 * 引数 pname に GL_UNPACK_ALIGNMENT を指定した場合，param にはメモリを参照するときの
	 * 「アドレス境界」を指定します．param には 1, 2, 4, 8 のいずれかが指定できます．
	 * 画素の色が各色 1 バイトで表されているとき，1 画素が RGBA で表現されていれば，
	 * メモリ上の 4 バイトごとに 1 画素が配置されていることになります（4 バイト境界＝ワード境界）．
	 * このときは param に 4 が指定できます．一方，1 画素が RGB で表現されていれば，メモリ上の
	 * 3 バイトごとに 1 画素が配置されていることになります．"3" というアドレス境界は許されませんから
	 * （param に 3 は指定できない），この場合は param に 1 を指定する必要があります．
	 * 一般に，この数が多いほど効率的なメモリアクセスが可能になります．pname に指定できる
	 * その他の値については，マニュアル等を参照してください．
	 * なお，同じことをする関数に glPixelStoref(GLenum pname, GLfloat param) があります
	 * (param のデータ型が違うだけ）．*/
	GLuint tex;
	glPixelStorei(GL_UNPACK_ALIGNMENT, alignment);
	GLCHECK("glPixelStorei");
	glGenTextures(1, &tex);
	GLCHECK("glGenTextures");
	glBindTexture(target, tex);
	GLCHECK("glBindTexture");
	// テクスチャの繰り返し方法を指定
	glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	GLCHECK("glTexParameteri");
	glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	GLCHECK("glTexParameteri");
	// テクスチャの拡大・縮小方法を指定GL_NEARESTにすると補間無し
	glTexParameteri(target, GL_TEXTURE_MIN_FILTER, /*GL_NEAREST*/ GL_LINEAR);
//	glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	GLCHECK("glTexParameteri");
	glTexParameteri(target, GL_TEXTURE_MAG_FILTER, /*GL_NEAREST*/ GL_LINEAR);
//	glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	GLCHECK("glTexParameteri");

	return tex;
}
