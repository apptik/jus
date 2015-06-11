package org.djodjo.comm.jus.rx;


import android.os.Handler;

import org.djodjo.comm.jus.ExecutorDelivery;

import java.util.concurrent.Executor;

public class RxExecutorDelivery extends ExecutorDelivery {
    public RxExecutorDelivery(Executor executor) {
        super(executor);
    }

    public RxExecutorDelivery(Handler handler) {
        super(handler);
    }
}
