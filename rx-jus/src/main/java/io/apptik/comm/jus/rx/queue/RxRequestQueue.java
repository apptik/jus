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

public final class RxRequestQueue {

    private RxRequestQueue() {}

    public static <T> Observable<JusEvent> allEventsObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.merge(
                resultObservable(queue, filter),
                errorObservable(queue, filter),
                markerObservable(queue, filter));
    }

    public static <T> Observable<ResultEvent<T>> resultObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestResponseOnSubscribe(queue, filter));
    }

    public static Observable<ErrorEvent> errorObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestErrorOnSubscribe(queue, filter));
    }

    public static Observable<MarkerEvent> markerObservable(
            RequestQueue queue, RequestQueue.RequestFilter filter) {
        return Observable.create(new QRequestMarkerOnSubscribe(queue, filter));
    }
}
