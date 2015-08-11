package org.djodjo.comm.jus.rx;


import org.djodjo.comm.jus.Request;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public class RequestEvent {

    //any message normally a marker description
    public final String message;
    //the request
    public final Request request;
    //actual response pojo or wahtever was requested or an Error
    //if JusError then it normally should contain the network response also
    public final Object response;

    public RequestEvent(String message, Request request, Object response) {
        this.message = message;
        this.request = request;
        this.response = response;
    }
}
