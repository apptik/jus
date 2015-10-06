package io.apptik.comm.jus.request;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.error.AuthFailureError;

public class TokenRequest extends StringRequest {

    protected final String key;
    protected final String secret;

    TokenRequest(String method, String url, String key, String secret, Listener.ResponseListener<String> listener, Listener.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.key = key;
        this.secret = secret;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "client_credentials");
        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        String auth = "Basic "
                + Base64.encodeToString((key + ":" + secret).getBytes(),
                Base64.NO_WRAP);
        headers.put("Authorization", auth);
        return headers;
    }
}
