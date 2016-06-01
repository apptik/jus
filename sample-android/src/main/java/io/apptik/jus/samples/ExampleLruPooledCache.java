package io.apptik.jus.samples;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import io.apptik.comm.jus.util.PooledBitmapLruCache;


public class ExampleLruPooledCache extends PooledBitmapLruCache {

    public ExampleLruPooledCache() {
        super();
        preFill(500, 500);
    }

    @Override
    public synchronized Bitmap getReusableBitmap(BitmapFactory.Options options) {
        Bitmap bitmap = super.getReusableBitmap(options);
        Log.e("Jus-Example", "getReusableBitmap: " + bitmap + " :: " + size() + "/" +
                maxSize() + " :: " + "" + pool().size() + "/" + pool().maxSize());
        return bitmap;
    }

    @Override
    public synchronized void addToPool(Bitmap bitmap) {
        Log.e("Jus-Example", "addToPool: " + bitmap + " :: " + size() + "/" +
                maxSize() + " :: " + "" + pool().size() + "/" + pool().maxSize());
        super.addToPool(bitmap);
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = super.getBitmap(url);
        Log.e("Jus-Example", "getBitmap: " + bitmap + " :: " + size() + "/" +
                maxSize() + " :: " + "" + pool().size() + "/" + pool().maxSize());
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.e("Jus-Example", "putBitmap: " + bitmap + " :: " + size() + "/" +
                maxSize() + " :: " + "" + pool().size() + "/" + pool().maxSize());
        super.putBitmap(url, bitmap);
    }
}
