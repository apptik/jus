package io.apptik.comm.jus.converter;


import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkResponse;

public abstract class AbstractSmartResponseConverter<T> implements Converter<NetworkResponse,T> {

    @Override
    public T convert(NetworkResponse value) throws IOException {
        return chooseConverter(value).convert(value);
    }

    protected abstract Converter<NetworkResponse,T> chooseConverter(NetworkResponse value);

}
