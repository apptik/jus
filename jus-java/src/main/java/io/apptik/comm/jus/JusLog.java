package io.apptik.comm.jus;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.apptik.comm.jus.error.JusError;

public class JusLog {

    public static Log log = new DefaultLog();
    private static boolean markerLogOn = false;
    private static boolean reponseLogOn = false;
    private static boolean errorLogOn = false;


    public static class ErrorLog extends RequestQueue.QErrorListener {
        public static void on() {
            errorLogOn = true;
        }

        public static boolean isOn() {
            return errorLogOn;
        }

        public static void off() {
            errorLogOn = false;
        }

        public ErrorLog(Request request) {
            super(request);
        }

        @Override
        public void onError(JusError error) {
            if (log == null) return;
            log.log(buildMessage(request, "Error: %s", error));
        }
    }

    public static class ResponseLog extends RequestQueue.QResponseListener {
        public static void on() {
            reponseLogOn = true;
        }

        public static void off() {
            reponseLogOn = false;
        }

        public static boolean isOn() {
            return reponseLogOn;
        }


        public ResponseLog(Request request) {
           super(request);
        }

        @Override
        public void onResponse(Object response) {
            if (log == null) return;
            log.log(buildMessage(request, "Response: %s", response));
        }
    }

    /**
     * A simple event log with records containing a name, threadId ID, and timestamp.
     */
    public static class MarkerLog extends RequestQueue.QMarkerListener {
        public static void on() {
            markerLogOn = true;
        }

        public static void off() {
            markerLogOn = false;
        }

        public static boolean isOn() {
            return markerLogOn;
        }

        /**
         * Minimum duration from first marker to last in an marker log to warrant logging.
         */
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        @Override
        public void onMarker(Marker marker, Object... args) {
            if (log == null) return;
            add(marker, args);
        }

        public static class Marker {
            public final String name;
            public final long threadId;
            public final String threadName;
            public final long time;

            public Marker(String name, long threadId, String threadName, long time) {
                this.name = name;
                this.threadId = threadId;
                this.threadName = threadName;
                this.time = time;
            }

            @Override
            public String toString() {
                return "Marker{" +
                        "name='" + name + '\'' +
                        ", threadId=" + threadId +
                        ", threadName='" + threadName + '\'' +
                        ", time=" + time +
                        '}';
            }
        }

        private final List<Marker> mMarkers = new ArrayList<Marker>();
        private volatile boolean mFinished = false;

        public MarkerLog(Request request) {
           super(request);
        }

        /**
         * Adds a marker to this log with the specified name.
         */
        public synchronized void add(Marker marker, Object... args) {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished request");
            }
            log.log(buildMessage(request, "%s\n\t\targs=%s", marker.toString(), Arrays.toString
                    (args)));
            mMarkers.add(marker);
            if (Request.EVENT_DONE.equals(marker.name)) {
                finish("[" + request.getMethod() + "]" + request.getUrlString());
            }
        }

        /**
         * Closes the log, dumping it to logcat if the time difference between
         * the first and last markers is greater than {@link #MIN_DURATION_FOR_LOGGING_MS}.
         *
         * @param header Header string to print above the marker log.
         */
        public synchronized void finish(String header) {
            mFinished = true;

            long duration = getTotalDuration();
            if (duration <= MIN_DURATION_FOR_LOGGING_MS) {
                return;
            }

            long prevTime = mMarkers.get(0).time;
            log.log(buildMessage(request, "(%-10d ns) %s", duration, header));
            for (Marker marker : mMarkers) {
                long thisTime = marker.time;
                log.log(String.format("(+%-10d) [%2d/%s] %s", (thisTime - prevTime)
                        , marker.threadId, marker
                        .threadName, marker.name));
                prevTime = thisTime;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            // Catch requests that have been collected (and hence end-of-lifed)
            // but had no debugging output printed for them.
            if (!mFinished) {
                finish("Request on the loose");
                log.log(buildMessage(request, "Marker log finalized without finish() - " +
                        "uncaught exit point for request"));
            }
        }

        /**
         * Returns the time difference between the first and last events in this log.
         */
        private long getTotalDuration() {
            if (mMarkers.size() == 0) {
                return 0;
            }

            long first = mMarkers.get(0).time;
            long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }


    private static String buildMessage(Request request, String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        return String.format(Locale.US, "[%d]: %s:\n\t\t%s",
                Thread.currentThread().getId(), request.toString(), msg);
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling threadId ID and method name.
     */
    private static String buildCallerMessage(String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of JusLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(JusLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s",
                Thread.currentThread().getId(), caller, msg);
    }

}
