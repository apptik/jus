/*
 * Copyright (C) 2015 AppTik Project
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
