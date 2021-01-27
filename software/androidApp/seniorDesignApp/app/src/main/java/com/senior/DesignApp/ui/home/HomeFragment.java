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
        //modifyUIText modUIText = new modifyUIText();
//        modifyUIText.PrivatemodifyUIText mod = new modifyUIText.PrivatemodifyUIText();
        PrivatemodifyUIText mod = new PrivatemodifyUIText();
        sensorDataObj = (EditText) root.findViewById(R.id.sensorDataText);

//        Handler handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg){
//                if(msg.what == 0){
//                    updateUI();
//                }else{
//                    showError();
//                }
//            }
//        };

//        Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                sensorDataObj.setText(msg.toString());
//            }
//        };

        Runnable runnable = new Runnable(){
            public void run() {
                try {
                    Thread.sleep(1000*sensorGrabTime);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                sensorData = mDatabase.child("testObj").child("Key2").get().toString();
//                sensorDataObj.setText("yessir");
//                sensorDataObj.setText(sensorData, TextView.BufferType.EDITABLE);
//                sensorDataObj.textView.setText("Blah");
//                modUIText.
                mod.sendEmptyMessage(0);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

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


        //LISTENER CODE//
        EditText receivedkey2 = (EditText) root.findViewById(R.id.receivedKey2);
        ValueEventListener postListener = new ValueEventListener(){
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
        mPostReference.addValueEventListener(postListener);
        //END LISTENER CODE//

        return root;
    }
    private static class PrivatemodifyUIText extends Handler {
        public String sensorData;
        HomeFragment frag = new HomeFragment();
        public void handleMessage(Message msg) {
            if(msg.what == 0)
            {
                sensorData = frag.mDatabase.child("testObj").child("Key2").get().toString();
                frag.sensorDataObj.setText("sdolihnfaos");
            }
        }
    };
}