package io.apptik.comm.jus.rx.event;


import io.apptik.comm.jus.Request;

public abstract class JusEvent<T> {
    //the request
    public final Request<T> request;

    public JusEvent(Request request) {
        this.request = request;
    }
}
