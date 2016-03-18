package com.serenegiant.mediaeffect;

import android.graphics.Matrix;
import android.opengl.GLES20;
import android.util.Log;

import com.serenegiant.glutils.GLHelper;

public class MediaEffectTexProjection extends MediaEffectGLESBase {
	private static final boolean DEBUG = true;
	private static final String TAG = "MediaEffectTexProjection";

	private static class MediaEffectTexProjectionDrawer extends MediaEffectKernelDrawer {
		private float[] texMatrix2 = new float[9];
		private final int muTexMatrixLoc2;

		public MediaEffectTexProjectionDrawer(final String vss, final String fss) {
			super(false, vss, fss);
			muTexMatrixLoc2 = GLES20.glGetUniformLocation(getProgram(), "uTexMatrix2");
			GLHelper.checkLocation(muTexMatrixLoc2, "uTexMatrix2");
		}

		@Override
		protected void preDraw(final int tex_id, final float[] tex_matrix, final int offset) {
			super.preDraw(tex_id, tex_matrix, offset);
			// テクスチャ変換行列をセット
			GLES20.glUniformMatrix3fv(muTexMatrixLoc2, 1, false, texMatrix2, 0);
			GLHelper.checkGlError("glUniformMatrix3fv");
		}

		public void setTexProjection(final float[] matrix) {
			texMatrix2[0] = matrix[0];
			texMatrix2[1] = matrix[3];
			texMatrix2[2] = matrix[6];
			texMatrix2[3] = matrix[1];
			texMatrix2[4] = matrix[4];
			texMatrix2[5] = matrix[7];
			texMatrix2[6] = matrix[2];
			texMatrix2[7] = matrix[5];
			texMatrix2[8] = matrix[8];
		}
	}

	public static final String VERTEX_SHADER = MediaEffectDrawer.SHADER_VERSION +
		"uniform mat4 uMVPMatrix;\n" +		// モデルビュー変換行列
		"uniform mat4 uTexMatrix;\n" +		// テクスチャ変換行列
		"uniform mat3 uTexMatrix2;\n" +		// テクスチャ変換行列
		"attribute vec4 aPosition;\n" +		// 頂点座標
		"attribute vec4 aTextureCoord;\n" +	// テクスチャ情報
		"varying vec2 vTextureCoord;\n" +	// フラグメントシェーダーへ引き渡すテクスチャ座標
		"void main() {\n" +
			"gl_Position = uMVPMatrix * aPosition;\n" +
			"vTextureCoord = (uTexMatrix2 * aTextureCoord.xyz).xy;\n" +
		"}\n";

	private static final String FRAGMENT_SHADER_BASE = MediaEffectDrawer.SHADER_VERSION +
		"%s" +
		"#define KERNEL_SIZE " + MediaEffectKernelDrawer.KERNEL_SIZE + "\n" +
	"precision highp float;\n" +
		"varying       vec2 vTextureCoord;\n" +
		"uniform %s    sTexture;\n" +
		"uniform float uKernel[18];\n" +
		"uniform vec2  uTexOffset[KERNEL_SIZE];\n" +
		"uniform float uColorAdjust;\n" +
		"void main() {\n" +
			"gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
		"}\n";
	private static final String FRAGMENT_SHADER
		= String.format(FRAGMENT_SHADER_BASE, MediaEffectDrawer.HEADER_2D, MediaEffectDrawer.SAMPLER_2D);
	private static final String FRAGMENT_SHADER_EXT
		= String.format(FRAGMENT_SHADER_BASE, MediaEffectDrawer.HEADER_OES, MediaEffectDrawer.SAMPLER_OES);

	public MediaEffectTexProjection() {
		super(new MediaEffectTexProjectionDrawer(VERTEX_SHADER, FRAGMENT_SHADER));
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
	}

	/**
	 *
	 * @param src (x,y) pair, 4 pairs (4 points) = float[8]
	 * @param dst (x,y) pair, 4 pairs (4 points) = float[8]
	 */
	public void calcPerspectiveTransform(final float[] src, final float[] dst) {
		final Matrix mat = new Matrix();
		mat.setPolyToPoly(src, 0, dst, 0, 4);
		final float[] m = new float[9];
		mat.getValues(m);
		((MediaEffectTexProjectionDrawer)mDrawer).setTexProjection(m);
//		texMatrix2[0] = m[0];
//		texMatrix2[1] = m[3];
//		texMatrix2[2] = m[6];
//		texMatrix2[3] = m[1];
//		texMatrix2[4] = m[4];
//		texMatrix2[5] = m[7];
//		texMatrix2[6] = m[2];
//		texMatrix2[7] = m[5];
//		texMatrix2[8] = m[8];
/*		final double [][] mat = new double[8][8];
		final double[] v = new double[8];
		for (int i = 0; i < 4; i++) {
			final double x = src[i * 2];
			final double y = src[i * 2 + 1];
			final double s = dst[i * 2];
			final double t = dst[i * 2 + 1];
			mat[i][0] = mat[i+4][3] = x;
			mat[i][1] = mat[i+4][4] = y;
			mat[i][2] = mat[i+4][5] = 1;
			mat[i][3] = mat[i][4] = mat[i][5]
				= mat[i+4][0] = mat[i+4][1] = mat[i+4][2] = 0;
			mat[i][6] = -x * s;
			mat[i][7] = -y * s;
			mat[i+4][6] = -x * t;
			mat[i+4][7] = -y * t;
			v[i] = s;
			v[i+4] = t;
		}
		// solve...(--;)
		*/
	}

//	protected void internal_apply(final MediaEffectDrawer drawer, final int tex_id, final float[] tex_matrix, final int offset) {
//		// テクスチャ変換行列をセット
//		GLES20.glUniformMatrix3fv(muTexMatrixLoc2, 1, false, texMatrix2, 0);
//		GLHelper.checkGlError("glUniformMatrix3fv");
//		super.internal_draw(tex_id, tex_matrix, 0);
//	}

/* Calculates coefficients of perspective transformation
 * which maps (xi,yi) to (ui,vi), (i=1,2,3,4):
 *
 *      c00*xi + c01*yi + c02
 * ui = ---------------------
 *      c20*xi + c21*yi + c22
 *
 *      c10*xi + c11*yi + c12
 * vi = ---------------------
 *      c20*xi + c21*yi + c22
 *
 * Coefficients are calculated by solving linear system:
 * / x0 y0  1  0  0  0 -x0*u0 -y0*u0 \ /c00\ /u0\
 * | x1 y1  1  0  0  0 -x1*u1 -y1*u1 | |c01| |u1|
 * | x2 y2  1  0  0  0 -x2*u2 -y2*u2 | |c02| |u2|
 * | x3 y3  1  0  0  0 -x3*u3 -y3*u3 |.|c10|=|u3|,
 * |  0  0  0 x0 y0  1 -x0*v0 -y0*v0 | |c11| |v0|
 * |  0  0  0 x1 y1  1 -x1*v1 -y1*v1 | |c12| |v1|
 * |  0  0  0 x2 y2  1 -x2*v2 -y2*v2 | |c20| |v2|
 * \  0  0  0 x3 y3  1 -x3*v3 -y3*v3 / \c21/ \v3/
 *
 * where:
 *   cij - matrix coefficients, c22 = 1
 */
/*
cv::Mat cv::getPerspectiveTransform( const Point2f src[], const Point2f dst[] )
{
    Mat M(3, 3, CV_64F), X(8, 1, CV_64F, M.ptr());
    double a[8][8], b[8];
    Mat A(8, 8, CV_64F, a), B(8, 1, CV_64F, b);

    for( int i = 0; i < 4; ++i )
    {
        a[i][0] = a[i+4][3] = src[i].x;
        a[i][1] = a[i+4][4] = src[i].y;
        a[i][2] = a[i+4][5] = 1;
        a[i][3] = a[i][4] = a[i][5] =
        a[i+4][0] = a[i+4][1] = a[i+4][2] = 0;
        a[i][6] = -src[i].x*dst[i].x;
        a[i][7] = -src[i].y*dst[i].x;
        a[i+4][6] = -src[i].x*dst[i].y;
        a[i+4][7] = -src[i].y*dst[i].y;
        b[i] = dst[i].x;
        b[i+4] = dst[i].y;
    }

    solve( A, B, X, DECOMP_SVD );
    M.ptr<double>()[8] = 1.;

    return M;
}
*/
}
