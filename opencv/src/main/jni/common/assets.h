/*
 * Androusb
 * Copyright (c) 2013-2015 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

#ifndef ASSETS_H_
#define ASSETS_H_

#include <android/asset_manager.h>
/**
 * android_mainの引数に渡されてくるandroid_app->activity->assetManagerを参照して
 * assetsからファイルを読み込むためのクラス
 * ANativeActivity *activity
 * AAssetManager *assetManager
 */
class Assets {
private:
protected:
	const AAssetManager *mAssetManager;
	AAsset *mAsset;
public:
	Assets(AAssetManager *assetManager);
	~Assets();
	int open(const char *fileName, const int mode = AASSET_MODE_RANDOM);
	int close(void);
	const off_t length(void) const;
	int read(void *buffer, const int bytes);
	int seek(const off_t &offset, const int whence);
};


#endif /* ASSETS_H_ */
