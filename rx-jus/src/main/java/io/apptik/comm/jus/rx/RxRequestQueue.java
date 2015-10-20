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


    public BehaviorSubject<JusEvent> getAllEventSubject() {
        return allEventSubject;
    }

    @Override
    public <T> Request<T> add(Request<T> request) {
       Observable<JusEvent> jusEventObservable = RxRequest.allEventsObservable(request);
        jusEventObservable.subscribe(allEventSubject);
        return super.add(request);
    }
}
