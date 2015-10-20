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

import com.squareup.wire.Message;
import com.squareup.wire.Wire;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.WireRequestBodyConverter;
import io.apptik.comm.jus.converter.WireResponseBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class WireRequest<T extends Message> extends Request<T> {

    public WireRequest(String method, HttpUrl url, Wire wire, Class<T> cls) {
        super(method, url, new WireResponseBodyConverter<T>(wire, cls));
    }

    public WireRequest(String method, HttpUrl url, Class<T> cls) {
        this(method, url, new Wire(), cls);
    }

    public WireRequest<T> setObjectRequest(T objectRequest) {
        super.setObjectRequest(objectRequest, new WireRequestBodyConverter<T>());
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/x-protobuf")
                .build());
        return this;
    }

}
