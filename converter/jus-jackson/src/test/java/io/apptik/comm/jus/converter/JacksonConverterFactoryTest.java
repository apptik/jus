/*
 * Copyright (C) 2015 AppTik Project
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
package io.apptik.comm.jus.converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.POST;

import static io.apptik.comm.jus.converter.Common.AnImplementation;
import static io.apptik.comm.jus.converter.Common.AnInterface;
import static io.apptik.comm.jus.converter.Common.AnInterfaceDeserializer;
import static io.apptik.comm.jus.converter.Common.AnInterfaceSerializer;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonConverterFactoryTest {


    interface Service {
        @POST("/")
        Request<AnImplementation> anImplementation(@Body AnImplementation impl);

        @GET("/")
        Request<AnImplementation> anImplementation();

        @POST("/")
        Request<AnInterface> anInterface(@Body AnInterface impl);

        @GET("/")
        Request<AnInterface> anInterface();
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        SimpleModule module = new SimpleModule();
        module.addSerializer(AnInterface.class, new AnInterfaceSerializer());
        module.addDeserializer(AnInterface.class, new AnInterfaceDeserializer());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        mapper.setVisibilityChecker(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .requestQueue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void anInterfaceGet() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

        AnInterface body = service.anInterface().getFuture().get();
        assertThat(body.getName()).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().size()).isEqualTo(0);
    }

    @Test
    public void anInterfacePost() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

        AnInterface body = service.anInterface(new AnImplementation("value")).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    public void anImplementationPost() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        AnImplementation body = service.anImplementation(new AnImplementation("value"))
                .getFuture().get();
        assertThat(body.theName).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    public void anImplementationGet() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

        AnImplementation body = service.anImplementation()
                .getFuture().get();
        assertThat(body.theName).isEqualTo("value");

        RecordedRequest request = server.takeRequest();
        // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
        assertThat(request.getBody().size()).isEqualTo(0);

    }

    public void after() {
        queue.stopWhenDone();
    }
}
