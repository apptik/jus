package org.djodjo.comm.jus.rx;


import org.djodjo.comm.jus.Request;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public class RequestEvent {

    public final String message;
    public final Request request;
    public final Object response;

    public RequestEvent(String message, Request request, Object response) {
        this.message = message;
        this.request = request;
        this.response = response;
    }
}
