package com.serenegiant.mediaeffect;

import android.opengl.GLES20;

import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.glutils.TextureOffscreen;

public class MediaEffectGLESTwoPassBase extends MediaEffectGLESBase {

	protected FullFrameRect mDrawer2;
	protected TextureOffscreen mOutputOffscreen2;

	public MediaEffectGLESTwoPassBase(final String shader) {
		super(shader);
		mDrawer2 = null;
	}

	public MediaEffectGLESTwoPassBase(final String vss, final String fss) {
		super(vss, fss);
		mDrawer2 = null;
	}

	public MediaEffectGLESTwoPassBase(final String vss1, final String fss1, final String vss2, final String fss2) {
		super(vss1, fss1);
		mDrawer2 = new FullFrameRect(new Texture2dProgram(GLES20.GL_TEXTURE_2D, vss2, fss2));
	}

	@Override
	public void release() {
		if (mDrawer2 != null) {
			mDrawer2.release();
			mDrawer2 = null;
		}
		if (mOutputOffscreen2 != null) {
			mOutputOffscreen2.release();
			mOutputOffscreen2 = null;
		}
		super.release();
	}

	@Override
	public MediaEffectGLESBase resize(final int width, final int height) {
		super.resize(width, height);
		if ((mOutputOffscreen2 == null) || (width != mOutputOffscreen2.getWidth())
			|| (height != mOutputOffscreen2.getHeight())) {
			if (mOutputOffscreen2 != null)
				mOutputOffscreen2.release();
			mOutputOffscreen2 = new TextureOffscreen(width, height, false);
		}
		if (mDrawer2 != null) {
			mDrawer2.getProgram().setTexSize(width, height);
		}
		return this;
	}

	/**
	 * If you know the source texture came from MediaSource,
	 * using #apply(MediaSource) is much efficient instead of this
	 * @param src_tex_ids
	 * @param width
	 * @param height
	 * @param out_tex_id
	 */
	@Override
	public void apply(final int [] src_tex_ids, final int width, final int height, final int out_tex_id) {
		if (!mEnabled) return;

		if (mOutputOffscreen == null) {
			mOutputOffscreen = new TextureOffscreen(width, height, false);
		}
		mOutputOffscreen.bind();
		mDrawer.draw(src_tex_ids[0], mOutputOffscreen.getTexMatrix(), 0);
		mOutputOffscreen.unbind();

		if (mOutputOffscreen2 == null) {
			mOutputOffscreen2 = new TextureOffscreen(width, height, false);
		}
		if ((out_tex_id != mOutputOffscreen2.getTexture())
			|| (width != mOutputOffscreen2.getWidth())
			|| (height != mOutputOffscreen2.getHeight())) {
			mOutputOffscreen2.assignTexture(out_tex_id, width, height);
		}
		mOutputOffscreen2.bind();
		if (mDrawer2 != null) {
			mDrawer2.draw(mOutputOffscreen.getTexture(), mOutputOffscreen2.getTexMatrix(), 0);
		} else {
			mDrawer.draw(mOutputOffscreen.getTexture(), mOutputOffscreen2.getTexMatrix(), 0);
		}
		mOutputOffscreen2.unbind();
	}

	@Override
	public void apply(final ISource src) {
		if (!mEnabled) return;
		if (src instanceof MediaSource) {
			final TextureOffscreen output_tex = ((MediaSource)src).getOutputTexture();
			final int[] src_tex_ids = src.getSourceTexId();
			final int width = src.getWidth();
			final int height = src.getHeight();
			if (mOutputOffscreen == null) {
				mOutputOffscreen = new TextureOffscreen(width, height, false);
			}
			mOutputOffscreen.bind();
			mDrawer.draw(src_tex_ids[0], mOutputOffscreen.getTexMatrix(), 0);
			mOutputOffscreen.unbind();

			output_tex.bind();
			if (mDrawer2 != null) {
				mDrawer2.draw(mOutputOffscreen.getTexture(), output_tex.getTexMatrix(), 0);
			} else {
				mDrawer.draw(mOutputOffscreen.getTexture(), output_tex.getTexMatrix(), 0);
			}
			output_tex.unbind();
		} else {
			apply(src.getSourceTexId(), src.getWidth(), src.getHeight(), src.getOutputTexId());
		}
	}
}
