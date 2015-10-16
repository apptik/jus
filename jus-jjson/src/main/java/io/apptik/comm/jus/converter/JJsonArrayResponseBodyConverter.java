
package io.apptik.comm.jus.converter;

import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonElement;

import java.io.IOException;
import java.io.Reader;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;

public final class JJsonArrayResponseBodyConverter implements Converter<NetworkResponse, JsonArray> {

  public JJsonArrayResponseBodyConverter() {
  }

  @Override public JsonArray convert(NetworkResponse value) throws IOException {
    Reader reader = value.getCharStream();
    try {
      return JsonElement.readFrom(reader).asJsonArray();
    } finally {
      Utils.closeQuietly(reader);
    }
  }
}
