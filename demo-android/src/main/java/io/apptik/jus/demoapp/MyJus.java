package io.apptik.jus.demoapp;

import android.content.Context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.converter.JJsonConverterFactory;
import io.apptik.comm.jus.converter.JJsonObjectResponseConverter;
import io.apptik.comm.jus.okhttp3.MarkerInterceptorFactory;
import io.apptik.comm.jus.okhttp3.OkHttpStack;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Tag;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.util.PooledBitmapLruCache;
import io.apptik.json.JsonArray;
import io.apptik.jus.demoapp.api.Instructables;

public class MyJus {

    public static MyJus inst;
    private Instructables instructables;
    private RequestQueue queue;
    private ImageLoader imageLoader;
    private SampleHubShield hub;

    private MyJus(Context ctx) {
        JusLog.MarkerLog.on();
        queue = AndroidJus.newRequestQueue(ctx,
                new OkHttpStack(new MarkerInterceptorFactory.DefaultMIF()));
        hub = new SampleHubShield(queue);

        instructables = new RetroProxy.Builder()
                .baseUrl(Instructables.baseUrl)
                .requestQueue(queue)
                //Example of complex custom converter
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[]
                            annotations) {
                        //First we check if we have tag which is REQ_LIST
                        if (hasAnnotatedTag(annotations, Instructables.REQ_LIST)) {
                            return new Converter<NetworkResponse, Object>() {
                                JJsonObjectResponseConverter baseConv = new
                                        JJsonObjectResponseConverter();

                                @Override
                                public JsonArray convert(NetworkResponse value) throws IOException {
                                    //then when converting we exctract the 'items' JsonArray from
                                    // the root JsonObject.
                                    return baseConv.convert(value).getJsonArray("items");
                                }
                            };
                        }
                        return null;
                    }

                    @Override
                    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[]
                            annotations) {
                        return null;
                    }
                })
                .addConverterFactory(new JJsonConverterFactory())
                .build().create(Instructables.class);
        PooledBitmapLruCache defaultBitmapLruCache = new ExampleLruPooledCache();
        imageLoader = new ImageLoader(queue,
                // new NoCache()
                defaultBitmapLruCache, defaultBitmapLruCache
        );
//        RxRequestQueue.resultObservable(queue, null)
//                .subscribe(resultEvent -> {
//                    Log.d("Jus-Test", "jus received response for: " + resultEvent.request);
//                    if (!(resultEvent.response instanceof Bitmap)) {
//                        Log.d("Jus-Test", "jus response : " + resultEvent.response);
//                    }
//                });
//
//        RxRequestQueue.errorObservable(queue, null)
//                .subscribe(errorEvent -> {
//                    Log.e("Jus-Test", "jus received ERROR for: " + errorEvent.request);
//                    if (errorEvent.error != null) {
//                        Log.e("Jus-Test", "jus ERROR : " + errorEvent.error);
//                    }
//                });
    }

    public static void init(Context ctx) {
        inst = new MyJus(ctx);
    }


    public static MyJus get() {
        return inst;
    }

    public static Instructables intructablesApi() {
        if (inst == null) throw new IllegalStateException("Not Initialized");
        return inst.instructables;
    }

    public static RequestQueue queue() {
        if (inst == null) throw new IllegalStateException("Not Initialized");
        return inst.queue;
    }

    public static SampleHubShield hub() {
        if (inst == null) throw new IllegalStateException("Not Initialized");
        return inst.hub;
    }

    public static ImageLoader imageLoader() {
        if (inst == null) throw new IllegalStateException("Not Initialized");
        return inst.imageLoader;
    }


    private static boolean hasAnnotatedTag(Annotation[] annotations, String tag) {
        for (Annotation annotation : annotations) {
            if (Tag.class.isInstance(annotation) && tag.equals(((Tag)
                    annotation).value())) {
                return true;
            }
        }
        return false;
    }
}
