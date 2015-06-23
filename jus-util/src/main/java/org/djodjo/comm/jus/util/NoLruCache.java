package org.djodjo.comm.jus.util;


import android.graphics.Bitmap;

import org.djodjo.comm.jus.ui.ImageLoader;


public class NoLruCache
        implements ImageLoader.ImageCache {

    @Override
    public Bitmap getBitmap(String url) {
        return null;
    }


    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        //do nothing;
    }
}

