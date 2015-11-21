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

package io.apptik.comm.jus.rx.request;

import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.rx.BaseSubscription;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.Observable;
import rx.Subscriber;

public class RequestResponseOnSubscribe<T> implements Observable.OnSubscribe<ResultEvent<T>> {
    private final Request<T> request;

    public RequestResponseOnSubscribe(Request<T> request) {
        this.request = request;
    }

    @Override
    public void call(final Subscriber<? super ResultEvent<T>> subscriber) {
        final RequestListener.ResponseListener listener = new RequestListener.ResponseListener<T>() {
            @Override
            public void onResponse(T response) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(new ResultEvent<>(request, response));
                }
            }
        };
        request.addResponseListener(listener);

        subscriber.add(new BaseSubscription() {
            @Override
            protected void doUnsubscribe() {
                request.removeResponseListener(listener);
            }
        });
    }

}
