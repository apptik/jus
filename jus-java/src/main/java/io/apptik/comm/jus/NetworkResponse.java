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

import java.net.HttpURLConnection;
import java.util.Arrays;

import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.MediaType;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkResponse {

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /** Response headers. */
    public final Headers headers;

    /** Network roundtrip time in milliseconds. */
    public final long networkTimeNs;

    public final MediaType contentType;


    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param networkTimeNs Round-trip network time to receive network response
     */
    public NetworkResponse(int statusCode, byte[] data, Headers headers, long networkTimeNs) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.networkTimeNs = networkTimeNs;
        this.contentType = MediaType.parse(this.headers.get(HTTP.CONTENT_TYPE));
    }

    /** True if the server returned a 304 (Not Modified). */
    public final boolean isNotModified() {
        return HttpURLConnection.HTTP_NOT_MODIFIED == statusCode;
    }



    @Override
    public String toString() {
        return "NetworkResponse{" +
                "contentType=" + contentType +
                ", statusCode=" + statusCode +
                ", data=" + Arrays.toString(data) +
                ", headers=" + headers +
                ", networkTimeNs=" + networkTimeNs +
                '}';
    }

    public static class Builder {

        /** The HTTP status code. */
        public int statusCode = HttpURLConnection.HTTP_OK;

        /** Raw data from this response. */
        public byte[] data;

        /** Response headers. */
        public Headers.Builder headers;

        /** Network roundtrip time in milliseconds. */
        public long networkTimeNs = 0;

        public NetworkResponse.Builder setHeader(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public NetworkResponse.Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public NetworkResponse.Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public NetworkResponse.Builder setHeaders(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        public NetworkResponse.Builder setBody(byte[] data) {
            this.data = data;
            return this;
        }

        public NetworkResponse.Builder setNetworkTimeNs(long networkTimeNs) {
            this.networkTimeNs = networkTimeNs;
            return this;
        }

        public NetworkResponse.Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public NetworkResponse build() {
            return new NetworkResponse(statusCode, data, headers.build(), networkTimeNs);
        }
    }
}

