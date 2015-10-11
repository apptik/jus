/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2014 Kalin Maldzhanski
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apptik.comm.jus.stack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Request.Method;
import io.apptik.comm.jus.error.AuthFailureError;
import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.apptik.comm.jus.toolbox.PoolingByteArrayOutputStream;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        public String rewriteUrl(String originalUrl);
    }

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public HurlStack() {
        this(null);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    /**
     * @param urlRewriter      Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public NetworkResponse performRequest(Request<?, ?> request, Map<String, String> additionalHeaders, ByteArrayPool byteArrayPool)
            throws IOException, AuthFailureError {
        String url = request.getUrlString();
        HashMap<String, String> requestHeaders = new HashMap<String, String>();
        /// response params
        int statusCode;
        byte[] data;
        Headers.Builder headers = new Headers.Builder();
        ///
        requestHeaders.putAll(request.getHeadersMap());
        requestHeaders.putAll(additionalHeaders);
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }

        long requestStart = System.nanoTime();

        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        for (String headerName : requestHeaders.keySet()) {
            connection.addRequestProperty(headerName, requestHeaders.get(headerName));
        }
        setConnectionParametersForRequest(connection, request);
        // Initialize NetworkResponse with data from the HttpURLConnection.
        statusCode = connection.getResponseCode();
        if (statusCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        if (hasResponseBody(request.getMethod(), statusCode)) {
            data = getContentBytes(connection, byteArrayPool);
        } else {
            // Add 0 byte response as a way of honestly representing a
            // no-content request.
            data = new byte[0];
        }
        headers.addMMap(connection.getHeaderFields());

        return new NetworkResponse(statusCode, data, headers.build(),
                System.nanoTime() - requestStart);
    }

    /**
     * Checks if a response message contains a body.
     *
     * @param requestMethod request method
     * @param responseCode  response status code
     * @return whether the response has a body
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     */
    private static boolean hasResponseBody(String requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD
                && !(100 <= responseCode && responseCode < HttpURLConnection.HTTP_OK)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    /**
     * Reads the contents of HttpEntity into a byte[].
     */
    private byte[] getContentBytes(HttpURLConnection connection, ByteArrayPool byteArrayPool) throws IOException {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(byteArrayPool, connection.getContentLength());
        byte[] buffer = null;
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        try {

            if (inputStream == null) {
                return new byte[0];
            }
            buffer = byteArrayPool.getBuf(1024);
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources
                inputStream.close();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                JusLog.v("Error occurred when calling consumingContent");
            }
            byteArrayPool.returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     *
     * @param url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?, ?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    static void setConnectionParametersForRequest(HttpURLConnection connection,
                                                  Request<?, ?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Method.GET:
                // Not necessary to set the request method because connection defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            case Method.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                break;
            case Method.TRACE:
                connection.setRequestMethod("TRACE");
                break;
            case Method.PATCH:
                connection.setRequestMethod("PATCH");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?, ?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            if (request.getBodyContentType() != null) {
                connection.addRequestProperty(HTTP.CONTENT_TYPE, request.getBodyContentType());
            }
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
}
