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
package io.apptik.comm.jus.retro;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.SocketPolicy;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.ParseError;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.TimeoutError;
import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.HEAD;
import io.apptik.comm.jus.retro.http.POST;
import io.apptik.comm.jus.retro.http.Streaming;

import static com.squareup.okhttp.mockwebserver.SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class RequestTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    public RetroProxy retroProxy;

    interface Service {
        @GET("/")
        Request<String> getString();

        @HEAD("/")
        Request<NetworkResponse> getHead();

        @GET("/")
        Request<Number> getNumber();

        @GET("/")
        Request<NetworkResponse> getBody();

        @GET("/")
        @Streaming
        Request<NetworkResponse> getStreamingBody();

        @POST("/")
        Request<String> postString(@Body String body);

        @POST("/")
        Request<Number> postNumber(@Body Number body);
    }

    @Test
    public void http200Sync() throws IOException, ExecutionException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        Request<String> request = example.getString();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isEqualTo("Hi");
        assertThat(response.error).isNull();
    }

    @Test
    public void httpHead200Sync() throws IOException, ExecutionException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hello"));

        Request<NetworkResponse> request = example.getHead();
        NetworkResponse networkResponse = request.getFuture().get();
        Response<NetworkResponse> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.error).isNull();
        assertThat(networkResponse.data.length).isEqualTo(0);
        assertThat(networkResponse.headers.get(HTTP.CONTENT_LEN)).isEqualTo("5");
    }

    @Test
    public void http200Async() throws InterruptedException, ExecutionException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        final AtomicReference<String> responseRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Request<String> request = example.getString().addErrorListener(new RequestListener.ErrorListener() {
            @Override
            public void onError(JusError error) {
                error.printStackTrace();
            }
        }).addResponseListener(new RequestListener.ResponseListener<String>() {
            @Override
            public void onResponse(String response) {
                responseRef.set(response);
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, SECONDS));
        Response<String> response = request.getRawResponse();
        String response1 = responseRef.get();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isEqualTo("Hi");
        assertThat(response.error).isNull();
        assertThat(response1).isEqualTo("Hi");
    }

    @Test
    public void http404Sync() throws IOException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .execManually()
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"));

        Request<String> request = example.getString();
        try {
            request.enqueue().getFuture().get();
        } catch (ExecutionException e) {
            JusError error = (JusError)e.getCause();
            assertThat(error).isNotNull();
            assertThat(error.networkResponse.statusCode).isEqualTo(404);
            assertThat(error.networkResponse.getBodyAsString()).isEqualTo("Hi");
        }
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.result).isNull();
        assertThat(response.error).isNotNull();
        assertThat(response.error.networkResponse.statusCode).isEqualTo(404);
        assertThat(response.error.networkResponse.getBodyAsString()).isEqualTo("Hi");
    }

    @Test
    public void http404Async() throws InterruptedException, IOException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())

                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"));

        final AtomicReference<String> responseRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Request<String> request = example.getString().addErrorListener(new RequestListener.ErrorListener() {
            @Override
            public void onError(JusError error) {
                responseRef.set(error.networkResponse.getBodyAsString());
                latch.countDown();
            }
        }).addResponseListener(new RequestListener.ResponseListener<String>() {
            @Override
            public void onResponse(String response) {
                fail();
            }
        });
        assertTrue(latch.await(2, SECONDS));

        String response1 = responseRef.get();
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.result).isNull();
        assertThat(response.error).isNotNull();
        assertThat(response.error.networkResponse.statusCode).isEqualTo(404);
        assertThat(response.error.networkResponse.getBodyAsString()).isEqualTo("Hi");
    }

    @Test
    public void transportProblemSync() {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())

                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        Request<String> call = example.getString();
        try {
            call.getFuture().get();
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void transportProblemAsync() throws InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .execManually()
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        final AtomicReference<Throwable> failureRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Request<String> request = example.getString()
                .addErrorListener(new RequestListener.ErrorListener() {
                    @Override
                    public void onError(JusError error) {
                        failureRef.set(error);
                        latch.countDown();
                    }
                })
                .addResponseListener(new RequestListener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        throw new AssertionError();
                    }
                });
        request.enqueue();
        //needs to wait more as the default retry policy is 1+2+4
        assertTrue(latch.await(17, SECONDS));

        Throwable failure = failureRef.get();
        assertThat(failure).isInstanceOf(TimeoutError.class);
        assertThat(failure.getCause()).isInstanceOf(SocketTimeoutException.class);


        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.result).isNull();
        assertThat(response.error).isNotNull();
        assertThat(response.error).isInstanceOf(TimeoutError.class);
        assertThat(response.error.getCause()).isInstanceOf(SocketTimeoutException.class);
    }

    @Test
    public void conversionProblemOutgoing() throws IOException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
                        return new Converter<Number, NetworkRequest>() {
                            @Override
                            public NetworkRequest convert(Number value) throws IOException {
                                throw new UnsupportedOperationException("I am broken!");
                            }
                        };
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        try {
            example.postNumber(777);
            fail();
        } catch (UnsupportedOperationException e) {
            assertThat(e).hasMessage("I am broken!");
        }
    }


    @Test
    public void conversionProblemIncomingSync() throws IOException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                        return new Converter<NetworkResponse, Number>() {
                            @Override
                            public Number convert(NetworkResponse value) throws IOException {
                                throw new UnsupportedOperationException("I am broken!");
                            }
                        };
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hi"));

        Request<Number> call = example.postNumber(777);
        try {
            call.getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(ParseError.class);
            assertThat(e.getCause().getCause())
                    .isExactlyInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("I am broken!");
        }
    }

    @Test
    public void conversionProblemIncomingConverterRuntimeException() throws IOException, InterruptedException {

        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                        return new Converter<NetworkResponse, Number>() {
                            @Override
                            public Number convert(NetworkResponse value) throws IOException {
                                // Some serialization libraries mask transport problems in runtime exceptions. Bad!
                                throw new RuntimeException("wrapper", new IOException("cause"));
                            }
                        };
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("777"));

        Request<Number> call = example.getNumber();
        try {
            call.getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(ParseError.class);
            assertThat(e.getCause().getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getCause().getCause()).isInstanceOf(IOException.class);
            assertThat(e.getCause().getCause().getCause()).hasMessage("cause");
        }
    }

    @Test
    public void conversionProblemIncomingAsync() throws InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                        return new Converter<NetworkResponse, Number>() {
                            @Override
                            public Number convert(NetworkResponse value) throws IOException {
                                throw new UnsupportedOperationException("I am broken!");
                            }
                        };
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("777"));

        final AtomicReference<Throwable> failureRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);


        Request<Number> request = example.postNumber(777)
                .addErrorListener(new RequestListener.ErrorListener() {
                    @Override
                    public void onError(JusError error) {
                        failureRef.set(error.getCause());
                        latch.countDown();
                    }
                })
                .addResponseListener(new RequestListener.ResponseListener<Number>() {
                    @Override
                    public void onResponse(Number response) {
                        throw new AssertionError();
                    }
                });

        assertTrue(latch.await(2, SECONDS));

        assertThat(failureRef.get()).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("I am broken!");
    }

    /**
     * 10.2.5 204 No Content - means what it says, however representations of HTTP responses may not only
     * consider the entity-body(which anyway is not 'MUST' restricted) but entity-headers as well
     */
    @Test
    public void http204_DOES_NOT_SkipConverter() throws IOException, ExecutionException, InterruptedException {
        final Converter<NetworkResponse, Number> converter = spy(new Converter<NetworkResponse, Number>() {
            @Override
            public Number convert(NetworkResponse value) throws IOException {
                if (value.data == null || value.data.length == 0) {
                    return null;
                }
                return Double.parseDouble(value.getBodyAsString());
            }
        });
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                        return converter;
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setStatus("HTTP/1.1 204 Nothin"));

        Request<Number> request = example.getNumber();
        request.getFuture().get();
        Response<Number> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isNull();
        verify(converter).convert((NetworkResponse) Mockito.anyObject());
        verifyNoMoreInteractions(converter);
    }

    /**
     * 10.2.6 205 Reset Content - MUST NOT include an entity, however a converter may
     * anyway decide to use this(to generate Event to be emitted for example)
     */
    @Test
    public void http205_DOES_NOT_SkipConverter() throws IOException, ExecutionException, InterruptedException {
        final Converter<NetworkResponse, Number> converter = spy(new Converter<NetworkResponse, Number>() {
            @Override
            public Number convert(NetworkResponse value) throws IOException {
                if (value == null || value.data == null || value.data.length == 0) {
                    return null;
                }
                return Double.parseDouble(value.getBodyAsString());
            }
        });
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(new ToNumberConverterFactory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                        return converter;
                    }
                })
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setStatus("HTTP/1.1 205 Nothin"));

        Request<Number> request = example.getNumber();
        request.getFuture().get();
        Response<Number> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isNull();
        verify(converter).convert((NetworkResponse) Mockito.anyObject());
        verifyNoMoreInteractions(converter);
    }

    @Test
    public void successfulRequestResponseWhenMimeTypeMissing() throws Exception {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hi").removeHeader("Content-Type"));

        Request<String> request = example.getString();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.result).isEqualTo("Hi");
    }

    @Test
    public void responseBody() throws IOException, ExecutionException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("1234"));

        NetworkResponse response = example.getBody().getFuture().get();
        assertThat(response.getBodyAsString()).isEqualTo("1234");
    }

    @Test
    public void responseBodyBuffers() throws IOException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse()
                .setBody("1234")
                .setSocketPolicy(DISCONNECT_DURING_RESPONSE_BODY));

        Request<NetworkResponse> buffered = example.getBody();
        // When buffering we will detect all socket problems before returning the Response.
        try {
            buffered.getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).hasMessage("Response Body not completely received");
        }
    }

    @Test
    public void responseBodyStreams() throws IOException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())

                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse()
                        .setBody("1234")
                        .setSocketPolicy(DISCONNECT_DURING_RESPONSE_BODY)
        );

        // When streaming we only detect socket problems as the ResponseBody is read.
        try {
            NetworkResponse response = example.getStreamingBody().getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).hasMessage("Response Body not completely received");
        }
    }


    @Test
    public void emptyResponse() throws IOException, ExecutionException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())

                .build();
        Service example = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("").addHeader("Content-Type", "text/stringy"));

        Request<String> request = example.getString();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.result).isEqualTo("");
    }

    @Test
    public void cancelBeforeEnqueue() {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .execManually()
                .build();
        Service service = retroProxy.create(Service.class);
        Request<String> call = service.getString();

        call.cancel();

        try {
            call.enqueue();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Canceled");
        }
    }

    @Test
    public void cloningExecutedRequestDoesNotCopyState() throws IOException,
            ExecutionException, InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())

                .build();
        Service service = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setBody("Hi"));
        server.enqueue(new MockResponse().setBody("Hello"));

        Request<String> call = service.getString();
        assertThat(call.getFuture().get()).isEqualTo("Hi");

        Request<String> cloned = call.clone();
        assertThat(cloned.enqueue().getFuture().get()).isEqualTo("Hello");
    }

    @Test
    public void cancelRequest() throws InterruptedException {
        retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .execManually()
                .build();
        Service service = retroProxy.create(Service.class);

        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Request<String> call = service.getString();

        final AtomicReference<JusLog.MarkerLog.Marker> markerRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        call
                .addErrorListener(new RequestListener.ErrorListener() {
                    @Override
                    public void onError(JusError error) {
                        throw new AssertionError();
                    }
                })
                .addResponseListener(new RequestListener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        throw new AssertionError();
                    }
                })
                .addMarkerListener(new RequestListener.MarkerListener() {
                    @Override
                    public void onMarker(JusLog.MarkerLog.Marker marker, Object... args) {

                        if (Request.EVENT_CACHE_DISCARD_CANCELED.equals(marker.name)
                                || Request.EVENT_NETWORK_DISCARD_CANCELED.equals(marker.name)
                                || Request.EVENT_CANCELED_AT_DELIVERY.equals(marker.name)) {
                            markerRef.set(marker);
                            latch.countDown();
                        }
                    }
                });
        call.enqueue().cancel();

        assertTrue(latch.await(2, SECONDS));
        assertThat(markerRef.get().name).containsIgnoringCase("canceled");
    }


    @After
    public void after() {
        retroProxy.requestQueue().stopWhenDone();
    }

}
