package com.AndersonBremerSweeney.sensor_controliotnetwork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class tabLightDataFragment extends Fragment {
    private static final String TAG = "tabLightDataFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.lightdata_fragment, container, false);

        return view;
    }

}
