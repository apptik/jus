package io.apptik.comm.jus.apachehttp;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.apptik.comm.jus.NetworkDispatcher;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.AbstractHttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;

public class ApacheHttpStack extends AbstractHttpStack {
    protected final HttpClient client;
    protected final HttpContext httpContext;
    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    public ApacheHttpStack() {
        this(HttpClients.createDefault());
    }

    public ApacheHttpStack(HttpClient client) {
        this(client, null);
    }

    public ApacheHttpStack(HttpClient client, HttpContext httpContext) {
        if (client == null) {
            throw new NullPointerException("Client must not be null.");
        }
        this.client = client;
        if (httpContext == null) {
            this.httpContext = new HttpClientContext();
        } else {
            this.httpContext = httpContext;
        }


    }


    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request, Headers additionalHeaders,
                                          ByteArrayPool byteArrayPool) throws IOException {
        HttpRequestBase httpRequest = createHttpRequest(request, additionalHeaders.toMap());
        addHeaders(httpRequest, additionalHeaders.toMap());
        if (request.getHeaders() != null) {
            addHeaders(httpRequest, request.getHeaders().toMap());
        }
        RequestConfig requestConfig = org.apache.http.client.config.RequestConfig.custom()
                .setSocketTimeout(request.getRetryPolicy().getCurrentReadTimeout())
                .setConnectTimeout(request.getRetryPolicy().getCurrentConnectTimeout())
                .build();
        httpRequest.setConfig(requestConfig);
        onPrepareRequest(httpRequest);
        long requestStart = System.nanoTime();
        HttpResponse response = client.execute(httpRequest, httpContext);
        byte[] data = null;
        if (NetworkDispatcher.hasResponseBody(request.getMethod(), response.getStatusLine()
                .getStatusCode())) {
            data = getContentBytes(response.getEntity().getContent(),
                    byteArrayPool, (int) response.getEntity().getContentLength());

        } else {
            // Add 0 byte response as a way of honestly representing a
            // no-content request.
            data = new byte[0];
        }
        return new NetworkResponse.Builder()
                .setHeaders(JusApache.jusHeaders(response.getAllHeaders()))
                .setStatusCode(response.getStatusLine().getStatusCode())
                .setBody(data)
                .setNetworkTimeNs(System.nanoTime() - requestStart)
                .build();
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    static HttpRequestBase createHttpRequest(
            Request<?> request, Map<String, String> additionalHeaders) {
        switch (request.getMethod()) {
            case Request.Method.GET:
                return new HttpGet(request.getUrlString());
            case Request.Method.DELETE:
                return new HttpDelete(request.getUrlString());
            case Request.Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrlString());
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Request.Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrlString());
                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            case Request.Method.HEAD:
                return new HttpHead(request.getUrlString());
            case Request.Method.OPTIONS:
                return new HttpOptions(request.getUrlString());
            case Request.Method.TRACE:
                return new HttpTrace(request.getUrlString());
            case Request.Method.PATCH: {
                HttpPatch patchRequest = new HttpPatch(request.getUrlString());
                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest,
                                                Request<?> request) {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     * <p/>
     * <p>Overwrite in subclasses to augment the request.</p>
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }

    /**
     * The HttpPatch class does not exist in the Android framework, so this has been defined here.
     */
    public static final class HttpPatch extends HttpEntityEnclosingRequestBase {
        public final static String METHOD_NAME = "PATCH";

        public HttpPatch() {
            super();
        }

        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }

        /**
         * @throws IllegalArgumentException if the uri is invalid.
         */
        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

}
