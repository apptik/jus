package io.apptik.comm.jus;


import org.junit.Before;
import org.junit.Test;

import io.apptik.comm.jus.mock.MockNetwork;
import io.apptik.comm.jus.mock.MockRequest;
import io.apptik.comm.jus.toolbox.NoCache;
import io.apptik.comm.jus.utils.ImmediateResponseDelivery;

import static org.junit.Assert.assertEquals;

public class RaceTest {

    private ResponseDelivery mDelivery;

    @Before
    public void setUp() throws Exception {
        mDelivery = new ImmediateResponseDelivery();
    }

    @Test
    public void runManySlowerRequests() throws Exception {
        MockNetwork network = new MockNetwork().setSlowness(100);

        RequestQueue queue = new RequestQueue(new NoCache(), network, 4, mDelivery);

        CacheDispatcher cacheDispatcher = new CacheDispatcher(queue.mCacheQueue, queue
                .mNetworkQueue, queue.getCache(), queue.mDelivery);

        queue.withCacheDispatcher(cacheDispatcher);
        queue.start();
        for (int i = 0; i < 500; i++) {
            MockRequest request = new MockRequest();
            queue.add(request);
            System.out.println("CACHE   QUEUE: " + cacheDispatcher.mCacheQueue.size());
            System.out.println("NETWORK QUEUE: " + cacheDispatcher.mNetworkQueue.size());
            System.out.println("CURRENT QUEUE: " + queue.getCurrentRequests() + " / " +
                    queue.getWaitingRequests());
        }

        queue.stopWhenDone();
        assertEquals(500, network.getRequestCnt());

    }

    @Test
    public void runManyFastRequests() throws Exception {
        MockNetwork network = new MockNetwork();

        RequestQueue queue = new RequestQueue(new NoCache(), network, 4, mDelivery);

        CacheDispatcher cacheDispatcher = new CacheDispatcher(queue.mCacheQueue, queue
                .mNetworkQueue, queue.getCache(), queue.mDelivery);

        queue.withCacheDispatcher(cacheDispatcher);
        queue.start();
        for (int i = 0; i < 500; i++) {
            MockRequest request = new MockRequest();
            queue.add(request);
            System.out.println("CACHE   QUEUE: " + cacheDispatcher.mCacheQueue.size());
            System.out.println("NETWORK QUEUE: " + cacheDispatcher.mNetworkQueue.size());
            System.out.println("CURRENT QUEUE: " + queue.getCurrentRequests() + " / " +
                    queue.getWaitingRequests());
        }

        queue.stopWhenDone();
        assertEquals(500, network.getRequestCnt());

    }
}
