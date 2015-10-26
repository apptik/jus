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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;

final class MethodHandler<T> {
    @SuppressWarnings("unchecked")
    static MethodHandler<?> create(RetroProxy retroProxy, Method method) {
        Type responseType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(responseType)) {
            throw Utils.methodError(method,
                    "Method return type must not include a type variable or wildcard: %s", responseType);
        }

        if (!Utils.checkIfRequestRawType(responseType)) {
            throw Utils.methodError(method, "Service methods can only return Request<> type");
        }
        responseType = Utils.getRequestResponseType(responseType);

        //Annotation[] annotations = method.getAnnotations();
        Converter<NetworkResponse, Object> responseConverter =
                (Converter<NetworkResponse, Object>) createResponseConverter(method, retroProxy, responseType);
        RequestFactory requestFactory = RequestFactoryParser.parse(method, retroProxy);
        return new MethodHandler<>(retroProxy.requestQueue(), requestFactory, responseConverter
                , retroProxy.execManually());
    }


    private static Converter<NetworkResponse, ?> createResponseConverter(Method method,
                                                                         RetroProxy retroProxy, Type responseType) {
        Annotation[] annotations = method.getAnnotations();
        try {
            return retroProxy.responseConverter(responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw Utils.methodError(e, method, "Unable to create converter for %s", responseType);
        }
    }

    private final RequestQueue requestQueue;
    private final RequestFactory requestFactory;
    private final Converter<NetworkResponse, T> responseConverter;
    private final boolean execManually;

    private MethodHandler(RequestQueue requestQueue, RequestFactory requestFactory,
                          Converter<NetworkResponse, T> responseConverter, boolean execManually) {
        this.requestQueue = requestQueue;
        this.requestFactory = requestFactory;
        this.responseConverter = responseConverter;
        this.execManually = execManually;
    }

    Object invoke(Object... args) {
        Request request = requestFactory.create(responseConverter, args);
        if (execManually) {
            return request.prepRequestQueue(requestQueue);
        } else {
            return requestQueue.add(request);
        }
    }
}
