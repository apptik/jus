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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import java.io.IOException;
import java.io.InputStream;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;

public final class ProtoResponseConverter<T extends MessageLite>
        implements Converter<NetworkResponse, T> {
    private final Parser<T> parser;

    public ProtoResponseConverter(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public T convert(NetworkResponse value) throws IOException {
        InputStream is = value.getByteStream();
        try {
            return parser.parseFrom(is);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e); // Despite extending IOException, this is data mismatch.
        } finally {
            Utils.closeQuietly(is);
        }
    }
}
