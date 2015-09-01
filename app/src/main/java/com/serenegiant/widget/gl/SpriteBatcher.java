package com.serenegiant.widget.gl;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Rect;
import android.graphics.RectF;

import com.serenegiant.math.RectangleBounds;
import com.serenegiant.math.Vector;

public class SpriteBatcher {
	private final float[] vertexBuffer;
	private final Vertex vertex;
	private int bufferIndex;
	private int numSprite;
	
	public SpriteBatcher(GLGraphics glGraphics, int maxSprites) {
		vertexBuffer = new float[maxSprites * 4 * 4];
		vertex = new Vertex(Vertex.DIM_2D, glGraphics, maxSprites * 4, maxSprites * 6, false, true, false);
		bufferIndex = 0;
		numSprite = 0;
		
		final short[] index = new short[maxSprites * 6];
		final int len = index.length;
		short j = 0;
		for (int i = 0; i < len; i+= 6, j+= 4) {
			index[i + 0] = (short)(j + 0);
			index[i + 1] = (short)(j + 1);
			index[i + 2] = (short)(j + 2);
			index[i + 3] = (short)(j + 2);
			index[i + 4] = (short)(j + 3);
			index[i + 5] = (short)(j + 0);
		}
		vertex.setIndexs(index, 0, index.length);
	}
	
	public void beginBatch(Texture texture) {
		texture.bind();
		numSprite = 0;
		bufferIndex = 0;
	}
	
	public void endBatch() {
		vertex.setVertex(vertexBuffer, 0, bufferIndex);
		vertex.bind();
		vertex.draw(GL10.GL_TRIANGLES, 0, numSprite * 6);
		vertex.unbind();
	}
	
//--------------------------------------------------------------------------------
	public void drawSprite(RectangleBounds bounds, TextureRegion region) {
		drawSprite(bounds.position.x, bounds.position.y, bounds.box.x * 2f, bounds.box.y * 2f, region);
	}

	public void drawSprite(Rect bounds, TextureRegion region) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width, region.height, region);
	}

	public void drawSprite(RectF bounds, TextureRegion region) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width, region.height, region);
	}
//--------------------------------------------------------------------------------
	public void drawSprite(RectangleBounds bounds, TextureRegion region, float aspect) {
		drawSprite(bounds.position.x, bounds.position.y, region.width * aspect, region.height * aspect, region);
	}

	public void drawSprite(Rect bounds, TextureRegion region, float aspect) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width * aspect, region.height * aspect, region);
	}

	public void drawSprite(RectF bounds, TextureRegion region, float aspect) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width * aspect, region.height * aspect, region);
	}
//--------------------------------------------------------------------------------
	public void drawSprite(RectangleBounds bounds, TextureRegion region, float aspect_width, float aspect_height) {
		drawSprite(bounds.position.x, bounds.position.y, region.width * aspect_width, region.height * aspect_height, region);
	}

	public void drawSprite(Rect bounds, TextureRegion region, float aspect_width, float aspect_height) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width * aspect_width, region.height * aspect_height, region);
	}

	public void drawSprite(RectF bounds, TextureRegion region, float aspect_width, float aspect_height) {
		drawSprite(bounds.centerX(), bounds.centerY(), region.width * aspect_width, region.height * aspect_height, region);
	}
//--------------------------------------------------------------------------------
	public void drawSprite(float center_x, float center_y, TextureRegion region) {
		drawSprite(center_x, center_y, region.width, region.height, region);
	}

	public void drawSprite(float center_x, float center_y, TextureRegion region, float a) {
		drawSprite(center_x, center_y, region.width * a, region.height * a, region);
	}

	public void drawSprite(float center_x, float center_y, TextureRegion region, float aspect_width, float aspect_height) {
		drawSprite(center_x, center_y, region.width * aspect_width, region.height * aspect_height, region);
	}
//--------------------------------------------------------------------------------
	// 回転角付き
	public void drawSprite(float center_x, float center_y, float angle, TextureRegion region) {
		if (angle == 0) {
			drawSprite(center_x, center_y, region.width, region.height, region);
			return;
		}
		drawSprite(center_x, center_y, region.width, region.height, angle, region);
	}

	public void drawSprite(float center_x, float center_y, float angle, TextureRegion region, float a) {
		if (angle == 0) {
			drawSprite(center_x, center_y, region.width * a, region.height * a, region);
			return;
		}
		drawSprite(center_x, center_y, region.width * a, region.height * a, angle, region);
	}

	public void drawSprite(float center_x, float center_y, float angle, TextureRegion region, float aspect_width, float aspect_height) {
		if (angle == 0) {
			drawSprite(center_x, center_y, region.width * aspect_width, region.height * aspect_height, region);
			return;
		}
		drawSprite(center_x, center_y, region.width * aspect_width, region.height * aspect_height, angle, region);
	}
//--------------------------------------------------------------------------------
	public void drawSprite(float center_x, float center_y, float width, float height, TextureRegion region) {
		final float halfWidth = width / 2;
		final float halfHeight = height / 2;
		
		final float x1 = center_x - halfWidth;
		final float y1 = center_y - halfHeight;
		final float x2 = center_x + halfWidth;
		final float y2 = center_y + halfHeight;
		
		vertexBuffer[bufferIndex++] = x1;
		vertexBuffer[bufferIndex++] = y1;
		vertexBuffer[bufferIndex++] = region.u1;
		vertexBuffer[bufferIndex++] = region.v2;

		vertexBuffer[bufferIndex++] = x2;
		vertexBuffer[bufferIndex++] = y1;
		vertexBuffer[bufferIndex++] = region.u2;
		vertexBuffer[bufferIndex++] = region.v2;

		vertexBuffer[bufferIndex++] = x2;
		vertexBuffer[bufferIndex++] = y2;
		vertexBuffer[bufferIndex++] = region.u2;
		vertexBuffer[bufferIndex++] = region.v1;

		vertexBuffer[bufferIndex++] = x1;
		vertexBuffer[bufferIndex++] = y2;
		vertexBuffer[bufferIndex++] = region.u1;
		vertexBuffer[bufferIndex++] = region.v1;
		
		numSprite++;
	}

//--------------------------------------------------------------------------------
	// 回転角付き
	public void drawSprite(float center_x, float center_y, float width, float height, float angle, TextureRegion region) {
		if (angle == 0) {
			drawSprite(center_x, center_y, width, height, region);
			return;
		}
		final float halfWidth = width / 2;
		final float halfHeight = height / 2;
		final float radian = angle * Vector.TO_RADIAN;
		final float cos = (float)Math.cos(radian);
		final float sin = (float)Math.sin(radian);
		
		final float x1 = -halfWidth * cos - (-halfHeight) * sin + center_x;
		final float y1 = -halfWidth * sin + (-halfHeight) * cos + center_y;
		final float x2 = halfWidth * cos - (-halfHeight) * sin + center_x;
		final float y2 = halfWidth * sin + (-halfHeight) * cos + center_y;
		final float x3 = halfWidth * cos - halfHeight * sin + center_x;
		final float y3 = halfWidth * sin + halfHeight * cos + center_y;
		final float x4 = -halfWidth * cos - halfHeight * sin + center_x;
		final float y4 = -halfWidth * sin + halfHeight * cos + center_y;
		
		vertexBuffer[bufferIndex++] = x1;
		vertexBuffer[bufferIndex++] = y1;
		vertexBuffer[bufferIndex++] = region.u1;
		vertexBuffer[bufferIndex++] = region.v2;

		vertexBuffer[bufferIndex++] = x2;
		vertexBuffer[bufferIndex++] = y2;
		vertexBuffer[bufferIndex++] = region.u2;
		vertexBuffer[bufferIndex++] = region.v2;

		vertexBuffer[bufferIndex++] = x3;
		vertexBuffer[bufferIndex++] = y3;
		vertexBuffer[bufferIndex++] = region.u2;
		vertexBuffer[bufferIndex++] = region.v1;

		vertexBuffer[bufferIndex++] = x4;
		vertexBuffer[bufferIndex++] = y4;
		vertexBuffer[bufferIndex++] = region.u1;
		vertexBuffer[bufferIndex++] = region.v1;
		
		numSprite++;
	}

}
