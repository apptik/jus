package io.apptik.comm.jus.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LruCache;


/**
 * True pooled Bitmap Cache. It works as follows.
 *
 * BitmapLruCache gets full --> overflowed bitmaps goes to the BitmapLruPool
 * BitmapLruPool gets full --> evicted bitmaps gets removed and eventually recycled by GC
 *
 * Note that explicit {@link Bitmap#recycle()} is not called since it might be still used
 * somewhere else (recycleview/listview for example) and also GC is doing perfect job anyway.
 *
 */
public class PooledBitmapLruCache extends DefaultBitmapLruCache {

    BitmapLruPool bPool;

    public PooledBitmapLruCache() {
        this(getDefaultLruCacheSize(), getPoolCache(getDefaultLruCacheSize()));
    }

    private static int getPoolCache(int memCache) {
        if(Build.VERSION.SDK_INT > 18) {
            return (int) (memCache*1.25);
        }
        return 0;
    }

    public PooledBitmapLruCache(int maxSizeCache, int maxSizePool) {
        super(maxSizeCache);
        bPool = new BitmapLruPool(maxSizePool);
    }

    public BitmapLruPool pool() {
        return bPool;
    }

    public void preFill(int w, int h) {
        int fill = size();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int size = BitmapLruPool.getBitmapSize(bmp);
        addToPool(bmp);
        fill+=size;
        while (fill<maxSize()) {
            addToPool(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888));
            fill+=size;
        }
    }

    @Override
    public synchronized Bitmap getReusableBitmap(BitmapFactory.Options options) {
        return bPool.getBitmap(options);
    }

    /**
     * adds bitmap to the LRU pool. If limit is reached value will be thrown at
     * {@link LruCache#entryRemoved(boolean, Object, Object, Object)} where it will be handled by
     * {@link DefaultBitmapLruCache#addToPool(Bitmap)}.
     */
    @Override
    public synchronized void addToPool(Bitmap bitmap) {
       bPool.returnBitmap(bitmap);
    }
}
