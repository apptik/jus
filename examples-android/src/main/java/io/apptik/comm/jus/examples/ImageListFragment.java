package io.apptik.comm.jus.examples;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import io.apptik.comm.jus.examples.adapter.RecyclerAdapter;
import io.apptik.comm.jus.examples.mock.MockData;


public class ImageListFragment extends Fragment {

    private static final String TAG = "ImageListFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER,
        STAGG_GRID_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RadioButton mLinearLayoutRadioButton;
    protected RadioButton mGridLayoutRadioButton;

    protected RecyclerView mRecyclerView;
    protected RecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    public static ImageListFragment newInstance() {
        ImageListFragment fragment = new ImageListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ImageListFragment() {
        // Required empty public constructor
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
        View v =  inflater.inflate(R.layout.fragment_imagelist, container, false);
        // Inflate the layout for this fragment
        ListScrollListener listScrollListener = new ListScrollListener();
        RecyclerAdapter recyclerAdapter =  new RecyclerAdapter(
                MockData.getMockJsonArray(500, 500)
                //MockData.getAssetsMock(getActivity())
                , getActivity(), listScrollListener);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.list_images);
        recyclerView.setOnScrollListener(listScrollListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerAdapter);

       return v;
    }

}
