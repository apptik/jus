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

package io.apptik.comm.jus.converter;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import io.apptik.comm.jus.converter.Common.AnImplementation;
import io.apptik.comm.jus.request.JacksonRequest;

import static io.apptik.comm.jus.converter.Common.AnInterface;
import static io.apptik.comm.jus.converter.Common.AnInterfaceDeserializer;
import static io.apptik.comm.jus.converter.Common.AnInterfaceSerializer;
import static org.assertj.core.api.Assertions.assertThat;


public class JacksonRequestTest  {

    @Rule
    public final MockWebServer server = new MockWebServer();
    private RequestQueue queue;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        SimpleModule module = new SimpleModule();
        module.addSerializer(AnInterface.class, new AnInterfaceSerializer());
        module.addDeserializer(AnInterface.class, new AnInterfaceDeserializer());
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
        mapper.setVisibilityChecker(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

    }
    @After
    public void after() {
        queue.stopWhenDone();
    }

    @Test
    public void anInterfaceNoBody() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));
        JacksonRequest<AnInterface> request = new JacksonRequest<AnInterface>
                (Request.Method.POST, server.url("").toString(), mapper, AnInterface.class)
                ;

        AnInterface body = queue.add(request).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().size()).isEqualTo(0);
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }
    @Test
    public void anInterface() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));
        JacksonRequest<AnInterface> request = new JacksonRequest<AnInterface>
                (Request.Method.POST, server.url("").toString(), mapper, AnInterface.class)
                .setRequestData(new AnImplementation("value"));

        AnInterface body = queue.add(request).getFuture().get();
        assertThat(body.getName()).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        assertThat(sRequest.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(sRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void anImplementationNoBody() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));
        JacksonRequest<AnImplementation> request = new JacksonRequest<AnImplementation>
                (Request.Method.POST, server.url("").toString(), mapper, AnImplementation.class);

        AnImplementation body = queue.add(request).getFuture().get();

        assertThat(body.theName).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
        assertThat(sRequest.getBody().size()).isEqualTo(0);
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
    }

    @Test
    public void anImplementation() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));
        JacksonRequest<AnImplementation> request = new JacksonRequest<AnImplementation>
                (Request.Method.POST, server.url("").toString(), mapper, AnImplementation.class)
                .setRequestData(new AnImplementation("value"));

        AnImplementation body = queue.add(request).getFuture().get();

        assertThat(body.theName).isEqualTo("value");

        RecordedRequest sRequest = server.takeRequest();
        // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
        assertThat(sRequest.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
        assertThat(sRequest.getHeader("Accept")).isEqualTo("application/json");
        assertThat(sRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
    }
}
