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

package io.apptik.comm.jus;


import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class Common {
    private Common() {}
    public interface AnInterface {
        String getName();
    }

    public static class AnImplementation implements AnInterface {
        public final String theName;

        public AnImplementation(String name) {
            theName = name;
        }

        @Override
        public String getName() {
            return theName;
        }
    }

    public static class AnInterfaceAdapter extends TypeAdapter<AnInterface> {
        @Override
        public void write(JsonWriter jsonWriter, AnInterface anInterface) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("name").value(anInterface.getName());
            jsonWriter.endObject();
        }

        @Override
        public AnInterface read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();

            String name = null;
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
                switch (jsonReader.nextName()) {
                    case "name":
                        name = jsonReader.nextString();
                        break;
                }
            }

            jsonReader.endObject();
            return new AnImplementation(name);
        }
    }
}
