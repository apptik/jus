package io.apptik.comm.jus.rx;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.rx.request.RxRequest;
import io.apptik.rxhub.AbstractRxHub;
import rx.Observable;
import rx.functions.Func1;


/**
 * More specific RequestQueue related RxHub.
 * It grabs all requests and sorts them in Subjects by their Tag if set
 */
public class RxQueueHub extends AbstractRxHub {

    public RxQueueHub(RequestQueue queue) {
        queue.addListenerFactory(
                new RequestListener.SimpleListenerFactory() {
                    @Override
                    public RequestListener.MarkerListener getMarkerListener
                            (Request<?> request) {
                        Object rTag = request.getTag();
                        if (rTag != null) {
                            RxQueueHub.this.addProvider(rTag, RxRequest
                                    .allEventsObservable(request));
                        }
                        return null;
                    }
                }
        );
    }

    public Observable getMarkers(Object tag) {
        return super.getNode(tag).filter(new Func1<JusEvent, Boolean>() {
            @Override
            public Boolean call(JusEvent jusEvent) {
                return jusEvent instanceof MarkerEvent;
            }
        });
    }

    public Observable getResults(Object tag) {
        return super.getNode(tag).filter(new Func1<JusEvent, Boolean>() {
            @Override
            public Boolean call(JusEvent jusEvent) {
                return jusEvent instanceof ResultEvent;
            }
        });
    }

    public Observable getErrors(Object tag) {
        return super.getNode(tag).filter(new Func1<JusEvent, Boolean>() {
            @Override
            public Boolean call(JusEvent jusEvent) {
                return jusEvent instanceof ErrorEvent;
            }
        });
    }

    @Override
    public NodeType getNodeType(Object tag) {
        return NodeType.BehaviorRelay;
    }

    @Override
    public boolean isNodeThreadsafe(Object tag) {
        return true;
    }
}
