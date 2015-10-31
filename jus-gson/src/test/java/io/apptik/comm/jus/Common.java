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
