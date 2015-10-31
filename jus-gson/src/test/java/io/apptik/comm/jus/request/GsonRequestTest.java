package io.apptik.comm.jus.request;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Common.AnImplementation;
import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;

import static io.apptik.comm.jus.Common.AnInterface;
import static io.apptik.comm.jus.Common.AnInterfaceAdapter;
import static org.assertj.core.api.Assertions.assertThat;

public class GsonRequestTest {
    @Rule
    public final MockWebServer server = new MockWebServer();
    private RequestQueue queue;
    private Gson gson;


    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        gson = new GsonBuilder()
                .registerTypeAdapter(AnInterface.class, new AnInterfaceAdapter())
                .create();

    }
    @After
    public void after() {
        queue.stopWhenDone();
    }

    @Test
    public void anInterface() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));
        GsonRequest<AnInterface> request =
                new GsonRequest<>(Request.Method.POST,
                        server.url("").toString(), AnInterface.class, gson);
        request.setRequestData(new AnImplementation("value"), gson, new AnInterfaceAdapter());

        AnInterface body =
                queue.add(request).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");
        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(sRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }


    @Test
    public void anImplementation() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        GsonRequest<AnImplementation> request =
                new GsonRequest<AnImplementation>(Request.Method.POST,
                        server.url("").toString(), AnImplementation.class)
                        .setRequestData(new AnImplementation("value"), gson);

        AnImplementation body = queue.add(request)
                .getFuture().get();

        assertThat(body.theName).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().readUtf8()).isEqualTo("{\"theName\":\"value\"}");
        assertThat(sRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }
    @Test
    public void anInterfaceNoBody() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));
        GsonRequest<AnInterface> request =
                new GsonRequest<>(Request.Method.POST,
                        server.url("").toString(), AnInterface.class, gson);

        AnInterface body =
                queue.add(request).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");
        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().size()).isEqualTo(0);
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }


    @Test
    public void anImplementationNoBody() throws IOException, InterruptedException,
            ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        GsonRequest<AnImplementation> request =
                new GsonRequest<AnImplementation>(Request.Method.POST,
                        server.url("").toString(), AnImplementation.class);

        AnImplementation body = queue.add(request)
                .getFuture().get();

        assertThat(body.theName).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().size()).isEqualTo(0);
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void serializeUsesConfiguration() throws IOException, InterruptedException,
            ExecutionException {
        server.enqueue(new MockResponse().setBody("{}"));

        GsonRequest<AnImplementation> request =
                new GsonRequest<AnImplementation>(Request.Method.POST,
                        server.url("").toString(), AnImplementation.class)
                        .setRequestData(new AnImplementation(null));

        queue.add(request).getFuture().get();

        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().readUtf8()).isEqualTo("{}"); // Null value was not serialized.
        assertThat(sRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }

}
