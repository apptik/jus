/*
 * Copyright (C) 2015 AppTik Project
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


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class BasicConverterFactory extends Converter.Factory {

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkResponseConverter();
            }
            if (String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringResponseConverter();
            }
            if (Void.class.equals(type)) {
                return new Converter<NetworkResponse, Void>() {
                    @Override
                    public Void convert(NetworkResponse value) throws IOException {
                        return null;
                    }
                };
            }
        }

        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkRequest.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkRequestConverter();
            }
            if (String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringRequestConverter();
            }
        }
        return null;
    }
}
