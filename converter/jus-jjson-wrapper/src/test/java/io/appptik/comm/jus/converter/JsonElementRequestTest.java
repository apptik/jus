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
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.request.JsonObjectArrayWrapperRequest;
import io.apptik.comm.jus.request.JsonObjectWrapperRequest;
import io.apptik.json.JsonArray;
import io.apptik.json.wrapper.JsonObjectArrayWrapper;
import io.apptik.json.wrapper.JsonObjectWrapper;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonElementRequestTest {

    class Service {

        JsonObjectWrapperRequest<RespWrapper> aJsonObject(ReqWrapper jsonObjectWrapper) throws
                IOException {
            return queue.add(new JsonObjectWrapperRequest("POST", server.url("/").toString(),
                    RespWrapper.class)
                    .setRequestData(jsonObjectWrapper));
        }

        JsonObjectArrayWrapperRequest<RespWrapper> aJsonArray(JsonObjectArrayWrapper jsonArray)
                throws IOException {
            return queue.add(new JsonObjectArrayWrapperRequest("POST", server.url("/").toString(),
                    RespWrapper.class)
                    .setRequestData(jsonArray));
        }

        JsonObjectWrapperRequest<RespWrapper> aJsonObjectGET() throws IOException {
            return queue.add(new JsonObjectWrapperRequest("GET", server.url("/").toString(),
                    RespWrapper.class
            ));
        }

        JsonObjectArrayWrapperRequest<RespWrapper> aJsonArrayGET() throws IOException {
            return queue.add(new JsonObjectArrayWrapperRequest("GET", server.url("/").toString(),
                    RespWrapper.class
            ));
        }
    }

    public static class ReqWrapper extends JsonObjectWrapper {
        public ReqWrapper setName(String name) {
            getJson().put("name", name);
            return this;
        }
    }

    public static class RespWrapper extends JsonObjectWrapper {
        public String getTheName() {
            return getJson().optString("theName");
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

        RespWrapper body = service.aJsonObject(new ReqWrapper().setName("value"))
                .getFuture().get();
        assertThat(body.getTheName()).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonArray() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody(
                "[{\"theName\":\"value1\"}, " +
                        "{\"theName\":\"value2\"}]"));

        JsonObjectArrayWrapper<RespWrapper> body =
                service.aJsonArray(new JsonObjectArrayWrapper()
                        .wrap(JsonArray.readFrom("[\"name\",\"value\"]").asJsonArray(),
                                ReqWrapper.class))
                        .getFuture().get();
        assertThat(body.get(0).getTheName()).isEqualTo("value1");
        assertThat(body.get(1).getTheName()).isEqualTo("value2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readUtf8()).isEqualTo("[\"name\",\"value\"]");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonObjectGET() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        RespWrapper body = service.aJsonObjectGET()
                .getFuture().get();
        assertThat(body.getTheName()).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().size()).isEqualTo(0);
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void aJsonArrayGET() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody(
                "[{\"theName\":\"value1\"}, " +
                        "{\"theName\":\"value2\"}]"));

        JsonObjectArrayWrapper<RespWrapper> body =
                service.aJsonArrayGET().getFuture().get();
        assertThat(body.get(0).getTheName()).isEqualTo("value1");
        assertThat(body.get(1).getTheName()).isEqualTo("value2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().size()).isEqualTo(0);
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }
}
