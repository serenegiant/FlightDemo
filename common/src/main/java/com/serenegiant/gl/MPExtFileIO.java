package com.serenegiant.gl;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 外部ストレージアクセスクラス
 * @author saki
 *
 */
public class MPExtFileIO implements FileIO {
	protected final String mBasePath;
	
	/**
	 * コンストラクト、外部ストレージのベースディレクトリを設定する
	 * @param uniqueDir
	 */
	public MPExtFileIO(String uniqueDir) {
		mBasePath
			= Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + uniqueDir + File.separator;
	}
	
	@Override
	public InputStream readFile(String fileName) throws IOException {
		return new FileInputStream(mBasePath + fileName);
	}

	@Override
	public OutputStream writeFile(String fileName) throws IOException {
		final String name = mBasePath + fileName;
		final File file = new File(name);
		final File path = new File(file.getParent());	// 作成するファイルのディレクトリパスを取得
		if (!path.exists()) {					// ディレクトリが存在しなければ
			path.mkdirs();						// ディレクトリを作成する
		}
		if (!file.exists()) {					// ファイルが存在しなければ
			file.createNewFile();				// ファイルを作成する
		}
		return new FileOutputStream(name);
	}

	@Override
	public boolean fileExists(String fileName) {
		final File file = new File(mBasePath + fileName);
		return file.exists();	
	}
	
	@Override
	public boolean deleteFile(String fileName) {
		final File file = new File(mBasePath + fileName);
		return file.delete();
	}

}
