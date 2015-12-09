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
 * Indicates that the server responded with an error response 4xx.
 */
@SuppressWarnings("serial")
public class RequestError extends JusError {

    public RequestError(NetworkResponse response) {
        super(response);
    }

    public RequestError(NetworkResponse response, String exceptionMessage) {
        super(response, exceptionMessage);
    }

    public RequestError(NetworkResponse response, String exceptionMessage, Throwable reason) {
        super(response, exceptionMessage, reason);
    }

    public RequestError(NetworkResponse response, Throwable reason) {
        super(response, reason);
    }

    public boolean isBadRequest() {
        if(networkResponse!=null) {
            return 400 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isUnauthorized() {
        if(networkResponse!=null) {
            return 401 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isPaymentRequired() {
        if(networkResponse!=null) {
            return 402 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isForbidden() {
        if(networkResponse!=null) {
            return 403 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isNotFound() {
        if(networkResponse!=null) {
            return 404 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isMethodNotAllowed() {
        if(networkResponse!=null) {
            return 405 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isNotAcceptable() {
        if(networkResponse!=null) {
            return 406 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isProxyAuthenticationRequired() {
        if(networkResponse!=null) {
            return 407 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isRequestTimeOut() {
        if(networkResponse!=null) {
            return 408 == networkResponse.statusCode;
        }
        return false;
    }

    public boolean isConflict() {
        if(networkResponse!=null) {
            return 409 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isGone() {
        if(networkResponse!=null) {
            return 410 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isLengthRequired() {
        if(networkResponse!=null) {
            return 411 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isPreconditionFailed() {
        if(networkResponse!=null) {
            return 412 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isRequestEntityTooLarge() {
        if(networkResponse!=null) {
            return 413 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isRequestURITooLarge() {
        if(networkResponse!=null) {
            return 414 == networkResponse.statusCode;
        }
        return false;
    }
    public boolean isUnsupportedMediaType() {
        if(networkResponse!=null) {
            return 415 == networkResponse.statusCode;
        }
        return false;
    }

    @Override
    public String toString() {
        return "RequestError{} " + super.toString();
    }
}
