
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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.converter.JsonWrapperResponseConverter.DefaultJsonWrapperFactory;
import io.apptik.json.wrapper.JsonElementWrapper;
import io.apptik.json.wrapper.JsonObjectArrayWrapper;
import io.apptik.json.wrapper.JsonObjectWrapper;
import io.apptik.json.wrapper.JsonStringArrayWrapper;


/**
 * A {@linkplain Converter.Factory converter} which uses JustJson.
 * <p/>
 */
public final class JsonWrapperConverterFactory extends Converter.Factory {
    /**
     * Create an instance.
     */
    public static JsonWrapperConverterFactory create() {
        return new JsonWrapperConverterFactory();
    }

    DefaultJsonWrapperFactory defaultWrapperFactory;

    public JsonWrapperConverterFactory() {
    }

    public JsonWrapperConverterFactory(DefaultJsonWrapperFactory defaultWrapperFactory) {
        this.defaultWrapperFactory = defaultWrapperFactory;
    }

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (JsonElementWrapper.class.isAssignableFrom((Class<?>) type)) {
            if (type.equals(JsonObjectWrapper.class)) {
                return new JsonObjectWrapperResponseConverter();
            } else if (JsonObjectWrapper.class.isAssignableFrom((Class<?>) type)) {
                return new JsonObjectWrapperResponseConverter((Class<?>) type);
            } else if (type.equals(JsonStringArrayWrapper.class)) {
                return new JsonStringArrayWrapperResponseConverter();
            }

            if (type instanceof ParameterizedType) {
                Type type1 = ((ParameterizedType) type).getRawType();
                if (JsonObjectArrayWrapper.class.isAssignableFrom((Class<?>) type1)) {
                    return new JsonObjectArrayWrapperResponseConverter
                            ((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0]);
                }
            }

            if(defaultWrapperFactory!=null) {
                return new JsonWrapperResponseConverter(defaultWrapperFactory);
            }

            return null;
        }

        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (JsonElementWrapper.class.isAssignableFrom((Class<?>) type)) {
            return new JsonWrapperRequestConverter();
        }

        return null;
    }
}
