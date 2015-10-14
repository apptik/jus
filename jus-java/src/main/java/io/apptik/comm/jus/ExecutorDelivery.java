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

package io.apptik.comm.jus;

import java.util.concurrent.Executor;

/**
 * Delivers responses and errors.
 */
public class ExecutorDelivery extends BaseDelivery {
    /** Used for posting responses. */
    private final Executor mResponsePoster;

    /**
     * Creates a new response delivery interface, mockable version
     * for testing.
     * @param executor For running delivery tasks
     */
    public ExecutorDelivery(Executor executor) {
        mResponsePoster = executor;
    }

    @Override
    void doDeliver(Request request, Response response, Runnable runnable) {
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
    }


    /**
     * A Runnable used for delivering network responses to a listener on the
     * main threadId.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final Request request;
        private final Response response;
        private final Runnable runnable;

        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
            this.request = request;
            this.response = response;
            this.runnable = runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // Deliver a normal response or error, depending.
            if (response.isSuccess()) {
                request.deliverResponse(response.result);
            } else {
                request.deliverError(response.error);
            }

            // If we have been provided a post-delivery runnable, run it.
            if (runnable != null) {
                runnable.run();
            }
       }
    }
}
