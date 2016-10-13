package com.serenegiant.math;

import android.graphics.Rect;

import com.serenegiant.gameengine.v1.TextureRegion;

// 四角オブジェクト(2D)
public class RectangleBoundsEx extends RectangleBounds {
	/**
	 * 
	 */
	private static final long serialVersionUID = 260429282595037220L;

	/**
	 * コンストラクタ 中心座標と幅・高さを指定して生成
	 * @param center_x
	 * @param center_y
	 * @param center_z
	 * @param width
	 * @param height
	 * @param depth
	 */
	public RectangleBoundsEx(final float center_x, final float center_y, final float center_z, final float width, final float height, final float depth) {
		super(center_x, center_y, center_z, width, height, depth);
	}

	/**
	 * コンストラクタ 中心座標と幅・高さを指定して生成
	 * @param center_x
	 * @param center_y
	 * @param width
	 * @param height
	 */
	public RectangleBoundsEx(final float center_x, final float center_y, final float width, final float height) {
		super(center_x, center_y, 0f, width, height, 0f);
	}
	
	/**
	 * コンストラクタ 中心座標と幅・高さを指定して生成
	 * @param center
	 * @param width
	 * @param height
	 */
	public RectangleBoundsEx(final Vector center, final float width, final float height) {
		super(center.x, center.y, center.z, width, height, 0f);
	}
	
	/**
	 *  コンストラクタ 左下と右上の座標を指定して生成
	 * @param lowerLeft		左下座標
	 * @param upperRight	右上座標
	 */
	public RectangleBoundsEx(final Vector lowerLeft, final Vector upperRight) {
		super(lowerLeft, upperRight);
	}
	
	/**
	 * コンストラクタ 中心座標とTextureRegion, スケールファクタを指定
	 * @param center_x
	 * @param center_y
	 * @param region
	 * @param a
	 */
	public RectangleBoundsEx(final float center_x, final float center_y, final TextureRegion region, final float a) {
		super(center_x, center_y, region.width * a, region.height * a);
	}
	
	/**
	 * コンストラクタ 中心座標とTextureRegion, スケールファクタを指定
	 * @param center_x
	 * @param center_y
	 * @param region
	 * @param ax widthのスケールファクタ
	 * @param bx heightのスケールファクタ
	 */
	public RectangleBoundsEx(final float center_x, final float center_y, final TextureRegion region, final float ax, final float bx) {
		super(center_x, center_y, region.width * ax, region.height * bx);
	}

	/**
	 * コンストラクタ 中心座標とTextureRegionを指定(スケールファクタ=1)
	 * @param center_x
	 * @param center_y
	 * @param region
	 */
	public RectangleBoundsEx(final float center_x, final float center_y, final TextureRegion region) {
		super(center_x, center_y, region.width, region.height);
	}

	/**
	 * コンストラクタ 外形枠を指定
	 * @param rect
	 */
	public RectangleBoundsEx(final Rect rect) {
		super(rect.centerX(), rect.centerY(), rect.width(), rect.height());
	}

}
