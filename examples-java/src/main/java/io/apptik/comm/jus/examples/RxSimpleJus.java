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

import static java.lang.System.out;

public class RxSimpleJus {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
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

        queue.stopWhenDone();
    }
}
