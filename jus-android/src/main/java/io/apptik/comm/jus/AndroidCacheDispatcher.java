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

package io.apptik.comm.jus;

import android.os.Process;

import java.util.concurrent.BlockingQueue;


public class AndroidCacheDispatcher extends CacheDispatcher {
    /**
     * Creates a new cache triage dispatcher threadId.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue   Queue of incoming requests for triage
     * @param networkQueue Queue to post requests that require network to
     * @param cache        Cache interface to use for resolution
     * @param delivery     Delivery interface to use for posting responses
     */
    public AndroidCacheDispatcher(BlockingQueue<Request<?>> cacheQueue,
                                  BlockingQueue<Request<?>> networkQueue,
                                  Cache cache, ResponseDelivery delivery) {
        super(cacheQueue, networkQueue, cache, delivery);
    }

    @Override
    public void setThreadPriority() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }
}
