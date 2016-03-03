package com.serenegiant.gameengine1;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GLObjLoader {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLObjLoader";

	private final IModelView mModelView;
	private final float[] vertexArray;
	private final float[] normalArray;
	private final float[] uvArray;
	
	private int numVertex = 0;
	private int numNormal = 0;
	private int numUV = 0;
	private int numFace = 0;
	
	private final int[] faceVerts;
	private final int[] faceNormals;
	private final int[] faceUV;
	
	// コンストラクタ
	public GLObjLoader(final IModelView game, final String fileName) {
		mModelView = game;
		final List<String> lines = new ArrayList<String>();
		InputStream in = null;
		try {
			in = new BufferedInputStream(game.getAssetIO().readFile(fileName));
			readLines(in, lines);
		} catch (final Exception e) {
			throw new RuntimeException("couldn't load '" + fileName + "'");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {

				}
			}
		}

		vertexArray = new float[lines.size() * 3];
		normalArray = new float[lines.size() * 3];
		uvArray = new float[lines.size() * 2];
			
		faceVerts = new int[lines.size() * 3];
		faceNormals = new int[lines.size() * 3];
		faceUV = new int[lines.size() * 3];

		load(lines);

		if (DEBUG) Log.v(TAG, String.format("vertex=%d,normal=%d,uv=%d,face=%d", numVertex, numNormal, numUV, numFace));
	}

	private void load(final List<String> lines) {
		// 読み込み用変数
		int vertexIndex = 0;
		int normalIndex = 0;
		int uvIndex = 0;
		int faceIndex = 0;

		// ファイルからの読み込み処理
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			if (line.startsWith("v ")) {		// 頂点座標
				final String[] tokens = line.split("[ ]+");
				vertexArray[vertexIndex] = Float.parseFloat(tokens[1]);
				vertexArray[vertexIndex + 1] = Float.parseFloat(tokens[2]);
				vertexArray[vertexIndex + 2] = Float.parseFloat(tokens[3]);
				vertexIndex += 3;
				numVertex++;
				continue;
			} if (line.startsWith("vn ")) {		// 頂点法線ベクトル情報
				final String[] tokens = line.split("[ ]+");
				normalArray[normalIndex] = Float.parseFloat(tokens[1]);
				normalArray[normalIndex + 1] = Float.parseFloat(tokens[2]);
				normalArray[normalIndex + 2] = Float.parseFloat(tokens[3]);
				normalIndex += 3;
				numNormal++;
				continue;
			} if (line.startsWith("vt ")) {		// テクスチャ座標
				final String[] tokens = line.split("[ ]+");
				uvArray[uvIndex] = Float.parseFloat(tokens[1]);
				uvArray[uvIndex + 1] = Float.parseFloat(tokens[2]);
				uvIndex += 2;
				numUV++;
				continue;
			} if (line.startsWith("f ")) {		// 三角形の面情報
				final String[] tokens = line.split("[ ]+");

				String[] parts = tokens[1].split("/");
				faceVerts[faceIndex] = getIndex(parts[0], numVertex);			// 頂点座標値番号

				if (parts.length > 2) {
					faceNormals[faceIndex] = getIndex(parts[2], numNormal);		// 頂点法線ベクトル番号
				}
				if (parts.length > 1)
					faceUV[faceIndex] = getIndex(parts[1], numUV);				// テクスチャ座標番号
				faceIndex++;

				parts = tokens[2].split("/");
				faceVerts[faceIndex] = getIndex(parts[0], numVertex);			// 頂点座標番号

				if (parts.length > 2)
					faceNormals[faceIndex] = getIndex(parts[2], numNormal);		// 頂点法線ベクトル番号
				if (parts.length > 1)
					faceUV[faceIndex] = getIndex(parts[1], numUV);				// テクスチャ座標番号
				faceIndex++;

				parts = tokens[3].split("/");
				faceVerts[faceIndex] = getIndex(parts[0], numVertex);			// 頂点座標番号

				if (parts.length > 2)
					faceNormals[faceIndex] = getIndex(parts[2], numNormal);		// 頂点法線ベクトル番号
				if (parts.length > 1)
					faceUV[faceIndex] = getIndex(parts[1], numUV);				// テクスチャ座標番号
				faceIndex++;

				numFace++;
				continue;
			}
		}
	}

	public Vertex getVertex() {
		return getVertex(0, numFace);
	}

	public Vertex getVertex(int faceOffset, int faceNum) {
		int n = faceOffset + faceNum;
		if (n > numFace) n = numFace;
		if (faceOffset < 0) faceOffset = 0;
		faceNum = n - faceOffset;
		// Vertexオブジェクトの生成処理
		// 準備
		final float[] verts = new float[(faceNum * 3)
		    * (3 + (numNormal > 0 ? 3 : 0)
		    	 + (numUV > 0 ? 2 : 0))];
		// 面の数だけ繰り返す
		for (int i = faceOffset * 3, vi = 0; i < n * 3; i++) {
			// 頂点座標の追加
			final int vertexIdx = faceVerts[i] * 3;
			verts[vi++] = vertexArray[vertexIdx];
			verts[vi++] = vertexArray[vertexIdx + 1];
			verts[vi++] = vertexArray[vertexIdx + 2];
			// テクスチャ座標の追加
			if (numUV > 0) {
				int uvIdx = faceUV[i] * 2;
				verts[vi++] = uvArray[uvIdx];
				verts[vi++] = 1 - uvArray[uvIdx + 1];
			}
			// 法線ベクトルの追加
			if (numNormal > 0) {
				int normalIdx = faceNormals[i] * 3;
				verts[vi++] = normalArray[normalIdx];
				verts[vi++] = normalArray[normalIdx + 1];
				verts[vi++] = normalArray[normalIdx + 2];
			}
		}
		
		final Vertex model = new Vertex(
			Vertex.DIM_3D, mModelView.getGLGraphics(), numFace * 3,
			0, false, numUV > 0, numNormal > 0);
		model.setVertex(verts, 0, verts.length);
		return model;
	}
	
	// 1個のファイルから1個のVertexオブジェクトを読み込む時はGLObjLoaderインスタンスを生成せずに直接読み込み可能
	public static Vertex load(final IModelView modelView, final String fileName) {
		final GLObjLoader loader = new GLObjLoader(modelView, fileName);
		return loader.getVertex();
	}

	private static int getIndex(final String index, final int size) {
		if ((index != null) && (!index.equals(""))) {
			int idx = Integer.parseInt(index);
			if (idx < 0)
				return size + idx;
			else
				return idx - 1;
		} else
			return 0;
	}
	
	private static List<String> readLines(final InputStream in, final List<String> lines) {

		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			
		}
		return lines;
	}
}
