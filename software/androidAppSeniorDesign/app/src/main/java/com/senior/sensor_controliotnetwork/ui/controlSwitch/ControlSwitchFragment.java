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
        Button controlSwitchOnButton = (Button) root.findViewById(R.id.controlSwitchButtonOn);
        Button controlSwitchOffButton = (Button) root.findViewById(R.id.controlSwitchButtonOff);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){
                    controlSwitchConnectionText.setText("Connected");
                    onOrOff = true;
                    controlSwitchOnButton.setEnabled(true);
                    controlSwitchOffButton.setEnabled(true);
                }
                else if(onOff == 0) {
                    controlSwitchConnectionText.setText("Not Connected");
                    onOrOff = false;
                    controlSwitchOnButton.setEnabled(false);
                    controlSwitchOffButton.setEnabled(false);
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


        TextView controlSwitchStatusText = (TextView) root.findViewById(R.id.textControlSwitchStatus);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                String onOffString = (String) dataSnapshot.getValue();
                String[] onOff = onOffString.split("~");
                int onOff1 = Integer.parseInt(onOff[0]);
                int onOff0 = Integer.parseInt(onOff[1]);
                if(onOff1 == 1){
                    controlSwitchStatusText.setText("ON");
                }
                else if(onOff1 == 0) {
                    controlSwitchStatusText.setText("OFF");
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


        Button buttonOn = (Button) root.findViewById(R.id.controlSwitchButtonOn);

        buttonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue("1");    //set ControlSwitch to 1 or "on" in database
                }
            }
        });

        Button buttonOff = (Button) root.findViewById(R.id.controlSwitchButtonOff);

        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    mDatabase.child("dataFromApp").child("ControlSwitch").setValue("0"); //set ControlSwitch to 0 or "off" in database
                }
            }
        });

        return root;
    }
}