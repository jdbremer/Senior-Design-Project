package com.senior.sensor_controliotnetwork.ui.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.senior.sensor_controliotnetwork.R;

public class TempFragment extends Fragment {

    private TempViewModel tempViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        tempViewModel =
                new ViewModelProvider(this).get(TempViewModel.class);
        View root = inflater.inflate(R.layout.fragment_temp, container, false);

        TabLayout tabLayout = root.findViewById(R.id.tempTabBar);
        ViewPager viewPager = root.findViewById(R.id.viewPagerTemp);




        //TabItem tabData = root.findViewById(R.id.tempData);
        //TabItem tabGraph = root.findViewById(R.id.tempGraph);

//        tabLayout.addTab(tabLayout.newTab().setText("Data"));
//        tabLayout.addTab(tabLayout.newTab().setText("Graph"));
        TempPagerAdapter tempPagerAdapter = new TempPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(tempPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


        //tabLayout.setupWithViewPager(viewPager);
        //pagerAdapter.getPageTitle(viewPager);



//        tempViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//
//            }
//        });
        return root;
    }

}