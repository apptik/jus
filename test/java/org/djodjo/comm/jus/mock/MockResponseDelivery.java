/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2014 Kalin Maldzhanski
 * Copyright (C) 2011 The Android Open Source Project
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

import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.ResponseDelivery;

public class MockResponseDelivery implements ResponseDelivery {

    public boolean postResponse_called = false;
    public boolean postError_called = false;

    public boolean wasEitherResponseCalled() {
        return postResponse_called || postError_called;
    }

    public Response<?> responsePosted = null;
    @Override
    public void postResponse(Request<?,?> request, Response<?> response) {
        postResponse_called = true;
        responsePosted = response;
    }

    @Override
    public void postResponse(Request<?,?> request, Response<?> response, Runnable runnable) {
        postResponse_called = true;
        responsePosted = response;
        runnable.run();
    }

    @Override
    public void postError(Request<?,?> request, JusError error) {
        postError_called = true;
    }
}
