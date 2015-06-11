package org.djodjo.comm.jus.rx;


import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.Response;

public class JusSignal {

    public final String message;
    public final Request request;
    public final Response response;

    public JusSignal(String message, Request request, Response response) {
        this.message = message;
        this.request = request;
        this.response = response;
    }
}
