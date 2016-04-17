package io.apptik.jus.examples;

import android.content.Context;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.BasicConverterFactory;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.jus.examples.api.Intructables;

public class MyJus {

    public static MyJus inst;
    private Intructables instructables;


    private static class Result {

    }
    private MyJus(Context ctx) {
        RequestQueue queue = AndroidJus.newRequestQueue(ctx);
        instructables = new RetroProxy.Builder()
                .baseUrl(Intructables.baseUrl)
                .requestQueue(queue)
                .addConverterFactory(new BasicConverterFactory())
                .build().create(Intructables.class);
    }

    public static void init(Context ctx) {
        inst = new MyJus(ctx);
    }


    public static MyJus get() {
        return inst;
    }

    public static Intructables api() {
        if(inst==null) throw new IllegalStateException("Not Initialized");
        return inst.instructables;
    }

}
