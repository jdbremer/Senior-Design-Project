package com.senior.sensor_controliotnetwork.ui.temp;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.sensor_controliotnetwork.R;

public class TempDataFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceTempConnectionStatus;
    private DatabaseReference mPostReferenceTempSampleInterval;
    private DatabaseReference mPostReferenceTempThreshold;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    boolean onOrOff = false;    //status of weather or not the temp sensor is connected
    //    public int sensorGrabTime = 5;  //time in seconds for how often temp sensor grabs new data
    public String sensorGrabTime = "5";  //time in seconds for how often temp sensor grabs new data
    public String sampleInterval;
    public static String threshold;
    boolean setThreshold = false;



    private TempViewModel tempViewModel;

    public static boolean active = false;

    DataLevelReceiver receiver;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        getActivity().registerReceiver(receiver, new IntentFilter("sensorVal"));  //<----Register

//        Intent serviceIntent = new Intent(tempService.class.getName());
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
                String tempSensorData = (String)intent.getSerializableExtra("SENSOR");
//                setSensorTxtBox(tempSensorData);
                TextView sensorValueTxt = (TextView) getActivity().findViewById(R.id.tempSensorDataTxt);
                sensorValueTxt.setText(tempSensorData);
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tempViewModel =
                new ViewModelProvider(this).get(TempViewModel.class);
        View root = inflater.inflate(R.layout.fragment_temp_data, container, false);


        mPostReferenceTempConnectionStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections").child("TempSensor");  //LISTENER OBJECT
        mPostReferenceTempSampleInterval = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromApp").child("TempSensor");  //LISTENER OBJECT
        mPostReferenceTempThreshold = FirebaseDatabase.getInstance().getReference().child(userId).child("internalAppData").child("thresholds").child("TempSensor");  //LISTENER OBJECT

        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        mDatabase.child(userId).child("internalAppData").child("thresholds").child("TempSensor").setValue("0");
        mDatabase.child(userId).child("dataFromApp").child("TempSensor").setValue("5");    //set sample interval in database

        Button setThresholdButton = (Button) root.findViewById(R.id.setThreshold);
        setThresholdButton.setEnabled(false);
        Button thresholdONOFF = (Button) root.findViewById(R.id.thresholdOnOff);
        EditText tempThresholdText = (EditText) root.findViewById(R.id.TempThresholdText);

        TextView connectionTempText = (TextView) root.findViewById(R.id.textTempConnectionStatus);
        Button tempSampleIntervalButton = (Button) root.findViewById(R.id.buttonTempSampleInterval);

        //CONSTANT LISTENER CODE//
        ValueEventListener tempConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //on
                    connectionTempText.setText("Connected");   //change text so show temp sensor is connected
                    onOrOff = true; //temp sensor is on
                    tempSampleIntervalButton.setEnabled(true); //allow user to press button to change sample rate
                    thresholdONOFF.setEnabled(true);
                }
                else if(onOff == 0) {   //off
                    connectionTempText.setText("Not Connected");   //change text to show temp sensor is not connected
                    onOrOff = false;    //temp sensor is off
                    tempSampleIntervalButton.setEnabled(false);    //do NOT allow user to press button to change sample rate
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
        mPostReferenceTempConnectionStatus.addValueEventListener(tempConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        //END CONSTANT LISTENER CODE//

        //CONSTANT LISTENER CODE//
        EditText tempIntervalText = (EditText) root.findViewById(R.id.textTempDataSampleInterval);
        ValueEventListener tempIntervalConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                tempIntervalText.setText(dataSnapshot.getValue().toString());
                sampleInterval = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceTempSampleInterval.addValueEventListener(tempIntervalConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        ValueEventListener tempThreshold = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                tempThresholdText.setText(dataSnapshot.getValue().toString());
                threshold = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceTempThreshold.addValueEventListener(tempThreshold);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)

        Button tempSampleButton = (Button) root.findViewById(R.id.buttonTempSampleInterval);

        tempSampleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
//                sensorGrabTime = Integer.parseInt(tempIntervalText.getText().toString());        //grabs the value in char form and converts it to an int if button was pressed FOR INTEGER VERSION, NOT STRING
                sensorGrabTime = tempIntervalText.getText().toString();
                if(isNumeric(sensorGrabTime) && !"0".equals(sensorGrabTime))   //check if non zero integer was entered
                    mDatabase.child(userId).child("dataFromApp").child("TempSensor").setValue(sensorGrabTime);    //set sample interval in database
                else    //invalid entry
                    tempIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value

                TempUtils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });







        setThresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempThreshold;
                tempThreshold = tempThresholdText.getText().toString();
                if(isNumeric(tempThreshold) && !"0".equals(tempThreshold)) {   //check if non zero integer was entered
                    threshold = tempThreshold;
                    mDatabase.child(userId).child("internalAppData").child("thresholds").child("TempSensor").setValue(threshold);
                }
                //mDatabase.child(userId).child("dataFromApp").child("TempSensor").setValue(sensorGrabTime);    //set sample interval in database
                else {
                    tempThresholdText.setText(threshold);
                }   //invalid entry
                //tempIntervalText.setText(sampleInterval);      //sets the value in the txt box to the initial value


                TempUtils.hideKeyboard(getActivity());  //hide keyboard after button press
            }
        });




        thresholdONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setThreshold = !setThreshold;
                if(setThreshold == true){
                    setThresholdButton.setEnabled(true);
                    mDatabase.child(userId).child("internalAppData").child("thresholds").child("TempSensor").setValue("0");
                }
                else{
                    setThresholdButton.setEnabled(false);
                    mDatabase.child(userId).child("internalAppData").child("thresholds").child("TempSensor").setValue("0");
                }
            }
        });

        return root;
    }

//    public void setSensorTxtBox(String value){
//        TextView sensorValueTxt = (TextView) getActivity().findViewById(R.id.textTempConnectionStatus);
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