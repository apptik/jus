package io.apptik.comm.jus.examples.api;


import android.content.Context;
import android.util.Log;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.AndroidRxJus;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.RxRequestQueue;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.DefaultBitmapLruCache;

public class CustomJusHelper {
    private static RequestQueue queue;
    private static ImageLoader imageLoader;
    private static RxRequestQueue rxQueue;
    private static ImageLoader rxImageLoader;


    private CustomJusHelper() {
        // no instances
    }


    public static void init(Context context) {
        queue = AndroidJus.newRequestQueue(context);

        imageLoader = new ImageLoader(queue,
                // new NoCache()
                new DefaultBitmapLruCache()
        );

        rxQueue = AndroidRxJus.newRequestQueue(context);

        rxImageLoader = new ImageLoader(rxQueue,
                // new NoCache()
                new DefaultBitmapLruCache()
        );
    }


    public static RequestQueue getRequestQueue() {
        if (queue != null) {
            return queue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static RequestQueue getRxRequestQueue() {
        if (rxQueue != null) {
            return rxQueue;
        } else {
            throw new IllegalStateException("RxRequestQueue not initialized");
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
        if (imageLoader != null) {
            return imageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static ImageLoader getRxImageLoader() {
        if (rxImageLoader != null) {
            return rxImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static void addRequest(Request request) {
        queue.add(request);
    }

    public static void addRxRequest(Request request) {
        rxQueue.add(request);
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
