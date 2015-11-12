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
            str.append("\n\t" + entry.getKey() + " : " + entry.getValue());
        }
        throw new RuntimeException(str.toString());
    }

    public ContentTypeBasedRespConverter<T> add(MediaType mediaType, Converter<NetworkResponse, T>
            converter) {
        converters.put(mediaType, converter);
        return this;
    }

}
