/*
 * Copyright (C) 2015 Square, Inc.
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
package io.apptik.comm.jus.converter;

import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;
import okio.BufferedSource;

public final class MoshiResponseBodyConverter<T> implements Converter<NetworkResponse, T> {
    private final JsonAdapter<T> adapter;

    public MoshiResponseBodyConverter(JsonAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(NetworkResponse value) throws IOException {
        BufferedSource source = value.getBufferedSource();
        try {
            return adapter.fromJson(source);
        } finally {
            Utils.closeQuietly(source);
        }
    }
}
