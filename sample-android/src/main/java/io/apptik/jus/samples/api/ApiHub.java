package io.apptik.jus.samples.api;

import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.rx.queue.RxRequestQueue;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

import static io.apptik.jus.samples.api.Instructables.REQ_LIST;

public class ApiHub {

    private Map<String, Subject> observableMap = new HashMap<>();

    public ApiHub(RequestQueue queue) {
        this.put(REQ_LIST, RxRequestQueue
                .resultObservable(queue, request -> REQ_LIST.equals(request.getTag())));
    }

    public void put(String tag, Observable<ResultEvent<?>> observable) {
        Subject s = BehaviorSubject.create();
        observable.subscribe(s);
        observableMap.put(tag, s);
    }

    public Observable get(String tag) {
        return observableMap.get(tag);
    }

}
