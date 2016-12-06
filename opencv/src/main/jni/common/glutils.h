/*
 * Androusb
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef GLUTILS_H_
#define GLUTILS_H_

#ifndef __gl3_h_
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#undef GLCHECK

#ifdef DEBUG_GL_CHECK
	#define	GLCHECK(OP) checkGlError(OP)
#else
	#define	GLCHECK(OP)
#endif

extern const GLfloat IDENTITY_MATRIX[];

// 単位行列にする
// 境界チェックしていないのでOffsetから16個必ず確保しておくこと
void setIdentityMatrix(GLfloat *m, int offset);
// 行列をlogCatへ出力
void printMatrix(const GLfloat *m);

void printGLString(const char *name, GLenum s);
void checkGlError(const char* op);
// テクスチャを生成する
GLuint createTexture(GLenum target, int alignment);

#endif /* GLUTILS_H_ */
