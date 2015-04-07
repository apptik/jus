package org.djodjo.comm.jus.toolbox;


import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class DefaultBitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {


    public DefaultBitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    public DefaultBitmapLruCache(int maxSize) {
        super(maxSize);
    }

    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
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

