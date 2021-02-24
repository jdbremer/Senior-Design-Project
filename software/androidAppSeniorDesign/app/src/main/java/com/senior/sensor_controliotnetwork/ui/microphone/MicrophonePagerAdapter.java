package com.senior.sensor_controliotnetwork.ui.microphone;

import android.graphics.pdf.PdfDocument;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MicrophonePagerAdapter extends FragmentPagerAdapter {

    private int numOfTabs;


    public MicrophonePagerAdapter(FragmentManager fm, int numOfTabs){
        //super(fm);
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numOfTabs = numOfTabs;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new MicrophoneDataFragment();

            case 1:
                return new MicrophoneGraphFragment();
            default:
                return null;
        }


    }



    @Override
    public int getCount() {
        return numOfTabs;
    }
}