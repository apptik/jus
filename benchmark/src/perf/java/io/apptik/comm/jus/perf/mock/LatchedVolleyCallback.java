package io.apptik.comm.jus.perf.mock;


import java.util.concurrent.CountDownLatch;

public class LatchedVolleyCallback implements
        com.android.volley.Response.Listener<com.android.volley.NetworkResponse> {

    public final CountDownLatch latch;

    public LatchedVolleyCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onResponse(com.android.volley.NetworkResponse response) {
        latch.countDown();
    }
}
