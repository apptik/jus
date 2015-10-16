/*
 * Copyright (C) 2015 Square, Inc.
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
package io.apptik.comm.jus;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.converter.MoshiConverterFactory;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.POST;

import static org.assertj.core.api.Assertions.assertThat;

public final class MoshiConverterFactoryTest {
    interface AnInterface {
        String getName();
    }

    static class AnImplementation implements AnInterface {
        private final String theName;

        AnImplementation(String name) {
            theName = name;
        }

        @Override
        public String getName() {
            return theName;
        }
    }

    static class AnInterfaceAdapter {
        @ToJson
        public void write(JsonWriter jsonWriter, AnInterface anInterface) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("name").value(anInterface.getName());
            jsonWriter.endObject();
        }

        @FromJson
        public AnInterface read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();

            String name = null;
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "name":
                        name = jsonReader.nextString();
                        break;
                }
            }

            jsonReader.endObject();
            return new AnImplementation(name);
        }
    }

    interface Service {
        @POST("/")
        Request<AnImplementation> anImplementation(@Body AnImplementation impl);

        @POST("/")
        Request<AnInterface> anInterface(@Body AnInterface impl);
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        Moshi moshi = new Moshi.Builder()
                .add(new AnInterfaceAdapter())
                .build();
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .queue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void anInterface() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

        AnInterface body = service.anInterface(new AnImplementation("value")).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    public void anImplementation() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        AnImplementation body = service.anImplementation(new AnImplementation("value")).getFuture()
                .get();
        assertThat(body.theName).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"theName\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    public void after() {
        queue.stopWhenDone();
    }
}
