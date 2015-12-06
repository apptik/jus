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
import io.apptik.comm.jus.error.AuthError;
import io.apptik.comm.jus.http.HttpUrl;

/**
 * An interface for interacting with auth tokens.
 */
public interface Authenticator {

    /**
     * Synchronously retrieves an auth token.
     * It should handle refreshing the token if needed (evaluating its lifetime for example).
     * Implementations must have into consideration that this can be a
     * called from different threads at the same time.
     *
     * @throws AuthError If authentication did not succeed
     */
    String getToken() throws AuthError;

    /**
     * Clears cached user token. In typical implementation calling this before {@link #getToken()}
     * will make sure the token returned is not expired.
     */
    void clearToken();

    /**
     * Invalidates the provided auth token.
     */
     void invalidateToken();

    /**
     * Authenticator Factory class that provides authenticators for specific requests
     */
    abstract class Factory {
        /**
         * Overwrite this method to return authenticator for specific request
         * @param url The url of the request
         * @param networkRequest Request data
         * @return Authenticator providing token for this request
         */
       public Authenticator forRequest(HttpUrl url, NetworkRequest networkRequest) {
           return null;
       }
    }
}
