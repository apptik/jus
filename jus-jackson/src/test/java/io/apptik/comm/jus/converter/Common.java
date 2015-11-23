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


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class Common {

    interface AnInterface {
        String getName();
    }

    static class AnImplementation implements AnInterface {
        String theName;

        AnImplementation() {
        }

        AnImplementation(String name) {
            theName = name;
        }

        @Override
        public String getName() {
            return theName;
        }
    }

    static class AnInterfaceSerializer extends StdSerializer<AnInterface> {
        AnInterfaceSerializer() {
            super(AnInterface.class);
        }

        @Override
        public void serialize(AnInterface anInterface, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("name");
            jsonGenerator.writeString(anInterface.getName());
            jsonGenerator.writeEndObject();
        }
    }

    static class AnInterfaceDeserializer extends StdDeserializer<AnInterface> {
        AnInterfaceDeserializer() {
            super(AnInterface.class);
        }

        @Override
        public AnInterface deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
                throw new AssertionError("Expected start object.");
            }

            String name = null;

            while (jp.nextToken() != JsonToken.END_OBJECT) {
                switch (jp.getCurrentName()) {
                    case "name":
                        name = jp.getValueAsString();
                        break;
                }
            }

            return new AnImplementation(name);
        }
    }
}
