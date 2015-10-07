package io.apptik.comm.jus.examples;

import android.app.Application;

import io.apptik.comm.jus.examples.api.CustomJusHelper;


public class JusApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        CustomJusHelper.init(this);

    }


}
