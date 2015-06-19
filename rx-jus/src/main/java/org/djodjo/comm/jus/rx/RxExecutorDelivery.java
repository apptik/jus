package org.djodjo.comm.jus.rx;


import android.os.Handler;

import org.djodjo.comm.jus.ExecutorDelivery;
import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.Response;
import org.djodjo.comm.jus.error.JusError;

import java.util.concurrent.Executor;

public class RxExecutorDelivery extends ExecutorDelivery {
    public RxExecutorDelivery(Executor executor) {
        super(executor);
    }

    public RxExecutorDelivery(Handler handler) {
        super(handler);
    }

    @Override
    public void postError(Request<?> request, JusError error) {
        super.postError(request, error);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        super.postResponse(request, response);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
        super.postResponse(request, response, runnable);
    }
}
