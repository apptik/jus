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

package io.apptik.jus.samples;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.apptik.comm.jus.rx.event.ResultEvent;
import io.apptik.jus.samples.adapter.RecyclerAdapter;
import io.apptik.jus.samples.adapter.RxAdapter;

import static io.apptik.jus.samples.api.Instructables.REQ_LIST;


public class InstructablesListFragment extends Fragment {

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
        RecyclerAdapter recyclerAdapter = new RxAdapter(
                MyJus.hub().get(REQ_LIST).map(o -> ((ResultEvent)o).response));
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.list_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerAdapter);

        return v;
    }


}
