
package io.apptik.comm.jus.converter;

import org.djodjo.json.JsonElement;
import org.djodjo.json.JsonObject;

import java.io.IOException;
import java.io.Reader;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;

public final class JJsonObjectResponseBodyConverter implements Converter<NetworkResponse, JsonObject> {

  public JJsonObjectResponseBodyConverter() {
  }

  @Override public JsonObject convert(NetworkResponse value) throws IOException {
    Reader reader = value.getCharStream();
    try {
      return JsonElement.readFrom(reader).asJsonObject();
    } finally {
      Utils.closeQuietly(reader);
    }
  }
}
