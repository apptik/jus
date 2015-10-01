/*
 * Copyright (C) 2015 Apptik Project
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

package io.apptik.comm.jus.rx;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.File;

import io.apptik.comm.jus.Network;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.stack.HttpStack;
import io.apptik.comm.jus.stack.HurlStack;
import io.apptik.comm.jus.toolbox.BasicNetwork;
import io.apptik.comm.jus.toolbox.DiskBasedCache;
import rx.subjects.BehaviorSubject;

/**
 * reactive jus.
 * provides reactive request queue
 */
public class RxJus {

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "rxjus";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack   An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        String userAgent = "rxjus/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
            //should not happen
            throw new RuntimeException(e);
        }

        if (stack == null) {
            stack = new HurlStack();
        }

        Network network = new BasicNetwork(stack);

        RxRequestQueue queue = new RxRequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, null);
    }

    public BehaviorSubject<JusEvent> getJusSubject() {
        return JusEmitter.get().getJusSubject();
    }
}
