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


import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.http.MediaType;

public class ContentTypeBasedRespConverter<T> extends AbstractSmartResponseConverter<T>{

    Map<MediaType, Converter<NetworkResponse, T>> converters = new HashMap<>();

    @Override
    protected Converter<NetworkResponse, T> chooseConverter(NetworkResponse value) {
       Converter converter =  converters.get(value.contentType);
        if(converter!=null) {
            return converter;
        }
        StringBuilder str = new StringBuilder("Cannot find converter for :" + value + ", tried:");
        for(Map.Entry entry:converters.entrySet()) {
            str.append("\n\t").append(entry.getKey()).append(" : ").append(entry.getValue());
        }
        throw new RuntimeException(str.toString());
    }

    public ContentTypeBasedRespConverter<T> add(MediaType mediaType, Converter<NetworkResponse, T>
            converter) {
        converters.put(mediaType, converter);
        return this;
    }

}
