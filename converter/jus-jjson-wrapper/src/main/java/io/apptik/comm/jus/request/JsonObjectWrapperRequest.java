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
import io.apptik.comm.jus.converter.JsonObjectWrapperResponseConverter;
import io.apptik.comm.jus.converter.JsonWrapperRequestConverter;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.json.wrapper.JsonElementWrapper;
import io.apptik.json.wrapper.JsonObjectWrapper;

public class JsonObjectWrapperRequest<T extends JsonObjectWrapper> extends Request<T> {

    public JsonObjectWrapperRequest(String method, HttpUrl url, Class<T> wrapperType) {
        super(method, url, new JsonObjectWrapperResponseConverter(wrapperType));
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json")
                .build());
    }

    public JsonObjectWrapperRequest(String method, String url, Class<T> wrapperType) {
        super(method, url, new JsonObjectWrapperResponseConverter(wrapperType));
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json")
                .build());
    }

    public JsonObjectWrapperRequest setRequestData(JsonElementWrapper requestData) {
        try {
            super.setRequestData(requestData, new JsonWrapperRequestConverter());
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert " + requestData + " to NetworkRequest", e);
        }
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json")
                .build());
        return this;
    }

}
