package io.apptik.comm.jus.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public interface BitmapPool {

    Bitmap getReusableBitmap(BitmapFactory.Options options);
    void addToPool(Bitmap bitmap);

}
