package io.apptik.comm.jus.rx;


import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.Network;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.ResponseDelivery;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.request.RxRequest;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class RxRequestQueue extends RequestQueue{


    private BehaviorSubject<JusEvent> allEventSubject = BehaviorSubject.create();

    public RxRequestQueue(Cache cache, Network network) {
        super(cache, network);
    }

    public RxRequestQueue(Cache cache, Network network, int threadPoolSize) {
        super(cache, network, threadPoolSize);
    }

    public RxRequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        super(cache, network, threadPoolSize, delivery);
    }

    @Override
    public <T> Request<T> add(Request<T> request) {
       Observable<JusEvent> jusEventObservable = RxRequest.allEventsObservable(request);
        jusEventObservable.subscribe(allEventSubject);
        return super.add(request);
    }
}
