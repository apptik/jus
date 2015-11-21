package io.apptik.comm.jus.examples.api;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.AndroidRxJus;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.queue.RxRequestQueue;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.DefaultBitmapLruCache;
import rx.functions.Action1;
import rx.functions.Func1;

public class CustomJusHelper {
    private static RequestQueue queue;
    private static ImageLoader imageLoader;
    private static RxRequestQueue rxQueue;
    private static ImageLoader rxImageLoader;


    private CustomJusHelper() {
        // no instances
    }


    public static void init(Context context) {
        JusLog.ErrorLog.on();
        JusLog.ResponseLog.on();
        JusLog.MarkerLog.on();
        queue = AndroidJus.newRequestQueue(context);

        imageLoader = new ImageLoader(queue,
                // new NoCache()
                new DefaultBitmapLruCache()
        );

        rxQueue = AndroidRxJus.newRequestQueue(context);
        rxQueue.getAllEventSubject().filter(
                new Func1<JusEvent, Boolean>() {
                    @Override
                    public Boolean call(JusEvent jusEvent) {
                        return (jusEvent instanceof ResultEvent);
                    }
                }
        ).map(new Func1<JusEvent, ResultEvent>() {
            @Override
            public ResultEvent call(JusEvent jusEvent) {
                return (ResultEvent) jusEvent;
            }
        }).subscribe(new Action1<ResultEvent>() {
            @Override
            public void call(ResultEvent resultEvent) {
                Log.d("Jus-Test", "jus received response for: " + resultEvent.request);
                if(! (resultEvent.response instanceof Bitmap)) {
                    Log.d("Jus-Test", "jus response : " + resultEvent.response);
                }
            }
        });

        rxQueue.getAllEventSubject().filter(
                new Func1<JusEvent, Boolean>() {
                    @Override
                    public Boolean call(JusEvent jusEvent) {
                        return (jusEvent instanceof ErrorEvent);
                    }
                }
        ).map(new Func1<JusEvent, ErrorEvent>() {
            @Override
            public ErrorEvent call(JusEvent errorEvent) {
                return (ErrorEvent) errorEvent;
            }
        }).subscribe(new Action1<ErrorEvent>() {
            @Override
            public void call(ErrorEvent errorEvent) {
                Log.e("Jus-Test", "jus received ERROR for: " + errorEvent.request);
                if(errorEvent.error !=null) {
                    Log.e("Jus-Test", "jus ERROR : " + errorEvent.error);
                }
            }
        });

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
                new RequestListener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Jus-Test", "jus response : " + response);
                    }
                },
                new RequestListener.ErrorListener() {
                    @Override
                    public void onError(JusError error) {
                        Log.d("Jus-Test", "jus error : " + error);
                    }
                }
        ));
    }

}
