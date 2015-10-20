/*
 * Copyright (C) 2015 AppTik Project
 * Copyright (C) 2015 Square, Inc.
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
package io.apptik.comm.jus.retro;


import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestBuilder;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.http.MediaType;

final class RequestFactory {
  private final String method;
  private final HttpUrl baseUrl;
  private final String relativeUrl;
  private final Headers headers;
  private final MediaType contentType;
  private final boolean hasBody;
  private final boolean isFormEncoded;
  private final boolean isMultipart;
  private final RequestBuilderAction[] requestBuilderActions;

  RequestFactory(String method, HttpUrl baseUrl, String relativeUrl, Headers headers,
      MediaType contentType, boolean hasBody, boolean isFormEncoded, boolean isMultipart,
      RequestBuilderAction[] requestBuilderActions) {
    this.method = method;
    this.baseUrl = baseUrl;
    this.relativeUrl = relativeUrl;
    this.headers = headers;
    this.contentType = contentType;
    this.hasBody = hasBody;
    this.isFormEncoded = isFormEncoded;
    this.isMultipart = isMultipart;
    this.requestBuilderActions = requestBuilderActions;
  }

  Request create(Converter<NetworkResponse, ?> responseConverter, Object... args) {
    RequestBuilder requestBuilder =
        new RequestBuilder(method, baseUrl, relativeUrl, responseConverter, headers, contentType, hasBody,
            isFormEncoded, isMultipart);

    if (args != null) {
      RequestBuilderAction[] actions = requestBuilderActions;
      if (actions.length != args.length) {
        throw new IllegalArgumentException("Argument count ("
            + args.length
            + ") doesn't match action count ("
            + actions.length
            + ")");
      }
      for (int i = 0, count = args.length; i < count; i++) {
        actions[i].perform(requestBuilder, args[i]);
      }
    }

    return requestBuilder.build();
  }
}
