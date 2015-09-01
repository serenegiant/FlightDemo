package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;

import javax.microedition.khronos.opengles.GL10;

public class GLSphereModel extends GLPolygonModel {

	public GLSphereModel(final GLGraphics glGraphics, final Vector offset, final float radius, final int num_theta, final int num_phi) {
		super(glGraphics, offset);
		final double d_theta = 2 * Math.PI / num_theta;
		final double d_phi = Math.PI / num_phi;
		
		// 頂点法線を設定しないとライティングが正しくできないみたい
		final int num = (num_phi - 1) * num_theta;
		final float[] vertices = new float[(2 + num) * Vertex.DIM_3D * 2];
		int pt = 0;
		double r, y;
		Vector work = new Vector();
		for (int i = 0; i < num_phi + 1; i++) {
			y = -radius * Math.cos(i * d_phi);
			r = Math.sqrt(radius * radius - y * y);
			if (i == 0) {					// 一番上
				// 頂点座標
				vertices[pt++] = 0;			// x
				vertices[pt++] = -radius;	// y
				vertices[pt++] = 0;			// z
				// 頂点法線ベクトル
				work.set(0,  -radius, 0).normalize();	// 頂点座標を正規化
				vertices[pt++] = work.x;	// x
				vertices[pt++] = work.y;	// y
				vertices[pt++] = work.z;	// z
			} else if (i == num_phi) { 		// 底部
				// 頂点座標
				vertices[pt++] = 0;			// x
				vertices[pt++] = radius;	// y
				vertices[pt++] = 0;			// z
				// 頂点法線ベクトル
				work.set(0, radius, 0).normalize();	// 頂点座標を正規化
				vertices[pt++] = work.x;	// x
				vertices[pt++] = work.y;	// y
				vertices[pt++] = work.z;	// z
			} else {
				for (int j = 0; j < num_theta; j++) {
					vertices[pt  ] = (float)(r * Math.cos(j * d_theta));
					vertices[pt+1] = (float)y;
					vertices[pt+2] = (float)(r * Math.sin(j * d_theta));
					// 頂点法線ベクトル
					work.set(vertices[pt], vertices[pt+1], vertices[pt+2]).normalize();	// 頂点座標を正規化
					vertices[pt+3] = work.x;	// x
					vertices[pt+4] = work.y;	// y
					vertices[pt+5] = work.z;	// z
					pt += 6;
				}
			}
		}
		final short[] indices = new short[num * 2 * Vertex.DIM_3D];
		pt = 0;
		int m;
		for (int i = 0; i < num_phi; i++) {
			m = (i - 1) * num_theta;
			for (int j = 0; j < num_theta; j++) {
				if (i == 0) {			//  一番上
					indices[pt++] = 0;
					indices[pt++] = (short)((j + 1) % num_theta + 1);
					indices[pt++] = (short)(j + 1);					
				} else if (i == num_phi - 1) { // 底部
					indices[pt++] = (short)(j + 1 + m);
					indices[pt++] = (short)((j + 1 + m) % num_theta + 1 + m);
					indices[pt++] = (short)(1 + m + num_theta);					
				} else {
					indices[pt++] = (short)(j + 1 + m);
					indices[pt++] = (short)((j + 1) % num_theta + 1 + m);
					indices[pt++] = (short)(j + 1 + m + num_theta);
					
					indices[pt++] = (short)((j + 1) % num_theta + 1 + m + num_theta);
					indices[pt++] = (short)(j + 1 + m + num_theta);
					indices[pt++] = (short)((j + 1) % num_theta + 1 + m);
				}
			}
		}
//		mVertex = new Vertex(Vertex.DIM_3D, glGraphics, 2 + num, indices.length);
		mVertex = new Vertex(Vertex.DIM_3D, glGraphics, vertices.length, indices.length, false, false, true);
		mVertex.setVertex(vertices, 0, vertices.length);
		mVertex.setIndexs(indices, 0, indices.length);

	}

	@Override
	public void draw() {
		final GL10 gl = glGraphics.getGL();
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		// スムーズシェーディング
		gl.glShadeModel(GL10.GL_SMOOTH);
		super.draw();
	}

}
