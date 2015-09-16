package com.serenegiant.gl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileIO {
	/**
	 * ファイル名を指定して読み込み用ストリームを開く
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public InputStream readFile(final String fileName) throws IOException;
	/**
	 * ファイル名を指定して書き込み用ストリームを開く
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public OutputStream writeFile(final String fileName) throws IOException;
	/**
	 * 指定したファイルが存在するかどうかを返す
	 * @param fileName
	 * @return 指定したファイルが存在していればtrueを返す
	 */
	public boolean fileExists(final String fileName);
	/**
	 * 指定したファイルを削除する
	 * @param fileName
	 * @return 正常に削除出来ればtrueを返す
	 */
	public boolean deleteFile(final String fileName);
}
