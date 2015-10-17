package io.apptik.comm.jus.rx.request;

import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.BaseSubscription;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import rx.Observable;
import rx.Subscriber;

public class RequestErrorOnSubscribe implements Observable.OnSubscribe<ErrorEvent> {
    private final Request request;

    public RequestErrorOnSubscribe(Request request) {
        this.request = request;
    }

    @Override
    public void call(final Subscriber<? super ErrorEvent> subscriber) {


        final Listener.ErrorListener listener = new Listener.ErrorListener() {
            @Override
            public void onErrorResponse(JusError error) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(new ErrorEvent(request, error));
                }
            }
        };
        request.addErrorListener(listener);

        subscriber.add(new BaseSubscription() {
            @Override
            protected void doUnsubscribe() {
                request.removeErrorListener(listener);
            }
        });
    }
}
