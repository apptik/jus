package io.apptik.comm.jus.rx.event;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.JusError;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public final class ErrorEvent extends JusEvent {

    //error
    public final JusError error;

    public ErrorEvent(Request request, JusError error) {
        super(request);
        this.error = error;
    }

    @Override
    public String toString() {
        return "ErrorEvent{" +
                "request=" + request +
                ", error=" + error +
                "} ";
    }
}
