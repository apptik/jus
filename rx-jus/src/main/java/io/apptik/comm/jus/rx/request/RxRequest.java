package io.apptik.comm.jus.rx.request;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.JusEmitter;
import io.apptik.comm.jus.rx.RequestEvent;

import rx.subjects.BehaviorSubject;

public abstract class RxRequest<F,T> extends Request<F,T> {


    public static final String EVENT_DELIVER_RESPONSE = "deliver response";
    public static final String EVENT_DELIVER_ERROR = "deliver error";
    private BehaviorSubject<RequestEvent> requestSubject = BehaviorSubject.create();

    public RxRequest(int method, String url) {
        super(method, url, null);
    }

    public BehaviorSubject<RequestEvent> getRequestSubject() {
        return requestSubject;
    }


    @Override
    protected void deliverResponse(T response) {
        JusEmitter.get()
                .emitRequestEvent(new RequestEvent(EVENT_DELIVER_RESPONSE, this, response));
        requestSubject.onNext(new RequestEvent(EVENT_DELIVER_RESPONSE, this, response));
    }

    @Override
    public RxRequest<F,T> addMarker(String tag, String... args) {
        JusEmitter.get()
                .emitRequestEvent(new RequestEvent(tag, this, null));
        requestSubject.onNext(new RequestEvent(tag, this, null));
        super.addMarker(tag);
        return this;
    }

    @Override
    public void deliverError(JusError error) {
        JusEmitter.get()
                .emitRequestError(new RequestEvent(EVENT_DELIVER_ERROR, this, error));
        requestSubject.onError(error);
    }

    @Override
    public void finish(String tag) {
        super.finish(tag);
        requestSubject.onCompleted();
    }

}
