/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2007 The Guava Authors
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
package io.apptik.comm.jus.toolbox;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;

public final class Utils {
    public static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static Type tryIdentifyResultType(Request request) {
        Type t = null;
        checkNotNull(request, "request == null");
        //method 1 using class
        try {
            if (request.getClass().getGenericSuperclass() instanceof ParameterizedType) {
                t = ((ParameterizedType) request.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (t != null) {
            if(hasUnresolvableType(t)) {
                t=null;
            }
        }
        //method 2 using parseNetworkResponse return type
        if(t==null) {
            try {
                java.lang.reflect.Method method = request.getClass().getDeclaredMethod
                        ("parseNetworkResponse", NetworkResponse.class);
                method.setAccessible(true);
                Type responseType = method.getGenericReturnType();
                t = ((ParameterizedType) responseType)
                        .getActualTypeArguments()[0];
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        if (t != null) {
            if(hasUnresolvableType(t)) {
                t=null;
            }
        }

        return t;
    }

    public static boolean hasUnresolvableType(Type type) {
        if (type instanceof Class<?>) {
            return false;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(typeArgument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return true;
        }
        if (type instanceof WildcardType) {
            return true;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + className);
    }

    private Utils() {
        // No instances.
    }
}
