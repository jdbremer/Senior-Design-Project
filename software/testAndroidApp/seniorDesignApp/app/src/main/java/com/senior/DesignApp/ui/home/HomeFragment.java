package com.senior.DesignApp.ui.home;
//package com.google.firebase.quickstart.database.java;


import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.os.Handler;
import android.widget.TextView;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.quickstart.database.databinding.ActivityNewPostBinding;
//import com.google.firebase.quickstart.database.java.models.Post;
//import com.google.firebase.quickstart.database.java.models.User;

import com.senior.DesignApp.R;

public class HomeFragment extends Fragment {
    public int sensorGrabTime = 5;
    public String sensorData = "";
    public DatabaseReference mDatabase;
    private DatabaseReference mPostReference;
    public EditText sensorDataObj;
    public EditText intervalTxt;

    // HANDLER //
    //A handler will be needed if you were to update data on the UI while not doing a listen operation
    //This is what you would call -> updatingUI.post(UIUpdate);
    private final Handler updatingUI = new Handler();

    final Runnable UIUpdate = new Runnable() {
        public void run() {
            sensorDataObj.setText(sensorData);
        }
    };
    // END HANDLER //


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mPostReference = FirebaseDatabase.getInstance().getReference().child("testObj").child("Key2");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });


        //CONSTANT LISTENER CODE//
        EditText receivedkey2 = (EditText) root.findViewById(R.id.receivedKey2);
        ValueEventListener constantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                // Get Post object and use the values to update the UI
                //System.out.println(dataSnapshot.getValue());
                receivedkey2.setText(dataSnapshot.getValue().toString());

                // ...
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        //mPostReference.addValueEventListener(constantListener);  //Uncomment this to start the continous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//

        //SINGLE LISTEN CODE// //This code will allow a single listen... It will do a single grab of data and will have to be reinitialized again.
        ValueEventListener singleListen = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                // Get Post object and use the values to update the UI
                //System.out.println(dataSnapshot.getValue());
                sensorDataObj.setText(dataSnapshot.getValue().toString());

                // ...
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        //END SINGLE LISTEN CODE//

        Button timeUpdate = (Button) root.findViewById(R.id.updateTime);
        intervalTxt = (EditText) root.findViewById(R.id.updateTimeText);
        intervalTxt.setText(String.valueOf(sensorGrabTime));        //sets the value in the txt box to the initial value

        timeUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorGrabTime = Integer.parseInt(intervalTxt.getText().toString());        //grabs the value in char form and converts it to an int if button was pressed
            }
        });


        //THREAD CODE//
        sensorDataObj = (EditText) root.findViewById(R.id.sensorDataText);

        Runnable runnable = new Runnable(){
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000 * sensorGrabTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mPostReference.addListenerForSingleValueEvent(singleListen); //do a single grab of data
                    //updatingUI.post(UIUpdate);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        //END THREAD CODE//






        Button b = (Button) root.findViewById(R.id.dataBaseSend);
        EditText key1 = (EditText) root.findViewById(R.id.Key1_text);


        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                mDatabase.child("testObj").child("Key1").setValue(key1.getText().toString());
            }
        });

        Button b2 = (Button) root.findViewById(R.id.dataBaseSend2);
        EditText key2 = (EditText) root.findViewById(R.id.Key2_text);

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                mDatabase.child("testObj").child("Key2").setValue(key2.getText().toString());
            }
        });




        return root;
    }

}