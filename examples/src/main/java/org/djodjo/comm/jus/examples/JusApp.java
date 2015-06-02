package org.djodjo.comm.jus.examples;

import android.app.Application;

import org.djodjo.comm.jus.util.JusHelper;


public class JusApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        JusHelper.init(this);

    }


}
