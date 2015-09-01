package com.serenegiant.widget.gl;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * assetsファイルアクセスクラス
 * @author saki
 *
 */
public class MPAssetIO implements FileIO {
	protected final AssetManager mAssets;

	public MPAssetIO(AssetManager assets) {
		mAssets = assets;
	}
	
	@Override
	public InputStream readFile(String fileName) throws IOException {
		return mAssets.open(fileName);
	}

	/**
	 * @return assets内へは書き込みできないので常にnullを返す
	 */
	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		return null;
	}
	
	@Override
	public boolean fileExists(String fileName) {
		try {
			mAssets.open(fileName);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * @return assets内のファイルは削除できないので常にfalseを返す
	 */
	@Override
	public boolean deleteFile(String fileName) {
		return false;
	}
}
