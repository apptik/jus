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


public interface Transformer {

    abstract class RequestTransformer {
        protected final RequestQueue.RequestFilter filter;
        public RequestTransformer(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        public abstract NetworkRequest transform(NetworkRequest networkRequest);
    }

    abstract class ResponseTransformer {
        protected final RequestQueue.RequestFilter filter;
        public ResponseTransformer(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        public abstract NetworkResponse transform(NetworkResponse networkResponse);
    }

}
