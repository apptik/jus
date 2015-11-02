/*
 * Copyright (C) 2012 The Android Open Source Project
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

package io.apptik.comm.jus.mock;

import io.apptik.comm.jus.FormEncodingBuilder;
import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;

public class TestRequest {
    private static final String TEST_URL = "http://foo.com";

    /** Base Request class for testing allowing both the deprecated and new constructor. */
    private static class Base extends Request<byte[]> {
        @SuppressWarnings("deprecation")
        public Base(String url, Listener.ErrorListener listener) {
            super(Method.GET, url, null);
            this.addErrorListener(listener);
        }

        public Base(String method, String url, Listener.ErrorListener listener) {
            super(method, url, null);
            this.addErrorListener(listener);
        }

        @Override
        protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
            return null;
        }

        @Override
        protected void deliverResponse(byte[] response) {
        }
    }

    public static class Get extends Base {
        public Get() {
            super(Method.GET, TEST_URL, null);
        }
    }

    /**
     * Test example of a POST request in the new style.  In the new style, it is possible
     * to have a POST with no body.
     */
    public static class Post extends Base {
        public Post() {
            super(Method.POST, TEST_URL, null);
        }
    }

    /** Test example of a POST request in the new style with a body. */
    public static class PostWithBody extends Post {

        public PostWithBody() {
            this.setNetworkRequest(new FormEncodingBuilder().add("testKey", "testValue").build());
        }
    }

    /**
     * Test example of a PUT request in the new style.  In the new style, it is possible to have a
     * PUT with no body.
     */
    public static class Put extends Base {
        public Put() {
            super(Method.PUT, TEST_URL, null);
        }
    }

    /** Test example of a PUT request in the new style with a body. */
    public static class PutWithBody extends Put {

        public PutWithBody() {
            this.setNetworkRequest(new FormEncodingBuilder().add("testKey", "testValue").build());
        }
    }

    /** Test example of a DELETE request in the new style. */
    public static class Delete extends Base {
        public Delete() {
            super(Method.DELETE, TEST_URL, null);
        }
    }

    /** Test example of a HEAD request in the new style. */
    public static class Head extends Base {
        public Head() {
            super(Method.HEAD, TEST_URL, null);
        }
    }

    /** Test example of a OPTIONS request in the new style. */
    public static class Options extends Base {
        public Options() {
            super(Method.OPTIONS, TEST_URL, null);
        }
    }

    /** Test example of a TRACE request in the new style. */
    public static class Trace extends Base {
        public Trace() {
            super(Method.TRACE, TEST_URL, null);
        }
    }

    /** Test example of a PATCH request in the new style. */
    public static class Patch extends Base {
        public Patch() {
            super(Method.PATCH, TEST_URL, null);
        }
    }

    /** Test example of a PATCH request in the new style with a body. */
    public static class PatchWithBody extends Patch {
        public PatchWithBody() {
            this.setNetworkRequest(new FormEncodingBuilder().add("testKey", "testValue").build());
        }

    }
}
