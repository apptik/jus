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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.MediaType;
import io.apptik.comm.jus.toolbox.Utils;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Source;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public final class NetworkResponse {

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /** Response headers. */
    public final Headers headers;

    /** Network roundtrip time in nanoseconds. */
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
        if(data == null) {
            data = new byte[0];
        }
        if(headers == null) {
            headers = new Headers.Builder().build();
        }
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

    public Charset getCharset() {
        return contentType != null ? contentType.charset(HTTP.CHARSET_UTF_8) : HTTP.CHARSET_UTF_8;
    }

    public ByteArrayInputStream getByteStream() {
        return new ByteArrayInputStream(data);
    }

    public final InputStreamReader getCharStream() throws IOException {
        return new InputStreamReader(getByteStream(), getCharset());
    }

    public Source getSource() {
        return Okio.source(getByteStream());
    }

    public BufferedSource getBufferedSource() {
        return Okio.buffer(getSource());
    }

    public ByteString getByteString() throws IOException {
       return getBufferedSource().readByteString();
    }

    public String getBodyAsString() {
       return new String(data, getCharset());
    }

    @Override
    public String toString() {
        return "NetworkResponse{" +
                "contentType=" + contentType +
                ", statusCode=" + statusCode +
                ", data=" + new String(data, Charset.forName(HTTP.UTF_8)) +
                ", headers=" + headers +
                ", networkTimeNs=" + networkTimeNs +
                '}';
    }

    public static NetworkResponse create(MediaType mediaType, String data) {
        Utils.checkNotNull(mediaType, "mediaType==null");
        return create(mediaType, new Buffer()
                .writeString(data, mediaType.charset(Charset.forName(HTTP.UTF_8)))
                .readByteArray());
    }

    public static NetworkResponse create(MediaType mediaType, byte[] bytes) {
        Utils.checkNotNull(mediaType, "mediaType==null");
        return new NetworkResponse.Builder()
                .setContentType(mediaType)
                .setBody(bytes)
                .build();
    }

    public static class Builder {

        /** The HTTP status code. */
        private int statusCode = HttpURLConnection.HTTP_OK;

        /** Raw data from this response. */
        private byte[] data;

        /** Response headers. */
        private Headers.Builder headers = new Headers.Builder();

        /** Network roundtrip time in milliseconds. */
        private long networkTimeNs = 0;

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

        public NetworkResponse.Builder setContentType(MediaType value) {
            if (value != null) {
                this.headers.set(HTTP.CONTENT_TYPE, value.toString());
            } else {
                this.headers.removeAll(HTTP.CONTENT_TYPE);
            }
            return this;
        }

        public NetworkResponse build() {
            return new NetworkResponse(statusCode, data, headers.build(), networkTimeNs);
        }
    }
}

