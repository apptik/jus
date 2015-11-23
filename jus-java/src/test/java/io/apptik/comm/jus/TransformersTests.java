package io.apptik.comm.jus;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class TransformersTests {

    @Rule
    public final MockWebServer server = new MockWebServer();

    public RequestQueue queue;

    @Before
    public void setup() {
        queue = Jus.newRequestQueue();
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }

    @Test
    public void requestConverterTest() {
        //server.enqueue(new MockResponse().setBody("Hi"));
        Transformer.RequestTransformer inactiveTransformer =
                new Transformer.RequestTransformer(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return false;
                    }
                }) {
                    @Override
                    public NetworkRequest transform(NetworkRequest networkRequest) {
                        return NetworkRequest.create(MediaType.parse("text/plain"), "Holla");
                    }
                };
        Transformer.RequestTransformer activeTransformer =
                new Transformer.RequestTransformer(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                }) {
                    @Override
                    public NetworkRequest transform(NetworkRequest networkRequest) {
                        return NetworkRequest.create(MediaType.parse("text/plain"), "Holla");
                    }
                };
        Request request = new Request("POST", server.url("/").toString(), String.class)
                .setNetworkRequest
                        (NetworkRequest.create(MediaType.parse("text/plain"), "Hi"));

        queue.add(request).cancel();
        assertThat(request.getNetworkRequest().getBodyAsString()).isEqualTo("Hi");
        request = request.clone();
        queue.addRequestTransformer(inactiveTransformer);
        queue.add(request).cancel();
        assertThat(request.getNetworkRequest().getBodyAsString()).isEqualTo("Hi");
        request = request.clone();
        queue.addRequestTransformer(activeTransformer);
        queue.add(request).cancel();
        assertThat(request.getNetworkRequest().getBodyAsString()).isEqualTo("Holla");


    }

    @Test
    public void responseConverterTest() throws ExecutionException, InterruptedException {
        Transformer.ResponseTransformer inactiveTransformer =
                new Transformer.ResponseTransformer(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return false;
                    }
                }) {
                    @Override
                    public NetworkResponse transform(NetworkResponse networkResponse) {
                        return NetworkResponse.create(MediaType.parse("text/plain"), "Holla");
                    }
                };
        Transformer.ResponseTransformer activeTransformer =
                new Transformer.ResponseTransformer(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                }) {
                    @Override
                    public NetworkResponse transform(NetworkResponse networkResponse) {
                        return NetworkResponse.create(MediaType.parse("text/plain"), "Holla");
                    }
                };

        Request<String> request = new Request<String>("GET", server.url("/").toString(), String
                .class);

        server.enqueue(new MockResponse().setBody("Hi"));
        queue.add(request);
        assertThat(request.getFuture().get()).isEqualTo("Hi");
        request = request.clone();
        queue.addResponseTransformer(inactiveTransformer);
        server.enqueue(new MockResponse().setBody("Hi"));
        queue.add(request);
        assertThat(request.getFuture().get()).isEqualTo("Hi");
        request = request.clone();
        queue.addResponseTransformer(activeTransformer);
        server.enqueue(new MockResponse().setBody("Hi"));
        queue.add(request);
        assertThat(request.getFuture().get()).isEqualTo("Holla");

    }

}
