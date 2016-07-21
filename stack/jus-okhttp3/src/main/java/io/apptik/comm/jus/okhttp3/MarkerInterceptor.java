package io.apptik.comm.jus.okhttp3;


import io.apptik.comm.jus.Request;

public class MarkerInterceptor extends AbstractMarkerInterceptor {


    public MarkerInterceptor(Request<?> request) {
        super(request);
    }

    @Override
    Object[] getMarkerArgs(Request<?> request, okhttp3.Request okhttpRequest) {
        return new Object[]{okhttpRequest, okhttpRequest.headers()};
    }
}
