package io.apptik.comm.jus.rx.request;

import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.rx.BaseSubscription;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import rx.Observable;
import rx.Subscriber;

public class RequestMarkerOnSubscribe implements Observable.OnSubscribe<MarkerEvent> {
    private final Request request;

    public RequestMarkerOnSubscribe(Request request) {
        this.request = request;
    }

    @Override
    public void call(final Subscriber<? super MarkerEvent> subscriber) {
        final Listener.MarkerListener listener = new Listener.MarkerListener() {
            @Override
            public void onMarker(JusLog.MarkerLog.Marker marker, Object... args) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(new MarkerEvent(request, marker, args));
                }
            }
        };
        request.addMarkerListener(listener);

        subscriber.add(new BaseSubscription() {
            @Override
            protected void doUnsubscribe() {
                request.removeMarkerListener(listener);
            }
        });
    }
}
