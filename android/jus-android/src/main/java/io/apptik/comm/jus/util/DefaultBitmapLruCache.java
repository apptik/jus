/*
 * Copyright (C) 2015 AppTik Project
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

package io.apptik.comm.jus.util;


import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.apptik.comm.jus.ui.ImageLoader;

/**
 * Bitmap LRU cache with option to save (soft) and reuse Bitmaps when removed from cache.
 * As {@link #reusableBitmaps} is a {@link SoftReference} means that we might not have really big
 * pool thus re-usability and performance due to less GC will be not that high.
 *
 * For true pooled option use {@link PooledBitmapLruCache}.
 */

public class DefaultBitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader
        .ImageCache, BitmapPool {

    Set<SoftReference<Bitmap>> reusableBitmaps = Collections.synchronizedSet(new
            HashSet<SoftReference<Bitmap>>());

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

    /**
     * Notify the removed entry that is no longer being cached
     */
    @Override
    protected void entryRemoved(boolean evicted, String key,
                                Bitmap oldValue, Bitmap newValue) {
        if (Utils.hasHoneycomb()) {
            // We're running on Honeycomb or later, so add the bitmap
            // to a SoftReference set for possible use with inBitmap later
            addToPool(oldValue);
        }

    }

    /**
     * Measure item size in kilobytes rather than units which is more practical
     * for a bitmap cache
     */
    @Override
    protected int sizeOf(String key, Bitmap value) {
        final int bitmapSize = getBitmapSize(value) / 1024;
        return bitmapSize == 0 ? 1 : bitmapSize;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }


    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param bitmap
     * @return size in bytes
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap) {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if (Utils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }

        if (Utils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /////

    /**
     * @param options - BitmapFactory.Options with out* options populated
     * @return Bitmap that case be used for inBitmap
     */
    public Bitmap getReusableBitmap(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (reusableBitmaps != null && !reusableBitmaps.isEmpty()) {
            synchronized (reusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && canBePooled(item)) {
                        // Check to see it the item can be used for inBitmap
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @Override
    public void addToPool(Bitmap bitmap) {
        if (canBePooled(bitmap)) {
            reusableBitmaps.add(new SoftReference<>(bitmap));
        }
    }

    protected boolean canBePooled(Bitmap bitmap) {
        if (bitmap.isMutable() && !bitmap.isRecycled()) {
            return true;
        }
        try {
            bitmap.recycle();
        } catch(Exception ex) {}
        return false;
    }

    /**
     * @param candidate     - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     * <code>targetOptions</code>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     *
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    protected static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }


}

