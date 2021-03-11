package com.senior.sensor_controliotnetwork.ui.controlSwitch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.senior.sensor_controliotnetwork.MainActivity;
import com.senior.sensor_controliotnetwork.R;
import com.senior.sensor_controliotnetwork.ui.connections.ConnectionsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ControlSwitchFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceControlSwitchConnectionStatus;
    private DatabaseReference mPostReferenceControlSwitchStatus;

    boolean onOrOff = false;
    int relay1_onOff = 0;
    int relay2_onOff = 0;
    //ConnectionsFragment connectFrag = new ConnectionsFragment();

    private ControlSwitchViewModel controlSwitchViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controlSwitchViewModel =
                new ViewModelProvider(this).get(ControlSwitchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_control_switch, container, false);


        mPostReferenceControlSwitchConnectionStatus = FirebaseDatabase.getInstance().getReference().child("Connections").child("ControlSwitch");  //LISTENER OBJECT
        mPostReferenceControlSwitchStatus = FirebaseDatabase.getInstance().getReference().child("dataFromChild").child("ControlSwitch");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT


        TextView controlSwitchConnectionText = (TextView) root.findViewById(R.id.textControlSwitchConnectionStatus);
        Button controlSwitch1OnButton = (Button) root.findViewById(R.id.controlSwitch1ButtonOn);
        Button controlSwitch1OffButton = (Button) root.findViewById(R.id.controlSwitch1ButtonOff);
        Button controlSwitch2OnButton = (Button) root.findViewById(R.id.controlSwitch2ButtonOn);
        Button controlSwitch2OffButton = (Button) root.findViewById(R.id.controlSwitch2ButtonOff);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){
                    controlSwitchConnectionText.setText("Connected");
                    onOrOff = true;
                    controlSwitch1OnButton.setEnabled(true);
                    controlSwitch1OffButton.setEnabled(true);
                    controlSwitch2OnButton.setEnabled(true);
                    controlSwitch2OffButton.setEnabled(true);
                }
                else if(onOff == 0) {
                    controlSwitchConnectionText.setText("Not Connected");
                    onOrOff = false;
                    controlSwitch1OnButton.setEnabled(false);
                    controlSwitch1OffButton.setEnabled(false);
                    controlSwitch2OnButton.setEnabled(false);
                    controlSwitch2OffButton.setEnabled(false);
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceControlSwitchConnectionStatus.addValueEventListener(controlSwitchConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


        TextView controlSwitch1StatusText = (TextView) root.findViewById(R.id.textControlSwitch1Status);
        TextView controlSwitch2StatusText = (TextView) root.findViewById(R.id.textControlSwitch2Status);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                String onOffString = (String) dataSnapshot.getValue();
                String[] onOff = onOffString.split("~");
                int onOff1 = Integer.parseInt(onOff[0]);
                int onOff2 = Integer.parseInt(onOff[1]);
                if(onOff1 == 1){
                    controlSwitch1StatusText.setText("ON");
                }
                else if(onOff1 == 0) {
                    controlSwitch1StatusText.setText("OFF");
                }

                if(onOff2 == 1){
                    controlSwitch2StatusText.setText("ON");
                }
                else if(onOff2 == 0) {
                    controlSwitch2StatusText.setText("OFF");
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceControlSwitchStatus.addValueEventListener(controlSwitchStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


        Button switch1ButtonOn = (Button) root.findViewById(R.id.controlSwitch1ButtonOn);

        switch1ButtonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay1_onOff = 1;
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff;
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue(sending_msg);    //set ControlSwitch to 1 or "on" in database
                }
            }
        });

        Button switch1ButtonOff = (Button) root.findViewById(R.id.controlSwitch1ButtonOff);

        switch1ButtonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay1_onOff = 0;
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff;
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue(sending_msg); //set ControlSwitch to 0 or "off" in database
                }
            }
        });


        Button switch2ButtonOn = (Button) root.findViewById(R.id.controlSwitch2ButtonOn);

        switch2ButtonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay2_onOff = 1;
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff;
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue(sending_msg);    //set ControlSwitch to 1 or "on" in database
                }
            }
        });


        Button switch2ButtonOff = (Button) root.findViewById(R.id.controlSwitch2ButtonOff);

        switch2ButtonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay2_onOff = 0;
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff;
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue(sending_msg); //set ControlSwitch to 0 or "off" in database
                }
            }
        });

        return root;
    }
}