package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.rx.RxJus;
import io.apptik.comm.jus.rx.RxRequestQueue;
import io.apptik.comm.jus.rx.event.JusEvent;
import rx.Observer;

import static java.lang.System.out;

public class RxFullJus {
    public static void main(String[] args) {
        RxRequestQueue queue = RxJus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if (args != null) {
            Collections.addAll(opts, args);
        }

        if (opts.contains("beer")) {
            queue.add(new Request<String>(
                            Request.Method.GET,
                            HttpUrl.parse(BeerService.fullUrl),
                            new Converters.StringResponseConverter())
                            .addResponseListener((r) -> out.println("RESPONSE: " + r))
                            .addErrorListener((e) -> out.println("ERROR: " + e))
            );
        }

        queue.getAllEventSubject().subscribe(new Observer<JusEvent>() {
            @Override
            public void onCompleted() {
                System.out.println("RX: Completed");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("RX: Error: " + e);
            }

            @Override
            public void onNext(JusEvent jusEvent) {
                System.out.println("RX: Event: " + jusEvent);
            }
        });

        queue.stopWhenDone();
    }
}
