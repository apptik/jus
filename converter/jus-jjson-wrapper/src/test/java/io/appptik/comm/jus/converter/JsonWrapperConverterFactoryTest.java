/*
 * Copyright (C) 2013 Square, Inc.
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
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.JsonWrapperConverterFactory;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.POST;
import io.apptik.json.JsonArray;
import io.apptik.json.JsonObject;
import io.apptik.json.wrapper.JsonObjectWrapper;
import io.apptik.json.wrapper.JsonStringArrayWrapper;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonWrapperConverterFactoryTest {



    interface Service {
        @POST("/")
        Request<JsonObjectWrapper> aJsonObjectWrapper(@Body JsonObjectWrapper jsonObject);

        @POST("/")
        Request<JsonStringArrayWrapper> aJsonStringArrayWrapper(@Body JsonStringArrayWrapper
                                                                     jsonArray);
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(JsonWrapperConverterFactory.create())
                .requestQueue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void aJsonObject() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        JsonObjectWrapper body = service.aJsonObjectWrapper(
                new JsonObjectWrapper(new JsonObject().put("name","value")))
                .getFuture().get();
        assertThat(body.getJson().get("theName")).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    public void aJsonArray() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("[\"theName\",\"value\"]"));

        JsonStringArrayWrapper body = service.aJsonStringArrayWrapper(
                new JsonStringArrayWrapper().wrap(new JsonArray().put("name").put("value")))
                .getFuture().get();
        assertThat(body.getJson().get(0)).isEqualTo("theName");
        assertThat(body.getJson().get(1)).isEqualTo("value");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readUtf8()).isEqualTo("[\"name\",\"value\"]");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }
}
