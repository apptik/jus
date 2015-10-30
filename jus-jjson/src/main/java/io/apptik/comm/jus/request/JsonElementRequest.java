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


import java.io.IOException;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JJsonRequestConverter;
import io.apptik.comm.jus.converter.JJsonResponseConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JsonElementRequest extends Request<JsonElement> {

    public JsonElementRequest(String method, HttpUrl url) {
        super(method, url, new JJsonResponseConverter());
    }

    public JsonElementRequest(String method, String url) {
        super(method, url, new JJsonResponseConverter());
    }


    public JsonElementRequest setRequestData(JsonElement requestData) {
        try {
            super.setRequestData(requestData, new JJsonRequestConverter());
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert " + requestData + " to NetworkRequest", e);
        }
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

}
