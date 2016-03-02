/*
 * gltexsurfaceoffscreen.cpp
 *
 *  Created on: 2014/06/08
 *      Author: saki
 */

#define LOG_TAG "GLTexSurfaceOffscreen"
#define LOG_NDEBUG			// LOGV/LOGDを出力しない時
#undef USE_LOGALL			// 指定したLOGxだけを出力
//#define DEBUG_GL_CHECK	// GL関数のデバッグメッセージを表示する時

#include <string.h>
#include <android/native_window_jni.h>
#include "gltexsurfaceoffscreen.h"

GLTexSurfaceOffscreen::GLTexSurfaceOffscreen(JNIEnv *env, const GLint width, const GLint height)
:	mTexture(0),
 	mWindow(NULL) {
	ENTER();

	// テクスチャを生成
	glActiveTexture(GL_TEXTURE0);					// テクスチャユニット0を選択
	glGenTextures(1, &mTexture);
	glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTexture);

	// Java側で指定したテクスチャ名を使ってSurfaceTextureを生成し、Surfaceとして返してもらう
	// GL_TEXTURE_EXTERNAL_OES簡単に生成するため
    jclass clazz = env->FindClass("com/serenegiant/graphics/TextureOffscreen");
    jmethodID createTextureSurface = env->GetMethodID(clazz,
    	"createTextureSurface",	"(I)Landroid/view/Surface;");
    if (UNLIKELY(!createTextureSurface)) {
        LOGE("Can't find com/serenegiant/graphics/TextureOffscreen#createTextureSurface");
        EXIT();
    }
    jobject jSurface = env->CallStaticObjectMethod(clazz, createTextureSurface, mTexture);
    if (LIKELY(jSurface)) {
    	mWindow = jSurface ? ANativeWindow_fromSurface(env, jSurface) : NULL;
    	if (LIKELY(mWindow)) {
    		ANativeWindow_setBuffersGeometry(mWindow, width, height, WINDOW_FORMAT_RGBA_8888);
    	}
    } else {
    	LOGE("cound not get surface from createTextureSurface");
    }
    EXIT();
}

GLTexSurfaceOffscreen::~GLTexSurfaceOffscreen() {
	ENTER();

	if (mWindow) {
		ANativeWindow_release(mWindow);
		mWindow = NULL;
	}
	glDeleteTextures(1, &mTexture);
	mTexture = 0;

	EXIT();
}

// copyToSurfaceの下請け
static void copyFrame(const uint8_t *src, uint8_t *dest, const int width, int height, const int stride_src, const int stride_dest) {
	const int h8 = height % 8;
	for (int i = 0; i < h8; i++) {
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
	}
	for (int i = 0; i < height; i += 8) {
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
		memcpy(dest, src, width);
		dest += stride_dest; src += stride_src;
	}
}

int GLTexSurfaceOffscreen::assignTexture(uvc_frame_t *frame) {
	ENTER();

	int result = -1;
	if LIKELY(mWindow) {
		ANativeWindow_Buffer buffer;
		if (LIKELY(ANativeWindow_lock(mWindow, &buffer, NULL) == 0)) {
			// 転送元(イメージフレーム)
			const uint8_t *src = (uint8_t *)frame->data;
			const int src_pixelbytes = 2;							// yuyv
			const int src_w = frame->width * src_pixelbytes;
			const int src_step = frame->width * src_pixelbytes;		// フレームの各行のバイト数
			// 転送先(Surface)
			uint8_t *dest = (uint8_t *)buffer.bits;
			const int dest_pixcelbytes = 4;							// rgba/rgbx
			const int dest_w = buffer.width * dest_pixcelbytes;
			const int dest_step = buffer.stride * dest_pixcelbytes;	// surface側の各行のバイト数
			// 1行あたりの転送バイト数の少ない方を選択する
			const int w = src_w < dest_w ? src_w : dest_w;
			// 狭い方の高さを基準に転送する行数を計算
			const int h = frame->height < buffer.height ? frame->height : buffer.height;	// 縦方向のピクセル数
			copyFrame(src, dest, w, h, src_step, dest_step);
			ANativeWindow_unlockAndPost(mWindow);
			result = 0;
		} else {
			result = -1;
		}
	}
	RETURN(result, int);
}
