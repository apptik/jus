package io.apptik.comm.jus.examples.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by Vincent on 4/17/2016.
 */
public class LruBitmapCache extends LruCache<String, Bitmap>
		implements ImageLoader.ImageCache {

	private static LruBitmapCache mInstance;

	private LruBitmapCache(int maxSize) {
		super(maxSize);
	}

	private LruBitmapCache(Context ctx) {
		this(getCacheSize(ctx));
	}

	public static synchronized LruBitmapCache getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LruBitmapCache(context);
		}
		return mInstance;
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}

	@Override
	public Bitmap getBitmap(String url) {
		return get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
	}

	// Returns a cache size equal to approximately three screens worth of images.
	public static int getCacheSize(Context ctx) {
		final DisplayMetrics displayMetrics = ctx.getResources().
				getDisplayMetrics();
		final int screenWidth = displayMetrics.widthPixels;
		final int screenHeight = displayMetrics.heightPixels;
		// 4 bytes per pixel
		final int screenBytes = screenWidth * screenHeight * 4;

		return screenBytes * 3;
	}
}