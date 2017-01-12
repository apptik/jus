package io.apptik.comm.jus.perf.mock;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;
import org.apache.http.HttpResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MockVolleyHttpStack implements HttpStack {
    private HttpResponse mResponseToReturn;
    private IOException mExceptionToThrow;
    private String mLastUrl;
    private Map<String, String> mLastHeaders;
    private byte[] mLastPostBody;
    public String getLastUrl() {
        return mLastUrl;
    }
    public Map<String, String> getLastHeaders() {
        return mLastHeaders;
    }
    public byte[] getLastPostBody() {
        return mLastPostBody;
    }
    public void setResponseToReturn(HttpResponse response) {
        mResponseToReturn = response;
    }
    public void setExceptionToThrow(IOException exception) {
        mExceptionToThrow = exception;
    }
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        if (mExceptionToThrow != null) {
            throw mExceptionToThrow;
        }
        mLastUrl = request.getUrl();
        mLastHeaders = new HashMap<String, String>();
        if (request.getHeaders() != null) {
            mLastHeaders.putAll(request.getHeaders());
        }
        if (additionalHeaders != null) {
            mLastHeaders.putAll(additionalHeaders);
        }
        try {
            mLastPostBody = request.getBody();
        } catch (AuthFailureError e) {
            mLastPostBody = null;
        }
        return mResponseToReturn;
    }
}