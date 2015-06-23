package org.djodjo.comm.jus.examples.api;


import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import org.djodjo.comm.jus.ui.ImageLoader;


public class BitmapLruCache extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }


    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //or ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass()
        final int cacheSize = maxMemory / 8;
        Log.d("TEST", "(gc) LRU size:" + cacheSize);
        return cacheSize;
    }

    public BitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        Log.d("TEST", "(gc) LRU sizeOf:" + key);
        return value.getByteCount()  / 1024;
    }


    @Override
    public Bitmap getBitmap(String url) {
        Log.d("TEST", "(gc) LRU GET Bitmap:" + url);
        return get(url);
    }


    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d("TEST", "(gc) LRU PUT Bitmap:" + url);
        put(url, bitmap);
    }
}

