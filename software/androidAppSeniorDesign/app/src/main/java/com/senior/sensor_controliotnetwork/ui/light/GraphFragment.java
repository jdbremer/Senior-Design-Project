package com.senior.sensor_controliotnetwork.ui.light;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

import java.util.Iterator;

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

    private double graph2LastXValue = 0d;


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


        mSeries1 = new LineGraphSeries<>();
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
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(50);
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(10);
                graph.addSeries(mSeries1);
                int maxGraphPoints = 5;
                //double x = inc++;
//                if(x > maxGraphPoints){
//                    x = 0;
//                    inc = 0;
//                }
               // double y = 30+inc;




                 graph2LastXValue += 1d;
                //double y = 50;

//                mSeries1.appendData(new DataPoint(x,y),true,3);
//                mSeries1.appendData(new DataPoint(25,50),true,3);
//                mSeries1.appendData(new DataPoint(28,60),true,3);
//                mSeries1.appendData(new DataPoint(30,70),true,3);
//                mSeries1.appendData(new DataPoint(50,80),true,3);

                double y = Double.parseDouble((String) dataSnapshot.getValue());
                mSeries1.appendData(new DataPoint(graph2LastXValue,y*1000),true, maxGraphPoints);
//                DataPoint v = new DataPoint(x,y);
//                values[inc++] = v;
//                mSeries1 = new LineGraphSeries<>(values);
//                graph.addSeries(mSeries1);

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
