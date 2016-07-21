package io.apptik.comm.jus.okhttp3;

import java.io.IOException;

import io.apptik.comm.jus.Request;
import okhttp3.Interceptor;
import okhttp3.Response;

public abstract class AbstractMarkerInterceptor implements Interceptor {
    public static final String OKHTTP3_INTERCEPT = "okhttp3-intercept";
    public static final String OKHTTP3_ERROR = "okhttp3-error";
    final Request<?> request;

    public AbstractMarkerInterceptor(Request<?> request) {
        this.request = request;
    }

    abstract Object[] getMarkerArgs(Request<?> request, okhttp3.Request okhttpRequest);

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Request okhttpRequest = chain.request();
        request.addMarker(OKHTTP3_INTERCEPT, getMarkerArgs(request, okhttpRequest));
        Response response;
        try {
            response = chain.proceed(okhttpRequest);
        } catch (Exception e) {
            request.addMarker(OKHTTP3_ERROR, e);
            throw e;
        }
        return response;
    }
}
