package com.serenegiant.widget.gl;

import android.graphics.Rect;

public class TextureRegion {
	public final float u1, v1;
	public final float u2, v2;
	public final float width, height;
	public final Texture texture;
	
	public TextureRegion(final Texture texture, final float x, final float y, final float width, final float height) {
		this.width = width;
		this.height = height;
		this.u1 = x / texture.width;
		this.v1 = y / texture.height;
		this.u2 = this.u1 + width / texture.width;
		this.v2 = this.v1 + height / texture.height;
		this.texture = texture;
	}
	
	public Rect makeBoundsRect(final float center_x, final float center_y) {
		return makeBoundsRect(center_x, center_y, 1f, 1f);
	}

	public Rect makeBoundsRect(final float center_x, final float center_y, final float a) {
		return makeBoundsRect(center_x, center_y, a, a);
	}

	public Rect makeBoundsRect(final float center_x, final float center_y, final float ax, final float bx) {
		final float w = width * ax / 2f;
		final float h = height * bx / 2f;
		final Rect rect = new Rect(
			(int)(center_x - w), (int)(center_y - h),
			(int)(center_x + w), (int)(center_y + h)
		);
		return rect;
	}

}
