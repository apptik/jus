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

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import io.apptik.comm.jus.JusLog.MarkerLog;
import io.apptik.comm.jus.error.AuthFailureError;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.TimeoutError;
import io.apptik.comm.jus.request.FlexiRequest;
import io.apptik.comm.jus.toolbox.HttpHeaderParser;

/**
 * Base class for all network requests.
 * <p/>
 * for more simple and customizable requests using {@link Converter}
 * extend from {@link FlexiRequest}
 * <p/>
 * One Should implement:
 * {@link #getBody()} in case of Post or Put
 * {@link #getHeaders()} in case of specific headers
 * {@link #getBodyContentType()} in case of specific content type
 * Note that if {@link #getBodyContentType() != null} it will be added to the headers of the request
 *
 * @param <T> The type of parsed response this request expects.
 */
public class Request<F, T> implements Comparable<Request<F, T>>, Cloneable {

    public static final String EVENT_CACHE_HIT_EXPIRED = "cache-hit-expired";
    public static final String EVENT_POST_ERROR = "post-error";
    public static final String EVENT_POST_RESPONSE = "post-response";
    public static final String EVENT_INTERMEDIATE_RESPONSE = "intermediate-response";
    public static final String EVENT_CANCELED_AT_DELIVERY = "canceled-at-delivery";
    public static final String EVENT_DONE = "done";
    public static final String EVENT_NETWORK_QUEUE_TAKE = "network-queue-take";
    public static final String EVENT_NETWORK_DISCARD_CANCELLED = "network-discard-cancelled";
    public static final String EVENT_NETWORK_HTTP_COMPLETE = "network-http-complete";
    public static final String EVENT_NOT_MODIFIED = "not-modified";
    public static final String EVENT_NETWORK_PARSE_COMPLETE = "network-parse-complete";
    public static final String EVENT_NETWORK_CACHE_WRITTEN = "network-cache-written";
    public static final String EVENT_ADD_TO_QUEUE = "add-to-queue";
    public static final String EVENT_CACHE_QUEUE_TAKE = "cache-queue-take";
    public static final String EVENT_CACHE_DISCARD_CANCELED = "cache-discard-canceled";
    public static final String EVENT_CACHE_MISS = "cache-miss";
    public static final String EVENT_CACHE_HIT_EXPIRED_BUT_WILL_DELIVER_IT = "cache-hit-expired, but will deliver it";
    public static final String EVENT_CACHE_HIT = "cache-hit";
    public static final String EVENT_CACHE_HIT_PARSED = "cache-hit-parsed";
    public static final String EVENT_CACHE_HIT_REFRESH_NEEDED = "cache-hit-refresh-needed";

    /**
     * Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";


    /**
     * Supported request methods.
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    /**
     * An event log tracing the lifetime of this request; for debugging.
     */
    private final MarkerLog mEventLog = MarkerLog.ENABLED ? new MarkerLog() : null;

    /**
     * Request method of this request.  Currently supports GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    private final int method;

    /**
     * URL of this request.
     */
    private final String url;

    /**
     * Default tag for {@link TrafficStats}.
     */
    private final int mDefaultTrafficStatsTag;

    /**
     * Listener interface for errors.
     */
    private Listener.ErrorListener errorListener;

    /**
     * Listener interface for non error responses.
     */
    private Listener.ResponseListener responseListener;

    /**
     * Listener interface for markers.
     */
    private Listener.MarkerListener markerListener;

    /**
     * Sequence number of this request, used to enforce FIFO ordering.
     */
    protected Integer sequence;

    /**
     * The request queue this request is associated with.
     */
    private RequestQueue requestQueue;

    /**
     * Whether or not responses to this request should be cached.
     */
    private boolean shouldCache = true;

    /**
     * Whether or not this request has been canceled.
     */
    private boolean canceled = false;

    /**
     * Whether or not a response has been delivered for this request yet.
     */
    private boolean responseDelivered = false;

    // A cheap variant of request tracing used to dump slow requests.
    private long requestBirthTime = 0;

    /**
     * Threshold at which we should log the request (even when debug logging is not enabled).
     */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;

    /**
     * The retry policy for this request.
     */
    private RetryPolicy retryPolicy;

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

    private F networkRequest;
    private NetworkRequest innerNetworkRequest;
    private final Converter<NetworkResponse, T> converterFromResponse;
    private Converter<F, NetworkRequest> converterToRequest;


    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     */
    public Request(int method, String url, Converter<NetworkResponse, T> converterFromResponse) {
        this.method = method;
        this.url = url;
        this.converterFromResponse = converterFromResponse;
        setRetryPolicy(new DefaultRetryPolicy(
        ));
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    private void checkIfActive() {
        if (requestQueue != null) {
            throw new IllegalStateException("Request already added to a queue");
        }
    }

    public Request<F, T> clone() {
        return new Request<>(getMethod(), getUrl(), converterFromResponse);
    }

    /**
     * Return the method for this request.  Can be one of the values in {@link Method}.
     */
    public int getMethod() {
        return method;
    }

    /**
     * Returns the URL of this request.
     */
    public String getUrl() {
        return url;
    }

    public F getNetworkRequest() {
        return networkRequest;
    }

    public Request<F, T> setNetworkRequest(F networkRequest) {
        checkIfActive();
        this.networkRequest = networkRequest;
        //todo display warning if not set
        if (converterToRequest != null && networkRequest != null && !(networkRequest instanceof Void)) {
            try {
                innerNetworkRequest = converterToRequest.convert(networkRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public NetworkRequest getInnerNetworkRequest() {
        if (innerNetworkRequest == null && networkRequest != null &&
                converterToRequest != null && !(networkRequest instanceof Void)) {
            try {
                innerNetworkRequest = converterToRequest.convert(networkRequest);
            } catch (IOException e) {
                throw new IllegalStateException("cannot convert Network Request", e);
            }
        } else if(innerNetworkRequest != null && !(networkRequest instanceof Void) &&
                ( networkRequest != null || converterToRequest != null)) {
            throw new IllegalStateException("networkRequest and converterToRequest cannot be null" +
                    " for non Void input types");
        }

        return innerNetworkRequest;
    }

    public Converter<F, NetworkRequest> getConverterToRequest() {
        return converterToRequest;
    }

    public Request<F, T> setConverterToRequest(Converter<F, NetworkRequest> converterToRequest) {
        checkIfActive();
        this.converterToRequest = converterToRequest;
        //todo display warning if not set
        if (converterToRequest != null && networkRequest != null && !(networkRequest instanceof Void)) {
            try {
                innerNetworkRequest = converterToRequest.convert(networkRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    //--> Listeners

    public Listener.ResponseListener getResponseListener() {
        return responseListener;
    }

    public Request<F, T> setResponseListener(Listener.ResponseListener responseListener) {
        this.responseListener = responseListener;
        return this;
    }

    public Listener.MarkerListener getMarkerListener() {
        return markerListener;
    }

    public Request<F, T> setMarkerListener(Listener.MarkerListener markerListener) {
        this.markerListener = markerListener;
        return this;
    }

    public Listener.ErrorListener getErrorListener() {
        return errorListener;
    }

    public Request<F, T> setErrorListener(Listener.ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    //<-- Listeners

    public boolean isLogSlowRequests() {
        return logSlowRequests;
    }

    public Request<F, T> setLogSlowRequests(boolean logSlowRequests) {
        this.logSlowRequests = logSlowRequests;
        return this;
    }

    /**
     * Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<F, T> setTag(Object tag) {
        checkIfActive();
        this.tag = tag;
        return this;
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
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    /**
     * Sets the retry policy for this request.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<F, T> setRetryPolicy(RetryPolicy retryPolicy) {
        checkIfActive();
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Returns the retry policy that should be used  for this request.
     */
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }


    /**
     * Adds an event to this request's event log; for debugging.
     */
    public Request<F, T> addMarker(String tag, String... args) {
        if (markerListener != null) {
            markerListener.onMarker(
                    new MarkerLog.Marker(tag, Thread.currentThread().getId(),
                            SystemClock.elapsedRealtime()),
                    args);
        }

        if (MarkerLog.ENABLED) {
            mEventLog.add(tag, Thread.currentThread().getId());
        }
        if (logSlowRequests && requestBirthTime == 0) {
            requestBirthTime = SystemClock.elapsedRealtime();
        }

        return this;
    }

    /**
     * Notifies the request queue that this request has finished (successfully or with error).
     * <p/>
     * <p>Also dumps all events from this request's event log; for debugging.</p>
     */
    public void finish(final String tag) {
        if (requestQueue != null) {
            requestQueue.finish(this);
        }
        addMarker(tag);
        if (MarkerLog.ENABLED) {
            final long threadId = Thread.currentThread().getId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // If we finish marking off of the main thread, we need to
                // actually do it on the main thread to ensure correct ordering.
                //todo DO we really?
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventLog.finish(this.toString());
                    }
                });
                return;
            }
            mEventLog.finish(this.toString());
        }
        if (logSlowRequests) {
            long requestTime = SystemClock.elapsedRealtime() - requestBirthTime;
            if (requestTime >= SLOW_REQUEST_THRESHOLD_MS) {
                JusLog.d("%d ms: %s", requestTime, this.toString());
            }
        }
    }

    /**
     * Associates this request with the given queue. The request queue will be notified when this
     * request has finished.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<F, T> setRequestQueue(RequestQueue requestQueue) {
        checkIfActive();
        this.requestQueue = requestQueue;
        return this;
    }

    /**
     * Sets the sequence number of this request.  Used by {@link RequestQueue}.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<F, T> setSequence(int sequence) {
        checkIfActive();
        this.sequence = sequence;
        return this;
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
        return getUrl();
    }

    /**
     * Annotates this request with an entry retrieved for it from cache.
     * Used for cache coherency support.
     *
     * @return This Request object to allow for chaining.
     */
    public <F, T> Request<F, T> setCacheEntry(Cache.Entry entry) {
        cacheEntry = entry;
        return (Request<F, T>) this;
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
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    public Map<String, String> getHeaders() throws AuthFailureError {
        NetworkRequest networkRequest = getInnerNetworkRequest();
        if (networkRequest != null && networkRequest.headers != null) {
            return networkRequest.headers.toMap();
        }
        return Collections.emptyMap();
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request.  Can throw
     * {@link AuthFailureError} as authentication may be required to provide these values.
     * <p/>
     * <p>Note that you can directly override {@link #getBody()} for custom data.</p>
     *
     * @throws AuthFailureError in the event of auth failure
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        //TODO
        return null;
    }

    /**
     * Returns which encoding should be used when converting POST or PUT parameters returned by
     * {@link #getParams()} into a raw POST or PUT body.
     * <p/>
     * <p>This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values into bytes prior
     * to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters into a raw
     * byte array.</li>
     * </ol>
     */
    protected String getParamsEncoding() {
        //TODO
        return DEFAULT_PARAMS_ENCODING;
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    public String getBodyContentType() {
        NetworkRequest networkRequest = getInnerNetworkRequest();
        if (networkRequest != null) {
            return networkRequest.contentType.toString();
        }
        return null;
        //return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    /**
     * Returns the raw POST or PUT body to be sent.
     * <p/>
     * <p>By default, the body consists of the request parameters in
     * application/x-www-form-urlencoded format. When overriding this method, consider overriding
     * {@link #getBodyContentType()} as well to match the new body format.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    public byte[] getBody() throws AuthFailureError {
        NetworkRequest networkRequest = getInnerNetworkRequest();
        if (networkRequest != null) {
            return networkRequest.data;
        }
        //TODO
//        Map<String, String> params = getParams();
//        if (params != null && params.size() > 0) {
//            return encodeParameters(params, getParamsEncoding());
//        }
        return null;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * Set whether or not responses to this request should be cached.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<F, T> setShouldCache(boolean shouldCache) {
        checkIfActive();
        this.shouldCache = shouldCache;
        return this;
    }

    /**
     * Returns true if responses to this request should be cached.
     */
    public final boolean shouldCache() {
        return shouldCache;
    }

    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by default.
     */
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Returns the socket timeout in milliseconds per retry attempt. (This value can be changed
     * per retry attempt if a backoff is specified via backoffTimeout()). If there are no retry
     * attempts remaining, this will cause delivery of a {@link TimeoutError} error.
     */
    public final int getTimeoutMs() {
        return retryPolicy.getCurrentTimeout();
    }

    /**
     * Mark this request as having a response delivered on it.  This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    public void markDelivered() {
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
     * called from a worker thread.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        T parsed = null;
        try {
            parsed = converterFromResponse.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    /**
     * Subclasses can override this method to parse 'networkError' and return a more specific error.
     * <p/>
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
        if (responseListener != null) {
            responseListener.onResponse(response);
        }
    }

    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    public void deliverError(JusError error) {
        if (errorListener != null) {
            errorListener.onErrorResponse(error);
        }
    }


    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override
    public int compareTo(Request<F, T> other) {
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
        return (canceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " "
                + getPriority() + " " + sequence;
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
