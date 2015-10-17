package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.BasicConverterFactory;
import io.apptik.comm.jus.retro.RetroProxy;

import static java.lang.System.out;

public class RetroJus {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if(args!=null) {
            Collections.addAll(opts, args);
        }

        RetroProxy retroJus = new RetroProxy.Builder()
                .baseUrl(BeerService.baseUrl)
                .queue(queue)
                .addConverterFactory(new BasicConverterFactory())
                .build();

        BeerService beerService = retroJus.create(BeerService.class);

        if(opts.contains("beer")) {
            beerService.getBeer(BeerService.userString, "777")
                    .addResponseListener((r) -> out.println("RESPONSE: " + r))
                    .addErrorListener((e) -> out.println("ERROR: " + e.networkResponse));
        }

        queue.stopWhenDone();
    }
}
