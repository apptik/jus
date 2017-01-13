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

import io.apptik.comm.jus.error.JusError;

/**
 * Retry policy for a request.
 */
public interface RetryPolicy {

    /**
     * Returns the current connect timeout (used for logging).
     */
    int getCurrentConnectTimeout();

    /**
     * Returns the current read timeout (used for logging).
     */
    int getCurrentReadTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * @param error The error code of the last attempt.
     * @throws JusError In the event that the retry could not be performed (for example if we
     * ran out of attempts), the passed in error is thrown.
     */
    void retry(JusError error) throws JusError;


    abstract class Factory {
        public RetryPolicy get(Request request) {
            return new DefaultRetryPolicy(request);
        }
    }
}
