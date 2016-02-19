/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * BitmapLruPool is a source and repository of <code>Bitmap</code> objects. Its purpose is to
 * supply those buffers to consumers who need to use them for a short period of time and then
 * dispose of them. Simply creating and disposing such buffers in the conventional manner can
 * considerable heap churn and garbage collection delays on Android, which lacks good management of
 * short-lived heap objects. It may be advantageous to trade off some memory in the form of a
 * permanently allocated pool of buffers in order to gain heap performance improvements; that is
 * what this class does.
 * <p>
 * A good candidate user for this class is something like an I/O system that uses large temporary
 * <code>Bitmap</code> buffers to copy data around. In these use cases, often the consumer wants
 * the buffer to be a certain minimum size to ensure good performance (e.g. when copying data chunks
 * off of a stream), but doesn't mind if the buffer is larger than the minimum. Taking this into
 * account and also to maximize the odds of being able to reuse a recycled buffer, this class is
 * free to return buffers larger than the requested size. The caller needs to be able to gracefully
 * deal with getting buffers any size over the minimum.
 * <p>
 * If there is not a suitably-sized bitmap in its recycling pool when a buffer is requested null
 * will be returned.
 * <p>
 * This class has no special ownership of buffers it creates; the caller is free to take a buffer
 * it receives from this pool, use it permanently, and never return it to the pool; additionally,
 * it is not harmful to return to this pool a buffer that was allocated elsewhere, provided there
 * are no other lingering references to it.
 * <p>
 * This class ensures that the total size of the bitmaps in its recycling pool never exceeds a
 * certain byte limit. When a buffer is returned that would cause the pool to exceed the limit,
 * least-recently-used buffers are disposed.
 */
public class BitmapLruPool {

   // private Set<Bitmap> bitmaps = new
    private List<Bitmap> bitmapsByLastUse = new LinkedList<>();
    private List<Bitmap> bitmapsBySize = new ArrayList<>(64);
    /** The buffer pool, arranged both by last use and by buffer size */

    /** The total size of the buffers in the pool */
    private int currentSize = 0;

    /**
     * The maximum aggregate size of the buffers in the pool. Old buffers are discarded to stay
     * under this limit.
     */
    private final int sizeLimit;

    /**
     * @param sizeLimit the maximum size of the pool, in bytes
     */
    public BitmapLruPool(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    /** Compares buffers by size */
    protected static final Comparator<Bitmap> BITMAP_COMPARATOR = new Comparator<Bitmap>() {
        @Override
        public int compare(Bitmap lhs, Bitmap rhs) {
            return getBitmapSize(lhs) - getBitmapSize(rhs);
        }
    };


    /**
     * Returns a bitmap from the pool if one is available in the requested size, or allocates a new
     * one if a pooled one is not available.
     *
     * @param options the options required to reuse the bitmap. The returned bitmap
     *                may be larger.
     * @return Bitmap or null if not found.
     */
    public synchronized Bitmap getBitmap(BitmapFactory.Options options) {
        Bitmap bitmap = null;
        synchronized (bitmapsBySize) {
            final Iterator<Bitmap> iterator = bitmapsBySize.iterator();
            Bitmap item;

            while (iterator.hasNext()) {
                item = iterator.next();

                if (null != item && canBePooled(item)) {
                    // Check to see it the item can be used for inBitmap
                    if (BitmapLruPool.canUseForInBitmap(item, options)) {
                        bitmap = item;

                        currentSize -= getBitmapSize(bitmap);
                        // Remove from reusable set so it can't be used again
                        iterator.remove();
                        bitmapsByLastUse.remove(bitmap);
                        break;
                    }
                } else {
                    currentSize -= getBitmapSize(item);
                    // Remove from the set if the reference has been cleared.
                    iterator.remove();
                    bitmapsByLastUse.remove(item);
                }
            }
        }

        return bitmap;
    }

    /**
     * Returns a buffer to the pool, throwing away old buffers if the pool would exceed its allotted
     * size.
     *
     * @param bitmap the buffer to return to the pool.
     */
    public synchronized void returnBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmapsBySize.contains(bitmap) ) {
            return;
        } else if(getBitmapSize(bitmap) > sizeLimit || !canBePooled(bitmap)) {
            if(bitmap!=null) {
                bitmap.recycle();
            }
            return;
        }
        bitmapsByLastUse.add(bitmap);
        int pos = Collections.binarySearch(bitmapsBySize, bitmap, BITMAP_COMPARATOR);
        if (pos < 0) {
            pos = -pos - 1;
        }
        bitmapsBySize.add(pos, bitmap);
        currentSize += getBitmapSize(bitmap);
        trim();
    }

    /**
     * Removes buffers from the pool until it is under its size limit.
     */
    private synchronized void trim() {
        while (currentSize > sizeLimit) {
            Bitmap bitmap = bitmapsByLastUse.remove(0);
            bitmapsBySize.remove(bitmap);
            currentSize -= getBitmapSize(bitmap);
            bitmap.recycle();
        }
    }

    protected static boolean canBePooled(Bitmap bitmap) {
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
        if(targetOptions.inSampleSize < 1) {
            targetOptions.inSampleSize = 1;
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
        if(bitmap==null) return 0;
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

    public int size() {
        return currentSize;
    }

    public int maxSize() {
        return sizeLimit;
    }
}
