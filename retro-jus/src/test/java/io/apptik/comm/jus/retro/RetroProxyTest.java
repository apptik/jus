// Copyright 2013 Square, Inc.
package io.apptik.comm.jus.retro;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.BasicConverterFactory;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.http.MediaType;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.POST;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class RetroProxyTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    interface RequestMethod {
        @GET("/")
        Request<Number> disallowed();

        @POST("/")
        Request<NetworkResponse> disallowed(@Body Number body);

        @GET("/")
        Request<NetworkResponse> getNetworkResponse();

        @GET("/")
        Request<Void> getVoid();

        @POST("/")
        Request<NetworkResponse> postNetworkRequest(@Body NetworkRequest body);
    }

    interface FutureMethod {
        @GET("/")
        Future<String> method();
    }

    interface Extending extends RequestMethod {
    }

    interface StringService {
        @GET("/")
        String get();
    }

    interface Unresolvable {
        @GET("/")
        <T> Request<T> typeVariable();

        @GET("/")
        <T extends NetworkResponse> Request<T> typeVariableUpperBound();

        @GET("/")
        <T> Request<List<Map<String, Set<T[]>>>> crazy();

        @GET("/")
        Request<?> wildcard();

        @GET("/")
        Request<? extends NetworkResponse> wildcardUpperBound();
    }

    interface VoidService {
        @GET("/")
        void nope();
    }

    interface Annotated {
        @GET("/")
        @Foo
        Request<Number> method();

        @POST("/")
        Request<NetworkResponse> parameter(@Foo @Body Number param);

        @Retention(RUNTIME)
        @interface Foo {
        }
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes") // We are explicitly testing this behavior.
    @Test
    public void objectMethodsStillWork() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);

        assertThat(example.hashCode()).isNotZero();
        assertThat(example.equals(this)).isFalse();
        assertThat(example.toString()).isNotEmpty();
    }

    @Test
    public void interfaceWithExtendIsNotSupported() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        try {
            retroProxy.create(Extending.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("API interfaces must not extend other interfaces.");
        }
    }

    @Test
    public void voidReturnTypeNotAllowed() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        VoidService service = retroProxy.create(VoidService.class);

        try {
            service.nope();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageStartingWith(
                    "Service methods can only return Request<> type\n    for method VoidService.nope");
        }
    }

    @Test
    public void validateEagerlyFailsAtCreation() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .validateEagerly()
                .build();

        try {
            retroProxy.create(VoidService.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageStartingWith(
                    "Service methods can only return Request<> type\n    for method VoidService.nope");
        }
    }

    @Test
    public void callRequestAdapterAddedByDefault() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);
        assertThat(example.getNetworkResponse()).isNotNull();
    }


    @Test
    public void methodAnnotationsPassedToConverter() throws ExecutionException, InterruptedException {
        final AtomicReference<Annotation[]> annotationsRef = new AtomicReference<>();

        class MyConverterFactory extends Converter.Factory {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                annotationsRef.set(annotations);
                return new Converters.StringResponseConverter();
            }
        }
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new MyConverterFactory())
                .build();
        Annotated annotated = retroProxy.create(Annotated.class);
        annotated.method(); // Trigger internal setup.

        Annotation[] annotations = annotationsRef.get();
        assertThat(annotations).hasAtLeastOneElementOfType(Annotated.Foo.class);
    }

    @Test
    public void parameterAnnotationsPassedToConverter() {
        final AtomicReference<Annotation[]> annotationsRef = new AtomicReference<>();
        class MyConverterFactory extends Converter.Factory {
            @Override
            public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
                annotationsRef.set(annotations);
                return new ToNumberConverterFactory().toRequest(type, annotations);
            }
        }
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new MyConverterFactory())
                .build();
        Annotated annotated = retroProxy.create(Annotated.class);
        annotated.parameter(233); // Trigger internal setup.

        Annotation[] annotations = annotationsRef.get();
        assertThat(annotations).hasAtLeastOneElementOfType(Annotated.Foo.class);
    }

    @Test
    public void missingConverterThrowsOnNonNetworkRequest() throws IOException {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);
        try {
            example.disallowed(1337);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Unable to create @Body converter for class java.lang.Number (parameter #1)\n"
                            + "    for method RequestMethod.disallowed");
            assertThat(e.getCause()).hasMessage(
                    "Could not locate Request converter for class java.lang.Number. Tried:\n"
                            + " * io.apptik.comm.jus.converter.BasicConverterFactory");
        }
    }

    @Test
    public void missingConverterThrowsOnNonNetworkResponse() throws IOException {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        try {
            example.disallowed();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Unable to create converter for class java.lang.Number\n"
                    + "    for method RequestMethod.disallowed");
            assertThat(e.getCause()).hasMessage(
                    "Could not locate Response converter for class java.lang.Number. Tried:\n"
                            + " * io.apptik.comm.jus.converter.BasicConverterFactory");
        }
    }

    @Test
    public void converterReturningNullThrows() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new Converter.Factory() {
                })
                .build();
        RequestMethod service = retroProxy.create(RequestMethod.class);
        try {
            service.disallowed();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Unable to create converter for class java.lang.Number\n"
                    + "    for method RequestMethod.disallowed");
            assertThat(e.getCause()).hasMessage(
                    "Could not locate Response converter for class java.lang.Number. Tried:\n"
                            + " * io.apptik.comm.jus.converter.BasicConverterFactory\n"
                            + " * io.apptik.comm.jus.retro.RetroProxyTest$1");
        }
    }

    @Test
    public void requestBodyOutgoingAllowed() throws IOException, ExecutionException, InterruptedException {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        NetworkResponse response = example.getNetworkResponse().getFuture().get();
        assertThat(response.getBodyAsString()).isEqualTo("Hi");
    }

    @Test
    public void voidOutgoingAllowed() throws IOException, ExecutionException, InterruptedException {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        Request<Void> request = example.getVoid();
        request.getFuture().get();
        assertThat(request.getRawResponse().isSuccess()).isTrue();
        //should be not null
        assertThat(request.getRawResponse().result).isNull();
    }

    @Test
    public void responseBodyIncomingAllowed() throws IOException, InterruptedException, ExecutionException {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        RequestMethod example = retroProxy.create(RequestMethod.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "Hey");
        NetworkResponse response = example.postNetworkRequest(body).getFuture().get();
        assertThat(response.getBodyAsString()).isEqualTo("Hi");

        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("Hey");
    }

    @Test
    public void unresolvableTypeThrows() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToStringConverterFactory())
                .build();
        Unresolvable example = retroProxy.create(Unresolvable.class);

        try {
            example.typeVariable();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Method return type must not include a type variable or wildcard: "
                    + "io.apptik.comm.jus.Request<T>\n    for method Unresolvable.typeVariable");
        }
        try {
            example.typeVariableUpperBound();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Method return type must not include a type variable or wildcard: "
                    + "io.apptik.comm.jus.Request<T>\n    for method Unresolvable.typeVariableUpperBound");
        }
        try {
            example.crazy();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Method return type must not include a type variable or wildcard: "
                    + "io.apptik.comm.jus.Request<java.util.List<java.util.Map<java.lang.String, java.util.Set<T[]>>>>\n"
                    + "    for method Unresolvable.crazy");
        }
        try {
            example.wildcard();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Method return type must not include a type variable or wildcard: "
                    + "io.apptik.comm.jus.Request<?>\n    for method Unresolvable.wildcard");
        }
        try {
            example.wildcardUpperBound();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Method return type must not include a type variable or wildcard: "
                    + "io.apptik.comm.jus.Request<? extends io.apptik.comm.jus.NetworkResponse>\n"
                    + "    for method Unresolvable.wildcardUpperBound");
        }
    }

    @Test
    public void baseUrlRequired() {
        try {
            new RetroProxy.Builder().build();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Base URL required.");
        }
    }

    @Test
    public void baseUrlNullThrows() {
        try {
            new RetroProxy.Builder().baseUrl((String) null);
            fail();
        } catch (NullPointerException e) {
            assertThat(e).hasMessage("baseUrl == null");
        }
        try {
            new RetroProxy.Builder().baseUrl((HttpUrl) null);
            fail();
        } catch (NullPointerException e) {
            assertThat(e).hasMessage("baseUrl == null");
        }
    }

    @Test
    public void baseUrlInvalidThrows() {
        try {
            new RetroProxy.Builder().baseUrl("ftp://foo/bar");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Illegal URL: ftp://foo/bar");
        }
    }

    @Test
    public void baseUrlStringPropagated() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .build();
        HttpUrl baseUrl = retroProxy.baseUrl();
        assertThat(baseUrl).isNotNull();
        assertThat(baseUrl.url().toString()).isEqualTo("http://example.com/");
    }

    @Test
    public void baseHttpUrlPropagated() {
        HttpUrl url = HttpUrl.parse("http://example.com/");
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(url)
                .build();
        HttpUrl baseUrl = retroProxy.baseUrl();
        assertThat(baseUrl).isNotNull();
        assertThat(baseUrl).isSameAs(url);
    }

    @Test
    public void queueNullThrows() {
        try {
            new RetroProxy.Builder().requestQueue(null);
            fail();
        } catch (NullPointerException e) {
            assertThat(e).hasMessage("requestQueue == null");
        }
    }

    @Test
    public void queueDefault() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com")
                .build();
        assertThat(retroProxy.requestQueue()).isNotNull();
    }

    @Test
    public void queuePropagated() {
        RequestQueue queue = Jus.newRequestQueue();
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .requestQueue(queue)
                .build();
        assertThat(retroProxy.requestQueue()).isSameAs(queue);
    }

    @Test
    public void converterNullThrows() {
        try {
            new RetroProxy.Builder().addConverterFactory(null);
            fail();
        } catch (NullPointerException e) {
            assertThat(e).hasMessage("converterFactory == null");
        }
    }

    @Test
    public void converterFactoryDefault() {
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .build();
        List<Converter.Factory> converterFactories = retroProxy.converterFactories();
        assertThat(converterFactories).hasSize(1);
        assertThat(converterFactories.get(0)).isInstanceOf(BasicConverterFactory.class);
    }

    @Test
    public void requestConverterFactoryQueried() {
        Type type = Number.class;
        Annotation[] annotations = new Annotation[0];

        Converter<?, NetworkRequest> expectedAdapter = mock(Converter.class);
        Converter.Factory factory = mock(Converter.Factory.class);

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .addConverterFactory(factory)
                .build();

        doReturn(expectedAdapter).when(factory).toRequest(type, annotations);

        Converter<?, NetworkRequest> actualAdapter = retroProxy.requestConverter(type, annotations);
        assertThat(actualAdapter).isSameAs(expectedAdapter);

        verify(factory).toRequest(type, annotations);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void requestConverterFactoryNoMatchThrows() {
        Type type = Number.class;
        Annotation[] annotations = new Annotation[0];

        Converter.Factory factory1 = spy(new Converter.Factory() {
            @Override
            public Converter<?, NetworkRequest> toRequest(Type returnType, Annotation[] annotations) {
                return null;
            }
        });

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .addConverterFactory(factory1)
                .build();

        try {
            retroProxy.requestConverter(type, annotations);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageStartingWith(
                    "Could not locate Request converter for class java.lang.Number. Tried:");
        }

        verify(factory1).toRequest(type, annotations);
        verifyNoMoreInteractions(factory1);
    }

    @Test
    public void responseConverterFactoryQueried() {
        Type type = Number.class;
        Annotation[] annotations = new Annotation[0];

        Converter<NetworkResponse, ?> expectedAdapter = mock(Converter.class);
        Converter.Factory factory = mock(Converter.Factory.class);

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .addConverterFactory(factory)
                .build();

        doReturn(expectedAdapter).when(factory).fromResponse(type, annotations);

        Converter<NetworkResponse, ?> actualAdapter = retroProxy.responseConverter(type, annotations);
        assertThat(actualAdapter).isSameAs(expectedAdapter);

        verify(factory).fromResponse(type, annotations);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void responseConverterFactoryNoMatchThrows() {
        Type type = Number.class;
        Annotation[] annotations = new Annotation[0];

        Converter.Factory factory1 = spy(new Converter.Factory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type returnType, Annotation[] annotations) {
                return null;
            }
        });

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .addConverterFactory(factory1)
                .build();

        try {
            retroProxy.responseConverter(type, annotations);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageStartingWith(
                    "Could not locate Response converter for class java.lang.Number. Tried:");
        }

        verify(factory1).fromResponse(type, annotations);
        verifyNoMoreInteractions(factory1);
    }

    @Test
    public void converterFactoryPropagated() {
        Converter.Factory factory = mock(Converter.Factory.class);
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .addConverterFactory(factory)
                .build();
        assertThat(retroProxy.converterFactories()).contains(factory);
    }

}
