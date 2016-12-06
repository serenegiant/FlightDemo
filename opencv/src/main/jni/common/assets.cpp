/*
 * Androusb
 * Copyright (c) 2013-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
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



