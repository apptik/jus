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

import java.lang.reflect.Type;

import io.apptik.comm.jus.toolbox.Utils;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void getRespTypeClassNotResolvable() {
        Request request = new SRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isNull();
    }

    @Test
    public void getRespTypeClass() {
        Request request = new StringRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isEqualTo(String.class);
    }

    @Test
    public void getRespTypeClassNotResolvable2() {
        Request<String> request = new Request<String>("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isNull();
    }

    @Test
    public void getRespTypeMethod() {
        Request request = new SmRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isEqualTo(String.class);
    }


    private class SmRequest extends Request {

        public SmRequest(java.lang.String method, java.lang.String url) {
            super(method, url);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            return super.parseNetworkResponse(response);
        }
    }

    private class SRequest<T extends String> extends Request<T> {

        public SRequest(java.lang.String method, java.lang.String url) {
            super(method, url);
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse response) {
            return super.parseNetworkResponse(response);
        }
    }

    private class StringRequest extends Request<String> {

        public StringRequest(String method, String url) {
            super(method, url);
        }
    }
}
