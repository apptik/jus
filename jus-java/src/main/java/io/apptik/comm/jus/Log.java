package io.apptik.comm.jus;

/**
 * Created by sic on 20/10/15.
 */
public interface Log {
    /**
     * Priority constant for the println method; use Log.v.
     */
    int VERBOSE = 2;
    /**
     * Priority constant for the println method; use Log.d.
     */
    int DEBUG = 3;
    /**
     * Priority constant for the println method; use Log.w.
     */
    int WARN = 5;
    /**
     * Priority constant for the println method; use Log.e.
     */
    int ERROR = 6;

    int v(String tag, String msg);

    int v(String tag, String msg, Throwable tr);

    int d(String tag, String msg);

    int d(String tag, String msg, Throwable tr);

    int w(String tag, String msg);

    int w(String tag, String msg, Throwable tr);

    boolean isLoggable(String tag, int level);

    /*
         * Send a {@link #WARN} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param tr An exception to log
         */
    int w(String tag, Throwable tr);

    int e(String tag, String msg);

    int e(String tag, String msg, Throwable tr);
}
