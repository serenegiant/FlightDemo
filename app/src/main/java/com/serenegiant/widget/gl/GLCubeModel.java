package com.serenegiant.widget.gl;


import com.serenegiant.math.Vector;

// 立方体オブジェクトクラス
public class GLCubeModel extends GLPolygonModel {
	
	public GLCubeModel(final GLGraphics glGraphics, final float scale, final int texMapNum) {
		this(glGraphics, Vector.zeroVector, scale, scale, scale, texMapNum);
	}

	public GLCubeModel(final GLGraphics glGraphics, final Vector offset, final float scale, final int texMapNum) {
		this(glGraphics, offset, scale, scale, scale, texMapNum);
	}

	public GLCubeModel(final GLGraphics glGraphics, final Vector offset, final int texMapNum) {
		this(glGraphics, Vector.zeroVector, 1f, 1f, 1f, texMapNum);
	}

	public GLCubeModel(final GLGraphics glGraphics, final int texMapNum) {
		this(glGraphics, Vector.zeroVector, 1f, 1f, 1f, texMapNum);
	}

	public GLCubeModel(final GLGraphics glGraphics, final Vector offset,
		final float width, final float height, final float depth) {
		
		this(glGraphics, offset, width, height, depth, 0);
	}
	
	public GLCubeModel(final GLGraphics glGraphics, final Vector offset,
		final float width, final float height, final float depth, final int texMapNum) {

		super(glGraphics, offset);
		final float x = width / 2;
		final float y = height / 2;
		final float z = depth / 2;
		
		final float[] vertices;
		final short[] indices;
		int vertex_size;	// 頂点情報のサイズ 頂点法線ベクトル計算用  2013/05/19
		int norm_offset;	// 頂点法線ベクトルの先頭オフセット 頂点法線ベクトル計算用  2013/05/19
		
		final boolean hasTexture = (texMapNum > 0);
		if (hasTexture) {	// テクスチャがある時
			vertices = new float[] {
				// 頂点座標,  テクスチャ座標,			頂点法線ベクトル(ダミー)
				-x, -y,  z, 0, texMapNum,			0, 0, 0,
				 x, -y,  z, texMapNum, texMapNum,	0, 0, 0,
				 x,  y,  z, texMapNum, 0,			0, 0, 0,
				-x,  y,  z, 0, 0,					0, 0, 0,
				
				 x, -y,  z, 0, texMapNum,			0, 0, 0,
				 x, -y, -z, texMapNum, texMapNum,	0, 0, 0,
				 x,  y, -z, texMapNum, 0,			0, 0, 0,
				 x,  y,  z, 0, 0,					0, 0, 0,
				 
				 x, -y, -z, 0, texMapNum,			0, 0, 0,
				-x, -y, -z, texMapNum, texMapNum,	0, 0, 0,
				-x,  y, -z, texMapNum, 0,			0, 0, 0,
				 x,  y, -z, 0, 0,					0, 0, 0,
				 
				-x, -y, -z, 0, texMapNum,			0, 0, 0,
				-x, -y,  z, texMapNum, texMapNum,	0, 0, 0,
				-x,  y,  z, texMapNum, 0,			0, 0, 0,
				-x,  y, -z, 0, 0,					0, 0, 0,
				
				-x,  y,  z, 0, texMapNum,			0, 0, 0,
				 x,  y,  z, texMapNum, texMapNum,	0, 0, 0,
				 x,  y, -z, texMapNum, 0,			0, 0, 0,
				-x,  y, -z, 0, 0,					0, 0, 0,
				
				-x, -y,  z, 0, texMapNum,			0, 0, 0,
				 x, -y,  z, texMapNum, texMapNum,	0, 0, 0,
				 x, -y, -z, texMapNum, 0,			0, 0, 0,
				-x, -y, -z, 0, 0,					0, 0, 0,
			};
			vertex_size = 8;	// 頂点法線ベクトル計算用  2013/05/19
			norm_offset = 5;	// 頂点法線ベクトル計算用  2013/05/19
			indices = new short[] {
				0, 1, 3, 1, 2, 3,
				4, 5, 7, 5, 6, 7,
				8, 9, 11, 9, 10, 11,
				12, 13, 15, 13, 14, 15,
				16, 17, 19, 17, 18, 19,
				20, 21, 23, 21, 22, 23
			};
//			mVertex = new Vertex(Vertex.DIM_3D, glGraphics, vertices.length, indices.length, false, true);
		} else {	// テクスチャがない時
/*
			vertices = new float[] {
					-x, -y,  z,
					 x, -y,  z,
					 x,  y,  z,
					-x,  y,  z,
					
					 x, -y,  z,
					 x, -y, -z,
					 x,  y, -z,
					 x,  y,  z,
					 
					 x, -y, -z,
					-x, -y, -z,
					-x,  y, -z,
					 x,  y, -z,
					 
					-x, -y, -z,
					-x, -y,  z,
					-x,  y,  z,
					-x,  y, -z,
					
					-x,  y,  z,
					 x,  y,  z,
					 x,  y, -z,
					-x,  y, -z,
					
					-x, -y,  z,
					 x, -y,  z,
					 x, -y, -z,
					-x, -y, -z,
				};
			indices = new short[] {
					0, 1, 3, 1, 2, 3,
					4, 5, 7, 5, 6, 7,
					8, 9, 11, 9, 10, 11,
					12, 13, 15, 13, 14, 15,
					16, 17, 19, 17, 18, 19,
					20, 21, 23, 21, 22, 23
				};
*/
			vertices = new float[] {
				// 頂点座標	頂点法線ベクトル(ダミー)
				-x, -y, -z, 0, 0, 0,
				 x, -y, -z, 0, 0, 0,
				 x,  y, -z, 0, 0, 0,
				-x,  y, -z, 0, 0, 0,
				-x, -y,  z, 0, 0, 0,
				 x, -y,  z, 0, 0, 0,
				 x,  y,  z, 0, 0, 0,
				-x,  y,  z, 0, 0, 0,
			};
			indices = new short[] {
				0, 4, 5, 0, 5, 1,
				1, 5, 6, 1, 6, 2,
				2, 6, 7, 2, 7, 3,
				3, 7, 4, 3, 4, 0,
				4, 7, 6, 4, 6, 5,
				3, 0, 1, 3, 1, 2,
			};
			vertex_size = 6;	// 頂点法線ベクトル計算用  2013/05/19
			norm_offset = 3;	// 頂点法線ベクトル計算用  2013/05/19

//			mVertex = new Vertex(Vertex.DIM_3D, glGraphics, vertices.length, indices.length);
		}
		mVertex = new Vertex(Vertex.DIM_3D, glGraphics, vertices.length, indices.length, false, hasTexture, true);
		// 頂点法線ベクトル(=頂点座標を正規化)を計算 2013/05/19
		final int n = vertices.length;
		final Vector work = new Vector();
		for (int ix = 0; ix < n; ix += vertex_size) {
			work.set(vertices[ix], vertices[ix + 1], vertices[ix + 2]).normalize();
			vertices[ix + norm_offset    ] = work.x;
			vertices[ix + norm_offset + 1] = work.y;
			vertices[ix + norm_offset + 2] = work.z;
		}		
		mVertex.setVertex(vertices, 0, vertices.length);
		mVertex.setIndexs(indices, 0, indices.length);
		
	}
}
