/**
 * MediaStorePhotoAdapter
 * Copyright(c) 2014 saki t_saki@serenegiant
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 
 * MediaStorePhotoAdapter is a descendent of CursorAdapter that can load images asynchronusly
 * from MediaStore.Images.Thumbnails and set them to ImageView(that id is R.id.thumbnail)
 * there are two type mode, one is displaying all images (DISPLAY_IMAGE)
 * and the other is group by bucketId and shows only top image of each group (DISPLAY_BUCKET).
 * You can also narrow the range of displaying images with bucketid in DISPLAY_IMAGE mode.
 * 
 * Most code of LoaderDrawable in this class is originally came
 * from BitmapJobDrawable.java in Android Gallery app
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

public class MediaStorePhotoAdapter extends CursorAdapter {

	private static final boolean DEBUG = false;	// TODO set false when releasing
	private static final String TAG = "MediaStorePhotoAdapter";

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

	public static final int DISPLAY_IMAGE = 0;
	public static final int DISPLAY_BUCKET = 1;

	private static final String[] PROJ_ID = {
		MediaStore.Images.Media._ID,					// index=0 for Cursor, column number=1 in SQL statement
		MediaStore.Images.Media.BUCKET_ID,				// index=1 for Cursor, column number=2 in SQL statement
		MediaStore.Images.Media.TITLE,					// index=2 for Cursor, column number=3 in SQL statement
		MediaStore.Images.Media.BUCKET_DISPLAY_NAME,	// index=3 for Cursor, column number=4 in SQL statement
	};
	
	private static final String[] PROJ_IMAGE = {
		MediaStore.Images.Media._ID,					// index=0 for Cursor, column number=1 in SQL statement
		MediaStore.Images.Media.BUCKET_ID,				// index=1 for Cursor, column number=2 in SQL statement
		MediaStore.Images.Media.TITLE,					// index=2 for Cursor, column number=3 in SQL statement
		MediaStore.Images.Media.BUCKET_DISPLAY_NAME,	// index=3 for Cursor, column number=4 in SQL statement
		MediaStore.Images.Media.DATA,					// index=4 for Cursor, column number=5 in SQL statement	
		MediaStore.Images.Media.DESCRIPTION,			// index=5 for Cursor, column number=6 in SQL statement
		MediaStore.Images.Media.ORIENTATION,			// index=6 for Cursor, column number=7 in SQL statement
	};
	// "1) GROUP BY (2" means "SELECT ... FROM ... WHERE (1) GROUP BY (2)"
	private static final String SELECTION_GROUP_BY_BUCKET = "1) GROUP BY (2";	// group by BUCKET_ID
	private static final String SELECTION_IMAGE = MediaStore.Images.Media.BUCKET_ID + "=?";
	
	// these values should be fit to PROJ_ID/PROJ_IMAGE
	private static final int PROJ_INDEX_ID = 0;
	private static final int PROJ_INDEX_BUCKET_ID = 1;
	private static final int PROJ_INDEX_TITLE = 2;
	private static final int PROJ_INDEX_BUCKET_NAME = 3;
	private static final int PROJ_IMAGE_INDEX_DATA = 4;			// only for PROJ_IMAGE
	private static final int PROJ_IMAGE_INDEX_DESCRIPTION = 5;	// only for PROJ_IMAGE
	private static final int PROJ_IMAGE_INDEX_ORIENTATION = 6;	// only for PROJ_IMAGE
	
	private final LayoutInflater mInflater;
	private final ContentResolver mCr;
	private final int mMemClass;
	private final int mLayoutId;
	private final MyAsyncQueryHandler mQueryHandler;
	private Cursor mMediaInfoCursor;
	private String mSelection;
	private String[] mSelectionArgs;
	private int mDisplayType = DISPLAY_IMAGE;
	private boolean mShowTitle;
	private String mBucketId;
	
	public static class MediaInfo {
		public String bucketId;
		public String data;
		public String title;
		public String bucketName;
		public String description;
		public int orientation;
		public String contentType;
		public int width;		// API >= 16
		public int height;		// API >= 16
	}
	
	public MediaStorePhotoAdapter(Context context, int id_layout) {
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
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// this method is called within UI thread and should return as soon as possible
		final View view = mInflater.inflate(mLayoutId, parent, false);
		getViewHolder(view);
		return view;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
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
			if (mShowTitle)
				tv.setText(cursor.getString(mDisplayType == DISPLAY_IMAGE
					? PROJ_INDEX_TITLE : PROJ_INDEX_BUCKET_NAME));
		}
	}

	private ViewHolder getViewHolder(View view) {
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
	public Bitmap getItem(int position) {
		Bitmap result = null;
		try {
			result = getThumbnail(mCr, getItemId(position), mThumbnailWidth, mThumbnailHeight);
		} catch (FileNotFoundException e) {
			Log.w(TAG, e);
		} catch (IOException e) {
			Log.w(TAG, e);
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
	public Bitmap getImage(int position, int width, int height)
		throws FileNotFoundException, IOException {

		return getImage(mCr, getItemId(position), width, height);
	}
	
	/**
	 * get mediainfo at specified position
	 * @param position
	 * @return
	 */
	public synchronized MediaInfo getMediaInfo(int position) {
		MediaInfo info = null;
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
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJ_IMAGE,
				mSelection, mSelectionArgs, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
		}
		if (mMediaInfoCursor.moveToPosition(position)) {
			info = readMediaInfo(mMediaInfoCursor, new MediaInfo());
		}
		return info;
	}

	private static final MediaInfo readMediaInfo(Cursor cursor, MediaInfo info) {
		info.bucketId = cursor.getString(PROJ_INDEX_BUCKET_ID);
		info.title = cursor.getString(PROJ_INDEX_TITLE);
		info.bucketName = cursor.getString(PROJ_INDEX_BUCKET_NAME);
		info.data = cursor.getString(PROJ_IMAGE_INDEX_DATA);
		info.description = cursor.getString(PROJ_IMAGE_INDEX_DESCRIPTION);
		info.orientation = cursor.getInt(PROJ_IMAGE_INDEX_ORIENTATION);	
		return info;
	}
	/**
	 * set thumbnail size, if you set size to zero, the size is 96x96(MediaStore.Images.Thumbnails.MICRO_KIND)
	 * @param size
	 */
	public void setThumbnailSize(int size) {
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
	public void setThumbnailSize(int width, int height) {
		if ((mThumbnailWidth != width) || (mThumbnailHeight != height)) {
			mThumbnailWidth = width;
			mThumbnailHeight = height;
			createBitmapCache(true);
			onContentChanged();
		}
	}

	public void setShowTitle(boolean showTitle) {
		if (mShowTitle != showTitle) {
			mShowTitle = showTitle;
			onContentChanged();
		}
	}
	
	public boolean getShowTitle() {
		return mShowTitle;
	}
	
	/**
	 * @param displayType = DISPLAY_IMAGE/DISPLAY_BUCKET
	 */
	public synchronized void setDisplayType(int displayType) {
		if (mDisplayType != displayType % 2) {
			mDisplayType = displayType % 2;
			onContentChanged();
		}
		mBucketId = null;
	}
	
	public int getDisplayType() {
		return mDisplayType;
	}
	
	public String getBucketId() {
		return mBucketId;
	}
	
	public void setBucketId(String bucketId) {
		if (mBucketId != bucketId) {
			mBucketId = bucketId;
			onContentChanged();
		}
	}

	/**
	 * asynchronusly add bitmap to standard camera directory
	 * @param bitmap
	 * @param title
	 * @param description
	 */
	public void add(final Bitmap bitmap, final String title, final String description) {
		if (bitmap != null) {
			EXECUTER.execute(new Runnable() {
				@Override
				public void run() {
					final String url = MediaStore.Images.Media.insertImage(mCr, bitmap, title, description);
					if (url == null) {
						Log.w(TAG, "fail to insert image");
					}
				}		
			});
		}
	}

	/**
		 * asynchronusly add bitmap to specific named directory with automatically generated file name.
		 * @param bitmap
		 * @param dirName application specific directory name(ExternalStorageDirectory is added automatically inside this method)
		 * @param title
		 * @param description
		 */
		public void add(final Bitmap bitmap, final String dirName, final String title, final String description) {
			if (bitmap != null && !TextUtils.isEmpty(dirName)) {
				EXECUTER.execute(new Runnable() {
					@SuppressLint("InlinedApi")
					@Override
					public void run() {
						final StringBuilder sb = new StringBuilder();
						// directory name
						sb.append(Environment.getExternalStorageDirectory()).append("/").append(dirName);
						final String directory = sb.toString();
						final File dir = new File(directory);
						if (!dir.exists()) {
							dir.mkdirs();	// we need to create directory with missing parent if not exist
						}
						// file path
						sb.setLength(0);
						sb.append(directory).append("/").append(System.currentTimeMillis()).append(".jpg");
						final String filePath = sb.toString();
						// API >= 8
		                OutputStream out = null;
						try {
							out = new FileOutputStream(new File(filePath));
							bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
						} catch (FileNotFoundException e) {
							Log.w(TAG, e);
		                } finally {
		                	if (out != null) {
								try {
									out.close();
								} catch (IOException e) {
									Log.w(TAG, e);
								}
		                	}
		                }
/*						// we can request to add image to MediaStore using MediaScannerConnection#scanFile
						// but that method cannot add specified title/description,
						// so we add(insert) by ourselves
						MediaScannerConnection.scanFile(mContext,
							new String[] {filePath}, new String[]{"image/jpeg"}, null); */
						final ContentValues values = new ContentValues();
						values.put(MediaStore.Images.Media.TITLE, title);
						values.put(MediaStore.Images.Media.DESCRIPTION, description);
						values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
						values.put(MediaStore.Images.Media.DATA, filePath);
						// if you want to confirm the result of insertion,
						// you can use AsyncQueryHandler#startInsert and #onInsertComplete callback
						// instead of using ContentResolver#insert
//						mQueryHandler.startInsert(0, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
						mCr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					}
				});
			}
		}

	/**
	 * asynchronusly delete image at specified position.
	 * @param position
	 */
	public void delete(int position) {
		final long id = getItemId(position);
		if (id > 0) {
			mQueryHandler.startDelete(0, this,
				ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
				null, null);
		}
	}

	/**
	 * request to run command on other thread than UI thread
	 */
	public static void queuEvent(Runnable command) {
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
		private final MediaStorePhotoAdapter mAdapter;
		public MyAsyncQueryHandler(ContentResolver cr, MediaStorePhotoAdapter adapter) {
			super(cr);
			mAdapter = adapter;
		}
		
		public void requery() {
			synchronized (mAdapter) {
				mAdapter.mSelection = null;
				mAdapter.mSelectionArgs = null;
				if (mAdapter.mMediaInfoCursor != null) {
					mAdapter.mMediaInfoCursor.close();
					mAdapter.mMediaInfoCursor = null;
				}
				switch (mAdapter.mDisplayType) {
				case DISPLAY_IMAGE:
					if (!TextUtils.isEmpty(mAdapter.mBucketId)) {
						mAdapter.mSelection = SELECTION_IMAGE;
						mAdapter.mSelectionArgs = new String[] {mAdapter.mBucketId};
					}
					break;
				case DISPLAY_BUCKET:
					mAdapter.mSelection = SELECTION_GROUP_BY_BUCKET;
					break;
				}
				startQuery(0, mAdapter, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJ_ID,
					mAdapter.mSelection, mAdapter.mSelectionArgs, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
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
	private final void createBitmapCache(boolean clear) {
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

	private static final Bitmap getImage(ContentResolver cr, long id, int requestWidth, int requestHeight)
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
	
	private static final Bitmap getThumbnail(ContentResolver cr, long id, int requestWidth, int requestHeight)
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
				if (DEBUG) Log.v(TAG, String.format("getThumbnail:id=%d(%d,%d)", id, result.getWidth(), result.getHeight()));
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
	private static final int calcSampleSize(BitmapFactory.Options options, final int requestWidth, final int requestHeight) {
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
		
		public LoaderDrawable(ContentResolver cr) {
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
		public void draw(Canvas canvas) {
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

		private void updateDrawMatrix(Rect bounds) {
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
		public void setAlpha(int alpha) {
	        int oldAlpha = mPaint.getAlpha();
	        if (alpha != oldAlpha) {
	            mPaint.setAlpha(alpha);
	            invalidateSelf();
	        }
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
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
		public void startLoad(long id) {
        	
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

		private void setBitmap(Bitmap bitmap) {
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
		
	    public ThumbnailLoader(LoaderDrawable parent) {
	    	mParent = parent;
			mTask = new FutureTask<Bitmap>(this, null); 
	    }
	    
	    /**
	     * start loading
	     * @param id
	     */
		public synchronized void startLoad(long id) {
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
					mBitmap = getThumbnail(mParent.mContentResolver, id, mThumbnailWidth, mThumbnailHeight); 
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
