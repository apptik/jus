package io.apptik.comm.jus.perf.mock;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

public class MockVolleyRequest extends Request<com.android.volley.NetworkResponse> {

   private com.android.volley.Response.Listener<com.android.volley.NetworkResponse> listener;

    public MockVolleyRequest(String url, String method) {
        super(getMethod(method), url, null);
    }

    @Override
    protected Response<com.android.volley.NetworkResponse>
    parseNetworkResponse(com.android.volley.NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(com.android.volley.NetworkResponse response) {
        listener.onResponse(response);
    }

    public void setListener(Response.Listener<NetworkResponse> listener) {
        this.listener = listener;
    }

    private static int getMethod(String method) {

        switch (method) {
            case "GET":return Method.GET;
            case "POST": return Method.POST;
            case "PUT": return Method.PUT;
            case "DELETE": return Method.DELETE;
        }

        throw new RuntimeException("WTF!");
    }

}
