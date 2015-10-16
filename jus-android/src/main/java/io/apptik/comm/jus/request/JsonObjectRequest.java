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

package io.apptik.comm.jus.request;

import org.json.JSONObject;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JSONConverter;

/**
 * A request for retrieving a {@link JSONObject} response body at a given URL, allowing for an
 * optional {@link JSONObject} to be passed in as part of the request body.
 */
public class JsonObjectRequest extends Request<JSONObject> {

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     */
    public JsonObjectRequest(String method, String url) {
        super(method, url, new JSONConverter.JSONObjectResponseConverter());
    }

    public Request<JSONObject> setObjectRequest(JSONObject objectRequest) {
        return super.setObjectRequest(objectRequest, new JSONConverter.JSONObjectRequestConverter());
    }
}
