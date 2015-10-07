/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2014 Kalin Maldzhanski
 * Copyright (C) 2011 The Android Open Source Project
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

package io.apptik.comm.jus.request;

import android.os.Handler;
import android.os.Looper;

import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;

/**
 * A synthetic request used for clearing the cache.
 */
public class ClearCacheRequest extends Request<Void,Object> {
    private final Cache mCache;
    private final Runnable mCallback;

    /**
     * Creates a synthetic request for clearing the cache.
     * @param cache Cache to clear
     * @param callback Callback to make on the main threadId once the cache is clear,
     * or null for none
     */
    public ClearCacheRequest(Cache cache, Runnable callback) {
        super(Method.GET, (String)null, null);
        mCache = cache;
        mCallback = callback;
    }

    @Override
    public ClearCacheRequest clone() {
        return new ClearCacheRequest(mCache, mCallback);
    }

    @Override
    public boolean isCanceled() {
        // This is a little bit of a hack, but hey, why not.
        mCache.clear();
        if (mCallback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postAtFrontOfQueue(mCallback);
        }
        return true;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    @Override
    public Response<Object> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

}
