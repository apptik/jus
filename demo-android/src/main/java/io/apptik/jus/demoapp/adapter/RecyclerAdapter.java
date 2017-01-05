/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.apptik.jus.demoapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.apptik.comm.jus.ui.ImageLoader;
import io.apptik.comm.jus.ui.NetworkImageView;
import io.apptik.json.JsonArray;
import io.apptik.jus.demoapp.DetailFragment;
import io.apptik.jus.demoapp.MainActivity;
import io.apptik.jus.demoapp.MyJus;
import io.apptik.jus.demoapp.R;


/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
	private static final String TAG = "RecyclerAdapter";

	private JsonArray jarr;
	private final ImageLoader imageLoader;

	// BEGIN_INCLUDE(recyclerViewSampleViewHolder)

	/**
	 * Provide a reference to the type of views that you are using (custom ViewHolder)
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView title;
		private final TextView author;
		private final TextView views;
		private final TextView favorites;
		private final View view;
		public final NetworkImageView niv;


		public ViewHolder(View v) {
			super(v);
			view = v;
			niv = (NetworkImageView) v.findViewById(R.id.niv_mainImage);
			title = (TextView) v.findViewById(R.id.tv_title);
			author = (TextView) v.findViewById(R.id.tv_author);
			views = (TextView) v.findViewById(R.id.tv_views);
			favorites = (TextView) v.findViewById(R.id.tv_favorites);


		}
	}


	// END_INCLUDE(recyclerViewSampleViewHolder)

	/**
	 * Initialize the dataset of the Adapter.
	 */
	public RecyclerAdapter(JsonArray jsonArray) {
		jarr = jsonArray;
		this.imageLoader = MyJus.imageLoader();
	}

	protected void updateData(JsonArray jsonArray) {
		if (jarr != null && !jarr.contains(jsonArray.get(0))) {
			appendData(jsonArray);
		} else {
			jarr = jsonArray;
			notifyDataSetChanged();
		}
	}

	protected void appendData(JsonArray jsonArray) {
		jarr.addAll(jsonArray);
		notifyDataSetChanged();
	}

	// BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
	// Create new views (invoked by the layout manager)
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		// Create a new view.
		View v = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.rv_card, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(v);

		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int itemPosition = viewHolder.getAdapterPosition();

				RecyclerAdapter.this.getJarr();
				String id = jarr.get(itemPosition).asJsonObject().getString("id");
				Log.d(TAG, "Element " + itemPosition + " clicked. with id : " + id);
				MyJus.intructablesApi().info(id);
				Fragment prevFragment = ((MainActivity) v.getContext())
						.getSupportFragmentManager().findFragmentById(R.id.container);
				Fragment fragment = DetailFragment.newInstance();
				((MainActivity) v.getContext()).getSupportFragmentManager()
						.beginTransaction()
						.hide(prevFragment).add(R.id.container, fragment).addToBackStack(null)
						.commit();
			}
		});

		return viewHolder;
	}
	// END_INCLUDE(recyclerViewOnCreateViewHolder)

	// BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		if (jarr == null) return;
		Log.d(TAG, "Element " + position + " set.");
		// Get element from your dataset at this position and replace the contents of the view
		// with that element
		Log.d("jus", jarr.get(position).asJsonObject().getString("imageUrl"));

		viewHolder.niv.setImageUrl(jarr.get(position).asJsonObject().getString("imageUrl"),
				imageLoader);
		viewHolder.title.setText(jarr.get(position).asJsonObject().getString("title"));
		viewHolder.author.setText(jarr.get(position).asJsonObject().getString("author"));
		viewHolder.views.setText(jarr.get(position).asJsonObject().getInt("views").toString());
		viewHolder.favorites.setText(jarr.get(position).asJsonObject().getInt("favorites")
				.toString());


	}
	// END_INCLUDE(recyclerViewOnBindViewHolder)

	public JsonArray getJarr() {
		return jarr;
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		if (jarr == null) return 0;
		return jarr.size();
	}


	@Override
	public int getItemViewType(int position) {
		return 0;
	}


}
