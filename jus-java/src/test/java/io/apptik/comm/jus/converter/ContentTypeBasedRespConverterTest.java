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


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ContentTypeBasedRespConverterTest {

    NetworkResponse networkResponse;

    @Before
    public void setup() {
        networkResponse = NetworkResponse.create(MediaType.parse("application/json"), ("{\"key\" " +
                ":" +
                " \"value\"}")
                .getBytes());
    }

    @Test
    public void emptyConvertersMap() throws IOException {
        Converter<NetworkResponse, String> converter = new ContentTypeBasedRespConverter<>();
        try {
            converter.convert(networkResponse);
            fail();
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Cannot find converter for " +
                    ":NetworkResponse{contentType=application/json, statusCode=200, data={\"key\"" +
                    " " +
                    ": \"value\"}, headers=Content-Type: application/json\n" +
                    ", networkTimeNs=0}, tried:");
        }
    }

    @Test
    public void cannotFindConverter() throws IOException {
        ContentTypeBasedRespConverter<String> converter = new ContentTypeBasedRespConverter<>();
        converter.add(MediaType.parse("text/plain"), new Converters.StringResponseConverter());
        converter.add(MediaType.parse("applicaton/xml"), new Converters.StringResponseConverter());
        try {
            converter.convert(networkResponse);
            fail();
        } catch (RuntimeException e) {
            assertThat(e).hasMessageStartingWith("Cannot find converter for " +
                    ":NetworkResponse{contentType=application/json, statusCode=200, data={\"key\"" +
                    " " +
                    ": \"value\"}, headers=Content-Type: application/json\n" +
                    ", networkTimeNs=0}, tried:");
        }
    }

    @Test
    public void goodConverter() throws IOException {
        ContentTypeBasedRespConverter<String> converter = new ContentTypeBasedRespConverter<>();
        converter.add(MediaType.parse("text/plain"), new Converters.StringResponseConverter());
        converter.add(MediaType.parse("application/xml"), new Converters.StringResponseConverter());
        converter.add(MediaType.parse("application/json"), new Converters.StringResponseConverter
                ());

        String s = converter.convert(networkResponse);
        assertThat(s).isEqualTo("{\"key\" : \"value\"}");
    }

}
