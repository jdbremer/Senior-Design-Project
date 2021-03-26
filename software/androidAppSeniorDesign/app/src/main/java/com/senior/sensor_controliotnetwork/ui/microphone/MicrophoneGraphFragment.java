package com.senior.sensor_controliotnetwork.ui.microphone;

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

public class MicrophoneGraphFragment extends Fragment {

    public  static boolean active = false;
    private DatabaseReference mPostReference;

    private int maxDataPoints = 25;


    private DataPoint[] values = new DataPoint[50];
    private GraphView graph;
    private LineGraphSeries<DataPoint> mSeries1 = new LineGraphSeries<>();

    private ArrayList<DataPoint> mSeries2 = new ArrayList<>();

    // private Map<String, String> sensorValues = new HashMap<String, String>();
    GraphLevelReceiver receiver;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MicrophoneGraphFragment() {
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
    public static MicrophoneGraphFragment newInstance(String param1, String param2) {
        MicrophoneGraphFragment fragment = new MicrophoneGraphFragment();
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
        getActivity().registerReceiver(receiver, new IntentFilter("SensorMap"));  //<----Register

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

            if(intent.getAction().equals("SensorMap"))
            {
                HashMap<Integer, String> hashMap = (HashMap<Integer, String>)intent.getSerializableExtra("MAPS");
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

        View root = inflater.inflate(R.layout.fragment_microphone_graph, container, false);

        mPostReference = FirebaseDatabase.getInstance().getReference().child("dataFromChild").child("dBMeter");  //LISTENER OBJECT

        graph = (GraphView) root.findViewById(R.id.dBMetergraph);

        mSeries1 = new LineGraphSeries<>();
        mSeries1.setThickness(15);
        mSeries1.setColor(Color.rgb(210,180,140));
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(maxDataPoints);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1000);

        // mSeries1 = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        double x = 20;
        return root;
    }



//    BroadcastReceiver br = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("MAPS");
//        }
//    };

    public void testFunc(HashMap sensorValues){

        //trying to remove old data on the graph and add new data
        mSeries1.clearReference(graph);
        graph.removeSeries(mSeries1);
        mSeries1 = new LineGraphSeries<>();
        //LineGraphSeries<DataPoint> mSeries1 = new LineGraphSeries<>();
        mSeries1.setThickness(15);
        mSeries1.setColor(Color.rgb(210,180,140));
        graph.addSeries(mSeries1);
        graph.refreshDrawableState();
        graph.onDataChanged(true, true);


        //this is here as a test to just continue adding data
        //mSeries1.appendData(new DataPoint(inc2++,y*1000),false, 10);


        TreeMap<Integer,String> sortedSensorValues = new TreeMap<Integer,String>(sensorValues); //convert the hashmaps (which aren't sorted) to treemaps (which are sorted)
        //iterate through the hash table to then add the shifted data to the series
        Iterator<TreeMap.Entry<Integer, String>> itr = sortedSensorValues.entrySet().iterator();
        while(itr.hasNext()){
            //iterate.next();
            TreeMap.Entry<Integer, String> entry = itr.next();
            int x = entry.getKey();
            double y2 = (Double.parseDouble((String) entry.getValue()));
            mSeries1.appendData(new DataPoint(x,y2),false, maxDataPoints);
            graph.refreshDrawableState();
            graph.onDataChanged(true, true);
        }
    }
}
