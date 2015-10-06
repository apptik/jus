package io.apptik.comm.jus;


import io.apptik.comm.jus.error.JusError;

public class Listener {
    /** Callback interface for delivering parsed responses. */
    public interface ResponseListener<T> {
        /** Called when a response is received. */
        void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        void onErrorResponse(JusError error);
    }

    /** Callback interface for delivering markers. */
    public interface MarkerListener {
        /** Called when a marker is set. */
        void onMarker(JusLog.MarkerLog.Marker marker, Object... args);
    }


}
