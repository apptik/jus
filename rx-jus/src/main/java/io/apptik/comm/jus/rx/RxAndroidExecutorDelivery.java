package io.apptik.comm.jus.rx;


import android.os.Handler;

import io.apptik.comm.jus.AndroidExecutorDelivery;

public class RxAndroidExecutorDelivery extends AndroidExecutorDelivery {

    /**
     * Creates a new response delivery interface.
     *
     * @param handler {@link Handler} to post responses on
     */
    public RxAndroidExecutorDelivery(Handler handler) {
        super(handler);
    }
}
