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

package io.apptik.comm.jus.examples.api;


import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import io.apptik.comm.jus.ui.ImageLoader;

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

