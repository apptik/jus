package io.apptik.comm.jus.mock;

import java.io.IOException;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.HurlStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;


public class CustomHttpStack extends HurlStack {


    public static final String MY_CUSTOM_MARKER = "my-custom-marker";
    @Override
    public NetworkResponse performRequest(Request<?> request, Headers additionalHeaders, ByteArrayPool byteArrayPool) throws IOException {
        request.addMarker(MY_CUSTOM_MARKER);
        return super.performRequest(request, additionalHeaders, byteArrayPool);
    }
}
