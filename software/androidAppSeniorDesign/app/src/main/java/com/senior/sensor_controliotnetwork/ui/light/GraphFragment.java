package com.senior.sensor_controliotnetwork.ui.light;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {


    private DatabaseReference mPostReference;


    private DataPoint[] values = new DataPoint[50];
    private GraphView graph;
    private LineGraphSeries<DataPoint> mSeries1 = new LineGraphSeries<>();

    private ArrayList<DataPoint> mSeries2 = new ArrayList<>();

    private Map<String, String> sensorValues = new HashMap<String, String>();



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GraphFragment() {
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
    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    int inc = 0;
    int inc2 = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_light_graph, container, false);





        mPostReference = FirebaseDatabase.getInstance().getReference().child("dataFromChild").child("LightSensor");  //LISTENER OBJECT




        graph = (GraphView) root.findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
//        });
//        graph.addSeries(series);
//        graph.removeSeries(series);
//        mSeries2
        mSeries1 = new LineGraphSeries<>();
        mSeries1.setThickness(15);
        mSeries1.setColor(Color.rgb(210,180,140));
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(30);

        //graph.setDrawingCacheBackgroundColor(true);
        //graph.setDrawingCacheBackgroundColor(getResources().getColor(R.color.blue));

//
//        mSeries1.appendData(new DataPoint(1,1),false,4);
//        mSeries1.appendData(new DataPoint(2,2),false,4);
//        mSeries1.appendData(new DataPoint(3,3),false,4);
//        mSeries1.appendData(new DataPoint(4,4),false,4);
        //graph.removeAllSeries();



       // mSeries1 = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();


        double x = 20;


        //double y = 50;
        //DataPoint v = new DataPoint(x,y);
        //values[inc++] = v;
        //mSeries1 = new LineGraphSeries<>();
//        graph.addSeries(mSeries1);
//        mSeries1.appendData(new DataPoint(x,y),true,3);
//        mSeries1.appendData(new DataPoint(25,50),true,3);
//        mSeries1.appendData(new DataPoint(28,60),true,3);
//        mSeries1.appendData(new DataPoint(30,70),true,3);
//        mSeries1.appendData(new DataPoint(50,80),true,3);




        //CONSTANT LISTENER CODE//
        ValueEventListener constantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                //LineGraphSeries<DataPoint> mSeries3 = new LineGraphSeries<>();

                int maxGraphPoints = 11;


                double y = Double.parseDouble((String) dataSnapshot.getValue());

                //add data to a hash table
                sensorValues.put(String.valueOf(inc), (String) dataSnapshot.getValue() );


                //if the max number of points was reached
                if(inc >= maxGraphPoints){

                    //shift all the data within the hash table
                    for ( int i = 0; i < maxGraphPoints+1 ; i++){
                        if(i == 0){

                            sensorValues.remove(String.valueOf(i));
                        }
                        else{
                            String key = sensorValues.get(String.valueOf(i));
                            sensorValues.remove(String.valueOf(i));
                            sensorValues.put(String.valueOf(i-1), key);
                        }
                    }


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

                    //iterate through the hash table to then add the shifted data to the series
                    Iterator<Map.Entry<String, String>> itr = sensorValues.entrySet().iterator();
                    while(itr.hasNext()){
                        //iterate.next();
                        Map.Entry<String, String> entry = itr.next();
                        int x = Integer.parseInt(entry.getKey());
                        double y2 = (Double.parseDouble((String) entry.getValue()))*1000;
                        mSeries1.appendData(new DataPoint(x,y2),false, 10);
                        graph.refreshDrawableState();
                        graph.onDataChanged(true, true);
                    }


                }
                //the max number of points hasn't been reached yet
                else{
                    mSeries1.appendData(new DataPoint(inc++,y*1000),false, 10);
                    graph.refreshDrawableState();
                    graph.onDataChanged(true, true);
                    inc2++;
                }

            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReference.addValueEventListener(constantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//




        return root;
    }



}
