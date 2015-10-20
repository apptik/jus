
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

package io.apptik.comm.jus.converter;

import org.djodjo.json.JsonElement;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.http.MediaType;
import io.apptik.comm.jus.toolbox.Utils;

public final class JJsonRequestBodyConverter implements Converter<JsonElement, NetworkRequest> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    public JJsonRequestBodyConverter() {
    }

    @Override
    public NetworkRequest convert(JsonElement value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(baos));
        value.writeTo(writer);
        Utils.closeQuietly(writer);
        byte[] bytes = baos.toByteArray();
        return new NetworkRequest.Builder().setContentType(MEDIA_TYPE).setBody(bytes).build();
    }
}
