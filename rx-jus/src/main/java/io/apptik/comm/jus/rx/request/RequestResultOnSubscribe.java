package io.apptik.comm.jus.rx.request;

import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.rx.BaseSubscription;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.Observable;
import rx.Subscriber;

public class RequestResultOnSubscribe<T> implements Observable.OnSubscribe<ResultEvent<T>> {
    private final Request<T> request;

    public RequestResultOnSubscribe(Request<T> request) {
        this.request = request;
    }

    @Override
    public void call(final Subscriber<? super ResultEvent<T>> subscriber) {
        final Listener.ResponseListener listener = new Listener.ResponseListener<T>() {
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
