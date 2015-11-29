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


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.rx.queue.RxRequestQueue;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.DefaultBitmapLruCache;
import rx.functions.Action1;

public class CustomJusHelper {
    private static RequestQueue queue;
    private static ImageLoader imageLoader;

    private CustomJusHelper() {
        // no instances
    }


    public static void init(Context context) {
        JusLog.ErrorLog.on();
        JusLog.ResponseLog.on();
        JusLog.MarkerLog.on();
        queue = AndroidJus.newRequestQueue(context);
        DefaultBitmapLruCache defaultBitmapLruCache = new DefaultBitmapLruCache();
        imageLoader = new ImageLoader(queue,
                // new NoCache()
                defaultBitmapLruCache, defaultBitmapLruCache
        );

        RxRequestQueue.resultObservable(queue, null).subscribe(new Action1<ResultEvent>() {
            @Override
            public void call(ResultEvent resultEvent) {
                Log.d("Jus-Test", "jus received response for: " + resultEvent.request);
                if (!(resultEvent.response instanceof Bitmap)) {
                    Log.d("Jus-Test", "jus response : " + resultEvent.response);
                }
            }
        });

        RxRequestQueue.errorObservable(queue, null).subscribe(new Action1<ErrorEvent>() {
            @Override
            public void call(ErrorEvent errorEvent) {
                Log.e("Jus-Test", "jus received ERROR for: " + errorEvent.request);
                if (errorEvent.error != null) {
                    Log.e("Jus-Test", "jus ERROR : " + errorEvent.error);
                }
            }
        });
    }


    public static RequestQueue getRequestQueue() {
        if (queue != null) {
            return queue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively
     * means
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

    public static void addRequest(Request request) {
        queue.add(request);
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
