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
