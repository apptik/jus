package org.djodjo.comm.jus.rx;


import org.djodjo.comm.jus.Cache;
import org.djodjo.comm.jus.Network;
import org.djodjo.comm.jus.NetworkDispatcher;
import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.ResponseDelivery;

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
