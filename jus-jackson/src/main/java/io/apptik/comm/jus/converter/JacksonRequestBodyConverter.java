/*
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
package io.apptik.comm.jus.converter;

import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.http.MediaType;

public final class JacksonRequestBodyConverter<T> implements Converter<T, NetworkRequest> {
  private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

  private final ObjectWriter adapter;

 public JacksonRequestBodyConverter(ObjectWriter adapter) {
    this.adapter = adapter;
  }

  @Override public NetworkRequest convert(T value) throws IOException {
    byte[] bytes = adapter.writeValueAsBytes(value);
    return new NetworkRequest.Builder().setContentType(MEDIA_TYPE).setBody(bytes).build();
  }
}
