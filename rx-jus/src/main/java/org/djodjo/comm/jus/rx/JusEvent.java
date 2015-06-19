package org.djodjo.comm.jus.rx;


import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.Response;
import org.djodjo.comm.jus.error.JusError;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public class JusEvent {

    public final String message;
    public final Request request;
    public final Response response;
    public final JusError error;

    public JusEvent(String message, Request request, Response response, JusError error) {
        this.message = message;
        this.request = request;
        this.response = response;
        this.error = error;
    }
}
