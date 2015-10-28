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

import org.json.JSONArray;

import java.io.IOException;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JSONConverter;

/**
 * A request for retrieving a {@link JSONArray} response body at a given URL.
 */
public class JsonArrayRequest extends Request<JSONArray> {

    /**
     * Creates a new request.
     * @param url URL to fetch the JSON from
     */
    public JsonArrayRequest(String method, String url) {
        super(method, url, new JSONConverter.JSONArrayResponseConverter());
    }

    public JsonArrayRequest setRequestData(JSONArray requestData) {
        try {
            super.setRequestData(requestData, new JSONConverter.JSONArrayRequestConverter());
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert " + requestData + " to NetworkRequest", e);
        }
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

}
