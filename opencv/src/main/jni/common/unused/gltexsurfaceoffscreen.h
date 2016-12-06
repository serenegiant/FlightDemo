/*
 * gltexsurfaceoffscreen.h
 *
 *  Created on: 2014/06/08
 *      Author: saki
 */

#ifndef GLTEXSURFACEOFFSCREEN_H_
#define GLTEXSURFACEOFFSCREEN_H_

#pragma interface

#include <jni.h>
#include <android/native_window.h>
#include "glutils.h"
#include "libuvc.h"

class GLTexSurfaceOffscreen {
private:
	GLuint mTexture;
	ANativeWindow *mWindow;
public:
	GLTexSurfaceOffscreen(JNIEnv *env, const GLint width, const GLint height);
	~GLTexSurfaceOffscreen();
	int assignTexture(uvc_frame_t *frame);
};


#endif /* GLTEXSURFACEOFFSCREEN_H_ */
