package io.apptik.comm.jus.examples.api;


import android.content.Context;
import android.util.Log;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.DefaultBitmapLruCache;

public class CustomJusHelper {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private CustomJusHelper() {
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

    public static void addRequest(Request request) {
        mRequestQueue.add(request);
    }

    public static void addDummyRequest(String key, String val) {
        addRequest(Requests.getDummyRequest(key, val,
                new Listener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Jus-Test", "jus response : " + response);
                    }
                },
                new Listener.ErrorListener() {
                    @Override
                    public void onErrorResponse(JusError error) {
                        Log.d("Jus-Test", "jus error : " + error);
                    }
                }
        ));
    }

}
