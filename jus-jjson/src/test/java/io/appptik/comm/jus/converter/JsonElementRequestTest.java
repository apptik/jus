package io.appptik.comm.jus.converter;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.request.JsonArrayRequest;
import io.apptik.comm.jus.request.JsonObjectRequest;
import io.apptik.json.JsonArray;
import io.apptik.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonElementRequestTest {
    class Service {

        Request<JsonObject> aJsonObject(JsonObject jsonObject) throws IOException {
            return queue.add(new JsonObjectRequest("POST", server.url("/").toString())
                    .setRequestData(jsonObject));
        }

        Request<JsonArray> aJsonArray(JsonArray jsonArray) throws IOException {
            return queue.add(new JsonArrayRequest("POST", server.url("/").toString())
                    .setRequestData(jsonArray));
        }

        Request<JsonObject> aJsonObjectGET() throws IOException {
            return queue.add(new JsonObjectRequest("GET", server.url("/").toString()));
        }

        Request<JsonArray> aJsonArrayGET() throws IOException {
            return queue.add(new JsonArrayRequest("GET", server.url("/").toString()));
        }
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();

        service = new Service();
    }

    @Test
    public void aJsonObject() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        JsonObject body = service.aJsonObject(new JsonObject().put("name", "value")).getFuture().get();
        assertThat(body.get("theName")).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonArray() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("[\"theName\",\"value\"]"));

        JsonArray body = service.aJsonArray(new JsonArray().put("name").put("value"))
                .getFuture().get();
        assertThat(body.get(0)).isEqualTo("theName");
        assertThat(body.get(1)).isEqualTo("value");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readUtf8()).isEqualTo("[\"name\",\"value\"]");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonObjectGET() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        JsonObject body = service.aJsonObjectGET().getFuture().get();
        assertThat(body.get("theName")).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().size()).isEqualTo(0);
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonArrayGET() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("[\"theName\",\"value\"]"));

        JsonArray body = service.aJsonArrayGET()
                .getFuture().get();
        assertThat(body.get(0)).isEqualTo("theName");
        assertThat(body.get(1)).isEqualTo("value");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().size()).isEqualTo(0);
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }
}
