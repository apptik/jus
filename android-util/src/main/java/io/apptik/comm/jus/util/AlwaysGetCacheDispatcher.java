package io.apptik.comm.jus.util;

import android.os.Process;

import java.util.concurrent.BlockingQueue;

import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.CacheDispatcher;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.ResponseDelivery;

// Unless if not fully unexpired, dispatches all as Soft-expired cache hit,
// while keeping the original caches. i.e. We can deliver the cached response,
// but we need to also send the request to the network for
// refreshing.
public class AlwaysGetCacheDispatcher extends CacheDispatcher {

    /**
     * Creates a new cache triage dispatcher threadId.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue   Queue of incoming requests for triage
     * @param networkQueue Queue to post requests that require network to
     * @param cache        Cache interface to use for resolution
     * @param delivery     Delivery interface to use for posting responses
     */
    public AlwaysGetCacheDispatcher(BlockingQueue<Request<?,?>> cacheQueue, BlockingQueue<Request<?,?>> networkQueue, Cache cache, ResponseDelivery delivery) {
        super(cacheQueue, networkQueue, cache, delivery);
    }

    @Override
    public void run() {
        if (DEBUG) JusLog.v("start new dispatcher");
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        // Make a blocking call to initialize the cache.
        mCache.initialize();

        while (true) {
            try {
                // Get a request from the cache triage queue, blocking until
                // at least one is available.
                final Request<?,?> request = mCacheQueue.take();
                request.addMarker(Request.EVENT_CACHE_QUEUE_TAKE);

                // If the request has been canceled, don't bother dispatching it.
                if (request.isCanceled()) {
                    request.finish(Request.EVENT_CACHE_DISCARD_CANCELED);
                    continue;
                }

                // Attempt to retrieve this item from cache.
                Cache.Entry entry = mCache.get(request.getCacheKey());
                if (entry == null) {
                    request.addMarker(Request.EVENT_CACHE_MISS);
                    // Cache miss; send off to the network dispatcher.
                    mNetworkQueue.put(request);
                    continue;
                }

                // If it is completely expired, just send it to the network.
                if (entry.isExpired()) {
                    request.addMarker(Request.EVENT_CACHE_HIT_EXPIRED_BUT_WILL_DELIVER_IT);
                    Response<?> response = request.parseNetworkResponse(
                            new NetworkResponse(entry.data, entry.responseHeaders));
                    request.setCacheEntry(entry);
                    // Mark the response as intermediate.
                    response.intermediate = true;

                    // Post the intermediate response back to the user and have
                    // the delivery then forward the request along to the network.
                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mNetworkQueue.put(request);
                            } catch (InterruptedException e) {
                                // Not much we can do about this.
                            }
                        }
                    });
                }

                // We have a cache hit; parse its data for delivery back to the request.
                request.addMarker(Request.EVENT_CACHE_HIT);
                Response<?> response = request.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));
                request.addMarker(Request.EVENT_CACHE_HIT_PARSED);

                if (!entry.refreshNeeded()) {
                    // Completely unexpired cache hit. Just deliver the response.
                    mDelivery.postResponse(request, response);
                } else {
                    // Soft-expired cache hit. We can deliver the cached response,
                    // but we need to also send the request to the network for
                    // refreshing.
                    request.addMarker(Request.EVENT_CACHE_HIT_REFRESH_NEEDED);
                    request.setCacheEntry(entry);

                    // Mark the response as intermediate.
                    response.intermediate = true;

                    // Post the intermediate response back to the user and have
                    // the delivery then forward the request along to the network.
                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mNetworkQueue.put(request);
                            } catch (InterruptedException e) {
                                // Not much we can do about this.
                            }
                        }
                    });
                }

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }
        }
    }
}
