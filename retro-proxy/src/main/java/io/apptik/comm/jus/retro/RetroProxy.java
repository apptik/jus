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
package io.apptik.comm.jus.retro;


import android.content.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.BasicConverter;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.DELETE;
import io.apptik.comm.jus.retro.http.Field;
import io.apptik.comm.jus.retro.http.FormUrlEncoded;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.HEAD;
import io.apptik.comm.jus.retro.http.HTTP;
import io.apptik.comm.jus.retro.http.Header;
import io.apptik.comm.jus.retro.http.Headers;
import io.apptik.comm.jus.retro.http.Multipart;
import io.apptik.comm.jus.retro.http.PATCH;
import io.apptik.comm.jus.retro.http.POST;
import io.apptik.comm.jus.retro.http.PUT;
import io.apptik.comm.jus.retro.http.Part;
import io.apptik.comm.jus.retro.http.Path;
import io.apptik.comm.jus.retro.http.Query;

import static io.apptik.comm.jus.retro.Utils.checkNotNull;

/**
 * Adapts a Java interface to HTTP Calls.
 * <p/>
 * API endpoints are defined as methods on an interface with annotations providing metadata about
 * the form in which the HTTP call should be made.
 * <p/>
 * The relative path for a given method is obtained from an annotation on the method describing
 * the request type. The built-in methods are {@link GET GET},
 * {@link PUT PUT}, {@link POST POST}, {@link PATCH
 * PATCH}, {@link HEAD HEAD}, and {@link DELETE DELETE}. You can use a
 * custom HTTP method with {@link HTTP @HTTP}.
 * <p/>
 * Method parameters can be used to replace parts of the URL by annotating them with
 * {@link Path @Path}. Replacement sections are denoted by an identifier surrounded
 * by curly braces (e.g., "{foo}"). To add items to the query string of a URL use
 * {@link Query @Query}.
 * <p/>
 * The body of a request is denoted by the {@link Body @Body} annotation. The object
 * will be converted to request representation by one of the {@link Converter.Factory} instances.
 * A {@link io.apptik.comm.jus.NetworkRequest} can also be used for a raw representation.
 * <p/>
 * Alternative request body formats are supported by method annotations and corresponding parameter
 * annotations:
 * <ul>
 * <li>{@link FormUrlEncoded @FormUrlEncoded} - Form-encoded data with key-value
 * pairs specified by the {@link Field @Field} parameter annotation.
 * <li>{@link Multipart @Multipart} - RFC 2387-compliant multi-part data with parts
 * specified by the {@link Part @Part} parameter annotation.
 * </ul>
 * <p/>
 * Additional static headers can be added for an endpoint using the
 * {@link Headers @Headers} method annotation. For per-request control over a header
 * annotate a parameter with {@link Header @Header}.
 * <p/>
 * By default, methods return a {@link Call} which represents the HTTP request. The generic
 * parameter of the call is the response body type and will be converted by one of the
 * {@link Converter.Factory} instances. {@link io.apptik.comm.jus.NetworkResponse} can also be used for a raw
 * representation. {@link Void} can be used if you do not care about the body contents.
 * <p/>
 * For example:
 * <pre>
 * public interface CategoryService {
 *   &#64;POST("/category/{cat}")
 *   Call&lt;List&lt;Item&gt;&gt; categoryList(@Path("cat") String a, @Query("page") int b);
 * }
 * </pre>
 * <p/>
 * Calling {@link #create(Class) create()} with {@code CategoryService.class} will validate the
 * annotations and create a new implementation of the service definition.
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public final class RetroProxy {
    private final Map<Method, MethodHandler<?>> methodHandlerCache = new LinkedHashMap<>();

    private final RequestQueue requestQueue;
    private final HttpUrl baseUrl;
    private final List<Converter.Factory> converters;
    private final boolean validateEagerly;

    private RetroProxy(RequestQueue requestQueue, HttpUrl baseUrl, List<Converter.Factory> converters,
                       boolean validateEagerly) {
        this.requestQueue = requestQueue;
        this.baseUrl = baseUrl;
        this.converters = converters;
        this.validateEagerly = validateEagerly;
    }

    /**
     * Create an implementation of the API defined by the {@code service} interface.
     */
    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(final Class<T> service) {
        Utils.validateServiceInterface(service);
        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        return loadMethodHandler(method).invoke(args);
                    }
                });
    }

    private void eagerlyValidateMethods(Class<?> service) {
        for (Method method : service.getDeclaredMethods()) {
                loadMethodHandler(method);
        }
    }

    MethodHandler<?> loadMethodHandler(Method method) {
        MethodHandler<?> handler;
        synchronized (methodHandlerCache) {
            handler = methodHandlerCache.get(method);
            if (handler == null) {
                handler = MethodHandler.create(this, method);
                methodHandlerCache.put(method, handler);
            }
        }
        return handler;
    }

    public RequestQueue requestQueue() {
        return requestQueue;
    }

    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * TODO
     */
    public List<Converter.Factory> converterFactories() {
        return Collections.unmodifiableList(converters);
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link NetworkRequest} from the available
     * {@linkplain #converterFactories() factories}.
     */
    public Converter<?, NetworkRequest> requestConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        for (int i = 0, count = converters.size(); i < count; i++) {
            Converter<?, NetworkRequest> converter =
                    converters.get(i).toRequestBody(type, annotations);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(type)
                .append(". Tried:");
        for (Converter.Factory converterFactory : converters) {
            builder.append("\n * ").append(converterFactory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link Converter} for {@link io.apptik.comm.jus.NetworkResponse} to {@code type} from the available
     * {@linkplain #converterFactories() factories}.
     */
    public Converter<NetworkResponse, ?> responseConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        for (int i = 0, count = converters.size(); i < count; i++) {
            Converter<NetworkResponse, ?> converter =
                    converters.get(i).fromResponseBody(type, annotations);
            if (converter != null) {
                return converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(type)
                .append(". Tried:");
        for (Converter.Factory converterFactory : converters) {
            builder.append("\n * ").append(converterFactory.getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Build a new {@link RetroProxy}.
     * <p/>
     * Calling {@link #baseUrl} is required before calling {@link #build(Context)} ()}. All other methods
     * are optional.
     */
    public static final class Builder {
        private RequestQueue requestQueue;
        private HttpUrl baseUrl;
        private List<Converter.Factory> converters = new ArrayList<>();
        private boolean validateEagerly;

        public Builder() {
            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converters.add(new BasicConverter());
        }

        /**
         * The HTTP requestQueue used for requests.
         */
        public Builder client(RequestQueue requestQueue) {
            this.requestQueue = checkNotNull(requestQueue, "requestQueue == null");
            return this;
        }

        /** API base URL. */
        public Builder baseUrl(String baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        /**
         * API base URL.
         */
        public Builder baseUrl(HttpUrl baseUrl) {
            this.baseUrl = checkNotNull(baseUrl, "baseUrl == null");
            return this;
        }

        /**
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(Converter.Factory converterFactory) {
            converters.add(checkNotNull(converterFactory, "converterFactory == null"));
            return this;
        }

        /**
         * When calling {@link #create} on the resulting {@link RetroProxy} instance, eagerly validate
         * the configuration of all methods in the supplied interface.
         */
        public Builder validateEagerly() {
            validateEagerly = true;
            return this;
        }

        /**
         * Create the {@link RetroProxy} instances.
         */
        public RetroProxy build(Context context) {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            RequestQueue client = this.requestQueue;
            if (client == null) {
                client = Jus.newRequestQueue(context);
            }

            // Make a defensive copy of the converters.
            List<Converter.Factory> converterFactories = new ArrayList<>(this.converters);

            return new RetroProxy(client, baseUrl, converterFactories,
                    validateEagerly);
        }
    }
}
