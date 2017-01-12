package io.apptik.comm.jus.perf.mock;


import java.util.concurrent.CountDownLatch;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.RequestListener;

public class LatchedJusCallback implements RequestListener.ResponseListener<NetworkResponse> {

    public final CountDownLatch latch;

    public LatchedJusCallback(CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void onResponse(NetworkResponse response) {
        latch.countDown();
    }
}
