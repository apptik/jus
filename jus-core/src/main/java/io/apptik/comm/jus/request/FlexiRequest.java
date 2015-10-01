package io.apptik.comm.jus.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.ParseError;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.error.AuthFailureError;
import io.apptik.comm.jus.toolbox.HttpHeaderParser;

public abstract class FlexiRequest<F, T> extends Request<T>{

    private final F requestData;
    private final Converter.Factory<F, T> converter;
    private final NetworkRequest networkRequest;


    public FlexiRequest(int method, String url, Response.ErrorListener listener,
                        Converter.Factory<F, T> converter, F requestData) {
        super(method, url, listener);
        this.requestData = requestData;
        this.converter = converter;
        if(requestData == null) {
            networkRequest = null;
        } else {
            try {
                networkRequest = converter.toRequestBody(requestData.getClass(), null)
                        .convert(requestData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     *
     * @param method
     * @param url
     * @param listener
     */
    public FlexiRequest(int method, String url, Response.ErrorListener listener,
                        Converter.Factory<F, T> converter) {
        this(method, url, listener, converter, null);
    }

    @Override
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(converter.fromResponseBody(getResponseType(), null)
                            .convert(response),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(networkRequest == null) {
            return null;
        }
        return networkRequest.data;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(networkRequest == null) {
            return new HashMap<>();
        }
        return networkRequest.headers.toMap();
    }

    @Override
    public String getBodyContentType() {
        if(networkRequest == null) {
            return null;
        }
        return networkRequest.contentType.toString();
    }

    protected abstract Class<T> getResponseType();
}
