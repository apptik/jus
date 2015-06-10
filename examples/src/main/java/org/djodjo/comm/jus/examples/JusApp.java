package org.djodjo.comm.jus.examples;

import android.app.Application;

import org.djodjo.comm.jus.examples.api.CustomJusHelper;


public class JusApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        CustomJusHelper.init(this);

    }


}
