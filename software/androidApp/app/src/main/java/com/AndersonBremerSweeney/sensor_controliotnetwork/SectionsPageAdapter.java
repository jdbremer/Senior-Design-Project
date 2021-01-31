package com.AndersonBremerSweeney.sensor_controliotnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import androidx.annotation.StringRes;


public class SectionsPageAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.lightDataTab, R.string.lightGraphTab};
    private final Context mContext;

    public SectionsPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new tabLightDataFragment();  //open light data fragment
                break;
            case 1:
                fragment = new tabLightGraphFragment(); //open light graph fragment
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}

//public class SectionsPageAdapter extends FragmentPagerAdapter {
//    private final List<Fragment> mFragmentList = new ArrayList<>();
//    private final List<String> mFragmentTitleList = new ArrayList<>();
//
//    public void addFragment(Fragment fragment, String title){
//        mFragmentList.add(fragment);
//        mFragmentTitleList.add(title);
//    }
//
//    public SectionsPageAdapter(FragmentManager fm){
//        super(fm);
//    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        return mFragmentList.get(position);
//    }
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return mFragmentTitleList.get(position);
//    }
//
//    @Override
//    public int getCount() {
//        return mFragmentList.size();
//    }
//}
