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

package io.apptik.comm.jus.error;

import io.apptik.comm.jus.NetworkResponse;

/**
 * Indicates that the server responded with an error response 5xx.
 */
@SuppressWarnings("serial")
public class ServerError extends JusError {

    public ServerError(NetworkResponse response) {
        super(response);
    }

    public ServerError(NetworkResponse response, String exceptionMessage) {
        super(response, exceptionMessage);
    }

    public ServerError(NetworkResponse response, String exceptionMessage, Throwable reason) {
        super(response, exceptionMessage, reason);
    }

    public ServerError(NetworkResponse response, Throwable reason) {
        super(response, reason);
    }

    public boolean isInternalServerError() {
        if(networkResponse!=null) {
            return 500 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isNotImplemented() {
        if(networkResponse!=null) {
            return 501 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isBadGateway() {
        if(networkResponse!=null) {
            return 502 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isServiceUnavailable() {
        if(networkResponse!=null) {
            return 503 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isGatewayTimeout() {
        if(networkResponse!=null) {
            return 504 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isHTTPVersionNotSupported() {
        if(networkResponse!=null) {
            return 505 == networkResponse.statusCode;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ServerError{} " + super.toString();
    }
}
