package io.apptik.comm.jus.rx;

import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.rx.event.JusEvent;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * General purpose Rx Hub where requests can be observed by Subjects and sorted by a filter
 * Then consumers can subscribe to those subject which are linked to a specific Tag
 * <p>
 * for example:
 * rxHub.put(REQ_TAG, RxRequestQueue
 * .resultObservable(queue, request -> REQ_TAG.equals(request.getTag())));
 *
 * This Hub can be used for any other purpose.
 *
 */
public class RxHub {

    private Map<Object, Subject> observableMap = new HashMap<>();

    public void put(Object tag, Observable<JusEvent> observable) {
        Subject s = BehaviorSubject.create();
        observable.subscribe(s);
        observableMap.put(tag, s);
    }

    public Subject get(Object tag) {
        return observableMap.get(tag);
    }

}
