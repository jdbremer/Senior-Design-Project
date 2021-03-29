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

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    boolean onOrOff = false;
    int relay1_onOff = 0;
    int relay2_onOff = 0;
    int onOff1 = 0;
    int onOff2 = 0;
    //ConnectionsFragment connectFrag = new ConnectionsFragment();

    private ControlSwitchViewModel controlSwitchViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controlSwitchViewModel =
                new ViewModelProvider(this).get(ControlSwitchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_control_switch, container, false);


        mPostReferenceControlSwitchConnectionStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections").child("ControlSwitch");  //LISTENER OBJECT
        mPostReferenceControlSwitchStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("ControlSwitch");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT


        TextView controlSwitchConnectionText = (TextView) root.findViewById(R.id.textControlSwitchConnectionStatus);
        //Relay1 on off buttons
        Button controlSwitch1OnButton = (Button) root.findViewById(R.id.controlSwitch1ButtonOn);
        Button controlSwitch1OffButton = (Button) root.findViewById(R.id.controlSwitch1ButtonOff);
        //Relay2 on off buttons
        Button controlSwitch2OnButton = (Button) root.findViewById(R.id.controlSwitch2ButtonOn);
        Button controlSwitch2OffButton = (Button) root.findViewById(R.id.controlSwitch2ButtonOff);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //if the connection was set to active within the database
                    controlSwitchConnectionText.setText("Connected");   //set the text to connected
                    onOrOff = true; //set the onOrOff bool to true, this is a global var that can be used elsewhere
                    //enable all the buttons (make the button active)
                    controlSwitch1OnButton.setEnabled(true);
                    controlSwitch1OffButton.setEnabled(true);
                    controlSwitch2OnButton.setEnabled(true);
                    controlSwitch2OffButton.setEnabled(true);
                }
                else if(onOff == 0) { //if the connection was set to not active within the database
                    controlSwitchConnectionText.setText("Not Connected"); //set the text to not connected
                    onOrOff = false;    //set the onOrOff bool to false, this is a global var that can be used elsewhere
                    //disable all the buttons (grey them out and make them not active)
                    controlSwitch1OnButton.setEnabled(false);
                    controlSwitch1OffButton.setEnabled(false);
                    controlSwitch2OnButton.setEnabled(false);
                    controlSwitch2OffButton.setEnabled(false);
                    //set both relays off, these are used to generate the sending string to the database, these are global
                    relay1_onOff = 0;
                    relay2_onOff = 0;
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff; //create the string to send the default val to the database
                    mDatabase.child(userId).child("dataFromApp").child("ControlSwitch").setValue(sending_msg);    //send the value to the control switch database child
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
                String[] onOff = onOffString.split("~"); //the value in the database is in this form -> 0~0, 1~0, 0~1, 1~1.. so we need to split the two values at the delimiter
                onOff1 = Integer.parseInt(onOff[0]); //grab the first val in the array
                if(onOff.length > 1) {  //if the length of the array is greater than 1, then two relays are found, so parse the second val
                    onOff2 = Integer.parseInt(onOff[1]);
                }
                else{
                    onOff2 = 0; //if the second relay is not found, keep it OFF
                }
                //if the relays are found to be on/off set the proper text
                if(onOff1 == 1){
                    relay1_onOff = 1;
                    controlSwitch1StatusText.setText("ON");
                }
                else if(onOff1 == 0) {
                    relay1_onOff = 0;
                    controlSwitch1StatusText.setText("OFF");
                }

                if(onOff2 == 1){
                    relay2_onOff = 1;
                    controlSwitch2StatusText.setText("ON");
                }
                else if(onOff2 == 0) {
                    relay2_onOff = 0;
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
                    relay1_onOff = 1;   //set relay1 to ON
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff; //generate string relay1~relay2
                    mDatabase.child(userId).child("dataFromApp").child("ControlSwitch").setValue(sending_msg);  //send value to database
                }
            }
        });

        Button switch1ButtonOff = (Button) root.findViewById(R.id.controlSwitch1ButtonOff);

        switch1ButtonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay1_onOff = 0; //set relay1 to OFF
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff; //generate string relay1~relay2
                    mDatabase.child(userId).child("dataFromApp").child("ControlSwitch").setValue(sending_msg); //send value to database
                }
            }
        });


        Button switch2ButtonOn = (Button) root.findViewById(R.id.controlSwitch2ButtonOn);

        switch2ButtonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay2_onOff = 1; //set relay2 to ON
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff; //generate string relay1~relay2
                    mDatabase.child(userId).child("dataFromApp").child("ControlSwitch").setValue(sending_msg);  //send value to database
                }
            }
        });


        Button switch2ButtonOff = (Button) root.findViewById(R.id.controlSwitch2ButtonOff);

        switch2ButtonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(onOrOff == true) {
                    relay2_onOff = 0; //set relay2 to OFF
                    String sending_msg  = relay1_onOff + "~" + relay2_onOff;  //generate string relay1~relay2
                    mDatabase.child(userId).child("dataFromApp").child("ControlSwitch").setValue(sending_msg); //send value to database
                }
            }
        });

        return root;
    }
}