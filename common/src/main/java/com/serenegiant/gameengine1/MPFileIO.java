package com.serenegiant.gameengine1;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 内部ストレージアクセスクラス
 * @author saki
 *
 */
public class MPFileIO implements FileIO {
	private final Context mContext;
	
	public MPFileIO(Context context) {
		mContext = context;
	}

	@Override
	public InputStream readFile(String fileName) throws IOException {
		fileName = fileName.replace("/", "_");	// 内部ストレージにはpath separaterを含むことができないのでディレクトリ名も含めてファイル名にする
		return mContext.openFileInput(fileName);
	}

	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		fileName = fileName.replace("/", "_");	// 内部ストレージにはpath separaterを含むことができないのでディレクトリ名も含めてファイル名にする
		// openFileOutputはファイルが存在しなければ自動的に生成するのであらかじめファイルを作成する必要なし
		return mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
	}

	@Override
	public boolean fileExists(String fileName) {
		fileName = fileName.replace("/", "_");	// 内部ストレージにはpath separaterを含むことができないのでディレクトリ名も含めてファイル名にする
		return mContext.getFileStreamPath(fileName).exists();
	}
	
	@Override
	public boolean deleteFile(String fileName) {
		fileName = fileName.replace("/", "_");	// 内部ストレージにはpath separaterを含むことができないのでディレクトリ名も含めてファイル名にする
		return mContext.deleteFile(fileName);
	}

}
