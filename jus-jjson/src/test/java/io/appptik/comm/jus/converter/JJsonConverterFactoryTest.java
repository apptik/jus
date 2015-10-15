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

import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.JJsonConverterFactory;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.POST;

import static org.assertj.core.api.Assertions.assertThat;

public class JJsonConverterFactoryTest {



    interface Service {
        @POST("/")
        Request<JsonObject> aJsonObject(@Body JsonObject jsonObject);

        @POST("/")
        Request<JsonArray> aJsonArray(@Body JsonArray impl);
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
                .addConverterFactory(JJsonConverterFactory.create())
                .queue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void aJsonObject() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        JsonObject body = service.aJsonObject(new JsonObject().put("name","value")).getFuture().get();
        assertThat(body.get("theName")).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    public void aJsonArray() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("[\"theName\",\"value\"]"));

        JsonArray body = service.aJsonArray(new JsonArray().put("name").put("value"))
                .getFuture().get();
        assertThat(body.get(0)).isEqualTo("theName");
        assertThat(body.get(1)).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
        assertThat(request.getBody().readUtf8()).isEqualTo("[\"name\",\"value\"]");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    public void after() {
        queue.stopWhenDone();
    }
}
