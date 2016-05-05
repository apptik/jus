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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.apptik.comm.jus.AndroidJus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.examples.TestObjectForGson;
import io.apptik.comm.jus.examples.R;
import io.apptik.comm.jus.examples.jus.CustomJusHelper;
import io.apptik.comm.jus.request.GsonRequest;
import io.apptik.comm.jus.request.ImageRequest;
import io.apptik.comm.jus.request.JsonArrayRequest;
import io.apptik.comm.jus.request.JsonObjectRequest;
import io.apptik.comm.jus.request.StringRequest;
import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.ui.NetworkImageView;

public class JusFragment extends Fragment {
	RequestQueue requestQueue;
	public static final String TAG = "MyTag";

	public JusFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment Volley.
	 */
	// TODO: Rename and change types and number of parameters
	public static JusFragment newInstance() {
		JusFragment fragment = new JusFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_jus, container, false);

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

		// StringRequest with JUS with Standard RequestQueue
		// Instantiate the RequestQueue.
		requestQueue = AndroidJus.newRequestQueue(getContext());
		String url = "http://www.google.com";

		// Request a string response from the provided URL.
		StringRequest stringRequest = new StringRequest(Request.Method.GET, url);
		stringRequest.setTag(TAG);

		// Add to RequestQueue
		requestQueue.add(stringRequest
				.addResponseListener(new RequestListener.ResponseListener<String>() {
					@Override
					public void onResponse(String response) {
						mTextView.setText("Response is: " + response.substring(0, 500));
					}
				})
				.addErrorListener(new RequestListener.ErrorListener() {
					@Override
					public void onError(JusError error) {
						mTextView.setText("That didn't work!");
					}
				}));

	}

	private void customQueueImageRequest(View v) {
		// ImageRequest with JUS for Android
		final ImageView mImageView;
		String url = "http://i.imgur.com/7spzG.png";
		mImageView = (ImageView) v.findViewById(R.id.iv_image_request);

		// Instantiate the RequestQueue with a CustomHelper
		CustomJusHelper.init(getContext());

		ImageRequest request = new ImageRequest(url, 0, 0, ImageView.ScaleType.CENTER, null);

		CustomJusHelper.getRequestQueue().add(request.addResponseListener(new RequestListener.ResponseListener<Bitmap>
				() {
			@Override
			public void onResponse(Bitmap response) {
				mImageView.setImageBitmap(response);

			}
		}).addErrorListener(new RequestListener.ErrorListener() {
			@Override
			public void onError(JusError error) {
				mImageView.setImageResource(R.drawable.ic_error);

			}
		}));

	}

	private void networkImageViewRequest(View v) {
		ImageLoader mImageLoader;
		NetworkImageView mNetworkImageView;
		String url = "http://developer.android.com/images/training/system-ui.png";

		// Get the NetworkImageView that will display the image.
		mNetworkImageView = (NetworkImageView) v.findViewById(R.id.networkImageView);

		// Get the ImageLoader through your custom Helper.
		mImageLoader = CustomJusHelper.getImageLoader();

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

		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url);
		CustomJusHelper.getRequestQueue().add(jsonArrayRequest.addResponseListener(
				new RequestListener.ResponseListener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							mTxtDisplay.setText("Response: " + response.getJSONObject(0).toString()
									.substring(0, 50));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
		)).addErrorListener(new RequestListener.ErrorListener() {
			@Override
			public void onError(JusError error) {
				mTxtDisplay.setText("Error while loading Json: " + error.toString());

			}
		});
	}

	private void gsonRequest(View v) {
		final TextView mTxtDisplay;
		mTxtDisplay = (TextView) v.findViewById(R.id.tv_gsonRequest);

		String url = "https://api.github.com/users/mralexgray/repos";
		GsonRequest gsonRequest = new GsonRequest(Request.Method.GET, url, TestObjectForGson[]
				.class);

		CustomJusHelper.getRequestQueue().add(gsonRequest.addResponseListener(
				new RequestListener.ResponseListener<TestObjectForGson[]>() {
					@Override
					public void onResponse(TestObjectForGson[] response) {
						mTxtDisplay.setText("Response: " + response[0].getId());

					}
				}
		)).addErrorListener(new RequestListener.ErrorListener() {
			@Override
			public void onError(JusError error) {
				mTxtDisplay.setText("Error while loading Gson: " + error.toString());

			}
		});
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
