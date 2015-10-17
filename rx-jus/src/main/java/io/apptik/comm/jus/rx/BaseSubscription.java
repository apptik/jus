package io.apptik.comm.jus.rx;


import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import rx.Subscription;

public abstract class BaseSubscription implements Subscription {
    private volatile int unsubscribed;
    private static final AtomicIntegerFieldUpdater<BaseSubscription> atomicUnsubscribedUpdater =
            AtomicIntegerFieldUpdater.newUpdater(BaseSubscription.class, "unsubscribed");

    @Override
    public void unsubscribe() {
        if (atomicUnsubscribedUpdater.compareAndSet(this, 0, 1)) {
            doUnsubscribe();
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed != 0;
    }

    protected abstract void doUnsubscribe();
}
