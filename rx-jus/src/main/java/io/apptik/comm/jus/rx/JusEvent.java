package io.apptik.comm.jus.rx;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.error.JusError;

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
