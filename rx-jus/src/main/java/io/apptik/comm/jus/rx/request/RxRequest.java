package io.apptik.comm.jus.rx.request;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.JusEmitter;
import io.apptik.comm.jus.rx.RequestEvent;

import rx.subjects.BehaviorSubject;

public abstract class RxRequest<T> extends Request<T> {


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
                .emitRequestEvent(new RequestEvent("deliver response", this, response));
        requestSubject.onNext(new RequestEvent("deliver response", this, response));
    }

    @Override
    public void addMarker(String tag) {
        JusEmitter.get()
                .emitRequestEvent(new RequestEvent(tag, this, null));
        requestSubject.onNext(new RequestEvent(tag, this, null));
        super.addMarker(tag);

    }

    @Override
    public void deliverError(JusError error) {
        JusEmitter.get()
                .emitRequestError(new RequestEvent("error", this, error));
        requestSubject.onError(error);
    }

    @Override
    public void finish(String tag) {
        super.finish(tag);
        requestSubject.onCompleted();
    }

}
