package com.serenegiant.widget.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.serenegiant.math.Vector;

public class Vertex {
	public static final int DIM_2D = 2;				// 2次元座標系
	public static final int DIM_3D = 3; 			// 3次元座標系

	private static final int FLOAT_SZ = Float.SIZE / 8;	// float型のサイズ[4バイト]
	private static final int SHORT_SZ = Short.SIZE / 8;	// short型のサイズ[2バイト]
	private static final int INT_SZ = Integer.SIZE / 8;	// int型のサイズ[4バイト]

	private static final int COORD_NUM = 2;			// テクスチャ情報のサイズ[float x 個]
	private static final int COLOR_NUM = 4;			// 色情報のサイズ[float x 個]
	private static final int NORMAL_NUM = 3;		// 頂点法線情報のサイズ[float x 個]

	private final GLGraphics glGraphics;
	private final boolean hasColor;					// 色情報あり？
	private final boolean hasTexCoord; 				// テクスチャ座標あり？
	private final boolean hasNormals;				// 頂点法線あり？
	private final int vertexSize;					// 頂点情報のサイズ[バイト]
	private final IntBuffer vertexArray;			// 頂点情報配列
	private final ShortBuffer indexArray;			// 頂点インデックス配列
	private final int[] tmpBuffer;
	private final int dim_num ;
	private final int maxVertex, maxIndex;

	private static final int VBO_VERTEX = 0;
	private static final int VBO_INDEX = 1;
//	private static final int VBO_TEX = 2;
//	private static final int VBO_COLOR = 3;
//	private static final int VBO_NORM = 4;

	private final int[] vboId = new int[2];			// VBO ID
	private boolean hasVbo = false;

	public Vertex(final int dim_num, final GLGraphics glGraphics, final int maxVertex, final int maxIndex) {
		this(dim_num, glGraphics, maxVertex, maxIndex, false, false, false);
	}

	public Vertex(final int dim_num, final GLGraphics glGraphics, final int maxVertex, final int maxIndex,
			final boolean hasColor) {
		this(dim_num, glGraphics, maxVertex, maxIndex, hasColor, false, false);
	}

	public Vertex(final int dim_num, final GLGraphics glGraphics, final int maxVertex, final int maxIndex,
			final boolean hasColor, final boolean hasTexCoord) {
		this(dim_num, glGraphics, maxVertex, maxIndex, hasColor, hasTexCoord, false);
	}

	public Vertex(final int dim_num, final GLGraphics glGraphics, final int maxVertex, final int maxIndex,
			final boolean hasColor, final boolean hasTexCoord, final boolean hasNormals) {
		this.dim_num = dim_num;
		this.glGraphics = glGraphics;
		this.maxVertex = maxVertex;
		this.maxIndex = maxIndex;
		this.hasColor = hasColor;
		this.hasTexCoord = hasTexCoord;
		this.hasNormals = hasNormals;
		this.vertexSize = (dim_num
			+ (hasColor ? COLOR_NUM : 0)
			+ (hasTexCoord ? COORD_NUM : 0)
			+ (hasNormals ? NORMAL_NUM : 0) ) * FLOAT_SZ;	// [バイト]
		// 頂点情報配列の準備
		tmpBuffer = new int[vertexSize * maxVertex / INT_SZ];	// [int x 個]
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexSize * maxVertex).order(ByteOrder.nativeOrder());
		vertexArray = byteBuffer.asIntBuffer();
		// 頂点インデックス配列の準備
		if (maxIndex > 0) {
			byteBuffer = ByteBuffer.allocateDirect(SHORT_SZ * maxIndex).order(ByteOrder.nativeOrder()); // [バイト]
			indexArray = byteBuffer.asShortBuffer();
		} else {
			indexArray = null;
		}
	}

	// 既存のVertextの内容をコピーして新しいVertexを生成する
	public Vertex copy() {
		final Vertex vert = new Vertex(dim_num, glGraphics, maxVertex, maxIndex, hasColor, hasTexCoord, hasNormals);
		// vertexArrayをコピー
		if (vertexArray != null) {
			vertexArray.position(0);
			vert.vertexArray.put(vertexArray);
		}
		// indexArrayをコピー
		if (indexArray != null) {
			indexArray.position(0);
			vert.indexArray.put(indexArray);
		}
		return vert;
	}

	// 既存のVertextから頂点/インデックスのオフセットと個数を指定してコピーして新しいVertexを生成する
	public Vertex copy(final int vertOffset, final int vertNum, final int indexOffset, final int indexNum) {
		final Vertex vert = new Vertex(dim_num, glGraphics, vertNum, indexNum, hasColor, hasTexCoord, hasNormals);
		// vertexArrayをコピー
		if (vertexArray != null) {
			vertexArray.position(0);
			vert.vertexArray.put(vertexArray.array(), vertOffset, vertNum);
		}
		// indexArrayをコピー
		if (indexArray != null) {
			indexArray.position(0);
			vert.indexArray.put(indexArray.array(), indexOffset, indexNum);
		}
		return vert;
	}

	// 頂点情報をfloatの配列として取得
	public float[] getAsFloat() {
		final int n = vertexArray.limit();
		final float [] tmp = new float[n];
		for (int i = 0; i < n; i++)
			tmp[i] = Float.intBitsToFloat(vertexArray.get(i));
		return tmp;
	}

	// 頂点座標をずらず
	public void move(final Vector offset) {
		final float [] tmp = getAsFloat();
		final int n = tmp.length;
		final int size = vertexSize / FLOAT_SZ;	// 頂点情報の位置サイズ[x float]
		for (int i = 0; i < n; i+= size) {
			// 頂点だけずらせば良い(法線ベクトル・テクスチャ座標その他は変更しない)
			tmp[i] += offset.x;
			tmp[i+1] += offset.y;
			if (dim_num > 2)
				tmp[i+2] += offset.z;
		}
		setVertex(tmp, 0, tmp.length);
	}

	// Vertexを回転させる
	public void rotate(final Vector angle) {
		rotate(angle.x, angle.y, angle.z);
	}

	// Vertexを回転させる
	public void rotate(final float x, final float y, final float z) {
		final Vector v = new Vector();
		final float [] tmp = getAsFloat();
		final int n = tmp.length;
		final int size = vertexSize / FLOAT_SZ;	// 頂点情報の位置サイズ[x float]
		final int norm_offset = dim_num + (hasColor ? COLOR_NUM : 0) + (hasTexCoord ? COORD_NUM : 0);
		for (int i = 0; i < n; i+= size) {
			// 頂点座標を回転
			v.set(tmp[i], tmp[i+1], dim_num > 2 ? tmp[i+2] : 0);
			Vector.rotate(v, x, y, z);
			tmp[i] = v.x;
			tmp[i+1] = v.y;
			if (dim_num > 2) tmp[i+2] = v.z;
		    // 頂点法線を回転
		    if (hasNormals) {
				v.set(tmp[norm_offset + i], tmp[norm_offset + i + 1], dim_num > 2 ? tmp[norm_offset + i + 2] : 0);
				Vector.rotate(v, x, y, z);
				tmp[norm_offset + i] = v.x;
				tmp[norm_offset + i + 1] = v.y;
				if (dim_num > 2) tmp[norm_offset + i + 2] = v.z;
		    }
		}
		setVertex(tmp, 0, tmp.length);
	}

	public void setVertex(final float[] vertex, final int offset, final int length) {
		vertexArray.clear();
		addVertex(vertex, offset, length);
	}

	public void add(final Vertex vertex) {
		if (vertex == null) return;
		final int limit = vertexArray.limit();
		vertexArray.limit(vertexArray.capacity());
		vertexArray.position(limit);	// ポジションを最後に設定
		addVertex(vertex.getAsFloat());
		if ((vertex.indexArray != null) && (indexArray != null)) {	// インデックスがある時
			final short offset = (short)indexArray.limit();
			final int n = vertex.indexArray.limit();			// 追加するVertexオブジェクトのインデックスの個数
			final short[] ixs = vertex.indexArray.array();
			final short[] index = new short[n];
			for (int i = 0; i < n; i++) {
				index[i] = (short) (ixs[i] + offset);
			}
			addIndex(index, 0, n);
		}
	}

	public void addVertex(final float[] vertex) {
		addVertex(vertex, 0, vertex.length);
	}

	// srcの頂点の指定したoffsetから指定した個数をdstの頂点に追加する
	public void addVertex(final float[] verts, final int offset, final int length) {
		destroyVBO();
		final int len = offset + length;
		for (int i = offset, j = 0; i < len; i++, j++)
			tmpBuffer[j] = Float.floatToRawIntBits(verts[i]);
		vertexArray.put(tmpBuffer, 0, length);
		vertexArray.flip();
	}

	public void setIndexs(final short[] index, final int offset, final int length) {
		indexArray.clear();
		addIndex(index, offset, length);
	}

	// 指定したoffsetから指定した個数をdstのインデックスに追加する
	public void addIndex(final short[] index, final int offset, final int length) {
		destroyVBO();
		indexArray.put(index, offset, length);
		indexArray.flip();
	}


	public void bind() {
		final GL10 gl = glGraphics.getGL();
	    GL11 gl11 = null;
	    int offset_num;

		if ((gl instanceof GL11)) {
			gl11 = (GL11)gl;
			hasVbo = gl11.glIsBuffer(vboId[VBO_VERTEX]);	// 2013/09/07 追加(コンテキストの喪失に対応するため)
			if (!hasVbo)
				createVBO();	// VBOを試みる
		} else
			hasVbo = false;

		// 頂点座標設定
	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    if (hasVbo) {
			// 頂点配列の指定
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, vboId[VBO_VERTEX]);
			gl11.glVertexPointer(dim_num, GL10.GL_FLOAT, vertexSize, 0);
	    } else {
	    	vertexArray.position(0);
	    	gl.glVertexPointer(dim_num, GL10.GL_FLOAT, vertexSize, vertexArray);
	    }
		// 頂点色情報設定
	    if (hasColor) {
	        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		    if (hasVbo) {
		    	gl11.glColorPointer(COLOR_NUM, GL10.GL_FLOAT, vertexSize, dim_num * FLOAT_SZ);
		    } else {
		        vertexArray.position(dim_num);
		        gl.glColorPointer(COLOR_NUM, GL10.GL_FLOAT, vertexSize, vertexArray);
		    }
	    }
		// テクスチャ座標設定
	    if (hasTexCoord) {
	    	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    	offset_num = dim_num + (hasColor ? COLOR_NUM : 0);
		    if (hasVbo) {
	    		gl11.glTexCoordPointer(COORD_NUM, GL10.GL_FLOAT, vertexSize, offset_num * FLOAT_SZ);
		    } else {
		    	vertexArray.position(offset_num);
		    	gl.glTexCoordPointer(COORD_NUM, GL10.GL_FLOAT, vertexSize, vertexArray);
		    }
	    }
	    // 頂点法線設定
	    if (hasNormals) {
	    	gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
	    	offset_num = dim_num + (hasColor ? COLOR_NUM : 0) + (hasTexCoord ? COORD_NUM : 0);
		    if (hasVbo) {
		    	gl11.glNormalPointer(GL10.GL_FLOAT, vertexSize, offset_num * FLOAT_SZ);
		    } else {
		    	vertexArray.position(offset_num);
		    	gl.glNormalPointer(GL10.GL_FLOAT, vertexSize, vertexArray);
		    }
	    }
	    // 頂点インデックス
	    if ((indexArray != null) && (hasVbo)) {
			gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vboId[VBO_INDEX]);
	    }

	}

	public void unbind() {
		final GL10 gl = glGraphics.getGL();
	    if (hasTexCoord)
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	    if (hasColor)
	        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

	    if (hasNormals)
	        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

		if ((gl instanceof GL11) && hasVbo) {
			final GL11 gl11 = (GL11)gl;
    		gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    		if (indexArray != null)
    			gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
    	}
	}

	protected void draw(final int primitiveType, final int offset, final int numVertex) {
		final GL10 gl = glGraphics.getGL();
	    GL11 gl11 = null;

		if ((gl instanceof GL11)) {
			gl11 = (GL11)gl;
		} else
			hasVbo = false;

	    if (indexArray != null) {
			// 頂点インデックス設定
	    	if (hasVbo) {
				gl11.glDrawElements(primitiveType, numVertex, GL10.GL_UNSIGNED_SHORT, offset);
	    	} else {
	    		indexArray.position(offset);
	    		gl.glDrawElements(primitiveType, numVertex, GL10.GL_UNSIGNED_SHORT, indexArray);
	    	}
	    } else {
	    	if (hasVbo) {
	    		gl11.glDrawArrays(primitiveType, offset, numVertex);
	    	} else {
	    		gl.glDrawArrays(primitiveType, offset, numVertex);
	    	}
	    }
	}

	public void draw(final int primitiveType) {
	    if (indexArray != null)
	    	draw(primitiveType, 0, getNumIndex());
	    else
		   	draw(primitiveType, 0, getNumVertex());
	}

	public void resume() {
		createVBO();
	}

	public void pause() {
		destroyVBO();
	}

	// 頂点インデックスの数を返す
	protected int getNumIndex() {
		return indexArray.limit();
	}

	// 頂点座標の数を返す
	protected int getNumVertex() {
		return vertexArray.limit() / (vertexSize / FLOAT_SZ);
	}

	protected void createVBO() {
		final GL10 gl = glGraphics.getGL();

	    destroyVBO();

		if (!(gl instanceof GL11)) {
			return;
		}
		final GL11 gl11 = (GL11)gl;

		// VBO ID 配列の生成
		gl11.glGenBuffers(vboId.length, vboId, 0);

		// データの転送
		if (vboId[VBO_VERTEX] != 0) {
			// Vertexデータの転送
			vertexArray.position(0);
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, vboId[VBO_VERTEX]);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexArray.limit() * INT_SZ, vertexArray, GL11.GL_STATIC_DRAW);
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

			// TriangleVertexIndexデータの転送
			if (indexArray != null) {
				indexArray.position(0);
				gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vboId[VBO_INDEX]);
				gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexArray.limit() * SHORT_SZ, indexArray, GL11.GL_STATIC_DRAW);
				gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
			hasVbo = true;
		}
	}

	protected void destroyVBO() {
	    hasVbo = false;

	    final GL10 gl = glGraphics.getGL();

		if (vboId[VBO_VERTEX] == 0)
			return;
		if (!(gl instanceof GL11))
			return;

		final GL11 gl11 = (GL11)gl;

		gl11.glDeleteBuffers(vboId.length, vboId, 0);
		Arrays.fill(vboId, 0);
	}

}
