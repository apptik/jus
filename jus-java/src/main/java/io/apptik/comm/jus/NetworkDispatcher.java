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

package io.apptik.comm.jus;

import java.net.HttpURLConnection;
import java.util.concurrent.BlockingQueue;

import io.apptik.comm.jus.error.JusError;

/**
 * Provides a threadId for performing network dispatch from a queue of requests.
 * <p>
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 * </p>
 */
public class NetworkDispatcher extends Thread {
    /**
     * The queue of requests to service.
     */
    private final BlockingQueue<Request<?>> mQueue;
    /**
     * The network interface for processing requests.
     */
    private final Network mNetwork;
    /**
     * The cache to write to.
     */
    private final Cache mCache;
    /**
     * For posting responses and errors.
     */
    private final ResponseDelivery mDelivery;
    /**
     * Used for telling us to die.
     */
    private volatile boolean mQuit = false;

    /**
     * Creates a new network dispatcher threadId.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue    Queue of incoming requests for triage
     * @param network  Network interface to use for performing requests
     * @param cache    Cache interface to use for writing responses to cache
     * @param delivery Delivery interface to use for posting responses
     */
    public NetworkDispatcher(BlockingQueue<Request<?>> queue,
                             Network network, Cache cache,
                             ResponseDelivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }

    /**
     * Checks if a response message contains a body.
     *
     * @param requestMethod request method
     * @param responseCode  response status code
     * @return whether the response has a body
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     */
    public static boolean hasResponseBody(String requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD
                && !(100 <= responseCode && responseCode < HttpURLConnection.HTTP_OK)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_RESET
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    protected void addTrafficStatsTag(Request<?> request) {
       //TODO
    }

    protected void setThreadPriority() {
        this.setPriority(Thread.NORM_PRIORITY);
    }

    @Override
    public void run() {
        //todo add queue markers
        setThreadPriority();
        while (true) {
            long startTimeMs = System.nanoTime();
            Request<?> request;
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
                request.addMarker(Request.EVENT_NETWORK_QUEUE_TAKE);

                // If the request was cancelled already, do not perform the
                // network request.
                if (request.isCanceled()) {
                    request.finish(Request.EVENT_NETWORK_DISCARD_CANCELED);
                    continue;
                }

                addTrafficStatsTag(request);

                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);

                if (request.isCanceled()) {
                    request.finish(Request.EVENT_NETWORK_DISCARD_CANCELED);
                    continue;
                }
                request.addMarker(Request.EVENT_NETWORK_HTTP_COMPLETE, networkResponse);

                // If the server returned 304 AND we delivered a response already,
                // we're done -- don't deliver a second identical response.
                if (networkResponse.isNotModified() && request.hasHadResponseDelivered()) {
                    request.finish(Request.EVENT_NOT_MODIFIED);
                    continue;
                }

                Response<?> response;
                //try parse and wrap withing parse exception in case someone overwrites
                //Request, which handles this
                try {
                    // Parse the response here on the worker threadId.
                    response = request.parseNetworkResponse(networkResponse);
                    request.addMarker(Request.EVENT_NETWORK_PARSE_COMPLETE);
                } catch (Exception ex) {
                    if(JusError.class.isAssignableFrom(ex.getClass())) {
                        throw ex;
                    } else {
                        throw new ParseError(ex);
                    }
                }
                // Write to cache if applicable.
                // response.cacheEntry must not be null
                // TODO: Only update cache metadata instead of entire record for 304s.

                if (request.shouldCache() && response != null && response.cacheEntry != null &&
                        mCache !=null) {
                    mCache.put(request.getCacheKey(), response.cacheEntry);
                    request.addMarker(Request.EVENT_NETWORK_CACHE_WRITTEN);
                }

                // Post the response back.
                request.markDelivered();
                mDelivery.postResponse(request, response);
            } catch (JusError jusError) {
                jusError.setNetworkTimeNs(System.nanoTime() - startTimeMs);
                parseAndDeliverNetworkError(request, jusError);
            } catch (Exception e) {
                JusError jusError = new JusError(e);
                jusError.setNetworkTimeNs(System.nanoTime() - startTimeMs);
                mDelivery.postError(request, jusError);
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, JusError error) {
        error = request.parseNetworkError(error);
        mDelivery.postError(request, error);
    }

    public static class NetworkDispatcherFactory {

        /**
         * The queue of requests to service.
         */
        protected final BlockingQueue<Request<?>> mQueue;
        /**
         * The network interface for processing requests.
         */
        protected final Network mNetwork;
        /**
         * The cache to write to.
         */
        protected final Cache mCache;
        /**
         * For posting responses and errors.
         */
        protected final ResponseDelivery mDelivery;

        /**
         * Creates a new network dispatcher factory.  You must call {@link #create()}
         * in order to create {@link NetworkDispatcher}.
         *
         * @param queue    Queue of incoming requests for triage
         * @param network  Network interface to use for performing requests
         * @param cache    Cache interface to use for writing responses to cache
         * @param delivery Delivery interface to use for posting responses
         */
        public NetworkDispatcherFactory(BlockingQueue<Request<?>> queue,
                                        Network network, Cache cache,
                                        ResponseDelivery delivery) {
            mQueue = queue;
            mNetwork = network;
            mCache = cache;
            mDelivery = delivery;
        }

        public NetworkDispatcher create() {
            return new NetworkDispatcher(mQueue, mNetwork, mCache, mDelivery);
        }
    }
}
