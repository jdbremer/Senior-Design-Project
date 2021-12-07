package com.senior.sensor_controliotnetwork.ui.fire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class FireFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceFireConnectionStatus;
    private DatabaseReference mPostReferenceFireStatus;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    boolean onOrOff = false;

    private FireViewModel fireViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fireViewModel =
                new ViewModelProvider(this).get(FireViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fire, container, false);


        mPostReferenceFireConnectionStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections").child("Fire");  //LISTENER OBJECT
        mPostReferenceFireStatus = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("Fire");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        TextView fireConnectionText = (TextView) root.findViewById(R.id.textFireConnectionStatus);

        //CONSTANT LISTENER CODE//
        ValueEventListener fireConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //if the connection was set to active within the database
                    fireConnectionText.setText("Connected");   //set the text to connected
                    onOrOff = true; //set the onOrOff bool to true, this is a global var that can be used elsewhere
                }
                else if(onOff == 0) { //if the connection was set to not active within the database
                    fireConnectionText.setText("Not Connected"); //set the text to not connected
                    onOrOff = false;    //set the onOrOff bool to false, this is a global var that can be used elsewhere
                    mDatabase.child(userId).child("dataFromApp").child("Fire").setValue("0");    //send the value to the database child
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceFireConnectionStatus.addValueEventListener(fireConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


        TextView fireStatusText = (TextView) root.findViewById(R.id.textFireStatus);

        //CONSTANT LISTENER CODE//
        ValueEventListener fireStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int isFire = Integer.parseInt((String) dataSnapshot.getValue());
                if(isFire == 1){    //if there is a fire, not a bad idea to tell the user
                    fireStatusText.setText("Fire Detected");
                }
                else{   //isFire = 0
                    fireStatusText.setText("None");
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceFireStatus.addValueEventListener(fireStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


        Button fireReset = (Button) root.findViewById(R.id.resetFire);

        fireReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                mDatabase.child(userId).child("dataFromApp").child("Fire").setValue("resetPI");    //set reset flag
            }
        });


        return root;
    }
}