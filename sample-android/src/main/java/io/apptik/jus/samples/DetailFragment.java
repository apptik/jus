package io.apptik.jus.samples;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.apptik.comm.jus.ui.NetworkImageView;
import io.apptik.json.JsonObject;
import rx.Subscription;


public class DetailFragment extends Fragment {
	private TextView itemId;
	private TextView itemTitle;
	private NetworkImageView itemPic;
	Subscription subscription;

	protected void updateData(JsonObject item) {
		itemId.setText(item.getString("id"));
		itemTitle.setText(item.getString("title"));
		itemPic.setImageUrl(item.getString("mediumUrl"), MyJus.imageLoader());
	}


	public DetailFragment() {
	}

	public static DetailFragment newInstance() {
		DetailFragment fragment = new DetailFragment();
		return fragment;
	}


	@Override
	public void onPause() {
		super.onPause();
		if(subscription!=null) {
			subscription.unsubscribe();
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		itemId = (TextView) view.findViewById(R.id.details_id);
		itemTitle = (TextView) view.findViewById(R.id.details_title);
		itemPic = (NetworkImageView) view.findViewById(R.id.details_pic);

		subscription = MyJus.hub().getInfo().subscribe(this::updateData);


	}
}
