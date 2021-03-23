package com.senior.sensor_controliotnetwork.ui.light;

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

public class DataFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceLightConnectionStatus;
    private DatabaseReference mPostReferenceLightSampleInterval;
    private DatabaseReference mPostReferenceLightThreshold;

    boolean onOrOff = false;    //status of weather or not the light sensor is connected
//    public int sensorGrabTime = 5;  //time in seconds for how often light sensor grabs new data
    public String sensorGrabTime = "5";  //time in seconds for how often light sensor grabs new data
    public String sampleInterval;
    public static String threshold;
    boolean setThreshold = false;


    private LightViewModel lightViewModel;

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
                String lightSensorData = (String)intent.getSerializableExtra("SENSOR");
//                setSensorTxtBox(lightSensorData);
                TextView sensorValueTxt = (TextView) getActivity().findViewById(R.id.lightSensorDataTxt);
                sensorValueTxt.setText(lightSensorData);
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lightViewModel =
                new ViewModelProvider(this).get(LightViewModel.class);
        View root = inflater.inflate(R.layout.fragment_light_data, container, false);


        mPostReferenceLightConnectionStatus = FirebaseDatabase.getInstance().getReference().child("Connections").child("LightSensor");  //LISTENER OBJECT
        mPostReferenceLightSampleInterval = FirebaseDatabase.getInstance().getReference().child("dataFromApp").child("LightSensor");  //LISTENER OBJECT
        mPostReferenceLightThreshold = FirebaseDatabase.getInstance().getReference().child("internalAppData").child("thresholds").child("LightSensor");  //LISTENER OBJECT

        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        mDatabase.child("internalAppData").child("thresholds").child("LightSensor").setValue("0");
        mDatabase.child("dataFromApp").child("LightSensor").setValue("5");    //set sample interval in database

        Button setThresholdButton = (Button) root.findViewById(R.id.setThreshold);
        setThresholdButton.setEnabled(false);
        Button thresholdONOFF = (Button) root.findViewById(R.id.thresholdOnOff);
        EditText lightThresholdText = (EditText) root.findViewById(R.id.LightThresholdText);

        TextView connectionLightText = (TextView) root.findViewById(R.id.textLightConnectionStatus);
        Button lightSampleIntervalButton = (Button) root.findViewById(R.id.buttonLightSampleInterval);

        //CONSTANT LISTENER CODE//
        ValueEventListener lightConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //on
                    connectionLightText.setText("Connected");   //change text so show light sensor is connected
                    onOrOff = true; //light sensor is on
                    lightSampleIntervalButton.setEnabled(true); //allow user to press button to change sample rate
                    thresholdONOFF.setEnabled(true);
                }
                else if(onOff == 0) {   //off
                    connectionLightText.setText("Not Connected");   //change text to show light sensor is not connected
                    onOrOff = false;    //light sensor is off
                    lightSampleIntervalButton.setEnabled(false);    //do NOT allow user to press button to change sample rate
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
        mPostReferenceLightConnectionStatus.addValueEventListener(lightConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        //END CONSTANT LISTENER CODE//

        //CONSTANT LISTENER CODE//
        EditText lightIntervalText = (EditText) root.findViewById(R.id.textLightDataSampleInterval);
        ValueEventListener lightIntervalConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                lightIntervalText.setText(dataSnapshot.getValue().toString());
                sampleInterval = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceLightSampleInterval.addValueEventListener(lightIntervalConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        ValueEventListener lightThreshold = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                lightThresholdText.setText(dataSnapshot.getValue().toString());
                threshold = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceLightThreshold.addValueEventListener(lightThreshold);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        Button lightSampleButton = (Button) root.findViewById(R.id.buttonLightSampleInterval);

        lightSampleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
//                sensorGrabTime = Integer.parseInt(lightIntervalText.getText().toString());        //grabs the value in char form and converts it to an int if button was pressed FOR INTEGER VERSION, NOT STRING
                sensorGrabTime = lightIntervalText.getText().toString();
                if(isNumeric(sensorGrabTime) && !"0".equals(sensorGrabTime))   //check if non zero integer was entered
                    mDatabase.child("dataFromApp").child("LightSensor").setValue(sensorGrabTime);    //set sample interval in database
                else    //invalid entry
                    lightIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value

                Utils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });







        setThresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempThreshold;
                tempThreshold = lightThresholdText.getText().toString();
                if(isNumeric(tempThreshold) && !"0".equals(tempThreshold)) {   //check if non zero integer was entered
                    threshold = tempThreshold;
                    mDatabase.child("internalAppData").child("thresholds").child("LightSensor").setValue(threshold);
                }
                    //mDatabase.child("dataFromApp").child("LightSensor").setValue(sensorGrabTime);    //set sample interval in database
                else {
                    lightThresholdText.setText(threshold);
                }   //invalid entry
                    //lightIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value


                Utils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });




        thresholdONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setThreshold = !setThreshold;
                if(setThreshold == true){
                    setThresholdButton.setEnabled(true);
                    mDatabase.child("internalAppData").child("thresholds").child("LightSensor").setValue("0");
                }
                else{
                    setThresholdButton.setEnabled(false);
                    mDatabase.child("internalAppData").child("thresholds").child("LightSensor").setValue("0");
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