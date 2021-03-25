//package com.senior.sensor_controliotnetwork.ui.microphone;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.senior.sensor_controliotnetwork.R;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link MicrophoneDataFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class MicrophoneDataFragment extends Fragment {
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
//    public MicrophoneDataFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment DataFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MicrophoneDataFragment newInstance(String param1, String param2) {
//        MicrophoneDataFragment fragment = new MicrophoneDataFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_microphone_data, container, false);
//    }
//}







package com.senior.sensor_controliotnetwork.ui.microphone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.senior.sensor_controliotnetwork.R;

import java.util.HashMap;

public class MicrophoneDataFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceMicConnectionStatus;
    private DatabaseReference mPostReferenceMicSampleInterval;
    private DatabaseReference mPostReferenceMicThreshold;

    boolean onOrOff = false;    //status of weather or not the mic is connected
    //    public int sensorGrabTime = 5;  //time in seconds for how often mic grabs new data
    public String sensorGrabTime = "5";  //time in seconds for how often mic grabs new data
    public String sampleInterval;
    public static String threshold;
    boolean setThreshold = false;


    private MicrophoneViewModel microphoneViewModel;

    public static boolean active = false;

    DataLevelReceiver receiver;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        getActivity().registerReceiver(receiver, new IntentFilter("sensorVal"));  //<----Register

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

        receiver = new DataLevelReceiver();

    }


    class DataLevelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("sensorVal"))
            {
                String micSensorData = (String)intent.getSerializableExtra("SENSOR");
//                setSensorTxtBox(micSensorData);
                TextView sensorValueTxt = (TextView) getActivity().findViewById(R.id.micSensorDataTxt);
                sensorValueTxt.setText(micSensorData);
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        microphoneViewModel =
                new ViewModelProvider(this).get(MicrophoneViewModel.class);
        View root = inflater.inflate(R.layout.fragment_microphone_data, container, false);


        mPostReferenceMicConnectionStatus = FirebaseDatabase.getInstance().getReference().child("Connections").child("dBMeter");  //LISTENER OBJECT
        mPostReferenceMicSampleInterval = FirebaseDatabase.getInstance().getReference().child("dataFromApp").child("dBMeter");  //LISTENER OBJECT
        mPostReferenceMicThreshold = FirebaseDatabase.getInstance().getReference().child("internalAppData").child("thresholds").child("dBMeter");  //LISTENER OBJECT

        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        mDatabase.child("internalAppData").child("thresholds").child("dBMeter").setValue("0");
        mDatabase.child("dataFromApp").child("dBMeter").setValue("5");    //set sample interval in database

        Button setThresholdButton = (Button) root.findViewById(R.id.setThreshold);
        setThresholdButton.setEnabled(false);
        Button thresholdONOFF = (Button) root.findViewById(R.id.thresholdOnOff);
        EditText micThresholdText = (EditText) root.findViewById(R.id.MicThresholdText);

        TextView connectionMicText = (TextView) root.findViewById(R.id.textMicConnectionStatus);
        Button micSampleIntervalButton = (Button) root.findViewById(R.id.buttonMicSampleInterval);

        //CONSTANT LISTENER CODE//
        ValueEventListener micConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //on
                    connectionMicText.setText("Connected");   //change text so show mic sensor is connected
                    onOrOff = true; //mic sensor is on
                    micSampleIntervalButton.setEnabled(true); //allow user to press button to change sample rate
                    thresholdONOFF.setEnabled(true);
                }
                else if(onOff == 0) {   //off
                    connectionMicText.setText("Not Connected");   //change text to show mic sensor is not connected
                    onOrOff = false;    //mic sensor is off
                    micSampleIntervalButton.setEnabled(false);    //do NOT allow user to press button to change sample rate
                    setThresholdButton.setEnabled(false);
                    thresholdONOFF.setEnabled(false);
                    setThreshold = false;
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceMicConnectionStatus.addValueEventListener(micConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        //END CONSTANT LISTENER CODE//

        //CONSTANT LISTENER CODE//
        EditText micIntervalText = (EditText) root.findViewById(R.id.textMicDataSampleInterval);
        ValueEventListener micIntervalConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                micIntervalText.setText(dataSnapshot.getValue().toString());
                sampleInterval = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceMicSampleInterval.addValueEventListener(micIntervalConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        ValueEventListener micThreshold = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                micThresholdText.setText(dataSnapshot.getValue().toString());
                threshold = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceMicThreshold.addValueEventListener(micThreshold);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        Button micSampleButton = (Button) root.findViewById(R.id.buttonMicSampleInterval);

        micSampleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
//                sensorGrabTime = Integer.parseInt(lightIntervalText.getText().toString());        //grabs the value in char form and converts it to an int if button was pressed FOR INTEGER VERSION, NOT STRING
                sensorGrabTime = micIntervalText.getText().toString();
                if(isNumeric(sensorGrabTime) && !"0".equals(sensorGrabTime))   //check if non zero integer was entered
                    mDatabase.child("dataFromApp").child("dBMeter").setValue(sensorGrabTime);    //set sample interval in database
                else    //invalid entry
                    micIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value

                MicrophoneUtils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });







        setThresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempThreshold;
                tempThreshold = micThresholdText.getText().toString();
                if(isNumeric(tempThreshold) && !"0".equals(tempThreshold)) {   //check if non zero integer was entered
                    threshold = tempThreshold;
                    mDatabase.child("internalAppData").child("thresholds").child("dBMeter").setValue(threshold);
                }
                //mDatabase.child("dataFromApp").child("LightSensor").setValue(sensorGrabTime);    //set sample interval in database
                else {
                    micThresholdText.setText(threshold);
                }   //invalid entry
                //lightIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value


                MicrophoneUtils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });




        thresholdONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setThreshold = !setThreshold;
                if(setThreshold == true){
                    setThresholdButton.setEnabled(true);
                    mDatabase.child("internalAppData").child("thresholds").child("dBMeter").setValue("0");
                }
                else{
                    setThresholdButton.setEnabled(false);
                    mDatabase.child("internalAppData").child("thresholds").child("dBMeter").setValue("0");
                }
            }
        });

        return root;
    }

//    public void setSensorTxtBox(String value){
//        TextView sensorValueTxt = (TextView) getActivity().findViewById(R.id.textLightConnectionStatus);
//    }

    public static boolean isNumeric(String strNum) {    //check if a string is a number
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}