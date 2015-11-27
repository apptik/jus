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


import android.content.Context;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.ui.ImageLoader;

public class JusHelper {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private JusHelper() {
        // no instances
    }


    public static void init(Context context) {
        mRequestQueue = AndroidJus.newRequestQueue(context);

        mImageLoader = new ImageLoader(mRequestQueue,
                // new NoCache()
                new DefaultBitmapLruCache()
        );
    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }


    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
}
