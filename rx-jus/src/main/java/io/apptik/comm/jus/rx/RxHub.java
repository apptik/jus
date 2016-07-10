package io.apptik.comm.jus.rx;

import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.rx.event.JusEvent;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * General purpose Rx Hub where requests can be observed by Subjects and sorted by a filter
 * Then consumers can subscribe to those subject which are linked to a specific Tag
 * <p>
 * for example:
 * rxHub.put(REQ_TAG, RxRequestQueue
 * .resultObservable(queue, request -> REQ_TAG.equals(request.getTag())));
 * <p>
 * This Hub can be used for any other purpose.
 */
public class RxHub {

    private Map<Object, Subject> observableMap = new HashMap<>();
    private boolean threadsafe = false;

    public RxHub() {
    }

    public RxHub(boolean threadsafe) {
        this.threadsafe = threadsafe;
    }

    public void put(Object tag, Observable<JusEvent> observable) {
        Subject s;
        if (threadsafe) {
            s = new SerializedSubject(BehaviorSubject.create());
        } else {
            s = BehaviorSubject.create();
        }
        observable.subscribe(s);
        observableMap.put(tag, s);
    }

    public Subject get(Object tag) {
        return observableMap.get(tag);
    }

}
