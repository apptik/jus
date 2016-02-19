package io.apptik.comm.jus;


import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.NoConnectionError;

public interface NoConnectionPolicy {

    /**
     * This is called whenever no connection is available provided from the
     * {@link ConnectivityManager}. Implementation may also decide to re-queue a clone of this
     * request or wait until connection is available then return null in order to continue execution
     *
     * @param request that needs to be executed
     * @return the Error to throw or null to continue and try to exec the request anyway
     */
    JusError throwOnNoConnection(Request request);

    abstract class Factory {
        public NoConnectionPolicy get(Request request) {
            return new DefaultNoConnectionPolicy();
        }
    }

    class DefaultNoConnectionPolicy implements NoConnectionPolicy {
        @Override
        public JusError throwOnNoConnection(Request request) {
            return new NoConnectionError("No connection available");
        }
    }

    class IgnoreNoConnectionPolicy implements NoConnectionPolicy {
        @Override
        public JusError throwOnNoConnection(Request request) {
            return null;
        }
    }

}
