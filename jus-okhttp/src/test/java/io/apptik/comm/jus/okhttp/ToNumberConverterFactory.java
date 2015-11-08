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
package io.apptik.comm.jus.okhttp;

import com.squareup.okhttp.MediaType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.converter.Converters;

class ToNumberConverterFactory extends Converter.Factory {
  private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

  @Override
  public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
    if (Number.class.equals(type)) {
      return new Converter<NetworkResponse, Number>() {
        @Override
        public Number convert(NetworkResponse value) throws IOException {
          return Double.parseDouble(new Converters.StringResponseConverter().convert(value));
        }
      };
    }
    return null;
  }

  @Override public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
    if (Number.class.equals(type)) {
      return new Converter<Number, NetworkRequest>() {
        @Override
        public NetworkRequest convert(Number value) throws IOException {
          return new Converters.StringRequestConverter().convert(value.toString());
        }
      };
    }
    return null;
  }
}
