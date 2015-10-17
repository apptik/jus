package io.apptik.comm.jus.rx.event;


import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.Request;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public final class MarkerEvent extends JusEvent {

    //actual response
    public final JusLog.MarkerLog.Marker marker;

    //additional args
    public final Object[] args;

    public MarkerEvent(Request request, JusLog.MarkerLog.Marker marker, Object... args) {
        super(request);this.marker = marker;
        this.args = args;
    }
}
