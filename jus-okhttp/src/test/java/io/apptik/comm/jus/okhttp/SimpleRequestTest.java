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
package io.apptik.comm.jus.okhttp;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.SocketPolicy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Marker;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.ParseError;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.NoConnectionError;
import io.apptik.comm.jus.http.HTTP;

import static com.squareup.okhttp.mockwebserver.SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY;
import static io.apptik.comm.jus.Request.Method.GET;
import static io.apptik.comm.jus.Request.Method.HEAD;
import static io.apptik.comm.jus.Request.Method.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class SimpleRequestTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    public RequestQueue queue;
    public Service example = new Service();

    class Service {

        Request<String> getString() {
            return new Request<>(GET, server.url("/").toString(), String.class)
                    .prepRequestQueue(queue);
        }

        Request<NetworkResponse> getHead() {
            return new Request<>(HEAD, server.url("/").toString(), NetworkResponse.class)
                    .prepRequestQueue(queue);
        }

        Request<Number> getNumber() {
            return new Request<>(GET, server.url("/").toString(), Number.class)
                    .prepRequestQueue(queue);
        }

        Request<NetworkResponse> getBody() {
            return new Request<>(GET, server.url("/").toString(), NetworkResponse.class)
                    .prepRequestQueue
                            (queue);
        }

        Request<String> postString(String body) throws IOException {
            return new Request<>(POST, server.url("/").toString(), String.class)
                    .setRequestData(body, new Converters.StringRequestConverter())
                    .prepRequestQueue(queue);
        }


        Request<Number> postNumber(Number body, Converter converter) throws IOException {
            return new Request<>(POST, server.url("/").toString(), Number.class)
                    .setRequestData(body, converter)
                    .prepRequestQueue(queue);
        }
    }

    @Before
    public void setup() {
        queue = Jus.newRequestQueue(null, new OkHttpStack());
    }

    @Test
    public void http200Sync() throws IOException, ExecutionException, InterruptedException {

        server.enqueue(new MockResponse().setBody("Hi"));

        Request<String> request = example.getString().enqueue();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isEqualTo("Hi");
        assertThat(response.error).isNull();
    }

    @Test
    public void httpHead200Sync() throws IOException, ExecutionException, InterruptedException {


        server.enqueue(new MockResponse().setBody("Hello"));

        Request<NetworkResponse> request = example.getHead().enqueue();
        NetworkResponse networkResponse = request.getFuture().get();
        Response<NetworkResponse> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.error).isNull();
        assertThat(networkResponse.data.length).isEqualTo(0);
        assertThat(networkResponse.headers.get(HTTP.CONTENT_LEN)).isEqualTo("5");
    }

    @Test
    public void http200Async() throws InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("Hi"));

        final AtomicReference<String> responseRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Request<String> request = example.getString().addErrorListener(new RequestListener.ErrorListener
                () {
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
        }).enqueue();

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
        server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"));

        Request<String> request = example.getString();
        try {
            request.enqueue().getFuture().get();
        } catch (ExecutionException e) {
            JusError error = (JusError) e.getCause();
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
        server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"));

        final AtomicReference<String> responseRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Request<String> request = example.getString()
                .addErrorListener(new RequestListener.ErrorListener() {
                    @Override
                    public void onError(JusError error) {
                        responseRef.set(error.networkResponse.getBodyAsString());
                        latch.countDown();
                    }
                })
                .addResponseListener(new RequestListener.ResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        fail();
                    }
                }).enqueue();
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
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        Request<String> call = example.getString().enqueue();
        try {
            call.getFuture().get();
            fail();
        } catch (Exception ignored) {
            Throwable failure = ignored.getCause();
            assertThat(failure).isInstanceOf(NoConnectionError.class);
            assertThat(failure.getCause()).isInstanceOf(ConnectException.class);
            assertThat(failure.getCause().getMessage()).isEqualTo("Connection refused");

            Response<String> response = call.getRawResponse();
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.result).isNull();
            assertThat(response.error).isNotNull();
            assertThat(response.error).isInstanceOf(NoConnectionError.class);
            assertThat(response.error.getCause()).isInstanceOf(ConnectException.class);
            assertThat(failure.getCause().getMessage()).isEqualTo("Connection refused");

        }
    }

    @Test
    public void transportProblemAsync() throws InterruptedException {

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
        assertThat(failure.getCause()).isInstanceOf(ConnectException.class);
        assertThat(failure.getCause().getMessage()).isEqualTo("Connection refused");

        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.result).isNull();
        assertThat(response.error).isNotNull();
        assertThat(response.error).isInstanceOf(NoConnectionError.class);
        assertThat(response.error.getCause()).isInstanceOf(ConnectException.class);
        assertThat(failure.getCause().getMessage()).isEqualTo("Connection refused");
    }

    @Test
    public void conversionProblemOutgoing() throws IOException, InterruptedException {
        try {
            example.postNumber(777, new Converter<Number, NetworkRequest>() {
                @Override
                public NetworkRequest convert(Number value) throws IOException {
                    throw new UnsupportedOperationException("I am broken!");
                }
            }).enqueue();
            fail();
        } catch (IOException e) {
            assertThat(e).hasCauseExactlyInstanceOf(UnsupportedOperationException.class);
            assertThat(e.getCause()).hasMessage("I am broken!");
        }
    }


    @Test
    public void conversionProblemIncomingSync() throws IOException, InterruptedException {
        queue.addConverterFactory(new ToNumberConverterFactory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                return new Converter<NetworkResponse, Number>() {
                    @Override
                    public Number convert(NetworkResponse value) throws IOException {
                        throw new UnsupportedOperationException("I am broken!");
                    }
                };
            }
        });
        server.enqueue(new MockResponse().setBody("Hi"));

        Request<Number> call = example.postNumber(777, new
                ToNumberConverterFactory().toRequest(Number.class, null));
        try {
            call.enqueue().getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(ParseError.class);
            assertThat(e.getCause().getCause())
                    .isExactlyInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("I am broken!");
        }
    }

    @Test
    public void conversionProblemIncomingConverterRuntimeException() throws IOException,
            InterruptedException {

        queue.addConverterFactory(new ToNumberConverterFactory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                return new Converter<NetworkResponse, Number>() {
                    @Override
                    public Number convert(NetworkResponse value) throws IOException {
                        // Some serialization libraries mask transport problems in runtime
                        // exceptions. Bad!
                        throw new RuntimeException("wrapper", new IOException("cause"));
                    }
                };
            }
        });
        server.enqueue(new MockResponse().setBody("777"));

        Request<Number> call = example.getNumber();
        try {
            call.enqueue().getFuture().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(ParseError.class);
            assertThat(e.getCause().getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getCause().getCause()).isInstanceOf(IOException.class);
            assertThat(e.getCause().getCause().getCause()).hasMessage("cause");
        }
    }

    @Test
    public void conversionProblemIncomingAsync() throws InterruptedException, IOException {
        queue.addConverterFactory(new ToNumberConverterFactory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                return new Converter<NetworkResponse, Number>() {
                    @Override
                    public Number convert(NetworkResponse value) throws IOException {
                        throw new UnsupportedOperationException("I am broken!");
                    }
                };
            }
        });
        server.enqueue(new MockResponse().setBody("777"));

        final AtomicReference<Throwable> failureRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);


        Request<Number> request = example.postNumber(777, new
                ToNumberConverterFactory().toRequest(Number.class, null))
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
                }).enqueue();

        assertTrue(latch.await(2, SECONDS));

        assertThat(failureRef.get()).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("I am broken!");
    }

    /**
     * 10.2.5 204 No Content - means what it says, however representations of HTTP responses may
     * not only consider the entity-body(which anyway is not 'MUST' restricted) but
     * entity-headers as well
     */
    @Test
    public void http204_DOES_NOT_SkipConverter() throws IOException, ExecutionException,
            InterruptedException {
        final Converter<NetworkResponse, Number> converter = spy(new Converter<NetworkResponse,
                Number>() {
            @Override
            public Number convert(NetworkResponse value) throws IOException {
                if (value.data == null || value.data.length == 0) {
                    return null;
                }
                return Double.parseDouble(value.getBodyAsString());
            }
        });
        queue.addConverterFactory(new ToNumberConverterFactory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[]
                    annotations) {
                return converter;
            }
        });

        server.enqueue(new MockResponse().setStatus("HTTP/1.1 204 Nothin"));

        Request<Number> request = example.getNumber().enqueue();
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
    public void http205_DOES_NOT_SkipConverter() throws IOException, ExecutionException,
            InterruptedException {
        final Converter<NetworkResponse, Number> converter = spy(new Converter<NetworkResponse,
                Number>() {
            @Override
            public Number convert(NetworkResponse value) throws IOException {
                if (value == null || value.data == null || value.data.length == 0) {
                    return null;
                }
                return Double.parseDouble(value.getBodyAsString());
            }
        });
        queue.addConverterFactory(new ToNumberConverterFactory() {
            @Override
            public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
                return converter;
            }
        });
        server.enqueue(new MockResponse().setStatus("HTTP/1.1 205 Nothin"));

        Request<Number> request = example.getNumber().enqueue();
        request.getFuture().get();
        Response<Number> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isNull();
        verify(converter).convert((NetworkResponse) Mockito.anyObject());
        verifyNoMoreInteractions(converter);
    }

    @Test
    public void successfulRequestResponseWhenMimeTypeMissing() throws Exception {
        server.enqueue(new MockResponse().setBody("Hi").removeHeader("Content-Type"));

        Request<String> request = example.getString().enqueue();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.result).isEqualTo("Hi");
    }

    @Test
    public void responseBody() throws IOException, ExecutionException, InterruptedException {

        server.enqueue(new MockResponse().setBody("1234"));

        NetworkResponse response = example.getBody().enqueue().getFuture().get();
        assertThat(response.getBodyAsString()).isEqualTo("1234");
    }

    @Test
    public void responseBodyBuffers() throws IOException, InterruptedException {
        server.enqueue(new MockResponse()
                .setBody("1234")
                .setSocketPolicy(DISCONNECT_DURING_RESPONSE_BODY));

        Request<NetworkResponse> buffered = example.getBody();
        // When buffering we will detect all socket problems before returning the Response.
        try {
            buffered.enqueue().getFuture().get();
            fail();
        } catch (ExecutionException e) {
            //we have a protocol error from OkHttp as we read the whole body when we got it
            assertThat(e.getCause()).hasMessage("Response Body not completely received");
            assertThat(buffered.getRawResponse().error.networkResponse).isNotNull();
        }
    }

    @Test
    public void emptyResponse() throws IOException, ExecutionException, InterruptedException {
        server.enqueue(new MockResponse().setBody("").addHeader("Content-Type", "text/stringy"));

        Request<String> request = example.getString().enqueue();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.result).isEqualTo("");
    }

    @Test
    public void cancelBeforeEnqueue() {
        Request<String> call = example.getString();

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

        server.enqueue(new MockResponse().setBody("Hi"));
        server.enqueue(new MockResponse().setBody("Hello"));

        Request<String> call = example.getString().enqueue();
        assertThat(call.getFuture().get()).isEqualTo("Hi");

        Request<String> cloned = call.clone();
        assertThat(cloned.enqueue().getFuture().get()).isEqualTo("Hello");
    }

    @Test
    public void cancelRequest() throws InterruptedException {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Request<String> call = example.getString();

        final AtomicReference<Marker> markerRef = new AtomicReference<>();
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
                    public void onMarker(Marker marker, Object... args) {

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
        queue.stopWhenDone();
    }

}
