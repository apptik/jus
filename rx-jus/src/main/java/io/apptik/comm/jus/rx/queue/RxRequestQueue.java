/*
 * Copyright (C) 2015 AppTik Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apptik.comm.jus.rx.queue;


import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.Observable;

/**
 * RxJava {@link RequestQueue} wrapper
 * <p/>
 * this essentially attaches respective filtered listeners to the queue and the depending on a
 * {@link io.apptik.comm.jus.RequestQueue.RequestFilter} hooks to the events coming from a
 * {@link io.apptik.comm.jus.Request}
 */
public final class RxRequestQueue {

    private RxRequestQueue() {
    }

    /**
     * Returns merged {@link Observable} of results, errors and markers all together.
     *
     * @param queue  the {@link RequestQueue} to listen to
     * @param filter the {@link io.apptik.comm.jus.RequestQueue.RequestFilter} which will filter
     *               the requests to hook to. Set null for no filtering.
     * @return {@link Observable} of results, errors and markers
     */
    public static Observable<JusEvent> allEventsObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.merge(
                resultObservable(queue, filter),
                errorObservable(queue, filter),
                markerObservable(queue, filter));
    }

    /**
     * Returns {@link Observable} of the successful results coming as {@link ResultEvent}
     *
     * @param queue  the {@link RequestQueue} to listen to
     * @param filter the {@link io.apptik.comm.jus.RequestQueue.RequestFilter} which will filter
     *               the requests to hook to. Set null for no filtering.
     * @return {@link Observable} of results
     */
    public static Observable<ResultEvent<?>> resultObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestResponseOnSubscribe(queue, filter));
    }

    /**
     * Returns {@link Observable} of error events coming as {@link ErrorEvent}
     *
     * @param queue  the {@link RequestQueue} to listen to
     * @param filter the {@link io.apptik.comm.jus.RequestQueue.RequestFilter} which will filter
     *               the requests to hook to. Set null for no filtering.
     * @return {@link Observable} of errors
     */
    public static Observable<ErrorEvent> errorObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestErrorOnSubscribe(queue, filter));
    }

    /**
     * Returns {@link Observable} of the markers coming as {@link MarkerEvent}
     *
     * @param queue  the {@link RequestQueue} to listen to
     * @param filter the {@link io.apptik.comm.jus.RequestQueue.RequestFilter} which will filter
     *               the requests to hook to. Set null for no filtering.
     * @return {@link Observable} of markers
     */
    public static Observable<MarkerEvent> markerObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestMarkerOnSubscribe(queue, filter));
    }
}
