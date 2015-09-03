package io.apptik.comm.jus.rx;


import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.Network;
import io.apptik.comm.jus.NetworkDispatcher;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.ResponseDelivery;

import java.util.concurrent.BlockingQueue;

public class RxNetworkDispatcher extends NetworkDispatcher {
    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue    Queue of incoming requests for triage
     * @param network  Network interface to use for performing requests
     * @param cache    Cache interface to use for writing responses to cache
     * @param delivery Delivery interface to use for posting responses
     */
    public RxNetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
        super(queue, network, cache, delivery);
    }
}
