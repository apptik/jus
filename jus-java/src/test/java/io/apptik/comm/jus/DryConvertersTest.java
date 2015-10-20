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

package io.apptik.comm.jus;

import org.junit.Test;

import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HTTP;

import static org.assertj.core.api.Assertions.assertThat;

public class DryConvertersTest {

    @Test
    public void stringConverterTest() throws Exception {
        String val = "value";
        Converters.StringRequestConverter converter1 = new Converters.StringRequestConverter();
        NetworkRequest request = converter1.convert(val);
        NetworkResponse response = new NetworkResponse.Builder()
                .setBody(request.data)
                .setHeader(HTTP.CONTENT_TYPE, request.contentType.toString())
                .build();
        Converters.StringResponseConverter converter2 =  new Converters.StringResponseConverter();
        String val2 = converter2.convert(response);
        assertThat(val2).isEqualTo(val);
    }

}
