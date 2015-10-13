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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.POST;

import static org.assertj.core.api.Assertions.assertThat;

public final class GsonConverterFactoryTest {
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

    static class AnInterfaceAdapter extends TypeAdapter<AnInterface> {
        @Override
        public void write(JsonWriter jsonWriter, AnInterface anInterface) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("name").value(anInterface.getName());
            jsonWriter.endObject();
        }

        @Override
        public AnInterface read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();

            String name = null;
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
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
        Request<AnImplementation, AnImplementation> anImplementation(@Body AnImplementation impl);

        @POST("/")
        Request<AnInterface, AnInterface> anInterface(@Body AnInterface impl);
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;

    @Before
    public void setUp() {
        final RequestQueue requestQueue = Jus.newRequestQueue();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AnInterface.class, new AnInterfaceAdapter())
                .create();
        RetroProxy retroJus = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .queue(requestQueue)
                .build();
        service = retroJus.create(Service.class);
    }

    @Test
    public void anInterface() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

        Request<AnInterface, AnInterface> request = service.anInterface(new AnImplementation("value"));
        request.setResponseListener(
                new Listener.ResponseListener<AnInterface>() {
                    @Override
                    public void onResponse(AnInterface response) {
                        assertThat(response.getName()).isEqualTo("value");

                        RecordedRequest recordedRequest = null;
                        try {
                            recordedRequest = server.takeRequest();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
                        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
                    }
                }
        );
    }

    @Test
    public void anImplementation() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        Request<AnImplementation, AnImplementation> request = service.anImplementation(new AnImplementation("value"));
        request.setResponseListener(new Listener.ResponseListener<AnImplementation>() {
            @Override
            public void onResponse(AnImplementation response) {
                assertThat(response.theName).isEqualTo("value");
                RecordedRequest recordedRequest = null;
                try {
                    recordedRequest = server.takeRequest();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("{\"theName\":\"value\"}");
                assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
            }
        });


    }

    @Test
    public void serializeUsesConfiguration() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("{}"));

        service.anImplementation(new AnImplementation(null));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo(""); // Null value was not serialized.
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }
}
