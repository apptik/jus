/*
 * Copyright (C) 2015 AppTik Project
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

import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.io.Reader;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;

public final class GsonResponseConverter<T> implements Converter<NetworkResponse, T> {
    private final TypeAdapter<T> adapter;

    public GsonResponseConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(NetworkResponse value) throws IOException {
        if(value.statusCode == 204) {
            return null;
        } else {
            Reader reader = value.getCharStream();
            try {
                return adapter.fromJson(reader);
            } finally {
                Utils.closeQuietly(reader);
            }
        }
    }
}
