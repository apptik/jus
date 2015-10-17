package io.apptik.comm.jus.rx.request;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.Observable;

public final class RxRequest{

    public static <T> Observable<JusEvent> allEventsObservable(Request<T> request) {
        return Observable.merge(
                resultObservable(request),
                errorObservable(request),
                markerObservable(request));
    }

    public static <T> Observable<ResultEvent<T>> resultObservable(Request<T> request) {
        return Observable.create(new RequestResultOnSubscribe(request));
    }

    public static Observable<ErrorEvent> errorObservable(Request request) {
        return Observable.create(new RequestErrorOnSubscribe(request));
    }

    public static Observable<MarkerEvent> markerObservable(Request request) {
        return Observable.create(new RequestMarkerOnSubscribe(request));
    }
}
