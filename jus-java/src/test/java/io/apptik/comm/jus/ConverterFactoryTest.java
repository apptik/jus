package io.apptik.comm.jus;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static io.apptik.comm.jus.Request.Method.GET;
import static org.assertj.core.api.Assertions.assertThat;

public class ConverterFactoryTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    public RequestQueue queue;
    public Service example = new Service();

    class Service {

        Request<String> getString() {
            return new Request<String>(GET, server.url("/").toString(), String.class).prepRequestQueue
                    (queue);
        }

        StringRequest getString2() {
            return new StringRequest(GET, server.url("/").toString()).prepRequestQueue(queue);
        }

        Request<Number> getNumber() {
            return new Request<Number>(GET, server.url("/").toString()).prepRequestQueue(queue);
        }

        Request<NetworkResponse> getBody() {
            return new Request<NetworkResponse>(GET, server.url("/").toString()).prepRequestQueue
                    (queue);
        }
    }

    @Before
    public void setup() {
        queue = Jus.newRequestQueue();
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }

    @Test
    public void defaultStringConverter() throws IOException, ExecutionException,
            InterruptedException {
        server.enqueue(new MockResponse().setBody("Hi"));

        Request<String> request = example.getString().enqueue();

        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isEqualTo("Hi");
        assertThat(response.error).isNull();
    }

    //@Test
    public void defaultStringConverter2() throws IOException, ExecutionException,
            InterruptedException {

        server.enqueue(new MockResponse().setBody("Hi"));

        Request<String> request = example.getString2().enqueue();
        request.getFuture().get();
        Response<String> response = request.getRawResponse();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.result).isEqualTo("Hi");
        assertThat(response.error).isNull();
    }

    private class StringRequest extends Request<String> {

        public StringRequest(String method, String url) {
            super(method, url);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            return super.parseNetworkResponse(response);
        }
    }

}
