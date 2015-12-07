package io.apptik.comm.jus.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * True pooled Bitmap Cache. It works as follows.
 *
 * BitmapLruCache gets full --> overflowed bitmaps goes to the BitmapLruPool
 * BitmapLruPool gets full --> overflowed bitmaps goes to the SoftRef reusable Bitmap Set
 *
 */
public class PooledBitmapLruCache extends DefaultBitmapLruCache {

    LruCache<String, Bitmap> bPool;

    public PooledBitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    public PooledBitmapLruCache(int maxSize) {
        super(maxSize);
        bPool = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return PooledBitmapLruCache.super.sizeOf(key, value);
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap
                    newValue) {
                //passes to the soft ref set
                PooledBitmapLruCache.super.addToPool(oldValue);
            }
        };
    }

    @Override
    public Bitmap getReusableBitmap(BitmapFactory.Options options) {
        Set<Map.Entry<String, Bitmap>> bitmaps = bPool.snapshot().entrySet();
        Bitmap bitmap = null;

        if (bitmaps != null && !bitmaps.isEmpty()) {
            for (Map.Entry<String, Bitmap> bmpEntry : bitmaps) {
                Bitmap item = bmpEntry.getValue();
                if (null != item && item.isMutable()) {
                    // Check to see it the item can be used for inBitmap
                    if (canUseForInBitmap(item, options)) {
                        bitmap = item;
                        // Remove from reusable set so it can't be used again
                        bPool.remove(bmpEntry.getKey());
                        break;
                    }
                } else {
                    // Remove from the set if the reference has been cleared.
                    bPool.remove(bmpEntry.getKey());
                }
            }
        }

        if (bitmap != null) {
            return bitmap;
        } else {
            return super.getReusableBitmap(options);
        }
    }

    /**
     * adds bitmap to the LRU pool. If limit is reached value will be thrown at
     * {@link LruCache#entryRemoved(boolean, Object, Object, Object)} where it will be handled by
     * {@link DefaultBitmapLruCache#addToPool(Bitmap)}.
     */
    @Override
    public void addToPool(Bitmap bitmap) {
        bPool.put(UUID.randomUUID().toString(), bitmap);
    }
}
