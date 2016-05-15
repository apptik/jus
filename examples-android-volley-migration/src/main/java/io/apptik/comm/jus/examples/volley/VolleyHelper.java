package io.apptik.comm.jus.examples.volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleyHelper {
	private static VolleyHelper mInstance;
	private RequestQueue queue;
	private ImageLoader imageLoader;
	private static Context context;

	private VolleyHelper(Context context) {
		VolleyHelper.context = context;
		queue = getRequestQueue();
		imageLoader = new ImageLoader(queue, LruBitmapCache.getInstance(context));

	}

	public static synchronized VolleyHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VolleyHelper(context);
		}
		return mInstance;
	}


	public RequestQueue getRequestQueue() {
		if (queue == null) {
			// getApplicationContext() is key, it keeps you from leaking the
			// Activity or BroadcastReceiver if someone passes one in.
			queue = Volley.newRequestQueue(context.getApplicationContext());
		}
		return queue;
	}

	public <T> Request<T> addToRequestQueue(Request<T> req) {
		return getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}
}