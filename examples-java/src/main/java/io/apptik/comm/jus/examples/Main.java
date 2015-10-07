package io.apptik.comm.jus.examples;

import java.io.File;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;

public class Main {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        queue.add(Requests.getBeerRequest("230", new Listener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                },
                new Listener.ErrorListener() {
                    @Override
                    public void onErrorResponse(JusError error) {
                        System.out.println(error.networkResponse);
                    }
                }
        ));
    }
}
