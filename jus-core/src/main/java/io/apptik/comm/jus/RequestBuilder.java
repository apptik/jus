/*
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.http.MediaType;

public final class RequestBuilder {
    private final String method;

    private final HttpUrl baseUrl;
    private String relativeUrl;
    private HttpUrl.Builder urlBuilder;

    private final boolean hasBody;
    //TODO
//  private MultipartBuilder multipartBuilder;
//  private FormEncodingBuilder formEncodingBuilder;
    private NetworkRequest.Builder networkRequestBuilder;

    public RequestBuilder(String method, HttpUrl baseUrl, String relativeUrl, Headers headers,
                          MediaType contentType, boolean hasBody, boolean isFormEncoded, boolean isMultipart) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;
        this.hasBody = hasBody;
        this.networkRequestBuilder = new NetworkRequest.Builder();

        if (headers != null) {
            networkRequestBuilder.setHeaders(headers);
        }

        if (contentType != null) {
            networkRequestBuilder.setContentType(contentType);
        }

//TODO
//    if (isFormEncoded) {
//      // Will be set to 'body' in 'build'.
//      formEncodingBuilder = new FormEncodingBuilder();
//    } else if (isMultipart) {
//      // Will be set to 'body' in 'build'.
//      multipartBuilder = new MultipartBuilder();
//      multipartBuilder.type(MultipartBuilder.FORM);
//    }
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

    public RequestBuilder addPathParam(String name, String value, boolean encoded) {
        if (relativeUrl == null) {
            // The relative URL is cleared when the first query parameter is set.
            throw new AssertionError();
        }
        try {
            if (!encoded) {
                String encodedValue = URLEncoder.encode(String.valueOf(value), "UTF-8");
                // URLEncoder encodes for use as a query parameter. Path encoding uses %20 to
                // encode spaces rather than +. Query encoding difference specified in HTML spec.
                // Any remaining plus signs represent spaces as already URLEncoded.
                encodedValue = encodedValue.replace("+", "%20");
                relativeUrl = relativeUrl.replace("{" + name + "}", encodedValue);
            } else {
                relativeUrl = relativeUrl.replace("{" + name + "}", String.valueOf(value));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert path parameter \"" + name + "\" value to UTF-8:" + value, e);
        }
        return this;
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
//TODO
//  void addFormField(String name, String value, boolean encoded) {
//    if (encoded) {
//      formEncodingBuilder.addEncoded(name, value);
//    } else {
//      formEncodingBuilder.add(name, value);
//    }
//  }
//
//  void addPart(Headers headers, RequestBody body) {
//    multipartBuilder.addPart(headers, body);
//  }

    public RequestBuilder setBody(byte[] body) {
        networkRequestBuilder.setBody(body);
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

        if (networkRequestBuilder.data == null) {
            // Try to pull from one of the builders.
            //TODO
//      if (formEncodingBuilder != null) {
//        body = formEncodingBuilder.build();
//      } else if (multipartBuilder != null) {
//        body = multipartBuilder.build();
//      } else
            if (hasBody) {
                // Body is absent, make an empty body.
                networkRequestBuilder.setBody(new byte[0]);
            }
        }

        return null;

//    return requestBuilder
//        .url(url)
//        .method(method, body)
//        .build();
    }

}
