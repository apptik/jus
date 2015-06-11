package org.djodjo.comm.jus.rx;


import android.os.Handler;
import android.os.Looper;

import org.djodjo.comm.jus.Cache;
import org.djodjo.comm.jus.CacheDispatcher;
import org.djodjo.comm.jus.Network;
import org.djodjo.comm.jus.NetworkDispatcher;
import org.djodjo.comm.jus.RequestQueue;

public class RxRequestQueue extends RequestQueue{
    public RxRequestQueue(Cache cache, Network network) {
        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }

    public RxRequestQueue(Cache cache, Network network, int threadPoolSize) {
        super(cache, network, threadPoolSize, new RxExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RxRequestQueue(Cache cache, Network network, int threadPoolSize, RxExecutorDelivery delivery) {
        super(cache, network, threadPoolSize, delivery);
    }


    /**
     * Starts the dispatchers in this queue.
     */
    @Override
    public void start() {
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create the cache dispatcher and start it.

        if(mCacheDispatcher==null) {
            mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
            mCacheDispatcher.start();
        }

        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new RxNetworkDispatcher(mNetworkQueue, mNetwork,
                    mCache, mDelivery);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

}
