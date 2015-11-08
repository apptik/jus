package io.apptik.comm.jus.okhttp;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.util.Map;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.http.Headers;

public class JusOk {

    private JusOk() {
    }

    public static Headers jusHeaders(com.squareup.okhttp.Headers okHeaders) {
        return new Headers.Builder().addMMap(okHeaders.toMultimap()).build();
    }

    public static com.squareup.okhttp.Headers okHeaders(Headers jusHeaders, Map<String, String>
            additionalHeaders) {
        if(jusHeaders==null) {
            jusHeaders = new Headers.Builder().build();
        }
        Map<String, String> headers = jusHeaders.toMap();
        headers.putAll(additionalHeaders);
        return com.squareup.okhttp.Headers.of(headers);
    }

    public static RequestBody okBody(NetworkRequest request) {
        if(request==null)
            return null;
        return RequestBody.create(MediaType.parse(request.contentType.toString()), request.data);
    }
}
