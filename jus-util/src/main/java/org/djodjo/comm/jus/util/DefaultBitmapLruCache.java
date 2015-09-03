package org.djodjo.comm.jus.util;


import android.graphics.Bitmap;
import android.util.LruCache;

import org.djodjo.comm.jus.ui.ImageLoader;

public class DefaultBitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {


    public DefaultBitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    public DefaultBitmapLruCache(int maxSize) {
        super(maxSize);
    }

    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //or ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass()
        final int cacheSize = maxMemory / 8;
        return cacheSize;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight()  / 1024;
    }


    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }


    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}

