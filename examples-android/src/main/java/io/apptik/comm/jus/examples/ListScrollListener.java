package io.apptik.comm.jus.examples;

import android.support.v7.widget.RecyclerView;


public class ListScrollListener extends RecyclerView.OnScrollListener {
    private long previousEventTime = System.nanoTime();
    private double speed = 0;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy > 0) {
            long currTime = System.nanoTime();
            long timeToScrollOneElement = currTime - previousEventTime;
            //calc px/ms
            speed = ((double) dy / timeToScrollOneElement) * 1000 * 1000;

            previousEventTime = currTime;
        }

    }

    public double getSpeed() {
        return speed;
    }
}
