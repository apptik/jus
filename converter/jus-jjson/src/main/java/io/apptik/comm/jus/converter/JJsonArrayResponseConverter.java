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


import java.io.IOException;
import java.io.Reader;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.toolbox.Utils;
import io.apptik.json.JsonArray;
import io.apptik.json.JsonElement;

public final class JJsonArrayResponseConverter implements Converter<NetworkResponse, JsonArray> {

  public JJsonArrayResponseConverter() {
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
