package com.serenegiant.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.serenegiant.flightdemo.R;

public class MediaStoreAdapter extends CursorAdapter {

	private static final boolean DEBUG = false;	// TODO set false when releasing
	private static final String TAG = MediaStoreAdapter.class.getSimpleName();

	public static final int MEDIA_ALL = 0;
	public static final int MEDIA_IMAGE = 1;
	public static final int MEDIA_VIDEO = 2;
	private static final int MEDIA_TYPE_NUM = 3;

    // for thread pool
    private static final int CORE_POOL_SIZE = 4;		// initial/minimum threads
    private static final int MAX_POOL_SIZE = 32;		// maximum threads
    private static final int KEEP_ALIVE_TIME = 10;		// time periods while keep the idle thread
    private static final ThreadPoolExecutor EXECUTER
		= new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	// for thumbnail cache(in memory)
	// rate of memory usage for cache, 'CACHE_RATE = 8' means use 1/8 of available memory for image cache
	private static final int CACHE_RATE = 8;
	private static LruCache<Long, Bitmap> sThumbnailCache;

	private static int mThumbnailWidth = 200, mThumbnailHeight = 200;

	private static final String[] PROJ_MEDIA = {
		MediaStore.Files.FileColumns._ID,				// index=0 for Cursor, column number=1 in SQL statement
		MediaStore.Files.FileColumns.TITLE,				// index=1 for Cursor, column number=2 in SQL statement
		MediaStore.Files.FileColumns.MEDIA_TYPE,		// index=2 for Cursor, column number=2 in SQL statement
		// MEDIA_TYPE_NONE, MEDIA_TYPE_IMAGE, MEDIA_TYPE_AUDIO, MEDIA_TYPE_VIDEO, MEDIA_TYPE_PLAYLIST
		MediaStore.Files.FileColumns.MIME_TYPE,			// index=3 for Cursor, column number=2 in SQL statement
		MediaStore.Files.FileColumns.DATA,				// index=4 for Cursor, column number=2 in SQL statement
		MediaStore.Files.FileColumns.DISPLAY_NAME,		// index=5 for Cursor, column number=2 in SQL statement
//		MediaStore.Files.FileColumns.DATE_MODIFIED,		// index=6 for Cursor, column number=2 in SQL statement
//		MediaStore.Files.FileColumns.DATE_ADDED,		// index=7 for Cursor, column number=2 in SQL statement
	};
	
	private static final String SELECTION_MEDIA_ALL
		= MediaStore.Files.FileColumns.MEDIA_TYPE + "="
		+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
		+ " OR "
		+ MediaStore.Files.FileColumns.MEDIA_TYPE + "="
		+ MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

	private static final String SELECTION_MEDIA_IMAGE
		= MediaStore.Files.FileColumns.MEDIA_TYPE + "="
		+ MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

	private static final String SELECTION_MEDIA_VIDEO
		= MediaStore.Files.FileColumns.MEDIA_TYPE + "="
		+ MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

	// MEDIA_ALL, MEDIA_IMAGE, MEDIA_VIDEOの順にすること
	private static final String[] SELECTIONS = {SELECTION_MEDIA_ALL, SELECTION_MEDIA_IMAGE, SELECTION_MEDIA_VIDEO};

	// these values should be fit to PROJ_MEDIA
	private static final int PROJ_INDEX_ID = 0;
	private static final int PROJ_INDEX_TITLE = 1;
	private static final int PROJ_INDEX_MEDIA_TYPE = 2;
	private static final int PROJ_INDEX_MIME_TYPE = 3;
	private static final int PROJ_INDEX_DATA = 4;
	private static final int PROJ_INDEX_DISPLAY_NAME = 5;
//	private static final int PROJ_INDEX_DATE_MODIFIED = 6;
//	private static final int PROJ_INDEX_DATE_ADDED = 7;

	private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

	private final LayoutInflater mInflater;
	private final ContentResolver mCr;
	private final int mMemClass;
	private final int mLayoutId;
	private final MyAsyncQueryHandler mQueryHandler;
	private Cursor mMediaInfoCursor;
	private String mSelection;
	private String[] mSelectionArgs;
	private boolean mShowTitle;
	private int mMediaType = MEDIA_ALL;
	private final MediaInfo info = new MediaInfo();

	public static class MediaInfo {
		public String data;
		public String title;
		public String mime;
		public String displayName;
		public int mediaType;
	}
	
	public MediaStoreAdapter(final Context context, final int id_layout) {
		super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	    mInflater = LayoutInflater.from(context);
	    mCr = context.getContentResolver();
	    mQueryHandler = new MyAsyncQueryHandler(mCr, this);
		// getMemoryClass return the available memory amounts for app as mega bytes(API >= 5)
		mMemClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		mLayoutId = id_layout;
		onContentChanged();
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		// this method is called within UI thread and should return as soon as possible
		final View view = mInflater.inflate(mLayoutId, parent, false);
		getViewHolder(view);
		return view;
	}
	
	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		// this method is called within UI thread and should return as soon as possible
		final ViewHolder holder = getViewHolder(view);
		final ImageView iv = holder.mImageView;
		final TextView tv = holder.mTitleView;
		Drawable drawable = iv.getDrawable();
		if ((drawable == null) || !(drawable instanceof LoaderDrawable)) {
			drawable = new LoaderDrawable(mCr);
			iv.setImageDrawable(drawable);
		}
		((LoaderDrawable)drawable).startLoad(cursor.getLong(PROJ_INDEX_ID));
		if (tv != null) {
			tv.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);
			if (mShowTitle) {
				tv.setText(cursor.getString(PROJ_INDEX_TITLE));
			}
		}
	}

	private ViewHolder getViewHolder(final View view) {
		ViewHolder holder;
		// you can use View#getTag()/setTag() instead of using View#getTag(int)/setTag(int)
		// but we assume that using getTag(int)/setTag(int) and keeping getTag()/setTag() left for user is better.
		holder = (ViewHolder)view.getTag(R.id.mediastorephotoadapter);
		if (holder == null) {
			holder = new ViewHolder();
			if (view instanceof ImageView) {
				holder.mImageView = (ImageView)view;
				view.setTag(R.id.mediastorephotoadapter, holder);
			} else {
				View v = view.findViewById(R.id.thumbnail);
				if (v instanceof ImageView)
					holder.mImageView = (ImageView)v;
				v = view.findViewById(R.id.title);
				if (v instanceof TextView)
					holder.mTitleView = (TextView)v;
				view.setTag(R.id.mediastorephotoadapter, holder);
			}
		}
		return holder;
	}
	
	@Override
	protected void finalize() throws Throwable {
		changeCursor(null);
		if (mMediaInfoCursor != null) {
			mMediaInfoCursor.close();
			mMediaInfoCursor = null;
		}
		super.finalize();
	}

	@Override
	protected void onContentChanged() {
		createBitmapCache(false);
		mQueryHandler.requery();
	}

	/**
	 * return thumbnail image at specific position.
	 * this method is synchronously executed and may take time
	 * @return null if the position value is out of range etc. 
	 */
	@Override
	public Bitmap getItem(final int position) {
		Bitmap result = null;

		getMediaInfo(position, info);
		if (info.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
			try {
				result = getImageThumbnail(mCr, getItemId(position), mThumbnailWidth, mThumbnailHeight);
			} catch (final FileNotFoundException e) {
				Log.w(TAG, e);
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		} else if (info.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
			// FIXME 動画の場合のサムネイル取得は未実装
		}
		return result;
	}

	/**
	 * return image with specific size(only scale-down or original size are available now)
	 * if width=0 and height=0, return image with original size.
	 * this method is synchronously executed and may take time
	 * @return null if the position value is out of range etc. 
	 * @param position
	 * @param width
	 * @param height
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Bitmap getImage(final int position, final int width, final int height)
		throws FileNotFoundException, IOException {

		return getImage(mCr, getItemId(position), width, height);
	}
	
	/**
	 * get mediainfo at specified position
	 * @param position
	 * @return
	 */
	public MediaInfo getMediaInfo(final int position) {
		return getMediaInfo(position, null);
	}

	public synchronized MediaInfo getMediaInfo(final int position, final MediaInfo info) {
		final MediaInfo _info = info != null ? info : new MediaInfo();

/*		// if you don't need to frequently call this method, temporary query may be better to reduce memory usage.
		// but it will take more time.
		final Cursor cursor = mCr.query(
			ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, getItemId(position)),
			PROJ_IMAGE, mSelection, mSelectionArgs, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					info = readMediaInfo(cursor, new MediaInfo());
				}
			} finally {
				cursor.close();
			}
		} */
		if (mMediaInfoCursor == null) {
			mMediaInfoCursor = mCr.query(
				QUERY_URI, PROJ_MEDIA,
				mSelection, mSelectionArgs, null);
		}
		if (mMediaInfoCursor.moveToPosition(position)) {
			readMediaInfo(mMediaInfoCursor, _info);
		}
		return _info;
	}

	private static final MediaInfo readMediaInfo(final Cursor cursor, final MediaInfo info) {
		info.data = cursor.getString(PROJ_INDEX_DATA);
		info.title = cursor.getString(PROJ_INDEX_TITLE);
		info.mime = cursor.getString(PROJ_INDEX_MIME_TYPE);
		info.displayName = cursor.getString(PROJ_INDEX_DISPLAY_NAME);
		info.mediaType = cursor.getInt(PROJ_INDEX_MEDIA_TYPE);
		return info;
	}
	/**
	 * set thumbnail size, if you set size to zero, the size is 96x96(MediaStore.Images.Thumbnails.MICRO_KIND)
	 * @param size
	 */
	public void setThumbnailSize(final int size) {
		if ((mThumbnailWidth != size) || (mThumbnailHeight != size)) {
			mThumbnailWidth = mThumbnailHeight = size;
			createBitmapCache(true);
			onContentChanged();
		}
	}

	/**
	 * set thumbnail size, if you set both width and height to zero, the size is 96x96(MediaStore.Images.Thumbnails.MICRO_KIND)
	 * @param width
	 * @param height
	 */
	public void setThumbnailSize(final int width, final int height) {
		if ((mThumbnailWidth != width) || (mThumbnailHeight != height)) {
			mThumbnailWidth = width;
			mThumbnailHeight = height;
			createBitmapCache(true);
			onContentChanged();
		}
	}

	public void setShowTitle(final boolean showTitle) {
		if (mShowTitle != showTitle) {
			mShowTitle = showTitle;
			onContentChanged();
		}
	}
	
	public boolean getShowTitle() {
		return mShowTitle;
	}

	public int getMediaType() {
		return mMediaType % MEDIA_TYPE_NUM;
	}
	
	public void setMediaType(final int media_type) {
		if (mMediaType != (media_type % MEDIA_TYPE_NUM)) {
			mMediaType = media_type % MEDIA_TYPE_NUM;
			onContentChanged();
		}
	}

	/**
	 * request to run command on other thread than UI thread
	 */
	public static void queuEvent(final Runnable command) {
		EXECUTER.execute(command);
	}
	
	/**
	 * if you finish using this adapter class in your app,
	 * you can call this method to free internal thumbnail cache
	 */
	public static void destroy() {
		if (sThumbnailCache != null) {
			sThumbnailCache.evictAll();
			sThumbnailCache = null;
		}
	}
	
	private static final class MyAsyncQueryHandler extends AsyncQueryHandler {
		private final MediaStoreAdapter mAdapter;
		public MyAsyncQueryHandler(ContentResolver cr, MediaStoreAdapter adapter) {
			super(cr);
			mAdapter = adapter;
		}
		
		public void requery() {
			synchronized (mAdapter) {
				if (mAdapter.mMediaInfoCursor != null) {
					mAdapter.mMediaInfoCursor.close();
					mAdapter.mMediaInfoCursor = null;
				}
				mAdapter.mSelection = SELECTIONS[mAdapter.mMediaType % MEDIA_TYPE_NUM];
				mAdapter.mSelectionArgs = null;
				final Uri uri = MediaStore.Files.getContentUri("external");
				startQuery(0, mAdapter, uri, PROJ_MEDIA,
					mAdapter.mSelection, mAdapter.mSelectionArgs, null);
			}
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//			super.onQueryComplete(token, cookie, cursor);	// this is empty method
			final Cursor oldCursor = mAdapter.swapCursor(cursor);
			if ((oldCursor != null) && !oldCursor.isClosed())
				oldCursor.close();
		}

	}
	
	/**
	 * create thumbnail cache
	 */
	@SuppressLint("NewApi")
	private final void createBitmapCache(final boolean clear) {
		if (clear && (sThumbnailCache != null)) {
			sThumbnailCache.evictAll();
			System.gc();
		}
		if (sThumbnailCache == null) {
			// use 1/CACHE_RATE of available memory as memory cache
			final int cacheSize = 1024 * 1024 * mMemClass / CACHE_RATE;	// [MB] => [bytes]
			sThumbnailCache = new LruCache<Long, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(Long key, Bitmap bitmap) {
					// control memory usage instead of bitmap counts
					return bitmap.getRowBytes() * bitmap.getHeight();	// [bytes]
				}
			};
		}
		if (Build.VERSION.SDK_INT >= 9) {
			EXECUTER.allowCoreThreadTimeOut(true);	// this makes core threads can terminate  
		}
		// in many case, calling createBitmapCache method means start the new query
		// and need to prepare to run asynchronus tasks
		EXECUTER.prestartAllCoreThreads(); 
	}

	private static final Bitmap getImage(final ContentResolver cr, final long id, final int requestWidth, final int requestHeight)
		throws FileNotFoundException, IOException {
		
		Bitmap result = null;
		final ParcelFileDescriptor pfd = cr.openFileDescriptor(
			ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), "r");
		if (pfd != null) {
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				// just decorde to get image size
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, options);
				// calculate sub-sampling
				options.inSampleSize = calcSampleSize(options, requestWidth, requestHeight);
				options.inJustDecodeBounds = false;
				result = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, options);
			} finally {
				pfd.close();
			}
		}
		return result;
	}
	
	private static final Bitmap getImageThumbnail(final ContentResolver cr, final long id, final int requestWidth, final int requestHeight)
		throws FileNotFoundException, IOException {
		
		// try to get from internal thumbnail cache(in memory), this may be redundant
		Bitmap result = sThumbnailCache.get(id);
		if (result == null) {
			BitmapFactory.Options options = null;
			int kind = MediaStore.Images.Thumbnails.MICRO_KIND;
			if ((requestWidth > 96) || (requestHeight > 96) || (requestWidth * requestHeight > 128 * 128))
				kind = MediaStore.Images.Thumbnails.MINI_KIND;
			result = MediaStore.Images.Thumbnails.getThumbnail(cr, id, kind, options);
			if (result != null) {
				if (DEBUG) Log.v(TAG, String.format("getImageThumbnail:id=%d(%d,%d)", id, result.getWidth(), result.getHeight()));
				// add to internal thumbnail cache(in memory)
				sThumbnailCache.put(id, result);
			}
			
		}
		return result;
	}
	
	/**
	 * calculate maximum sub-sampling size that the image size is greater or equal to requested size
	 * @param options
	 * @param requestWidth
	 * @param requestHeight
	 * @return maximum sub-sampling size
	 */
	private static final int calcSampleSize(final BitmapFactory.Options options, final int requestWidth, final int requestHeight) {
		final int imageWidth = options.outWidth;
		final int imageHeight = options.outHeight;
		int reqWidth = requestWidth, reqHeight = requestHeight;
		if (requestWidth == 0) {
			if (requestHeight > 0)
				reqWidth = (int)(imageWidth * requestHeight / (float)imageHeight);
			else
				reqWidth = imageWidth;
		}
		if (requestHeight == 0) {
			if (requestWidth > 0)
				reqHeight = (int)(imageHeight * requestWidth / (float)imageHeight);
			else
				reqHeight = imageHeight;
		}
		int inSampleSize = 1;
		if ((imageHeight > reqHeight) || (imageWidth > reqWidth)) {
			if (imageWidth > imageHeight) {
				inSampleSize = (int)Math.round(imageHeight / (float)reqHeight);	// Math.floor
			} else {
				inSampleSize = (int)Math.round(imageWidth / (float)reqWidth);	// Math.floor
			}
		}
/*		if (DEBUG) Log.v(TAG, String.format("calcSampleSize:image=(%d,%d),request=(%d,%d),inSampleSize=%d",
				imageWidth, imageHeight, reqWidth, reqHeight, inSampleSize)); */
		return inSampleSize;
	}

	private static final class ViewHolder {
		TextView mTitleView;
		ImageView mImageView;
	}

	/**
	 * LoaderDrawable is a descendent of Drawable to load image asynchronusly and draw
	 * We want to use BitmapDrawable but we can't because it has no public/protected method
	 * to set Bitmap after construction.
	 * 
	 * Most code of LoaderDrawable came from BitmapJobDrawable.java in Android Gallery app
	 * 
	 * Copyright (C) 2013 The Android Open Source Project
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	private static final class LoaderDrawable extends Drawable implements Runnable {
		private final ContentResolver mContentResolver;
	    private final Paint mPaint = new Paint();
	    private final Paint mDebugPaint = new Paint();
	    private final Matrix mDrawMatrix = new Matrix();
		private Bitmap mBitmap;
	    private int mRotation = 0;
	    private ThumbnailLoader mLoader;
		
		public LoaderDrawable(final ContentResolver cr) {
			mContentResolver = cr;
			mDebugPaint.setColor(Color.RED);
			mDebugPaint.setTextSize(18);
		}
		
	    @Override
	    protected void onBoundsChange(Rect bounds) {
	        super.onBoundsChange(bounds);
	        updateDrawMatrix(getBounds());
	    }

	    @Override
		public void draw(final Canvas canvas) {
	        final Rect bounds = getBounds();
	        if (mBitmap != null) {
	            canvas.save();
	            canvas.clipRect(bounds);
	            canvas.concat(mDrawMatrix);
	            canvas.rotate(mRotation, bounds.centerX(), bounds.centerY());
	            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
	            canvas.restore();
	        } else {
	            mPaint.setColor(0xFFCCCCCC);
	            canvas.drawRect(bounds, mPaint);
	        }
            if (DEBUG) {
	            canvas.drawText(Long.toString(mLoader != null ? mLoader.mId : -1),
	            	bounds.centerX(), bounds.centerY(), mDebugPaint);
            }
		}

		private void updateDrawMatrix(final Rect bounds) {
		    if (mBitmap == null || bounds.isEmpty()) {
		        mDrawMatrix.reset();
		        return;
		    }
			
		    final float dwidth = mBitmap.getWidth();
		    final float dheight = mBitmap.getHeight();
		    final int vwidth = bounds.width();
		    final int vheight = bounds.height();
		
		    float scale;
		    float dx = 0, dy = 0;
		    
		    // Calculates a matrix similar to ScaleType.CENTER_CROP
            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight; 
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }
            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
/*		    // Calculates a matrix similar to ScaleType.CENTER_INSIDE
            if (dwidth <= vwidth && dheight <= vheight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) vwidth / (float) dwidth,
                        (float) vheight / (float) dheight);
            }         
            dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
            dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);
		    mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(dx, dy); */
		
		    invalidateSelf();
		}

		@Override
		public void setAlpha(final int alpha) {
	        int oldAlpha = mPaint.getAlpha();
	        if (alpha != oldAlpha) {
	            mPaint.setAlpha(alpha);
	            invalidateSelf();
	        }
		}

		@Override
		public void setColorFilter(final ColorFilter cf) {
	        mPaint.setColorFilter(cf);
	        invalidateSelf();
		}

	    @Override
	    public int getIntrinsicWidth() {
	    	return mThumbnailWidth;
	    }

	    @Override
	    public int getIntrinsicHeight() {
	    	return mThumbnailHeight;
	    }

		@Override
		public int getOpacity() {
	        Bitmap bm = mBitmap;
	        return (bm == null || bm.hasAlpha() || mPaint.getAlpha() < 255) ?
	                PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
		}
			
	    /**
	     * callback to set bitmap on UI thread after asyncronus loading
	     * request call this callback in ThumbnailLoader#run at the end of asyncronus loading
	     */
		@Override
		public void run() {
			setBitmap(mLoader.getBitmap());
		}	
		
		/**
		 * start loading image asynchronusly
		 * @param id
		 */
		public void startLoad(final long id) {
        	
			if (mLoader != null)
				mLoader.cancelLoad();
			
			// try to get from internal thumbnail cache
			final Bitmap newBitmap = sThumbnailCache.get(id);
			if (newBitmap == null) {
				// only start loading if the thumbnail does not exist in internal thumbnail cache
				mBitmap = null;
				// re-using ThumbnailLoader will cause several problems on some devices...
				mLoader = new ThumbnailLoader(this);
				mLoader.startLoad(id);
			} else {
				setBitmap(newBitmap);
			}
			invalidateSelf();
		}

		private void setBitmap(final Bitmap bitmap) {
			if (bitmap != mBitmap) {
				mBitmap = bitmap;
	            updateDrawMatrix(getBounds());
			}
		}
	}

	/**
	 * Runnable to load image asynchronusly
	 */
	private static final class ThumbnailLoader implements Runnable {
		private final LoaderDrawable mParent;
		private final FutureTask<Bitmap> mTask;
		private long mId;
		private Bitmap mBitmap;
		
	    public ThumbnailLoader(final LoaderDrawable parent) {
	    	mParent = parent;
			mTask = new FutureTask<Bitmap>(this, null); 
	    }
	    
	    /**
	     * start loading
	     * @param id
	     */
		public synchronized void startLoad(final long id) {
			mId = id;
			mBitmap = null;
			EXECUTER.execute(mTask);
		}
		
		/**
		 * cancel loading
		 */
		public void cancelLoad() {
			mTask.cancel(true);
		}

		@Override
		public void run() {
			long id;
			synchronized(this) {
				id = mId;
			}
			if (!mTask.isCancelled()) {
				try {
					mBitmap = getImageThumbnail(mParent.mContentResolver, id, mThumbnailWidth, mThumbnailHeight);
				} catch (Exception e) {
					if (DEBUG) Log.w(TAG, e);
				}
			}
			if (mTask.isCancelled() || (id != mId) || (mBitmap == null)) {
				return;	// return without callback
			}
			// set callback
			mParent.scheduleSelf(mParent, 0);
		}
		
		public Bitmap getBitmap() {
			return mBitmap;
		}
	}
}
