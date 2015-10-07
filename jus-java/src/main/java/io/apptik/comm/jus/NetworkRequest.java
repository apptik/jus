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

import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.MediaType;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkRequest {
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


    public static class Builder {
        /**
         * Raw data from this response.
         */
        public byte[] data;

        /**
         * Response headers.
         */
        public Headers.Builder headers;

        public MediaType contentType;

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

        public NetworkRequest.Builder setContentType(MediaType value) {
            contentType = value;
            this.headers.set(HTTP.CONTENT_TYPE, contentType.toString());
            return this;
        }

        public NetworkRequest.Builder setContentType(String value) {
            return setContentType(MediaType.parse(value));
        }

        public NetworkRequest.Builder setBody(byte[] data) {
            this.data = data;
            return this;
        }

        public NetworkRequest build() {
            return new NetworkRequest(data, headers.build(), contentType);
        }
    }

}

