package org.djodjo.comm.jus.examples.api;


import android.graphics.Bitmap;
import android.util.Log;

import org.djodjo.comm.jus.toolbox.ImageLoader;


public class NoCache
        implements ImageLoader.ImageCache {

    @Override
    public Bitmap getBitmap(String url) {
        Log.d("TEST", "(gc) LRU GET Bitmap:" + url);
        return null;
    }


    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d("TEST", "(gc) LRU PUT Bitmap:" + url);
    }
}

