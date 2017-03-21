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

#define LOG_TAG "Assets"
#ifndef LOG_NDEBUG
#define LOG_NDEBUG				// LOGV/LOGD/MARKを出力しない時
#endif
#undef USE_LOGALL				// 指定したLOGxだけを出力

#include "utilbase.h"
#include "assets.h"

Assets::Assets(AAssetManager *assetManager)
:	mAssetManager(assetManager),
	mAsset(NULL) {
}

Assets::~Assets() {
	ENTER();

	close();

	EXIT();
}

int Assets::open(const char *fileName, const int mode) {
	if (UNLIKELY(mAsset)) {
		close();
	}
	mAsset = AAssetManager_open((AAssetManager *)mAssetManager, fileName, mode);
	if (UNLIKELY(!mAsset)) {
		LOGE("Assets:open: Fail to open file '%s'", fileName);
	}
	return (mAsset != NULL);
}

int Assets::close(void) {
	ENTER();

	if (LIKELY(mAsset)) {
		try {
			AAsset_close(mAsset);
		} catch (...) {
			LOGE("AAsset_close failed");
		}
		mAsset = NULL;
	}

	RETURN(0, int);
}

const off_t Assets::length(void) const {
	return LIKELY(mAsset) ? AAsset_getLength(mAsset) : 0;
}

int Assets::read(void *buffer, const int bytes) {
	if (LIKELY(mAsset)) {
		int n = AAsset_read(mAsset, buffer, bytes);
		if (UNLIKELY(n < 0)) {
			LOGE("Assets:read: Fail to read from asset file(err=%d)", n);
		}
		return n;
	} else {
		return -1;
	}
}

int Assets::seek(const off_t &offset, const int whence) {
	if (LIKELY(mAsset)) {
		int pos = AAsset_seek(mAsset, offset, whence);
		if (UNLIKELY(pos < 0)) {
			LOGE("Assets:seek: Fail to seek asset file(err=%d)", pos);
		}
		return pos;
	} else {
		return -1;
	}

}



