package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.request.StringRequest;

public class CustomJus {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if (args != null) {
            Collections.addAll(opts, args);
        }

        if (opts.contains("beer")) {
            queue.add(getBeerRequest("230",
                    response -> System.out.println(response),
                    error -> System.out.println(error.networkResponse)
            ));
        }

        queue.stopWhenDone();
    }

    private static Request<Void, String> getBeerRequest(String q, Listener.ResponseListener<String> listener,
                                                 Listener.ErrorListener errorListener) {
        Request<Void, String> res;

        final String userString = "c6266a50b6603fe87d681ef34fe11e3e";
        final String baseUrl = "http://beermapping.com/webservice/";
        //get general info
        final String locquery = "locquery";
        //get lat/lon
        final String locmap = "locmap";
        //get ratings
        final String locscore = "locscore";
        //get pics
        final String locimage = "locimage";
        res = new StringRequest(baseUrl + locquery
                + "/" + userString + "/" + q, listener, errorListener);
        return res;
    }
}
