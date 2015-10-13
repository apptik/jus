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

package io.apptik.comm.jus;

import java.io.File;

import io.apptik.comm.jus.stack.HttpStack;
import io.apptik.comm.jus.stack.HurlStack;
import io.apptik.comm.jus.toolbox.DiskBasedCache;
import io.apptik.comm.jus.toolbox.HttpNetwork;
import io.apptik.comm.jus.toolbox.NoCache;

public class Jus {

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "jus";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param cacheLocation A {@link File} to use for creating the cache dir.
     * @param stack         An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(File cacheLocation, HttpStack stack) {

        File cacheDir = null;
        if (cacheLocation != null) {
            cacheDir = new File(cacheLocation, DEFAULT_CACHE_DIR);
        }

        String userAgent = "jus/0";

        if (stack == null) {
            stack = new HurlStack();
        }

        Network network = new HttpNetwork(stack);

        Cache cache = null;

        if(cacheDir!=null) {
            cache = new DiskBasedCache(cacheDir);
        } else {
            cache =  new NoCache();
        }


        RequestQueue queue = new RequestQueue(cache, network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param cacheLocation A {@link File} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(File cacheLocation) {
        return newRequestQueue(cacheLocation, null);
    }

    public static RequestQueue newRequestQueue() {
        return newRequestQueue(null, null);
    }
}
