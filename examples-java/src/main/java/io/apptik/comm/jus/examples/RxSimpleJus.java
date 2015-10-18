package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.comm.jus.rx.request.RxRequest;
import rx.Observer;

public class RxSimpleJus {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if (args != null) {
            Collections.addAll(opts, args);
        }

        if (opts.contains("beer")) {
           Request<String> request = new Request<String>(
                            Request.Method.GET,
                            HttpUrl.parse(BeerService.fullUrl),
                            new Converters.StringResponseConverter());

            RxRequest.allEventsObservable(request).subscribe(
                    new Observer<JusEvent>() {
                        @Override
                        public void onCompleted() {
                            System.out.println("allRX: Completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.println("allRX: Error: " + e);
                        }

                        @Override
                        public void onNext(JusEvent jusEvent) {
                            System.out.println("allRX: Event: " + jusEvent);
                        }
                    }
            );

            RxRequest.resultObservable(request).subscribe(
                    new Observer<ResultEvent<String>>() {
                        @Override
                        public void onCompleted() {
                            System.out.println("requestRX: Completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.println("requestRX: Error: " + e);
                        }

                        @Override
                        public void onNext(ResultEvent<String> stringResultEvent) {
                            System.out.println("responseRX: Response: " + stringResultEvent.response);
                        }
                    }
            );

            queue.add(request);
        }

        queue.stopWhenDone();
    }
}
