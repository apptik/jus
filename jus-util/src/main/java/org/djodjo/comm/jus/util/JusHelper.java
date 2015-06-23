package org.djodjo.comm.jus.util;


import android.content.Context;

import org.djodjo.comm.jus.RequestQueue;
import org.djodjo.comm.jus.ui.ImageLoader;
import org.djodjo.comm.jus.Jus;

public class JusHelper {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private JusHelper() {
        // no instances
    }


    public static void init(Context context) {
        mRequestQueue = Jus.newRequestQueue(context);

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
