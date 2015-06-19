package org.djodjo.comm.jus.rx.request;


import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.error.JusError;
import org.djodjo.comm.jus.rx.JusEmitter;
import org.djodjo.comm.jus.rx.RequestEvent;

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
                .emitRequestEvent(new RequestEvent("response", this, response));
        requestSubject.onNext(new RequestEvent("deliver response", this, response));
        requestSubject.onCompleted();
    }

    @Override
    public void addMarker(String tag) {
        JusEmitter.get()
                .emitRequestEvent(new RequestEvent("response", this, null));
        requestSubject.onNext(new RequestEvent("response", this, null));
        super.addMarker(tag);

    }

    @Override
    public void deliverError(JusError error) {
        JusEmitter.get()
                .emitRequestError(new RequestEvent("response", this, error));
        requestSubject.onError(error);
        super.deliverError(error);
    }

    @Override
    public void finish(String tag) {
        super.finish(tag);
        requestSubject.onCompleted();
    }

}
