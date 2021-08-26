package com.senior.sensor_controliotnetwork.ui.water;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class WaterFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceWaterConnectionStatus;
    private DatabaseReference mPostReferenceWaterStatus;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    boolean onOrOff = false;

    private WaterViewModel waterViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        waterViewModel =
                new ViewModelProvider(this).get(WaterViewModel.class);
        View root = inflater.inflate(R.layout.fragment_water, container, false);


        mPostReferenceWaterConnectionStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections").child("WaterDetection");  //LISTENER OBJECT
        mPostReferenceWaterStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("WaterDetection");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        TextView waterConnectionText = (TextView) root.findViewById(R.id.textWaterConnectionStatus);

        //CONSTANT LISTENER CODE//
        ValueEventListener waterConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //if the connection was set to active within the database
                    waterConnectionText.setText("Connected");   //set the text to connected
                    onOrOff = true; //set the onOrOff bool to true, this is a global var that can be used elsewhere
                }
                else if(onOff == 0) { //if the connection was set to not active within the database
                    waterConnectionText.setText("Not Connected"); //set the text to not connected
                    onOrOff = false;    //set the onOrOff bool to false, this is a global var that can be used elsewhere
                    mDatabase.child(userId).child("dataFromApp").child("WaterDetection").setValue("0");    //send the value to the database child
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceWaterConnectionStatus.addValueEventListener(waterConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


        TextView waterStatusText = (TextView) root.findViewById(R.id.textWaterStatus);

        //CONSTANT LISTENER CODE//
        ValueEventListener waterStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int isWater = Integer.parseInt((String) dataSnapshot.getValue());
                if(isWater == 1){    //if there is a flood, not a bad idea to tell the user
                    waterStatusText.setText("Flooding Detected");
                }
                else{   //isWater = 0
                    waterStatusText.setText("None");
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceWaterStatus.addValueEventListener(waterStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//

        return root;
    }
}