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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import io.apptik.comm.jus.NetworkDispatcher;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Request.Method;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.toolbox.ByteArrayPool;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack extends AbstractHttpStack {

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        String rewriteUrl(String originalUrl);
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
    public NetworkResponse performRequest(Request<?> request, Headers
            additionalHeaders, ByteArrayPool byteArrayPool) throws IOException {
        String url = request.getUrlString();
        HashMap<String, String> requestHeaders = new HashMap<String, String>();
        /// response params
        int statusCode;
        byte[] data;
        Headers.Builder headers = new Headers.Builder();
        ///TODO optimise
        requestHeaders.putAll(request.getHeadersMap());
        requestHeaders.putAll(additionalHeaders.toMap());
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
        headers.addMMap(connection.getHeaderFields());

        if (NetworkDispatcher.hasResponseBody(request.getMethod(), statusCode)) {
            data = getContentBytes(connection, byteArrayPool);
        } else {
            // Add 0 byte response as a way of honestly representing a
            // no-content request.
            data = new byte[0];
        }


        return new NetworkResponse(statusCode, data, headers.build(),
                System.nanoTime() - requestStart);
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
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);

        connection.setConnectTimeout(request.getRetryPolicy().getCurrentConnectTimeout());
        connection.setReadTimeout(request.getRetryPolicy().getCurrentReadTimeout());
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    static void setConnectionParametersForRequest(HttpURLConnection connection,
                                                  Request<?> request) throws IOException {
        switch (request.getMethod()) {
            case Method.GET:
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
                connection.setRequestMethod(request.getMethod());
                addBodyIfExists(connection, request);
                break;
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
}
