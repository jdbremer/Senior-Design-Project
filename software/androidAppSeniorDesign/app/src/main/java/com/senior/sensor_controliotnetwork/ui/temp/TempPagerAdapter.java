package com.senior.sensor_controliotnetwork.ui.temp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TempPagerAdapter extends FragmentPagerAdapter {

    private int numOfTabs;


    public TempPagerAdapter(FragmentManager fm, int numOfTabs){
        //super(fm);
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numOfTabs = numOfTabs;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new TempDataFragment();

            case 1:
                return new TempGraphFragment();
            default:
                return null;
        }

    }




    @Override
    public int getCount() {
        return numOfTabs;
    }
}
