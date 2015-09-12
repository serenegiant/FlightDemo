package com.serenegiant.media;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaStoreHelper {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = MediaStoreHelper.class.getSimpleName();

	public static final int MEDIA_ALL = 0;
	public static final int MEDIA_IMAGE = 1;
	public static final int MEDIA_VIDEO = 2;
	private static final int MEDIA_TYPE_NUM = 3;

	public static final int FLAG_AUTO_REQUERY = 0x01;
	public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

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
	protected static LruCache<String, Bitmap> sThumbnailCache;
	protected int mThumbnailWidth = 200, mThumbnailHeight = 200;

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
	public static final int PROJ_INDEX_ID = 0;
	public static final int PROJ_INDEX_TITLE = 1;
	public static final int PROJ_INDEX_MEDIA_TYPE = 2;
	public static final int PROJ_INDEX_MIME_TYPE = 3;
	public static final int PROJ_INDEX_DATA = 4;
	public static final int PROJ_INDEX_DISPLAY_NAME = 5;
//	public static final int PROJ_INDEX_DATE_MODIFIED = 6;
//	public static final int PROJ_INDEX_DATE_ADDED = 7;

	private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

	private final WeakReference<Context> mWeakContext;
//	private final LayoutInflater mInflater;
//	private final int mLayoutId;
	private final ContentResolver mCr;
	private final int mMemClass;
	private final MyAsyncQueryHandler mQueryHandler;
	private final int mHashCode;
	private final DataSetObservable mDataSetObservable = new DataSetObservable();
	private final MediaInfo info = new MediaInfo();
	protected ChangeObserver mChangeObserver;
	protected DataSetObserver mDataSetObserver;
	private Cursor mCursor;
	private Cursor mMediaInfoCursor;
	private String mSelection;
	private String[] mSelectionArgs;
	private boolean mShowTitle = false;
	private int mMediaType = MEDIA_ALL;
	protected int mRowIDColumn;
	protected boolean mDataValid;
	protected boolean mAutoRequery;

	public static class MediaInfo {
		public long id;
		public String data;
		public String title;
		public String mime;
		public String displayName;
		public int mediaType;

		@Override
		public String toString() {
			return String.format("MediaInfo(id=%d,title=%s,displayName=%s, mediaType=%s,mime=%s,data=%s)", id, title, displayName, mediaType(), mime, data);
		}

		private String mediaType() {
			switch (mediaType) {
			case MediaStore.Files.FileColumns.MEDIA_TYPE_NONE:
				return "none";
			case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
				return "image";
			case MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO:
				return "audio";
			case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
				return "video";
			case MediaStore.Files.FileColumns.MEDIA_TYPE_PLAYLIST:
				return "playlist";
			default:
				return String.format("unknown:%d", mediaType);
			}
		}
	}

	public MediaStoreHelper(final Context context) {
		mWeakContext = new WeakReference<Context>(context);
//		mInflater = LayoutInflater.from(context);
//		mLayoutId = id_layout;
		mCr = context.getContentResolver();
		mQueryHandler = new MyAsyncQueryHandler(mCr, this);
		// getMemoryClass return the available memory amounts for app as mega bytes(API >= 5)
		mMemClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		mHashCode = hashCode();
	}

	void init(Context context, Cursor c, int flags) {
		if ((flags & FLAG_AUTO_REQUERY) == FLAG_AUTO_REQUERY) {
			flags |= FLAG_REGISTER_CONTENT_OBSERVER;
			mAutoRequery = true;
		} else {
			mAutoRequery = false;
		}
		boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
		if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
			mChangeObserver = new ChangeObserver();
			mDataSetObserver = new MyDataSetObserver();
		} else {
			mChangeObserver = null;
			mDataSetObserver = null;
		}

		if (cursorPresent) {
			if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
		}
	}

	public ContentResolver getContentResolver() {
		return mCr;
	}

	public void requery() {
		mQueryHandler.requery();
	}

	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
			if (mCursor.moveToPosition(position)) {
				return mCursor.getLong(mRowIDColumn);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public int getCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	/**
	 * return thumbnail image at specific position.
	 * this method is synchronously executed and may take time
	 * @return null if the position value is out of range etc.
	 */
	public Bitmap getItem(final int position) {
		Bitmap result = null;

		getMediaInfo(position, info);
		if (info.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
			// 静止画の場合のサムネイル取得
			try {
				result = getImageThumbnail(mCr, mHashCode, getItemId(position), mThumbnailWidth, mThumbnailHeight);
			} catch (final FileNotFoundException e) {
				Log.w(TAG, e);
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		} else if (info.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
			// 動画の場合のサムネイル取得
			try {
				result = getVideoThumbnail(mCr, mHashCode, getItemId(position), mThumbnailWidth, mThumbnailHeight);
			} catch (final FileNotFoundException e) {
				Log.w(TAG, e);
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG && (result == null)) {
			Log.w(TAG, "failed to getItem(" + info.title + ") at position=" + position);
		}
		return result;
	}

	public int getPositionFromId(final long id) {
		int result = -1;
		final int n = getCount();
		final MediaInfo info = new MediaInfo();
		for (int i = 0; i < n; i++) {
			getMediaInfo(i, info);
			if (info.id == id) {
				result = i;
				break;
			}
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
	 * get MediaInfo at specified position
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
		info.id = cursor.getLong(PROJ_INDEX_ID);
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

	protected void changeCursor(final Cursor cursor) {
		final Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	protected Cursor swapCursor(final Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		final Cursor oldCursor = mCursor;
		if (oldCursor != null) {
			if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
			if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
		}
		mCursor = newCursor;
		if (newCursor != null) {
			if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
			mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
			mDataValid = true;
			// notify the observers about the new cursor
			notifyDataSetChanged();
		} else {
			mRowIDColumn = -1;
			mDataValid = false;
			// notify the observers about the lack of a data set
			notifyDataSetInvalidated();
		}
		return oldCursor;
	}

	protected void onContentChanged() {
		if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
		if (false) Log.v("Cursor", "Auto requerying " + mCursor + " due to update");
			mDataValid = mCursor.requery();
		}
	}

	protected void notifyDataSetChanged() {
		mDataSetObservable.notifyChanged();
	}

	protected void notifyDataSetInvalidated() {
		mDataSetObservable.notifyInvalidated();
	}

	/**
	 * request to run command on other thread than UI thread
	 */
	public static void queueEvent(final Runnable command) {
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
		private final MediaStoreHelper mParent;
		public MyAsyncQueryHandler(ContentResolver cr, MediaStoreHelper parent) {
			super(cr);
			mParent = parent;
		}

		public void requery() {
			synchronized (mParent) {
				if (mParent.mMediaInfoCursor != null) {
					mParent.mMediaInfoCursor.close();
					mParent.mMediaInfoCursor = null;
				}
				mParent.mSelection = SELECTIONS[mParent.mMediaType % MEDIA_TYPE_NUM];
				mParent.mSelectionArgs = null;
				startQuery(0, mParent, QUERY_URI, PROJ_MEDIA,
					mParent.mSelection, mParent.mSelectionArgs, null);
			}
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//			super.onQueryComplete(token, cookie, cursor);	// this is empty method
			final Cursor oldCursor = mParent.swapCursor(cursor);
			if ((oldCursor != null) && !oldCursor.isClosed())
				oldCursor.close();
		}

	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			notifyDataSetInvalidated();
		}
	}

	/**
	 * create thumbnail cache
	 */
	@SuppressLint("NewApi")
	public final void createBitmapCache(final boolean clear) {
		if (clear && (sThumbnailCache != null)) {
			clearBitmapCache(mHashCode);
		}
		if (sThumbnailCache == null) {
			// use 1/CACHE_RATE of available memory as memory cache
			final int cacheSize = 1024 * 1024 * mMemClass / CACHE_RATE;	// [MB] => [bytes]
			sThumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(final String key, final Bitmap bitmap) {
					// control memory usage instead of bitmap counts
					return bitmap.getRowBytes() * bitmap.getHeight();	// [bytes]
				}
			};
		}
		if (Build.VERSION.SDK_INT >= 9) {
			EXECUTER.allowCoreThreadTimeOut(true);	// this makes core threads can terminate
		}
		// in many case, calling createBitmapCache method means start the new query
		// and need to prepare to run asynchronous tasks
		EXECUTER.prestartAllCoreThreads();
	}

	private static void clearBitmapCache(final int hashCode) {
		if (sThumbnailCache != null) {
			if (hashCode != 0) {
				// 指定したhashCodeのMediaStoreAdapterインスタンスのキャッシュをクリアする
				final Map<String, Bitmap> snapshot = sThumbnailCache.snapshot();
				final String key_prefix = String.format("%d_", hashCode);
				final Set<String> keys = snapshot.keySet();
				for (final String key : keys) {
					if (key.startsWith(key_prefix)) {
						// このインスタンスのキーが見つかった
						sThumbnailCache.remove(key);
					}
				}
			} else {
				// 他のMediaStoreAdapterインスタンスのキャッシュも含めてすべてクリアする
				sThumbnailCache.evictAll();
			}
			System.gc();
		}
	}

	protected static String getKey(final long hashCode, final long id) {
		return String.format("%d_%d", hashCode, id);
	}

	protected static final Bitmap getImage(final ContentResolver cr, final long id, final int requestWidth, final int requestHeight)
		throws FileNotFoundException, IOException {

		Bitmap result = null;
		final ParcelFileDescriptor pfd = cr.openFileDescriptor(
			ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), "r");
		if (pfd != null) {
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				// just decode to get image size
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

	protected static final Bitmap getBitmapCache(final long hashCode, final long id) {
		return sThumbnailCache.get(getKey(hashCode, id));
	}

	protected static final Bitmap getImageThumbnail(final ContentResolver cr, final long hashCode, final long id, final int requestWidth, final int requestHeight)
		throws FileNotFoundException, IOException {

		// try to get from internal thumbnail cache(in memory), this may be redundant
		final String key = getKey(hashCode, id);
		Bitmap result = sThumbnailCache.get(key);
		if (result == null) {
			if ((requestWidth <= 0) || (requestHeight <= 0)) {
				result = getImage(cr, id, requestWidth, requestHeight);
			} else {
				BitmapFactory.Options options = null;
				int kind = MediaStore.Images.Thumbnails.MICRO_KIND;
				if ((requestWidth > 96) || (requestHeight > 96) || (requestWidth * requestHeight > 128 * 128))
					kind = MediaStore.Images.Thumbnails.MINI_KIND;
				result = MediaStore.Images.Thumbnails.getThumbnail(cr, id, kind, options);
			}
			if (result != null) {
				if (DEBUG) Log.v(TAG, String.format("getImageThumbnail:id=%d(%d,%d)", id, result.getWidth(), result.getHeight()));
				// add to internal thumbnail cache(in memory)
				sThumbnailCache.put(key, result);
			}

		}
		return result;
	}

	protected static final Bitmap getVideoThumbnail(final ContentResolver cr, final long hashCode, final long id, final int requestWidth, final int requestHeight)
		throws FileNotFoundException, IOException {

		// try to get from internal thumbnail cache(in memory), this may be redundant
		final String key = getKey(hashCode, id);
		Bitmap result = sThumbnailCache.get(key);
		if (result == null) {
			BitmapFactory.Options options = null;
			int kind = MediaStore.Video.Thumbnails.MICRO_KIND;
			if ((requestWidth > 96) || (requestHeight > 96) || (requestWidth * requestHeight > 128 * 128))
				kind = MediaStore.Video.Thumbnails.MINI_KIND;
			result = MediaStore.Video.Thumbnails.getThumbnail(cr, id, kind, options);
			if (result != null) {
				if (DEBUG) Log.v(TAG, String.format("getVideoThumbnail:id=%d(%d,%d)", id, result.getWidth(), result.getHeight()));
				// add to internal thumbnail cache(in memory)
				sThumbnailCache.put(key, result);
			} else {
				Log.w(TAG, "failed to get video thumbnail ofr id=" + id);
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
	protected static final int calcSampleSize(final BitmapFactory.Options options, final int requestWidth, final int requestHeight) {
		final int imageWidth = options.outWidth;
		final int imageHeight = options.outHeight;
		int reqWidth = requestWidth, reqHeight = requestHeight;
		if (requestWidth <= 0) {
			if (requestHeight > 0)
				reqWidth = (int)(imageWidth * requestHeight / (float)imageHeight);
			else
				reqWidth = imageWidth;
		}
		if (requestHeight <= 0) {
			if (requestWidth > 0)
				reqHeight = (int)(imageHeight * requestWidth / (float)imageHeight);
			else
				reqHeight = imageHeight;
		}
		int inSampleSize = 1;
		if ((imageHeight > reqHeight) || (imageWidth > reqWidth)) {
			if (imageWidth > imageHeight) {
				inSampleSize = Math.round(imageHeight / (float)reqHeight);	// Math.floor
			} else {
				inSampleSize = Math.round(imageWidth / (float)reqWidth);	// Math.floor
			}
		}
		if (DEBUG) Log.v(TAG, String.format("calcSampleSize:image=(%d,%d),request=(%d,%d),inSampleSize=%d",
				imageWidth, imageHeight, reqWidth, reqHeight, inSampleSize));
		return inSampleSize;
	}

}
