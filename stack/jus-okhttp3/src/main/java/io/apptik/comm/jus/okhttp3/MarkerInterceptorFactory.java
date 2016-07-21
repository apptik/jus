package io.apptik.comm.jus.okhttp3;


import io.apptik.comm.jus.Request;

public interface MarkerInterceptorFactory {

    AbstractMarkerInterceptor create(Request request);

    class DefaultMIF implements MarkerInterceptorFactory {

        @Override
        public AbstractMarkerInterceptor create(Request request) {
            return new MarkerInterceptor(request);
        }
    }
}
