package io.apptik.comm.jus.okhttp;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.util.Collections;
import java.util.Map;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.http.Headers;

public class JusOk {

    private JusOk() {
    }

    public static Headers jusHeaders(com.squareup.okhttp.Headers okHeaders) {
        return new Headers.Builder().addMMap(okHeaders.toMultimap()).build();
    }

    public static com.squareup.okhttp.Headers okHeaders(Headers jusHeaders, Headers
            additionalHeaders) {

        Map<String, String> headers;
        if (jusHeaders == null) {
            headers = Collections.emptyMap();
        } else {
            //check if we have content type in Jus headers ad remove it if yes
            headers = jusHeaders.toMap();
            headers.remove("Content-Type");
        }

        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders.toMap());
        }
        return com.squareup.okhttp.Headers.of(headers);
    }

    public static RequestBody okBody(NetworkRequest request) {
        if (request == null || (request.contentType == null && request.data == null))
            return null;
        MediaType mediaType = null;
        if (request.contentType != null) {
            mediaType = MediaType.parse(request.contentType.toString());
        }
        return RequestBody.create(mediaType, request.data);
    }
}
