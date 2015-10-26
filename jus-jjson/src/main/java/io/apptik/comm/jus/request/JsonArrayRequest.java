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

package io.apptik.comm.jus.request;


import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonElement;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JJsonArrayResponseConverter;
import io.apptik.comm.jus.converter.JJsonRequestConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JsonArrayRequest extends Request<JsonArray> {

    public JsonArrayRequest(String method, HttpUrl url) {
        super(method, url, new JJsonArrayResponseConverter());
    }

    public JsonArrayRequest(String method, String url) {
        super(method, url, new JJsonArrayResponseConverter());
    }
    
    public JsonArrayRequest setRequestData(JsonElement requestData) {
        super.setRequestData(requestData, new JJsonRequestConverter());
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

}
