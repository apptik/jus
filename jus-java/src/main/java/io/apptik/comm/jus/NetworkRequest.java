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
 * Data and headers which forms a {@link Request}.
 */
public final class NetworkRequest {
    /**
     * Creates a new network response.
     *
     * @param data    Response body
     * @param headers Headers returned with this response, or null for none
     */
    public NetworkRequest(byte[] data, Headers headers, MediaType contentType) {
        this.data = data;
        this.headers = headers;
        this.contentType = contentType;
    }

    /**
     * Raw data
     */
    public final byte[] data;

    /**
     * headers.
     */
    public final Headers headers;

    public final MediaType contentType;


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
        return "NetworkRequest{" +
                "\n\tcontentType=" + contentType +
                "\n\tdata=" + ((data == null) ? "null" : new String(data, Charset.forName(HTTP
                .UTF_8))) +
                "\n\theaders=" + headers +
                '}';
    }

    public static NetworkRequest create(MediaType mediaType, String data) {
        Utils.checkNotNull(mediaType, "mediaType==null");
        return create(mediaType, new Buffer()
                .writeString(data, mediaType.charset(Charset.forName(HTTP.UTF_8)))
                .readByteArray());
    }

    public static NetworkRequest create(MediaType mediaType, byte[] bytes) {
        Utils.checkNotNull(mediaType, "mediaType==null");
        return new NetworkRequest.Builder()
                .setContentType(mediaType)
                .setBody(bytes)
                .build();
    }

    public static class Builder {
        /**
         * Raw data from this response.
         */
        private byte[] data;

        /**
         * Response headers.
         */
        private Headers.Builder headers = new Headers.Builder();

        public static Builder from(final NetworkRequest networkRequest) {
            if (networkRequest == null) {
                return new Builder();
            }
            return new Builder()
                    .setBody(networkRequest.data)
                    .setContentType(networkRequest.contentType)
                    .setHeaders(networkRequest.headers);
        }

        public NetworkRequest.Builder setHeader(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public NetworkRequest.Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public NetworkRequest.Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public NetworkRequest.Builder setHeaders(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        public NetworkRequest.Builder addHeaders(Headers headers) {
            this.headers.addMMap(headers.toMultimap());
            return this;
        }

        public NetworkRequest.Builder setContentType(MediaType value) {
            if (value != null) {
                this.headers.set(HTTP.CONTENT_TYPE, value.toString());
            } else {
                this.headers.removeAll(HTTP.CONTENT_TYPE);
            }
            return this;
        }

        public NetworkRequest.Builder setContentType(String value) {
            return setContentType(MediaType.parse(value));
        }

        public NetworkRequest.Builder setBody(byte[] data) {
            this.data = data;
            return this;
        }

        public boolean hasBody() {
            return (data != null);
        }

        public boolean hasContentType() {
            return (this.headers.get(HTTP.CONTENT_TYPE) != null);
        }

        public NetworkRequest build() {
            return new NetworkRequest(data, headers.build(), MediaType.parse(this.headers.get
                    (HTTP.CONTENT_TYPE)));
        }
    }

}

