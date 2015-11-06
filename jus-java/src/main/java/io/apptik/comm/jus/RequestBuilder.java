/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2012 Square, Inc.
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

import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.http.MediaType;
import okio.Buffer;

public final class RequestBuilder {

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";


    private final String method;

    private final HttpUrl baseUrl;
    private String relativeUrl;
    private HttpUrl.Builder urlBuilder;
    Converter<NetworkResponse, ?> responseConverter;
    private final boolean hasBody;
    private MultipartBuilder multipartBuilder;
    private FormEncodingBuilder formEncodingBuilder;
    private NetworkRequest.Builder networkRequestBuilder;
    private Request.Priority priority;
    private Object tag;
    private boolean shouldCache;

    public RequestBuilder(String method, HttpUrl baseUrl, String relativeUrl,
                          Converter<NetworkResponse, ?> responseConverter, Headers headers,
                          MediaType contentType, boolean hasBody, boolean isFormEncoded,
                          boolean isMultipart, Request.Priority priority,
                          String tag, boolean shouldCache) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;
        this.hasBody = hasBody;
        this.networkRequestBuilder = new NetworkRequest.Builder();
        this.responseConverter = responseConverter;
        this.priority = priority;
        this.tag = tag;
        this.shouldCache = shouldCache;

        if (headers != null) {
            networkRequestBuilder.setHeaders(headers);
        }

        if (contentType != null) {
            networkRequestBuilder.setContentType(contentType);
        }


        if (isFormEncoded) {
            // Will be set to 'body' in 'build'.
            formEncodingBuilder = new FormEncodingBuilder();
        } else if (isMultipart) {
            // Will be set to 'body' in 'build'.
            multipartBuilder = new MultipartBuilder();
            multipartBuilder.type(MultipartBuilder.FORM);
        }
    }

    public RequestBuilder setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
        return this;
    }

    public RequestBuilder addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            networkRequestBuilder.setContentType(MediaType.parse(value));
        } else {
            networkRequestBuilder.addHeader(name, value);
        }
        return this;
    }

    public boolean hasContentTypeSet() {
        return (networkRequestBuilder.hasContentType());
    }

    public RequestBuilder addPathParam(String name, String value, boolean encoded) {
        if (relativeUrl == null) {
            // The relative URL is cleared when the first query parameter is set.
            throw new AssertionError();
        }
        relativeUrl = relativeUrl.replace("{" + name + "}", canonicalize(value, encoded));
        return this;
    }

    static String canonicalize(String input, boolean alreadyEncoded) {
        int codePoint;
        for (int i = 0, limit = input.length(); i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20 || codePoint >= 0x7f
                    || PATH_SEGMENT_ENCODE_SET.indexOf(codePoint) != -1
                    || (codePoint == '%' && !alreadyEncoded)) {
                // Slow path: the character at i requires encoding!
                Buffer out = new Buffer();
                out.writeUtf8(input, 0, i);
                canonicalize(out, input, i, limit, alreadyEncoded);
                return out.readUtf8();
            }
        }

        // Fast path: no characters required encoding.
        return input;
    }

    static void canonicalize(Buffer out, String input, int pos, int limit, boolean alreadyEncoded) {
        Buffer utf8Buffer = null; // Lazily allocated.
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded
                    && (codePoint == '\t' || codePoint == '\n' || codePoint == '\f' || codePoint == '\r')) {
                // Skip this character.
            } else if (codePoint < 0x20
                    || codePoint >= 0x7f
                    || PATH_SEGMENT_ENCODE_SET.indexOf(codePoint) != -1
                    || (codePoint == '%' && !alreadyEncoded)) {
                // Percent encode this character.
                if (utf8Buffer == null) {
                    utf8Buffer = new Buffer();
                }
                utf8Buffer.writeUtf8CodePoint(codePoint);
                while (!utf8Buffer.exhausted()) {
                    int b = utf8Buffer.readByte() & 0xff;
                    out.writeByte('%');
                    out.writeByte(HEX_DIGITS[(b >> 4) & 0xf]);
                    out.writeByte(HEX_DIGITS[b & 0xf]);
                }
            } else {
                // This character doesn't need encoding. Just copy it over.
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    public RequestBuilder addQueryParam(String name, String value, boolean encoded) {
        if (relativeUrl != null) {
            // Do a one-time combination of the built relative URL and the base URL.
            urlBuilder = baseUrl.resolve(relativeUrl).newBuilder();
            relativeUrl = null;
        }

        if (encoded) {
            urlBuilder.addEncodedQueryParameter(name, value);
        } else {
            urlBuilder.addQueryParameter(name, value);
        }
        return this;
    }

    public void addFormField(String name, String value, boolean encoded) {
        if (encoded) {
            formEncodingBuilder.addEncoded(name, value);
        } else {
            formEncodingBuilder.add(name, value);
        }
    }

    public void addPart(NetworkRequest networkRequest) {
        multipartBuilder.addPart(networkRequest);
    }

    public RequestBuilder setBody(byte[] body) {
        networkRequestBuilder.setBody(body);
        return this;
    }

    public RequestBuilder setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Request build() {
        HttpUrl url;
        HttpUrl.Builder urlBuilder = this.urlBuilder;
        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {
            // No query parameters triggered builder creation, just combine the relative URL and base URL.
            url = baseUrl.resolve(relativeUrl);
        }

        if (!networkRequestBuilder.hasBody()) {
            // Try to pull from one of the builders.
            if (formEncodingBuilder != null) {
                NetworkRequest nr = formEncodingBuilder.build();
                networkRequestBuilder
                        .setBody(nr.data)
                        .setContentType(nr.contentType)
                        .addHeaders(nr.headers);
            } else if (multipartBuilder != null) {
                NetworkRequest nr = multipartBuilder.build();
                networkRequestBuilder
                        .setBody(nr.data)
                        .setContentType(nr.contentType)
                        .addHeaders(nr.headers);
            } else if (hasBody) {
                // Body is absent, make an empty body.
                networkRequestBuilder.setBody(new byte[0]);
            }
        }


        Request request = new Request(method, url, responseConverter)
                .setNetworkRequest(networkRequestBuilder.build())
                .setPriority(priority)
                .setShouldCache(shouldCache)
                ;
        if(tag!=null) {
            request.setTag(tag);
        }

        return request;


    }
}
