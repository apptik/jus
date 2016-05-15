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

package io.apptik.comm.jus.examples.jus;


import android.content.Context;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.PooledBitmapLruCache;

public class JusHelper {
    private static JusHelper mInstance;
    private  RequestQueue queue;
    private  ImageLoader imageLoader;
    private static Context context;


    private JusHelper(Context context) {
        JusHelper.context = context;
        JusLog.ErrorLog.on();
        JusLog.ResponseLog.on();
        JusLog.MarkerLog.on();
        PooledBitmapLruCache bitmapLruCache = new PooledBitmapLruCache();
        queue = AndroidJus.newRequestQueue(context);
        imageLoader = new ImageLoader(queue, bitmapLruCache, bitmapLruCache);
    }

    public static synchronized JusHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new JusHelper(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (queue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            queue = AndroidJus.newRequestQueue(context.getApplicationContext());
        }
        return queue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public <T> Request<T> addToRequestQueue(Request<T> request) {
        return getRequestQueue().add(request);
    }


}
