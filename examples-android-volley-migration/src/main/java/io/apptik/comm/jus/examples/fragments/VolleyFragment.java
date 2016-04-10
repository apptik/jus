package io.apptik.comm.jus.examples.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import io.apptik.comm.jus.examples.R;
import io.apptik.comm.jus.examples.TestObjectForGson;
import io.apptik.comm.jus.examples.volley.CustomRequestQueue;
import io.apptik.comm.jus.examples.volley.GsonRequest;


public class VolleyFragment extends Fragment {
	public static final String TAG = "MyTag";
	RequestQueue requestQueue;

	public VolleyFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment Volley.
	 */
	// TODO: Rename and change types and number of parameters
	public static VolleyFragment newInstance() {
		VolleyFragment fragment = new VolleyFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_volley, container, false);

		standardQueueStringRequest(v);
		customQueueImageRequest(v);
		networkImageViewRequest(v);
		jsonRequest(v);
		gsonRequest(v);


		// Inflate the layout for this fragment
		return v;
	}

	private void standardQueueStringRequest(View v) {
		final TextView mTextView = (TextView) v.findViewById(R.id.tv_string_request);

		// StringRequest with VOLLEY with Standard RequestQueue
		// Instantiate the RequestQueue.
		requestQueue = Volley.newRequestQueue(v.getContext());
		String url = "http://www.google.com";

		// Request a string response from the provided URL.
		StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// Display the first 500 characters of the response string.
						mTextView.setText("Response is: " + response.substring(0, 500));
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				mTextView.setText("That didn't work!");
			}
		});

		stringRequest.setTag(TAG);
		// Add the request to the RequestQueue.
		requestQueue.add(stringRequest);
	}

	private void customQueueImageRequest(View v) {

		// ImageRequest with VOLLEY with Custom RequestQueue
		final ImageView mImageView;
		String url = "http://i.imgur.com/7spzG.png";
		mImageView = (ImageView) v.findViewById(R.id.iv_image_request);


		// Retrieves an image specified by the URL, displays it in the UI.
		ImageRequest request = new ImageRequest(url,
				new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap bitmap) {
						mImageView.setImageBitmap(bitmap);
					}
				}, 0, 0, null,
				new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						mImageView.setImageResource(R.drawable.ic_error);
					}
				});

		// Access the RequestQueue through your singleton class.
		CustomRequestQueue.getInstance(v.getContext()).addToRequestQueue(request);
	}


	private void networkImageViewRequest(View v) {

		ImageLoader mImageLoader;
		NetworkImageView mNetworkImageView;
		String url = "http://developer.android.com/images/training/system-ui.png";

		// Get the NetworkImageView that will display the image.
		mNetworkImageView = (NetworkImageView) v.findViewById(R.id
				.networkImageViewWithCustomLRUCache);

		// Get the ImageLoader through your singleton class and make use of LRU Cache
		mImageLoader = CustomRequestQueue.getInstance(v.getContext()).getImageLoader();

		// Set the URL of the image that should be loaded into this view, and
		// specify the ImageLoader that will be used to make the request.
		mNetworkImageView.setImageUrl(url, mImageLoader);

		// Set error image
		mNetworkImageView.setErrorImageResId(R.drawable.ic_error);

	}

	private void jsonRequest(View v) {
		final TextView mTxtDisplay;
		mTxtDisplay = (TextView) v.findViewById(R.id.tv_jsonRequest);
		String url = "https://api.github.com/users/mralexgray/repos";

		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
				(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							mTxtDisplay.setText("Response: " + response.getJSONObject(0).toString()
									.substring(0, 50));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub

					}
				});

		CustomRequestQueue.getInstance(v.getContext()).addToRequestQueue(jsonArrayRequest);
	}

	private void gsonRequest(View v) {
		final TextView mTxtDisplay;
		mTxtDisplay = (TextView) v.findViewById(R.id.tv_gsonRequest);
		String url = "https://api.github.com/users/mralexgray/repos";

		GsonRequest gsObjRequest = new GsonRequest(url, TestObjectForGson[].class,
				null, new Response.Listener<TestObjectForGson[]>() {
			@Override
			public void onResponse(TestObjectForGson[] response) {
				// in that case, the response is not a JSON object but a Java object
				mTxtDisplay.setText("Response: " + response[0].getId());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});

		CustomRequestQueue.getInstance(v.getContext()).addToRequestQueue(gsObjRequest);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (requestQueue != null) {
			requestQueue.cancelAll(TAG);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

	}

	@Override
	public void onDetach() {
		super.onDetach();
	}


}
