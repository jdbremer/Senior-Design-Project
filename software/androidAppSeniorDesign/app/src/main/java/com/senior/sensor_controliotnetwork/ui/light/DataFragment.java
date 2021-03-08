package com.senior.sensor_controliotnetwork.ui.light;

import android.app.Activity;
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

public class DataFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceLightConnectionStatus;
    private DatabaseReference mPostReferenceLightSampleInterval;

    boolean onOrOff = false;    //status of weather or not the light sensor is connected
//    public int sensorGrabTime = 5;  //time in seconds for how often light sensor grabs new data
    public String sensorGrabTime = "5";  //time in seconds for how often light sensor grabs new data
    public String sampleInterval;

    private LightViewModel lightViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lightViewModel =
                new ViewModelProvider(this).get(LightViewModel.class);
        View root = inflater.inflate(R.layout.fragment_light_data, container, false);


        mPostReferenceLightConnectionStatus = FirebaseDatabase.getInstance().getReference().child("Connections").child("LightSensor");  //LISTENER OBJECT
        mPostReferenceLightSampleInterval = FirebaseDatabase.getInstance().getReference().child("dataFromApp").child("LightSensor");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT


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
                }
                else if(onOff == 0) {   //off
                    connectionLightText.setText("Not Connected");   //change text to show light sensor is not connected
                    onOrOff = false;    //light sensor is off
                    lightSampleIntervalButton.setEnabled(false);    //do NOT allow user to press button to change sample rate
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

        return root;
    }

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