package io.apptik.comm.jus.rx;


import java.util.AbstractMap;
import java.util.Map;

import io.apptik.comm.jus.Marker;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.rx.request.RxRequest;
import io.apptik.rhub.AbstractRxJava1Hub;
import io.apptik.rhub.RxJava1ProxyType;
import rx.Observable;
import rx.functions.Func1;

/**
 * More specific RequestQueue related RxHub.
 * It grabs all requests and sorts them in Subjects by their Tag if set
 */
public final class RxQueueHub extends AbstractRxJava1Hub {

    public RxQueueHub(RequestQueue queue) {
        queue.addListenerFactory(
                new RequestListener.SimpleListenerFactory() {
                    @Override
                    public RequestListener.MarkerListener getMarkerListener
                            (Request<?> request) {
                        Object rTag = request.getTag();
                        if (rTag != null) {
                            RxQueueHub.this.addUpstream(rTag, RxRequest
                                    .allEventsObservable(request));
                        }
                        return null;
                    }
                }
        );
    }

    public Observable<MarkerEvent> markerEvents(Object tag) {
        return getPub(tag, MarkerEvent.class);
    }

    public Observable<Map.Entry<Marker, Object[]>> markers(Object tag) {
        return markerEvents(tag).map(new Func1<MarkerEvent, Map.Entry<Marker, Object[]>>() {
            @Override
            public Map.Entry<Marker, Object[]> call(MarkerEvent markerEvent) {
                return new AbstractMap.SimpleImmutableEntry<>(markerEvent.marker, markerEvent.args);
            }
        });
    }

    public Observable<ResultEvent> resultEvents(Object tag) {
        return getPub(tag, ResultEvent.class);
    }

    public Observable<?> results(Object tag) {
        return resultEvents(tag).compose(justResponse());
    }

    public Observable<?> finalResults(Object tag) {
        return resultEvents(tag).filter(new Func1<ResultEvent, Boolean>() {
            @Override
            public Boolean call(ResultEvent resultEvent) {
                return !resultEvent.request.getRawResponse().intermediate;
            }
        }).compose(justResponse());
    }

    public Observable<?> intermediateResults(Object tag) {
        return resultEvents(tag).filter(new Func1<ResultEvent, Boolean>() {
            @Override
            public Boolean call(ResultEvent resultEvent) {
                return resultEvent.request.getRawResponse().intermediate;
            }
        }).compose(justResponse());
    }

    public Observable<ErrorEvent> errorEvents(Object tag) {
        return getPub(tag, ErrorEvent.class);
    }

    public Observable<JusError> errors(Object tag) {
        return errorEvents(tag).map(new Func1<ErrorEvent, JusError>() {
            @Override
            public JusError call(ErrorEvent errorEvent) {
                return errorEvent.error;
            }
        });
    }

    private Observable.Transformer<ResultEvent, Object> justResponse() {
        return new Observable.Transformer<ResultEvent, Object>() {
            @Override
            public Observable<Object> call(Observable<ResultEvent> observable) {
                return observable.map(new Func1<ResultEvent, Object>() {
                    @Override
                    public Object call(ResultEvent resultEvent) {
                        return resultEvent.response;
                    }
                });
            }
        };
    }

    @Override
    public RxJava1ProxyType getProxyType(Object tag) {
        return RxJava1ProxyType.SerializedBehaviorRelayProxy;

    }

    @Override
    public boolean canTriggerEmit(Object tag) {
        return true;
    }
}
