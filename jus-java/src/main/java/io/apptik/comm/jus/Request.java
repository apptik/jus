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

package io.apptik.comm.jus;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.apptik.comm.jus.auth.Authenticator;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.toolbox.HttpHeaderParser;
import io.apptik.comm.jus.toolbox.Utils;

import static io.apptik.comm.jus.toolbox.Utils.tryIdentifyResultType;

/**
 * Base class for all network requests.
 * <br>
 * This class can be used directly for all standard requests.
 * Use {@link Request#setNetworkRequest(NetworkRequest)} to provide the http entity to be sent.
 * For complex requests you can use {@link io.apptik.comm.jus.http.MultipartBuilder} and
 * {@link io.apptik.comm.jus.http.FormEncodingBuilder}
 * to generate the {@link Request#setNetworkRequest(NetworkRequest)}
 * <br>
 * Use {@link Converter} or {@link Converter.Factory} to
 * transform data to {@link NetworkRequest} and Response
 * from {@link NetworkResponse}.
 * <p>
 * If more complex logic is required and request is extended then
 * one should implement:
 * {@link #getBody()} in case of Post or Put
 * {@link #getHeadersMap()} in case of specific headers
 * {@link #getBodyContentType()} in case of specific content type
 * Note that if {@link #getBodyContentType() != null} it will be added to the headers of the request
 * </p>
 *
 * @param <T> The type of parsed response this request expects.
 */
public class Request<T> implements Comparable<Request<T>>, Cloneable {

    /**
     * when Cache is expired
     */
    public static final String EVENT_CACHE_HIT_EXPIRED = "cache-hit-expired";
    /**
     * when error occurs and response is being prepared for delivery
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link JusError}</li>
     * </ol>
     */
    public static final String EVENT_POST_ERROR = "post-error";
    /**
     * when network call is OK and response is being prepared for delivery
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link Response}</li>
     * </ol>
     */
    public static final String EVENT_POST_RESPONSE = "post-response";
    /**
     * when the response is delivered but the request hasn't finished yet and more responses could
     * arrive
     */
    public static final String EVENT_INTERMEDIATE_RESPONSE = "intermediate-response";
    /**
     * when response is prepared for delivery but is the request is already cancelled so that
     * response will be ignored and Request#onResponse or Request#onError will not be called. Also
     * the request will be finalized.
     * <p>Although no response callback will be sent the Request response
     * from {@link #getRawResponse()} should available if needed for logging purposes for
     * example.</p>
     */
    public static final String EVENT_CANCELED_AT_DELIVERY = "canceled-at-delivery";
    /**
     * when request is completely finished and no more events will be received
     */
    public static final String EVENT_DONE = "done";
    /**
     * when request is being taken from the network queue by the
     * {@link NetworkDispatcher}
     */
    public static final String EVENT_NETWORK_QUEUE_TAKE = "network-queue-take";
    /**
     * when request is being send to the network stack
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkRequest}</li>
     * <li> Extra {@link Headers} that will be passed to the HttpStack</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_SEND = "network-stack-send";
    /**
     * when we have HTTP_UNAUTHORIZED = 401; from the network stack
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_AUTH_ERROR = "network-stack-auth-error";
    /**
     * when we have HTTP_PROXY_AUTH = 407; from the network stack
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_AUTH_PROXY_ERROR =
            "network-stack-auth-proxy-error";
    /**
     * when we have had 401 or 407 and we have proper Authenticator available we resend the request
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_AUTH_ERROR_RESEND =
            "network-stack-auth-error-resend";
    /**
     * when a new call is made because of a redirect
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>the new {@link Request} that will be sent to the Stack</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_REDIRECT_SEND = "network-stack-redirect-send";
    /**
     * when a new call(made because of a redirect) completes
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_REDIRECT_COMPLETE =
            "network-stack-redirect-complete";
    /**
     * when Network Stack returns Network Response
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>NetworkResponse as received form the Stack {@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_STACK_COMPLETE = "network-stack-complete";
    /**
     * when Original NetworkResponse is transformed in case of a Transformer available
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>the transformed {@link NetworkResponse}</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_TRANSFORM_COMPLETE = "network-transform-complete";
    /**
     * When request is found to be Cancelled just before being processed by the
     * {@link NetworkDispatcher}
     */
    public static final String EVENT_NETWORK_DISCARD_CANCELED = "network-discard-canceled";
    /**
     * when request was not successful and after validation from the {@link RetryPolicy}
     * is being resend to the Stack
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>retry info {@link String} of Format "%s-retry [conn-timeout=%s] [read-timeout=%s]"</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_RETRY = "network-retry";
    /**
     * when request was not successful and validation from the {@link RetryPolicy} does not pass
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>retry info {@link String}
     * of Format "%s-timeout-giveup [conn-timeout=%s] [read-timeout=%s]"</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_RETRY_FAILED = "network-retry-failed";
    /**
     * when a Response is returned from {@link Network} and is ging to be processed by the
     * {@link NetworkDispatcher}
     * <p>
     * the params attached are:</p>
     * <ol>
     * <li>{@link NetworkResponse}
     * of Format "%s-timeout-giveup [conn-timeout=%s] [read-timeout=%s]"</li>
     * </ol>
     */
    public static final String EVENT_NETWORK_HTTP_COMPLETE = "network-http-complete";
    /**
     * when cache response is delivered and the network response is "not-modified" i.e. not
     * diffeent from the cache. In that case no extra callback will be made.
     */
    public static final String EVENT_NOT_MODIFIED = "not-modified";
    /**
     * when parsing of the {@link NetworkResponse} completed in the
     * {@link NetworkDispatcher} using Request's
     * {@link Request#parseNetworkResponse(NetworkResponse)} method.
     */
    public static final String EVENT_NETWORK_PARSE_COMPLETE = "network-parse-complete";
    /**
     * when the Response is being cached an saved in your registered {@link Cache}
     */
    public static final String EVENT_NETWORK_CACHE_WRITTEN = "network-cache-written";
    /**
     * this is the very first event before the request is added to the
     * {@link RequestQueue}
     */
    public static final String EVENT_PRE_ADD_TO_QUEUE = "pre-add-to-queue";
    /**
     * when the Request was just added to the {@link RequestQueue}
     */
    public static final String EVENT_ADD_TO_QUEUE = "add-to-queue";
    /**
     * when request is being taken from the cache queue by the
     * {@link CacheDispatcher}
     */
    public static final String EVENT_CACHE_QUEUE_TAKE = "cache-queue-take";
    /**
     * when the Request appears to be cancelled just after taken by the
     * {@link CacheDispatcher} for processing
     */
    public static final String EVENT_CACHE_DISCARD_CANCELED = "cache-discard-canceled";
    /**
     * when the Request is cancelled event before even added to the
     * {@link RequestQueue}
     */
    public static final String EVENT_ADD_DISCARD_CANCELED = "cache-discard-canceled";
    /**
     * when {@link CacheDispatcher} cannot find Request cashe in {@link Cache}
     */
    public static final String EVENT_CACHE_MISS = "cache-miss";
    /**
     * when Cache is expired but we will deliver it as a response anyway ignoring http rules
     */
    public static final String EVENT_CACHE_HIT_EXPIRED_BUT_WILL_DELIVER_IT =
            "cache-hit-expired, but will deliver it";
    /**
     * when Cache record is found, no NetworkRequest will be needed
     */
    public static final String EVENT_CACHE_HIT = "cache-hit";
    /**
     * when cache record is parsed to {@link Response}
     */
    public static final String EVENT_CACHE_HIT_PARSED = "cache-hit-parsed";
    /**
     * when Cache record is found, but another NetworkRequest will be needed
     */
    public static final String EVENT_CACHE_HIT_REFRESH_NEEDED = "cache-hit-refresh-needed";
    /**
     * just before the response is delivered i.e.
     * {@link RequestListener.ResponseListener#onResponse(Object)} will be called
     */
    public static final String EVENT_DELIVER_RESPONSE = "deliver response";
    /**
     * just before the error is delivered i.e.
     * {@link RequestListener.ErrorListener#onError(JusError)} will be called
     */
    public static final String EVENT_DELIVER_ERROR = "deliver error";


    /**
     * Default encoding for POST or PUT parameters.
     */
    public static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * Supported request methods.
     */
    public static final class Method {

        private Method() {
            throw new RuntimeException("don't do that!");
        }

        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String HEAD = "HEAD";
        public static final String OPTIONS = "OPTIONS";
        public static final String TRACE = "TRACE";
        public static final String PATCH = "PATCH";
        public static final String REPORT = "REPORT";
        public static final String PROPFIND = "PROPFIND";
        public static final String SEARCH = "SEARCH";
        public static final String PRI = "PRI";
        public static final String PROPPATCH = "PROPPATCH";
        public static final String MKCOL = "MKCOL";
        public static final String COPY = "COPY";
        public static final String MOVE = "MOVE";
        public static final String UNLOCK = "UNLOCK";

        static boolean isSafe(String method) {
            return (GET.equals(method) ||
                    HEAD.equals(method) ||
                    OPTIONS.equals(method) ||
                    REPORT.equals(method) ||
                    PROPFIND.equals(method) ||
                    SEARCH.equals(method) ||
                    PRI.equals(method));
        }

        static boolean isIdempotent(String method) {
            return (GET.equals(method) ||
                    HEAD.equals(method) ||
                    PUT.equals(method) ||
                    DELETE.equals(method) ||
                    OPTIONS.equals(method) ||
                    TRACE.equals(method) ||
                    PROPFIND.equals(method) ||
                    PROPPATCH.equals(method) ||
                    MKCOL.equals(method) ||
                    COPY.equals(method) ||
                    MOVE.equals(method) ||
                    UNLOCK.equals(method) ||
                    PRI.equals(method));
        }

        static boolean canRetry(String method) {
            return  isSafe(method) ||  isIdempotent(method);
        }


    }

    /**
     * Request method of this request.  Currently supports GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    private final String method;

    /**
     * URL of this request.
     */
    private final HttpUrl url;

    /**
     * Default tag for {@see android.net.TrafficStats (Android)}.
     */
    private final int mDefaultTrafficStatsTag;

    /**
     * RequestListener interface for errors.
     */
    private final List<RequestListener.ErrorListener> errorListeners = new ArrayList<>();

    /**
     * RequestListener interface for non error responses.
     */
    private final List<RequestListener.ResponseListener<T>> responseListeners = new ArrayList<>();

    /**
     * RequestListener interface for markers.
     */
    private final List<RequestListener.MarkerListener> markerListeners = new ArrayList<>();

    /**
     * Sequence number of this request, used to enforce FIFO ordering.
     */
    protected Integer sequence;

    /**
     * The request queue this request is associated with.
     */
    private RequestQueue requestQueue;

    /**
     * The request queue this request is prepared to be associated with.
     * If this is set the requests is still not queuing but {@link #enqueue()} can be called.
     */
    private RequestQueue futureRequestQueue;
    /**
     * Whether or not responses to this request should be cached.
     */
    private boolean shouldCache = true;

    /**
     * Whether or not this request has been canceled.
     */
    private volatile boolean canceled = false;

    /**
     * Whether or not a response has been delivered for this request yet.
     */
    private boolean responseDelivered = false;

    // A cheap variant of request tracing used to dump slow requests.
    private long requestBirthTime = 0;

    /**
     * Threshold at which we should log the request (even when debug logging is not enabled).
     */
    private static final long SLOW_REQUEST_THRESHOLD_NS = 3000000000L;

    /**
     * The retry policy for this request.
     */
    private RetryPolicy retryPolicy;

    /**
     * The retry policy for this request.
     */
    private RedirectPolicy redirectPolicy;

    /**
     * When a request can be retrieved from cache but must be refreshed from
     * the network, the cache entry will be stored here so that in the event of
     * a "Not Modified" response, we can be sure it hasn't been evicted from cache.
     */
    private Cache.Entry cacheEntry = null;

    /**
     * An opaque token tagging this request; used for bulk cancellation.
     */
    private Object tag;

    volatile Response<T> response;

    private boolean logSlowRequests = false;

    private NetworkRequest networkRequest;
    private Converter<NetworkResponse, ? extends T> converterFromResponse;

    private Authenticator serverAuthenticator;
    private Authenticator proxyAuthenticator;

    private Priority priority = Priority.NORMAL;

    //used only until added to the queue to identify Response Converter
    private Type responseType = null;

    private ConnectivityManager connectivityManager;
    private NoConnectionPolicy noConnectionPolicy;


    public Request(String method, String url) {
        this(method, HttpUrl.parse(url));
    }

    public Request(String method, HttpUrl url) {
        this(method, url, (Converter) null);
    }

    public <TT extends T> Request(String method, String url, Class<TT> responseType) {
        this(method, HttpUrl.parse(url), responseType);
    }

    public <TT extends T> Request(String method, HttpUrl url, Class<TT> responseType) {
        this(method, url);
        this.responseType = responseType;
    }

    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     */
    public <TT extends T> Request(String method, HttpUrl url, Converter<NetworkResponse, TT>
            converterFromResponse) {
        this.method = method;
        this.url = url;
        this.converterFromResponse = converterFromResponse;
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public <TT extends T> Request(String method, String url, Converter<NetworkResponse, TT> converterFromResponse) {
        this(method, HttpUrl.parse(url), converterFromResponse);
    }

    public final RequestQueue getRequestQueue() {
        return requestQueue;
    }

    private void checkIfActive() {
        if (requestQueue != null) {
            throw new IllegalStateException("Request already added to a queue");
        }
    }

    public Request<T> clone() {
        Request<T> cloned = new Request<T>(getMethod(), getUrlString(), converterFromResponse)
                .setNetworkRequest(networkRequest);
        cloned.futureRequestQueue = requestQueue;
        return cloned;
    }

    public Converter<NetworkResponse, ? extends T> getConverterFromResponse() {
        return converterFromResponse;
    }

    /**
     * Return the method for this request.  Can be one of the values in {@link Method}.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the URL of this request.
     */
    public HttpUrl getUrl() {
        return url;
    }


    /**
     * Returns the URL String of this request.
     */
    public String getUrlString() {
        return url.toString();
    }

    public Response<T> getRawResponse() {
        return response;
    }

    public NetworkRequest getNetworkRequest() {
        return networkRequest;
    }


    public Request<T> setNetworkRequest(NetworkRequest networkRequest) {
        checkIfActive();
        this.networkRequest = networkRequest;
        return this;
    }

    /**
     * Set data to send which will be converted to {@link NetworkRequest}
     * This will overwrite all previous http body and headers defined
     *
     * @param requestData
     * @param converterToRequest
     * @return
     */
    public <R> Request<T> setRequestData(R requestData, Converter<R, NetworkRequest>
            converterToRequest)
            throws IOException {
        checkIfActive();
        Utils.checkNotNull(requestData, "networkRequest cannot be null");
        Utils.checkNotNull(converterToRequest, "converterToRequest cannot be null");
        try {
            this.networkRequest = converterToRequest.convert(requestData);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            //check if someone didnt wrap IOException
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else { //else wrap it in IOEx
                throw new IOException(e);
            }
        }
        return this;
    }

    //--> Listeners


    public <R extends Request<T>> R addResponseListener(RequestListener.ResponseListener<T>
                                                                responseListener) {
        if (responseListeners != null) {
            synchronized (responseListeners) {
                this.responseListeners.add(responseListener);
            }
        }
        return (R) this;
    }

    public <R extends Request<T>> R addMarkerListener(RequestListener.MarkerListener
                                                              markerListener) {
        if (markerListener != null) {
            synchronized (markerListeners) {
                this.markerListeners.add(markerListener);
            }
        }
        return (R) this;
    }

    public <R extends Request<T>> R addErrorListener(RequestListener.ErrorListener errorListener) {
        if (errorListener != null) {
            synchronized (errorListeners) {
                this.errorListeners.add(errorListener);
            }
        }
        return (R) this;
    }

    public Request<T> removeResponseListener(RequestListener.ResponseListener<T> responseListener) {
        synchronized (responseListeners) {
            this.responseListeners.remove(responseListener);
        }
        return this;
    }

    public <R extends Request<T>> R removeMarkerListener(RequestListener.MarkerListener
                                                                 markerListener) {
        synchronized (markerListeners) {
            this.markerListeners.remove(markerListener);
        }
        return (R) this;
    }

    public <R extends Request<T>> R removeErrorListener(RequestListener.ErrorListener
                                                                errorListener) {
        synchronized (errorListeners) {
            this.errorListeners.remove(errorListener);
        }
        return (R) this;
    }

    //<-- Listeners

    public NoConnectionPolicy getNoConnectionPolicy() {
        return noConnectionPolicy;
    }

    public <R extends Request<T>> R setNoConnectionPolicy(NoConnectionPolicy noConnectionPolicy) {
        this.noConnectionPolicy = noConnectionPolicy;
        return (R) this;
    }

    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public <R extends Request<T>> R setConnectivityManager(ConnectivityManager
                                                                   connectivityManager) {
        this.connectivityManager = connectivityManager;
        return (R) this;
    }

    public boolean isLogSlowRequests() {
        return logSlowRequests;
    }

    public <R extends Request<T>> R setLogSlowRequests(boolean logSlowRequests) {
        this.logSlowRequests = logSlowRequests;
        return (R) this;
    }

    /**
     * Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */
    public <R extends Request<T>> R setTag(Object tag) {
        checkIfActive();
        this.tag = tag;
        return (R) this;
    }

    /**
     * Returns this request's tag.
     *
     * @see Request#setTag(Object)
     */
    public Object getTag() {
        return tag;
    }

    /**
     * Set {@link Priority} of this request; {@link Priority#NORMAL} by default.
     *
     * @param priority {@link Priority} of this request
     */
    public <R extends Request<T>> R setPriority(Priority priority) {
        this.priority = priority;
        return (R) this;
    }

    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by default.
     */
    public Priority getPriority() {
        return priority;
    }

    public Authenticator getServerAuthenticator() {
        return serverAuthenticator;
    }

    public <R extends Request<T>> R setServerAuthenticator(Authenticator serverAuthenticator) {
        checkIfActive();
        this.serverAuthenticator = serverAuthenticator;
        return (R) this;
    }

    public Authenticator getProxyAuthenticator() {
        return proxyAuthenticator;
    }

    public <R extends Request<T>> R setProxyAuthenticator(Authenticator proxyAuthenticator) {
        checkIfActive();
        this.proxyAuthenticator = proxyAuthenticator;
        return (R) this;
    }

    /**
     * @return A tag for use with {@link NetworkDispatcher#addTrafficStatsTag}
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(HttpUrl url) {
        if (url != null) {
            String host = url.host();
            if (host != null) {
                return host.hashCode();
            }
        }
        return 0;
    }

    /**
     * Sets the retry policy for this request.
     *
     * @return This Request object to allow for chaining.
     */
    public <R extends Request<T>> R setRetryPolicy(RetryPolicy retryPolicy) {
        checkIfActive();
        this.retryPolicy = retryPolicy;
        return (R) this;
    }

    /**
     * Returns the retry policy that should be used  for this request.
     */
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Sets the redirect policy for this request.
     *
     * @return This Request object to allow for chaining.
     */
    public <R extends Request<T>> R setRedirectPolicy(RedirectPolicy redirectPolicy) {
        checkIfActive();
        this.redirectPolicy = redirectPolicy;
        return (R) this;
    }

    /**
     * Returns the redirect policy that should be used for this request.
     */
    public RedirectPolicy getRedirectPolicy() {
        return redirectPolicy;
    }


    /**
     * Adds an event to this request's event log; for debugging.
     */
    public <R extends Request<T>> R addMarker(String tag, Object... args) {
        Marker marker = new Marker(tag,
                Thread.currentThread().getId(),
                Thread.currentThread().getName(),
                System.nanoTime());

        synchronized (markerListeners) {
            for (RequestListener.MarkerListener markerListener : markerListeners) {
                markerListener.onMarker(marker, args);
            }
        }

        if (logSlowRequests && requestBirthTime == 0) {
            requestBirthTime = System.nanoTime();
        }

        return (R) this;
    }

    /**
     * Notifies the request queue that this request has finished (successfully or with error).
     * <p/>
     * <p>Also dumps all events from this request's event log; for debugging.</p>
     */
    void finish(final String tag) {
        if (requestQueue != null) {
            requestQueue.finish(this);
        }
        addMarker(tag);
        if (!EVENT_DONE.equals(tag)) {
            addMarker(EVENT_DONE);
        }
        if (logSlowRequests) {
            long requestTime = System.nanoTime() - requestBirthTime;
            if (requestTime >= SLOW_REQUEST_THRESHOLD_NS) {
                //todo add queue markers
                //JusLog.d("%d ns: %s", requestTime, this.toString());
            }
        }
        synchronized (markerListeners) {
            markerListeners.clear();
        }
    }

    /**
     * Associates this request with the given queue. The request queue will be notified when this
     * request has finished.
     *
     * @return This Request object to allow for chaining.
     */
    <R extends Request<T>> R setRequestQueue(RequestQueue requestQueue) {
        checkIfActive();
        java.lang.reflect.Method m = null;
        if (!(this.getClass().equals(Request.class))) {
            try {
                m = this.getClass().getDeclaredMethod("parseNetworkResponse", NetworkResponse
                        .class);
            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
            }
        }
        if (converterFromResponse == null) {
            Type t;
            if (responseType != null) {
                t = responseType;
            } else {
                t = tryIdentifyResultType(this);
            }
            if (t == null) {
                throw new IllegalArgumentException("Cannot resolve Response type in order to " +
                        "identify Response Converter for Request : " + this);
            }

            try {
                this.converterFromResponse =
                        (Converter<NetworkResponse, T>) requestQueue
                                .getResponseConverter(t, new Annotation[0]);
            } catch (IllegalArgumentException ex) {
                //check if parseNetworkResponse is overridden
                if (m == null) {
                    //it is not so it is for sure that we cannot parse response thus throw
                    throw ex;
                }
                //else keep quiet as conversion can probably be handled by the overridden method.
                // if it fails parse exception will be thrown.
            }


        }
        if (retryPolicy == null) {
            setRetryPolicy(new DefaultRetryPolicy(this));
        }
        this.requestQueue = requestQueue;
        return (R) this;
    }

    public <R extends Request<T>> R prepRequestQueue(RequestQueue requestQueue) {
        checkIfActive();
        this.futureRequestQueue = requestQueue;
        return (R) this;
    }

    public synchronized <R extends Request<T>> R enqueue() {
        checkIfActive();
        if (isCanceled()) {
            throw new IllegalStateException("Canceled");
        }
        Utils.checkNotNull(this.futureRequestQueue, "No future RequestQueue set. " +
                "Please call prepRequestQueue before calling this.");
        this.futureRequestQueue.add(this);
        return (R) this;
    }

    /**
     * Sets the sequence number of this request.  Used by {@link RequestQueue}.
     *
     * @return This Request object to allow for chaining.
     */
    protected final <R extends Request<T>> R setSequence(int sequence) {
        checkIfActive();
        this.sequence = sequence;
        return (R) this;
    }

    /**
     * Returns the sequence number of this request.
     */
    public final int getSequence() {
        if (sequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return sequence;
    }

    /**
     * Returns the cache key for this request.  By default, this is the URL.
     */
    public String getCacheKey() {
        return getUrlString();
    }

    /**
     * Annotates this request with an entry retrieved for it from cache.
     * Used for cache coherency support.
     *
     * @return This Request object to allow for chaining.
     */
    public <R extends Request<T>> R setCacheEntry(Cache.Entry entry) {
        cacheEntry = entry;
        return (R) this;
    }

    /**
     * Returns the annotated cache entry, or null if there isn't one.
     */
    public Cache.Entry getCacheEntry() {
        return cacheEntry;
    }

    /**
     * Mark this request as canceled.  No callback will be delivered.
     */
    public void cancel() {
        canceled = true;
    }

    /**
     * Returns true if this request has been canceled.
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request
     */
    public final Map<String, String> getHeadersMap() {
        if (networkRequest != null && networkRequest.headers != null) {
            return networkRequest.headers.toMap();
        }
        return Collections.emptyMap();
    }

    public final Headers getHeaders() {
        if (networkRequest != null && networkRequest.headers != null) {
            return networkRequest.headers;
        }
        return null;
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    public String getBodyContentType() {
        if (networkRequest != null && networkRequest.contentType != null) {
            return networkRequest.contentType.toString();
        }
        return null;
    }

    /**
     * Returns the raw POST or PUT body to be sent.
     */
    public byte[] getBody() {
        if (networkRequest != null) {
            return networkRequest.data;
        }
        return null;
    }

    /**
     * Set whether or not responses to this request should be cached.
     *
     * @return This Request object to allow for chaining.
     */
    public final <R extends Request<T>> R setShouldCache(boolean shouldCache) {
        checkIfActive();
        this.shouldCache = shouldCache;
        return (R) this;
    }

    /**
     * Returns true if responses to this request should be cached.
     */
    public final boolean shouldCache() {
        return shouldCache;
    }

    /**
     * Mark this request as having a response delivered on it.  This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    void markDelivered() {
        responseDelivered = true;
    }

    /**
     * Returns true if this request has had a response delivered for it.
     * this is useful in case cache is returned and the new response is "not modified"
     */
    public boolean hasHadResponseDelivered() {
        return responseDelivered;
    }

    /**
     * Subclasses can implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker threadId.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        T parsed = null;
        try {
            parsed = converterFromResponse.convert(response);
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    /**
     * Subclasses can override this method to parse 'networkError' and return a more specific error.
     * <p>
     * <p>The default implementation just returns the passed 'networkError'.</p>
     *
     * @param jusError the error retrieved from the network
     * @return an NetworkError augmented with additional information
     */
    protected JusError parseNetworkError(JusError jusError) {
        return jusError;
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     *                 {@link #parseNetworkResponse(NetworkResponse)}
     */
    protected void deliverResponse(T response) {
        synchronized (responseListeners) {
            for (RequestListener.ResponseListener responseListener : responseListeners) {
                responseListener.onResponse(response);
            }
            if (!this.response.intermediate) {
                responseListeners.clear();
            }
        }
        if (!this.response.intermediate) {
            synchronized (errorListeners) {
                errorListeners.clear();
            }
        }
    }

    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    public void deliverError(JusError error) {
        synchronized (errorListeners) {
            for (RequestListener.ErrorListener errorListener : errorListeners) {
                errorListener.onError(error);
            }
            errorListeners.clear();
        }
        synchronized (responseListeners) {
            responseListeners.clear();
        }
    }

    public RequestFuture<T> getFuture() {
        return new RequestFuture<T>().setRequest(this);
    }


    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override
    public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.
        return left == right ?
                this.sequence - other.sequence :
                right.ordinal() - left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        String mark = "[..]";
        if (canceled) {
            mark = "[X]";
        } else if (responseDelivered) {
            mark = "[O]";
        }
        return "Request " + mark + " {" +
                "\n\turl=" + url +
                "\n\tmethod='" + method + '\'' +
                "\n\ttag=" + tag +
                "\n\t(responseDelivered=" + responseDelivered +
                ", trafficStatsTag=" + trafficStatsTag +
                ", priority=" + getPriority() +
                ", requestBirthTime=" + requestBirthTime +
                ", sequence=" + sequence +
                ", priority=" + priority +
                ", shouldCache=" + shouldCache +
                ", logSlowRequests=" + logSlowRequests +
                ", canceled=" + canceled +
                ")" +
                "\nnetworkRequest=" + (networkRequest == null ? "null" :
                networkRequest.toString().replace("\n", "\n\t")) +
                "\nresponse=" + (response == null ? "null" :
                response.toString().replace("\n", "\n\t")) +
                "\n}";
    }

    /**
     * Priority values.  Requests will be processed from higher priorities to
     * lower priorities, in FIFO order.
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

}
