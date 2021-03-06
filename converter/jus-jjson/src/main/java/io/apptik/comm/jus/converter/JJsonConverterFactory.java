
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
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.json.JsonArray;
import io.apptik.json.JsonElement;
import io.apptik.json.JsonObject;

/**
 * A {@linkplain Converter.Factory converter} which uses JustJson.
 * <p/>
 */
public final class JJsonConverterFactory extends Converter.Factory {
    /**
     * Create an instance.
     */
    public static JJsonConverterFactory create() {
        return new JJsonConverterFactory();
    }

    public JJsonConverterFactory() {
    }

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (JsonElement.class.isAssignableFrom((Class<?>) type)) {
            if (type.equals(JsonObject.class)) {
                return new JJsonObjectResponseConverter();
            } else if (type.equals(JsonArray.class)) {
                return new JJsonArrayResponseConverter();
            }

            return new JJsonResponseConverter();
        }

        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (JsonElement.class.isAssignableFrom((Class<?>) type)) {
            return new JJsonRequestConverter();
        }

        return null;
    }
}
