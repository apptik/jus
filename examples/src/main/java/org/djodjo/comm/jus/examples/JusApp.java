package org.djodjo.comm.jus.examples;

import android.app.Application;

import org.djodjo.comm.jus.examples.api.MyJus;


public class JusApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        MyJus.init(this);

    }


}
