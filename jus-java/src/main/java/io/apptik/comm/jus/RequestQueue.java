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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.apptik.comm.jus.auth.Authenticator;
import io.apptik.comm.jus.converter.BasicConverterFactory;

import static io.apptik.comm.jus.toolbox.Utils.checkNotNull;

/**
 * A request dispatch queue with a threadId pool of dispatchers.
 * <p/>
 * Calling {@link #add(Request)} will enqueue the given Request for dispatch,
 * resolving from either cache or network on a worker threadId, and then delivering
 * a parsed response on the main threadId.
 */
public class RequestQueue {

    /**
     * Callback interface for completed requests.
     */
    public static interface RequestFinishedListener<T> {
        /**
         * Called when a request has finished processing.
         */
        public void onRequestFinished(Request<T> request);
    }

    /**
     * Used for generating monotonically-increasing sequence numbers for requests.
     */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    /**
     * Staging area for requests that already have a duplicate request in flight.
     * <p/>
     * <ul>
     * <li>containsKey(cacheKey) indicates that there is a request in flight for the given cache
     * key.</li>
     * <li>get(cacheKey) returns waiting requests for the given cache key. The in flight request
     * is <em>not</em> contained in that list. Is null if no requests are staged.</li>
     * </ul>
     */
    private final Map<String, Queue<Request<?>>> mWaitingRequests =
            new ConcurrentHashMap<String, Queue<Request<?>>>();

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    private final Set<Request<?>> mCurrentRequests
            = Collections.newSetFromMap(new ConcurrentHashMap<Request<?>, Boolean>());
    //   = new HashSet<Request<?>>();

    /**
     * The cache triage queue.
     */
    protected final PriorityBlockingQueue<Request<?>> mCacheQueue =
            new PriorityBlockingQueue<>();

    /**
     * The queue of requests that are actually going out to the network.
     */
    protected final PriorityBlockingQueue<Request<?>> mNetworkQueue =
            new PriorityBlockingQueue<>();

    /**
     * Number of network request dispatcher threads to start.
     */
    public static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /**
     * Cache interface for retrieving and storing responses.
     */
    protected final Cache mCache;

    /**
     * Network interface for performing requests.
     */
    protected final Network mNetwork;

    /**
     * Response delivery mechanism.
     */
    protected final ResponseDelivery mDelivery;

    /**
     * The network dispatchers.
     */
    protected NetworkDispatcher[] mDispatchers;

    /**
     * The cache dispatcher.
     */
    protected CacheDispatcher cacheDispatcher;

    /**
     * Network dispatcher factory
     */
    protected NetworkDispatcher.NetworkDispatcherFactory networkDispatcherFactory;

    private List<RequestFinishedListener> finishedListeners =
            new ArrayList<>();

    private final List<Authenticator.Factory> authenticatorFactories = new ArrayList<>();
    private final List<Converter.Factory> converterFactories = new ArrayList<>();
    private final List<Transformer.RequestTransformer> requestTransformers = new ArrayList<>();
    private final List<Transformer.ResponseTransformer> responseTransformers = new ArrayList<>();

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache          A Cache to use for persisting responses to disk
     * @param network        A Network interface for performing HTTP requests
     * @param threadPoolSize Number of network dispatcher threads to create
     * @param delivery       A ResponseDelivery interface for posting responses and errors
     */
    public RequestQueue(Cache cache, Network network, int threadPoolSize,
                        ResponseDelivery delivery) {
        mCache = cache;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
        converterFactories.add(new BasicConverterFactory());
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache          A Cache to use for persisting responses to disk
     * @param network        A Network interface for performing HTTP requests
     * @param threadPoolSize Number of network dispatcher threads to create
     */
    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
        this(cache, network, threadPoolSize,
                new ExecutorDelivery(new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        command.run();
                    }
                }));
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache   A Cache to use for persisting responses to disk
     * @param network A Network interface for performing HTTP requests
     */
    public RequestQueue(Cache cache, Network network) {
        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }


    public RequestQueue withCacheDispatcher(CacheDispatcher cacheDispatcher) {
        if (this.cacheDispatcher != null) {
            this.cacheDispatcher.quit();
        }
        this.cacheDispatcher = cacheDispatcher;
        return this;
    }

    public RequestQueue withNetworkDispatcherFactory(NetworkDispatcher.NetworkDispatcherFactory
                                                             networkDispatcherFactory) {
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
        this.networkDispatcherFactory = networkDispatcherFactory;
        return this;
    }

    /**
     * Create network dispatchers (and corresponding threads) up to the pool size.
     */
    private void setUpNetworkDispatchers() {
        if (networkDispatcherFactory == null) {
            networkDispatcherFactory = new NetworkDispatcher.NetworkDispatcherFactory
                    (mNetworkQueue, mNetwork,
                            mCache, mDelivery);
        }

        for (int i = 0; i < mDispatchers.length; i++) {
            mDispatchers[i] = networkDispatcherFactory.create();
            mDispatchers[i].start();
        }
    }

    /**
     * Starts the dispatchers in this queue.
     */
    public void start() {
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create the cache dispatcher and start it.

        if (cacheDispatcher == null) {
            cacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
        }
        cacheDispatcher.start();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        setUpNetworkDispatchers();
    }

    public void stopWhenDone() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getCurrentRequests() > 0) {
                    JusLog.d("Waiting to finish. Requests left: " +
                            getCurrentRequests() + " / " + getWaitingRequests());
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                JusLog.d("READY to finish. Requests left: " +
                        getCurrentRequests() + " / " + getWaitingRequests());
                synchronized (mCurrentRequests) {
                    mCurrentRequests.notify();
                }
                // stop();
            }
        }).start();

        synchronized (mCurrentRequests) {
            try {
                mCurrentRequests.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stop();
    }

    /**
     * Stops the cache and network dispatchers.
     */
    public void stop() {
        if (cacheDispatcher != null) {
            cacheDispatcher.quit();
        }
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * Gets the {@link Cache} instance being used.
     */
    public Cache getCache() {
        return mCache;
    }

    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    public interface RequestFilter {
        public boolean apply(Request<?> request);
    }

    /**
     * Cancels all requests in this queue for which the given filter applies.
     *
     * @param filter The filtering function to use
     */
    public void cancelAll(RequestFilter filter) {
        synchronized (mCurrentRequests) {
            for (Request<?> request : mCurrentRequests) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }

    /**
     * Cancels all requests in this queue with the given tag. Tag must be non-null
     * and equality is by identity.
     */
    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return request.getTag() == tag;
            }
        });
    }

    /**
     * Adds a Request to the dispatch queue.
     *
     * @param request The request to service
     * @return The passed-in request
     */
    public <R extends Request<T>, T> R add(R request) {
        for (Authenticator.Factory factory : authenticatorFactories) {
            Authenticator authenticator =
                    factory.forRequest(request.getUrl(), request.getNetworkRequest());
            if (authenticator != null) {
                request.setAuthenticator(authenticator);
                break;
            }
        }

        for (Transformer.RequestTransformer transformer : requestTransformers) {
            if (transformer.filter == null || transformer.filter.apply(request)) {
                request.setNetworkRequest(transformer.transform(request.getNetworkRequest()));
            }
        }

        synchronized (mCurrentRequests) {
            //check if not already cancelled
            if (request.isCanceled()) {
                request.finish(Request.EVENT_CACHE_DISCARD_CANCELED);
            }
            // Process requests in the order they are added.
            request.setSequence(getSequenceNumber());
            // Tag the request as belonging to this queue and add it to the set of current requests.
            request.setRequestQueue(this);
            mCurrentRequests.add(request);
        }
        request.addMarker(Request.EVENT_ADD_TO_QUEUE);

        // If the request is uncacheable, skip the cache queue and go straight to the network.
        if (!request.shouldCache()) {
            mNetworkQueue.add(request);
            return request;
        }

        // Insert request into stage if there's already a request with the same cache key in flight.
        synchronized (mWaitingRequests) {
            String cacheKey = request.getCacheKey();
            if (mWaitingRequests.containsKey(cacheKey)) {
                // There is already a request in flight. Queue up.
                Queue<Request<?>> stagedRequests = mWaitingRequests.get(cacheKey);
                if (stagedRequests == null) {
                    stagedRequests = new LinkedList<Request<?>>();
                }
                stagedRequests.add(request);
                mWaitingRequests.put(cacheKey, stagedRequests);
                if (JusLog.DEBUG) {
                    JusLog.v("Request for cacheKey=%s is in flight, putting on hold.", cacheKey);
                }
            } else {
                // Insert 'empty list' queue for this cacheKey, indicating there is now a request in
                // flight.
                mWaitingRequests.put(cacheKey, new LinkedList<Request<?>>());
                mCacheQueue.add(request);
            }
            return request;
        }
    }

    public final NetworkResponse transformResponse(Request<?> request, NetworkResponse response) {
        NetworkResponse currResponse = response;
        for (Transformer.ResponseTransformer transformer : responseTransformers) {
            if (transformer.filter == null || transformer.filter.apply(request)) {
                currResponse = transformer.transform(currResponse);
            }
        }
        return currResponse;
    }


    /**
     * Called from {@link Request#finish(String)}, indicating that processing of the given request
     * has finished.
     * <p/>
     * <p>Releases waiting requests for <code>request.getCacheKey()</code> if
     * <code>request.shouldCache()</code>.</p>
     */
    <T> void finish(Request<T> request) {
        // Remove from the set of requests currently being processed.
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
        synchronized (finishedListeners) {
            for (RequestFinishedListener<T> listener : finishedListeners) {
                listener.onRequestFinished(request);
            }
        }
        if (request.shouldCache()) {
            synchronized (mWaitingRequests) {
                String cacheKey = request.getCacheKey();
                Queue<Request<?>> waitingRequests = mWaitingRequests.remove(cacheKey);
                if (waitingRequests != null) {
                    if (JusLog.DEBUG) {
                        JusLog.v("Releasing %d waiting requests for cacheKey=%s.",
                                waitingRequests.size(), cacheKey);
                    }
                    // Process all queued up requests. They won't be considered as in flight, but
                    // that's not a problem as the cache has been primed by 'request'.
                    mCacheQueue.addAll(waitingRequests);
                }
            }
        }
    }

    public <T> void addRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (finishedListeners) {
            finishedListeners.add(listener);
        }
    }

    /**
     * Remove a RequestFinishedListener. Has no effect if listener was not previously added.
     */
    public <T> void removeRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (finishedListeners) {
            finishedListeners.remove(listener);
        }
    }

    public RequestQueue addAuthenticatorFactory(Authenticator.Factory factory) {
        synchronized (authenticatorFactories) {
            authenticatorFactories.add(factory);
        }

        return this;
    }


    public RequestQueue removeAuthenticatorFactory(Authenticator.Factory factory) {
        synchronized (authenticatorFactories) {
            authenticatorFactories.remove(factory);
        }

        return this;
    }

    public RequestQueue addConverterFactory(Converter.Factory factory) {
        synchronized (converterFactories) {
            converterFactories.add(factory);
        }

        return this;
    }


    public RequestQueue removeConverterFactory(Converter.Factory factory) {
        synchronized (converterFactories) {
            converterFactories.remove(factory);
        }

        return this;
    }

    public RequestQueue addRequestTransformer(Transformer.RequestTransformer transformer) {
        synchronized (requestTransformers) {
            requestTransformers.add(transformer);
        }
        return this;
    }


    public RequestQueue removeRequestTransformer(Transformer.RequestTransformer transformer) {
        synchronized (requestTransformers) {
            requestTransformers.remove(transformer);
        }
        return this;
    }

    public RequestQueue addResponseTransformer(Transformer.ResponseTransformer transformer) {
        synchronized (responseTransformers) {
            responseTransformers.add(transformer);
        }
        return this;
    }

    public RequestQueue removeResponseTransformer(Transformer.ResponseTransformer transformer) {
        synchronized (responseTransformers) {
            responseTransformers.remove(transformer);
        }
        return this;
    }

    /**
     * Returns a {@link Converter} for {@link io.apptik.comm.jus.NetworkResponse} to {@code type}
     * from the available
     * {@linkplain #converterFactories factories}.
     */
    public Converter<NetworkResponse, ?> getResponseConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<NetworkResponse, ?> converter =
                    converterFactories.get(i).fromResponse(type, annotations);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate Response converter for ")
                .append(type)
                .append(". Tried:");
        for (Converter.Factory converterFactory : converterFactories) {
            builder.append("\n * ").append(converterFactory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }


    public int getCurrentRequests() {
        synchronized (mCurrentRequests) {
            return mCurrentRequests.size();
        }
    }

    public int getWaitingRequests() {
        synchronized (mWaitingRequests) {
            return mWaitingRequests.size();
        }
    }
}
