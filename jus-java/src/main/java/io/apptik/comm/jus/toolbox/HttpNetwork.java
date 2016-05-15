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

package io.apptik.comm.jus.toolbox;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.Cache.Entry;
import io.apptik.comm.jus.Network;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RetryPolicy;
import io.apptik.comm.jus.auth.Authenticator;
import io.apptik.comm.jus.error.AuthError;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.NetworkError;
import io.apptik.comm.jus.error.RequestError;
import io.apptik.comm.jus.error.ServerError;
import io.apptik.comm.jus.error.TimeoutError;
import io.apptik.comm.jus.http.DateUtils;
import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.HttpStack;

/**
 * A network performing Jus requests over an {@link HttpStack}.
 */
public class HttpNetwork implements Network {

    protected final HttpStack httpStack;

    protected final ByteArrayPool pool;
    public static final int DEFAULT_POOL_SIZE = 4096;

    /**
     * @param httpStack HTTP stack to be used
     */
    public HttpNetwork(HttpStack httpStack) {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    /**
     * @param httpStack HTTP stack to be used
     * @param pool      a buffer pool that improves GC performance in copy operations
     */
    public HttpNetwork(HttpStack httpStack, ByteArrayPool pool) {
        this.httpStack = httpStack;
        this.pool = pool;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws JusError {
        long requestStart = System.nanoTime();
        while (true) {
            if (request.isCanceled()) {
                //it will be handled/ignored later
                return null;
            }
            if (request.getNoConnectionPolicy() != null
                    && request.getConnectivityManager() != null) {
                if (request.getConnectivityManager().getActiveNetwork() == null
                        || !request.getConnectivityManager().getActiveNetwork().isConnected()) {
                    JusError error = request.getNoConnectionPolicy().throwOnNoConnection(request);
                    if (error != null) {
                        throw error;
                    }
                }
            }
            NetworkResponse httpResponse = null;
            try {
                // Gather headers.
                Headers.Builder headers = new Headers.Builder();
                addCacheHeaders(headers, request.getCacheEntry());
                addServerAuthHeaders(request.getServerAuthenticator(), headers);
                addProxyAuthHeaders(request.getProxyAuthenticator(), headers);

                Headers extraHeaders = headers.build();
                request.addMarker(Request.EVENT_NETWORK_STACK_SEND, request.getNetworkRequest(),
                        extraHeaders);
                httpResponse = httpStack.performRequest(request, extraHeaders, pool);
                if (httpResponse == null) {
                    throw new NetworkError("No Response");
                }

                //currently all requests that came to here normally needs to be attached to the
                // queue
                //however due the complete decoupling of the components in Jus a Network may be set
                //to perform internal requests, i.e. which was not passed to the queue, possibly
                // auth
                //requests. So we shall check
                if (request.getRequestQueue() != null) {
                    httpResponse = request.getRequestQueue().transformResponse(request,
                            httpResponse);
                    request.addMarker(Request.EVENT_NETWORK_TRANSFORM_COMPLETE, httpResponse);
                }

                //Check for Auth
                if (httpResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // thrown when available Authenticator is available
                    request.addMarker(Request.EVENT_NETWORK_STACK_AUTH_ERROR,
                            httpResponse);
                    if (request.getServerAuthenticator() != null) {
                        request.getServerAuthenticator().clearAuthValue();
                        try {
                            //typical implementation would try to refresh the token
                            //after being set to invalid
                            request.getServerAuthenticator().getAuthValue();
                        } catch (AuthError authError) {
                            //finally we didn't succeed
                            throw authError;
                        }
                        //retry the request
                        request.addMarker(Request.EVENT_NETWORK_STACK_AUTH_ERROR_RESEND,
                                httpResponse);
                        continue;
                    } else {
                        //or if another way of auth is used
                        throw new AuthError(httpResponse);
                    }
                } else if (httpResponse.statusCode == HttpURLConnection.HTTP_PROXY_AUTH) {
                    request.addMarker(Request.EVENT_NETWORK_STACK_AUTH_PROXY_ERROR,
                            httpResponse);
                    // thrown when available Authenticator is available
                    if (request.getProxyAuthenticator() != null) {
                        request.getProxyAuthenticator().clearAuthValue();
                        try {
                            //typical implementation would try to refresh the token
                            //after being set to invalid
                            request.getProxyAuthenticator().getAuthValue();
                        } catch (AuthError authError) {
                            //finally we didn't succeed
                            throw authError;
                        }
                        //retry the request
                        request.addMarker(Request.EVENT_NETWORK_STACK_AUTH_ERROR_RESEND,
                                httpResponse);
                        continue;
                    } else {
                        //or if another way of auth is used
                        throw new AuthError(httpResponse);
                    }
                }

                //Check for redirects
                if (request.getRedirectPolicy() != null) {
                    Request newR = request.getRedirectPolicy().verifyRedirect(request,
                            httpResponse);
                    Headers rHeaders = new Headers.Builder().build();
                    while (newR != null) {
                        request.addMarker(Request.EVENT_NETWORK_STACK_REDIRECT_SEND, newR);
                        httpResponse = httpStack.performRequest(newR, rHeaders, pool);
                        request.addMarker(Request.EVENT_NETWORK_STACK_REDIRECT_COMPLETE,
                                httpResponse);
                        newR = request.getRedirectPolicy().verifyRedirect(request, httpResponse);
                    }
                }
                request.addMarker(Request.EVENT_NETWORK_STACK_COMPLETE, httpResponse);

                //check completeness of body
                if (httpResponse != null && httpResponse.headers != null) {
                    String contentLen = httpResponse.headers.get(HTTP.CONTENT_LEN);
                    if (contentLen != null) {
                        int cLen = Integer.parseInt(contentLen);
                        if (cLen > httpResponse.data.length
                                && request.getMethod() != Request.Method.HEAD) {
                            throw new NetworkError(httpResponse, "Response Body not completely " +
                                    "received");
                        }
                    }
                }

                // if the request is slow, log it.
                long requestLifetime = System.nanoTime() - requestStart;
                logSlowRequests(requestLifetime, request, httpResponse.data, httpResponse
                        .statusCode);

                // Handle cache validation.
                if (httpResponse.statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {

                    Entry entry = request.getCacheEntry();
                    if (entry != null) {

                        // A HTTP 304 response does not have all header fields. We
                        // have to use the header fields from the cache entry plus
                        // the new ones from the response.
                        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5
                        final Map<String, List<String>> responseHeaders = httpResponse.headers.toMultimap();
                        final Map<String, List<String>> cacheHeaders = entry.responseHeaders.toMultimap();
                        Headers.Builder hBuilder = new Headers.Builder();
                        hBuilder.addMMap(responseHeaders);
                        if (cacheHeaders != null) {
                            for (Map.Entry<String, List<String>> hentry : cacheHeaders.entrySet()) {
                                //could be status line
                                if (hentry.getKey() != null
                                        && !responseHeaders.containsKey(hentry.getKey())) {
                                    hBuilder.add(hentry.getKey(), hentry.getValue());
                                }
                            }
                        }

                        httpResponse = new NetworkResponse(
                                httpResponse.statusCode,
                                entry.data, hBuilder.build(),
                                System.nanoTime() - requestStart
                        );
                    } else {
                        httpResponse = new NetworkResponse(
                                httpResponse.statusCode,
                                null,
                                httpResponse.headers,
                                System.nanoTime() - requestStart
                        );
                    }
                } else if (httpResponse.statusCode < 200 || httpResponse.statusCode > 299) {
                    //todo Not really nice throw better
                    throw new IOException();
                }
                return httpResponse;
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket", request, new TimeoutError("socket Timeout", e));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrlString(), e);
            } catch (IOException e) {
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    networkResponse = httpResponse;
                } else {
                    throw new NetworkError(e);
                }
                //todo add queue markers
//                JusLog.e("Unexpected response code %d for %s", networkResponse.statusCode,
//                        request.getUrlString());
                if (networkResponse != null) {
                    if (networkResponse.statusCode == HttpURLConnection
                            .HTTP_CLIENT_TIMEOUT) {
                        attemptRetryOnException("http-client", request, new RequestError
                                (networkResponse, "HTTP_CLIENT_TIMEOUT"));
                    } else if (networkResponse.statusCode > 399 && networkResponse.statusCode <
                            500) {
                        //some request query error that does not make sense to retry, assuming
                        // the service we use is deterministic
                        throw new RequestError(networkResponse);
                    } else if (networkResponse.statusCode > 499) {
                        //some server error might not need to be retried
                        //however retry policy set to this request should handle it as it needs.
                        attemptRetryOnException("server",
                                request, new ServerError(networkResponse));
                    } else {
                        //unclassified error
                        throw new JusError(networkResponse, e);
                    }
                } else {
                    throw new NetworkError();
                }
            } catch (AuthError authError) {
                //we have failed to get a token so give it up
                throw authError;
            }
        }
    }

    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(long requestLifetime, Request<?> request,
                                 byte[] responseContents, int statusCode) {
        long SLOW_REQUEST_THRESHOLD_MS = 3000000000l;
        if (requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            //todo add queue markers
//            JusLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
//                            "[rc=%d], [retryCount=%s]", request, requestLifetime,
//                    responseContents != null ? responseContents.length : "null",
//                    statusCode, request.getRetryPolicy().getCurrentRetryCount());
        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request,
                                                JusError exception) throws JusError {
        RetryPolicy retryPolicy = request.getRetryPolicy();

        if (retryPolicy == null) {
            throw exception;
        }

        try {
            retryPolicy.retry(exception);
        } catch (JusError e) {
            request.addMarker(Request.EVENT_NETWORK_RETRY_FAILED,
                    String.format("%s-timeout-giveup [conn-timeout=%s] [read-timeout=%s]",
                            logPrefix, retryPolicy.getCurrentConnectTimeout(),
                            retryPolicy.getCurrentReadTimeout()));
            throw e;
        }
        request.addMarker(Request.EVENT_NETWORK_RETRY,
                String.format("%s-retry [conn-timeout=%s] [read-timeout=%s]",
                        logPrefix, retryPolicy.getCurrentConnectTimeout(),
                        retryPolicy.getCurrentReadTimeout()));
    }

    private void addServerAuthHeaders(Authenticator authenticator, Headers.Builder headers) throws
            AuthError {
        if (authenticator == null) return;
        headers.add("Authorization", authenticator.getAuthValue());
    }

    private void addProxyAuthHeaders(Authenticator authenticator, Headers.Builder headers) throws
            AuthError {
        if (authenticator == null) return;
        headers.add("Proxy-Authorization", authenticator.getAuthValue());
    }

    private void addCacheHeaders(Headers.Builder headers, Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            headers.add("If-None-Match", entry.etag);
        }

        if (entry.lastModified > 0) {
            Date refTime = new Date(entry.lastModified);
            headers.add("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

}
