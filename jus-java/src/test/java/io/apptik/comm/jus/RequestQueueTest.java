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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.apptik.comm.jus.Request.Priority;
import io.apptik.comm.jus.mock.MockNetwork;
import io.apptik.comm.jus.mock.MockRequest;
import io.apptik.comm.jus.mock.MockyRequest;
import io.apptik.comm.jus.toolbox.NoCache;
import io.apptik.comm.jus.utils.CacheTestUtils;
import io.apptik.comm.jus.utils.ImmediateResponseDelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RequestQueueTest {
    private ResponseDelivery mDelivery;

    @Before
    public void setUp() throws Exception {
        mDelivery = new ImmediateResponseDelivery();
    }

    /**
     * Make a list of requests with random priorities.
     *
     * @param count Number of requests to make
     */
    private List<MockRequest> makeRequests(int count) {
        Priority[] allPriorities = Priority.values();
        Random random = new Random();

        List<MockRequest> requests = new ArrayList<MockRequest>();
        for (int i = 0; i < count; i++) {
            MockRequest request = new MockRequest();
            Priority priority = allPriorities[random.nextInt(allPriorities.length)];
            request.setCacheKey(String.valueOf(i));
            request.setPriority(priority);
            requests.add(request);
        }
        return requests;
    }

    @Test
    public void useConverterFactories() throws Exception {
        MockNetwork network = new MockNetwork();
        byte[] dataToReturn = new byte[]{1, 2, 3, 4};
        network.setDataToReturn(dataToReturn);
        RequestQueue queue = new RequestQueue(new NoCache(), network, 4, mDelivery);
        queue.addConverterFactory(new Converter.Factory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                return new Converter<NetworkResponse, byte[]>() {
                    @Override
                    public byte[] convert(NetworkResponse value) throws IOException {
                        return value.data;
                    }
                };
            }
        });
        queue.start();

        MockyRequest request = queue.add(new MockyRequest());
        request.getFuture().get();

        assertEquals(dataToReturn, request.getRawResponse().result);

        queue.stopWhenDone();
    }

    @Test
    public void add_requestProcessedInCorrectOrder() throws Exception {
        int requestsToMake = 100;

        OrderCheckingNetwork network = new OrderCheckingNetwork();
        RequestQueue queue = new RequestQueue(new NoCache(), network, 1, mDelivery);

        for (Request<?> request : makeRequests(requestsToMake)) {
            queue.add(request);
        }

        queue.start();

        queue.stopWhenDone();
    }

    @Test
    public void add_dedupeByCacheKey() throws Exception {
        OrderCheckingNetwork network = new OrderCheckingNetwork();
        final AtomicInteger parsed = new AtomicInteger();
        final AtomicInteger delivered = new AtomicInteger();
        // Enqueue 2 requests with the same cache key. The first request takes 1.5s. Assert that the
        // second request is only handled after the first one has been parsed and delivered.
        DelayedRequest req1 = new DelayedRequest(1500, parsed, delivered);
        DelayedRequest req2 = new DelayedRequest(0, parsed, delivered) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                assertEquals(1, parsed.get());  // req1 must have been parsed.
                assertEquals(1, delivered.get());  // req1 must have been parsed.
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue queue = new RequestQueue(new NoCache(), network, 3, mDelivery);
        queue.add(req1);
        queue.add(req2);
        queue.start();

        queue.stopWhenDone();
    }

    @Test
    public void cancelAll_onlyCorrectTag() throws Exception {
        MockNetwork network = new MockNetwork();
        RequestQueue queue = new RequestQueue(new NoCache(), network, 3, mDelivery);
        Object tagA = new Object();
        Object tagB = new Object();
        MockRequest req1 = new MockRequest();
        req1.setTag(tagA);
        MockRequest req2 = new MockRequest();
        req2.setTag(tagB);
        MockRequest req3 = new MockRequest();
        req3.setTag(tagA);
        MockRequest req4 = new MockRequest();
        req4.setTag(tagA);

        queue.add(req1); // A
        queue.add(req2); // B
        queue.add(req3); // A
        queue.cancelAll(tagA);
        queue.add(req4); // A

        assertTrue(req1.cancel_called); // A cancelled
        assertFalse(req2.cancel_called); // B not cancelled
        assertTrue(req3.cancel_called); // A cancelled
        assertFalse(req4.cancel_called); // A added after cancel not cancelled

    }

    private class OrderCheckingNetwork implements Network {
        private Priority mLastPriority = Priority.IMMEDIATE;
        private int mLastSequence = -1;

        @Override
        public NetworkResponse performRequest(Request<?> request) {
            Priority thisPriority = request.getPriority();
            int thisSequence = request.getSequence();

            int priorityDiff = thisPriority.compareTo(mLastPriority);

            // Should never experience a higher priority after a lower priority
            assertFalse(priorityDiff > 0);

            // If we're not transitioning to a new priority block, check sequence numbers
            if (priorityDiff == 0) {
                assertTrue(thisSequence > mLastSequence);
            }
            mLastSequence = thisSequence;
            mLastPriority = thisPriority;

            return new NetworkResponse(200, new byte[16], null, 0);
        }
    }

    private class DelayedRequest extends Request<String> {
        private final long mDelayMillis;
        private final AtomicInteger mParsedCount;
        private final AtomicInteger mDeliveredCount;

        public DelayedRequest(long delayMillis, AtomicInteger parsed, AtomicInteger delivered) {
            super(Method.GET, "http://buganizer/");
            mDelayMillis = delayMillis;
            mParsedCount = parsed;
            mDeliveredCount = delivered;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            mParsedCount.incrementAndGet();
            try {
                Thread.sleep(mDelayMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Response.success(new String(), CacheTestUtils.makeRandomCacheEntry(null));
        }

        @Override
        protected void deliverResponse(String response) {
            mDeliveredCount.incrementAndGet();
        }
    }

}
