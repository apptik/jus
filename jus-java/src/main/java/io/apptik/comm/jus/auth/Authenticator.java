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

package io.apptik.comm.jus.auth;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.AuthError;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.toolbox.HttpNetwork;

/**
 * An interface for adding a Authorization header to the request
 */
public interface Authenticator {

    /**
     * Synchronously retrieves an Authorization header value.
     * Implementations must have into consideration that this can be a
     * called from different threads at the same time.
     *
     * @throws AuthError If authentication did not succeed
     */
    String getAuthValue() throws AuthError;

    /**
     * Clears cached Authorization value. Implementations can call this before
     * {@link #getAuthValue()} to make sure the value returned is not expired.
     * This is also called in {@link HttpNetwork#performRequest(Request)}  when 401 is returned
     * and followed by {@link #getAuthValue()}
     */
     void clearAuthValue();

    /**
     * Authenticator Factory class that provides authenticators for specific requests
     */
    abstract class Factory {
        /**
         * Overwrite this method to return authenticator for specific request
         *
         * @param url            The url of the request
         * @param networkRequest Request data
         * @return Authenticator providing token for this request
         */
        public Authenticator forRequest(HttpUrl url, NetworkRequest networkRequest) {
            return null;
        }
    }
}
