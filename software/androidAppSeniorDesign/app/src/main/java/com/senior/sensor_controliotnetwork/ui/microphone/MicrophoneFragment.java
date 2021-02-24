//package com.senior.sensor_controliotnetwork.ui.microphone;
//
//        import android.os.Bundle;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.ViewGroup;
//
//        import androidx.annotation.NonNull;
//        import androidx.annotation.Nullable;
//        import androidx.fragment.app.Fragment;
//        import androidx.lifecycle.Observer;
//        import androidx.lifecycle.ViewModelProvider;
//        import androidx.viewpager.widget.ViewPager;
//
//        import com.google.android.material.tabs.TabLayout;
//        import com.senior.sensor_controliotnetwork.R;
//
//public class MicrophoneFragment extends Fragment {
//
//    private MicrophoneViewModel microphoneViewModel;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//
//
//        microphoneViewModel =
//                new ViewModelProvider(this).get(MicrophoneViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_microphone, container, false);
//
//        TabLayout tabLayout = root.findViewById(R.id.microphoneTabBar);
//        ViewPager viewPagerMicrophone = root.findViewById(R.id.viewPagerMicrophone);
//
//
//
//
//        //TabItem tabData = root.findViewById(R.id.lightData);
//        //TabItem tabGraph = root.findViewById(R.id.lightGraph);
//
////        tabLayout.addTab(tabLayout.newTab().setText("Data"));
////        tabLayout.addTab(tabLayout.newTab().setText("Graph"));
//
//        MicrophonePagerAdapter pagerAdapter = new MicrophonePagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
//        viewPagerMicrophone.setAdapter(pagerAdapter);
////        viewPagerMicrophone.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        //tabLayout.setupWithViewPager(viewPagerMicrophone);
////        viewPagerMicrophone.addOnPageChangeListener(
////                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
////        );
//
////        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
////
////            @Override
////            public void onTabSelected(TabLayout.Tab tab) {
////                viewPagerMicrophone.setCurrentItem(tab.getPosition());
////            }
////
////            @Override
////            public void onTabUnselected(TabLayout.Tab tab) {
////
////            }
////
////            @Override
////            public void onTabReselected(TabLayout.Tab tab) {
////
////            }
////
////        });
//
//
//        //tabLayout.setupWithViewPager(viewPagerMicrophone);
//        //pagerAdapter.getPageTitle(viewPagerMicrophone);
//
//
//
////        microphoneViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
////            @Override
////            public void onChanged(@Nullable String s) {
////
////            }
////        });
//        return root;
//    }
//
//}

















//new attempt
package com.senior.sensor_controliotnetwork.ui.microphone;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.senior.sensor_controliotnetwork.R;

public class MicrophoneFragment extends Fragment {

    private MicrophoneViewModel microphoneViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        microphoneViewModel =
                new ViewModelProvider(this).get(MicrophoneViewModel.class);
        View root = inflater.inflate(R.layout.fragment_microphone, container, false);

        TabLayout tabLayout = root.findViewById(R.id.microphoneTabBar);
        ViewPager viewPager = root.findViewById(R.id.viewPagerMicrophone);




        //TabItem tabData = root.findViewById(R.id.lightData);
        //TabItem tabGraph = root.findViewById(R.id.lightGraph);

//        tabLayout.addTab(tabLayout.newTab().setText("Data"));
//        tabLayout.addTab(tabLayout.newTab().setText("Graph"));
        MicrophonePagerAdapter pagerAdapter = new MicrophonePagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
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



//        lightViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//
//            }
//        });
        return root;
    }

}