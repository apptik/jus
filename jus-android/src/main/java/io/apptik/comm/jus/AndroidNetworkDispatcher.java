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


import android.net.TrafficStats;
import android.os.Process;

import java.util.concurrent.BlockingQueue;


public class AndroidNetworkDispatcher extends NetworkDispatcher {
    /**
     * Creates a new network dispatcher threadId.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue    Queue of incoming requests for triage
     * @param network  Network interface to use for performing requests
     * @param cache    Cache interface to use for writing responses to cache
     * @param delivery Delivery interface to use for posting responses
     */
    public AndroidNetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
        super(queue, network, cache, delivery);
    }


    @Override
    protected void addTrafficStatsTag(Request<?> request) {
        // only for API >= 14
        TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
    }

    @Override
    public void setThreadPriority() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }

    public static class NetworkDispatcherFactory extends NetworkDispatcher.NetworkDispatcherFactory {

        /**
         * Creates a new network dispatcher factory.  You must call {@link #create()}
         * in order to create {@link NetworkDispatcher}.
         *
         * @param queue    Queue of incoming requests for triage
         * @param network  Network interface to use for performing requests
         * @param cache    Cache interface to use for writing responses to cache
         * @param delivery Delivery interface to use for posting responses
         */
        public NetworkDispatcherFactory(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
            super(queue, network, cache, delivery);
        }


        @Override
        public NetworkDispatcher create() {
            return new AndroidNetworkDispatcher(mQueue, mNetwork, mCache, mDelivery);
        }
    }
}
