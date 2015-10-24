/*
 * Copyright (C) 2015 AppTik Project
 * Copyright (C) 2013 Square, Inc.
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

import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

/**
 * A {@linkplain Converter.Factory converter} that uses Wire for protocol buffers.
 * <p/>
 * This converter only applies for types which extend from {@link Message}
 */
public final class WireConverterFactory extends Converter.Factory {
    /**
     * Create an instance for conversion.
     */
    public static WireConverterFactory create() {
        return new WireConverterFactory();
    }

    /**
     * Create a converter factory
     */
    private WireConverterFactory() {
    }

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (!(type instanceof Class<?>)) {
            return null;
        }
        Class<?> c = (Class<?>) type;
        if (!Message.class.isAssignableFrom(c)) {
            return null;
        }
        //noinspection unchecked
        ProtoAdapter<? extends Message> adapter = ProtoAdapter.get((Class<? extends Message>) c);
        return new WireResponseBodyConverter<>(adapter);
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (!(type instanceof Class<?>)) {
            return null;
        }
        Class<?> c = (Class<?>) type;
        if (!Message.class.isAssignableFrom(c)) {
            return null;
        }
        ProtoAdapter<? extends Message> adapter = ProtoAdapter.get((Class<? extends Message>) c);
        return new WireRequestBodyConverter<>(adapter);
    }
}
