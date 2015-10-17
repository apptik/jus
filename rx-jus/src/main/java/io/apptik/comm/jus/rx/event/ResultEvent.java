package io.apptik.comm.jus.rx.event;


import io.apptik.comm.jus.Request;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public final class ResultEvent<T> extends JusEvent<T> {

    //actual response
    public final T response;

    public ResultEvent(Request request, T response) {
        super(request);
        this.response = response;
    }
}
