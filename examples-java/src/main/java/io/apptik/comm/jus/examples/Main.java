package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.http.HttpUrl;

public class Main {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if(args!=null) {
            Collections.addAll(opts, args);
        }

        if(opts.contains("beer")) {
//            queue.add(Requests.getBeerRequest("230", new Listener.ResponseListener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            System.out.println(response);
//                        }
//                    },
//                    new Listener.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(JusError error) {
//                            System.out.println(error.networkResponse);
//                        }
//                    }
//            ));
            queue.add(new Request<Void, String>(
                            Request.Method.GET,
                            HttpUrl.parse("http://beermapping.com/webservice/" +
                                    "locquery/" +
                                    "c6266a50b6603fe87d681ef34fe11e3e/" +
                                    "230"),
                            new Converters.StringResponseConverter())

                            .setResponseListener(new Listener.ResponseListener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    System.out.println("RESPONSE: " + response);
                                }
                            })
                            .setErrorListener(new Listener.ErrorListener() {
                                @Override
                                public void onErrorResponse(JusError error) {
                                    System.out.println("ERROR: " + error.networkResponse);
                                }
                            })
            );
        }

        queue.stopWhenDone();
    }
}
