/*
 * Copyright (C) 2015 AppTik Project
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

package io.apptik.jus.demoapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.apptik.jus.demoapp.adapter.RecyclerAdapter;
import io.apptik.jus.demoapp.adapter.RxAdapter;
import io.apptik.jus.demoapp.api.Instructables;


public class InstructablesListFragment extends Fragment {


    SwipeRefreshLayout swiperefresh;
    int offset=0;

    public static InstructablesListFragment newInstance() {
        InstructablesListFragment fragment = new InstructablesListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public InstructablesListFragment() {
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
        View v = inflater.inflate(R.layout.fragment_imagelist, container, false);
        // Inflate the layout for this fragment
        RecyclerAdapter recyclerAdapter = new RxAdapter(MyJus.hub().getList());
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.list_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (swiperefresh != null && recyclerView != null) {
                    swiperefresh.setEnabled(((LinearLayoutManager) recyclerView
                            .getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0);
                }
                if((((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition() > offset+15)) {
                    offset+=20;
                    MyJus.intructablesApi().list(20, offset, Instructables.SORT_RECENT, "id");
                }
            }
        });
        swiperefresh = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MyJus.intructablesApi().list(20, 0, Instructables.SORT_RECENT, "id");
                offset = 0;
                swiperefresh.setRefreshing(false);
            }
        });
        return v;
    }


}
