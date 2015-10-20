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

package io.apptik.comm.jus.rx.request;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.rx.event.ErrorEvent;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.MarkerEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.Observable;

public final class RxRequest{

    public static <T> Observable<JusEvent> allEventsObservable(Request<T> request) {
        return Observable.merge(
                resultObservable(request),
                errorObservable(request),
                markerObservable(request));
    }

    public static <T> Observable<ResultEvent<T>> resultObservable(Request<T> request) {
        return Observable.create(new RequestResultOnSubscribe(request));
    }

    public static Observable<ErrorEvent> errorObservable(Request request) {
        return Observable.create(new RequestErrorOnSubscribe(request));
    }

    public static Observable<MarkerEvent> markerObservable(Request request) {
        return Observable.create(new RequestMarkerOnSubscribe(request));
    }
}
