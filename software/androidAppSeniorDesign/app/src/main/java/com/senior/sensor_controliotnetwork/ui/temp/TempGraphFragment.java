//package com.senior.sensor_controliotnetwork.ui.temp;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.fragment.app.Fragment;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;
//import com.senior.sensor_controliotnetwork.R;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.TreeMap;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link TempGraphFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class TempGraphFragment extends Fragment {
//
//    public  static boolean active = false;
//    private DatabaseReference mPostReference;
//
//    private int maxDataPoints = 25;
//
//    public static HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
//
//
//    private DataPoint[] values = new DataPoint[50];
//    private GraphView graph;
//    private LineGraphSeries<DataPoint> mSeries1 = new LineGraphSeries<>();
//
//    private ArrayList<DataPoint> mSeries2 = new ArrayList<>();
//
//    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//    String userId = user.getUid();  //assign userId the token for the user
//
//    // private Map<String, String> sensorValues = new HashMap<String, String>();
//    GraphLevelReceiver receiver;
//
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public TempGraphFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment GraphFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static TempGraphFragment newInstance(String param1, String param2) {
//        TempGraphFragment fragment = new TempGraphFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//    @Override
//    public void onStart() {
//        super.onStart();
//        active = true;
//        getActivity().registerReceiver(receiver, new IntentFilter("SensorMap"));  //<----Register
//
////        Intent serviceIntent = new Intent(tempService.class.getName());
////        serviceIntent.setAction("sendSensorMap")
////        getContext().startService(serviceIntent);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        active = false;
//        getActivity().unregisterReceiver(receiver);           //<-- Unregister to avoid memoryleak
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//        receiver = new GraphLevelReceiver();
//
//    }
//
//
//
//    class GraphLevelReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            if(intent.getAction().equals("SensorMap"))
//            {
//                hashMap = (HashMap<Integer, String>)intent.getSerializableExtra("MAPS");
//                testFunc(hashMap);
//                // Show it in GraphView
//            }
//
//        }
//
//
//    }
//
//
//
//
//    int inc = 0;
//    int inc2 = 0;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//
//        View root = inflater.inflate(R.layout.fragment_temp_graph, container, false);
//
//        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("TempSensor");  //LISTENER OBJECT
//
//        graph = (GraphView) root.findViewById(R.id.graph);
//
//        mSeries1 = new LineGraphSeries<>();
//        mSeries1.setThickness(15);
//        mSeries1.setColor(Color.rgb(210,180,140));
//        graph.addSeries(mSeries1);
//        graph.getViewport().setXAxisBoundsManual(true);
//        graph.getViewport().setMinX(1);
//        graph.getViewport().setMaxX(maxDataPoints);
//        graph.getViewport().setYAxisBoundsManual(true);
//        graph.getViewport().setMinY(0);
//        graph.getViewport().setMaxY(1000);
//
//
//        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//        if(!hashMap.isEmpty()) {
//            testFunc(hashMap);
//        }
//
//
//
//
//
//        return root;
//    }
//
//
//    public void testFunc(HashMap sensorValues){
//
//        //trying to remove old data on the graph and add new data
//        mSeries1.clearReference(graph);
//        graph.removeSeries(mSeries1);
//        mSeries1 = new LineGraphSeries<>();
//        mSeries1.setThickness(15);
//        mSeries1.setColor(Color.rgb(210,180,140));
//        graph.addSeries(mSeries1);
//        graph.refreshDrawableState();
//        graph.onDataChanged(true, true);
//
//
//        TreeMap<Integer,String> sortedSensorValues = new TreeMap<Integer,String>(sensorValues); //convert the hashmaps (which aren't sorted) to treemaps (which are sorted)
//        //iterate through the hash table to then add the shifted data to the series
//        Iterator<TreeMap.Entry<Integer, String>> itr = sortedSensorValues.entrySet().iterator();
//        int i = 0;
//        while(itr.hasNext()){
//
//            TreeMap.Entry<Integer, String> entry = itr.next();
//            int x = entry.getKey();
//            double y2 = (Double.parseDouble((String) entry.getValue()));
//            if(i == 0){
//                mSeries1.resetData(new DataPoint[] {new DataPoint(x, y2)});
//                i = 1;
//            }
//            else {
//                mSeries1.appendData(new DataPoint(x, y2), false, maxDataPoints);
//            }
//            graph.refreshDrawableState();
//            graph.onDataChanged(true, true);
//        }
//    }
//
//
//}






package com.senior.sensor_controliotnetwork.ui.temp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.senior.sensor_controliotnetwork.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import android.content.IntentFilter;


import android.app.Service;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TempGraphFragment extends Fragment {

    public  static boolean active = false;
    private DatabaseReference mPostReference;

    private int maxDataPoints = 25;

    public static HashMap<Integer, String> hashMap = new HashMap<Integer, String>();

    public static HashMap<Integer, String> hashMapF = new HashMap<Integer, String>();
    public static HashMap<Integer, String> hashMapC = new HashMap<Integer, String>();

    public static boolean isCelsius;

    public Switch unitSwitch;


    private DataPoint[] values = new DataPoint[50];
    private GraphView graph;
    private LineGraphSeries<DataPoint> mSeries1 = new LineGraphSeries<>();

    private ArrayList<DataPoint> mSeries2 = new ArrayList<>();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    // private Map<String, String> sensorValues = new HashMap<String, String>();
    GraphLevelReceiver receiver;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TempGraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TempGraphFragment newInstance(String param1, String param2) {
        TempGraphFragment fragment = new TempGraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onStart() {
        super.onStart();
        active = true;
        getActivity().registerReceiver(receiver, new IntentFilter("tempSensorMap"));  //<----Register

//        Intent serviceIntent = new Intent(lightService.class.getName());
//        serviceIntent.setAction("sendSensorMap")
//        getContext().startService(serviceIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
        getActivity().unregisterReceiver(receiver);           //<-- Unregister to avoid memoryleak
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        receiver = new GraphLevelReceiver();

    }



    class GraphLevelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("tempSensorMap"))
            {
                isCelsius = unitSwitch.isChecked();

                hashMapF = (HashMap<Integer, String>)intent.getSerializableExtra("tempMAPSF");
                hashMapC = (HashMap<Integer, String>)intent.getSerializableExtra("tempMAPSC");

                if(isCelsius == true)
                    hashMap = hashMapC;
                else    //Fahrenheit
                    hashMap = hashMapF;

                testFunc(hashMap);
                // Show it in GraphView
            }

        }


    }




    int inc = 0;
    int inc2 = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View root = inflater.inflate(R.layout.fragment_temp_graph, container, false);

        unitSwitch = (Switch) root.findViewById(R.id.tempUnitSwitch);

       // unitSwitch.setChecked(isCelsius);

        isCelsius = unitSwitch.isChecked();

        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("TempSensor");  //LISTENER OBJECT

        graph = (GraphView) root.findViewById(R.id.tempGraph);

        mSeries1 = new LineGraphSeries<>();
        mSeries1.setThickness(15);
        mSeries1.setColor(Color.rgb(210,180,140));
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(maxDataPoints);
        graph.getViewport().setYAxisBoundsManual(true);

        if(isCelsius){
            graph.getViewport().setMinY(-50);
            graph.getViewport().setMaxY(120);
        }
        else{
            graph.getViewport().setMinY(-50);
            graph.getViewport().setMaxY(50);
        }


        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        if(!hashMap.isEmpty()) {
            testFunc(hashMap);
        }


        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    hashMap = hashMapC;
                }
                else{
                    hashMap = hashMapF;
                }

                testFunc(hashMap);
                // do something, the isChecked will be
                // true if the switch is in the On position
            }
        });



        return root;
    }


    public void testFunc(HashMap sensorValues){

        //trying to remove old data on the graph and add new data
        mSeries1.clearReference(graph);
        graph.removeSeries(mSeries1);
        graph.clearSecondScale();

        mSeries1 = new LineGraphSeries<>();
        mSeries1.setThickness(15);
        mSeries1.setColor(Color.rgb(210,180,140));
        graph.addSeries(mSeries1);
        graph.getViewport().setYAxisBoundsManual(true);
        if(isCelsius){
            graph.getViewport().setMinY(-50);
            graph.getViewport().setMaxY(120);
        }
        else{
            graph.getViewport().setMinY(-50);
            graph.getViewport().setMaxY(50);
        }
       // graph.refreshDrawableState();
        //graph.onDataChanged(false, false);


        TreeMap<Integer,String> sortedSensorValues = new TreeMap<Integer,String>(sensorValues); //convert the hashmaps (which aren't sorted) to treemaps (which are sorted)
        //iterate through the hash table to then add the shifted data to the series
        Iterator<TreeMap.Entry<Integer, String>> itr = sortedSensorValues.entrySet().iterator();
        int i = 0;
        while(itr.hasNext()){

            TreeMap.Entry<Integer, String> entry = itr.next();
            int x = entry.getKey();
            double y2 = (Double.parseDouble((String) entry.getValue()));
            if(i == 0){
                mSeries1.resetData(new DataPoint[] {new DataPoint(x, y2)});
                i = 1;
            }
            else {
                mSeries1.appendData(new DataPoint(x, y2), false, maxDataPoints);
            }
            graph.refreshDrawableState();
            graph.onDataChanged(true, false);
        }
    }


}