package com.AndersonBremerSweeney.sensor_controliotnetwork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class tabLightGraphFragment extends Fragment {
    private static final String TAG = "tabLightGraphFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.lightgraph_fragment, container, false);

        return view;
    }

}
