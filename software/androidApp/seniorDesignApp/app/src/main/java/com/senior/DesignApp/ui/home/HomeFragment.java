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
import android.widget.TextView;

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
import com.google.firebase.quickstart.database.databinding.ActivityNewPostBinding;
import com.google.firebase.quickstart.database.java.models.Post;
import com.google.firebase.quickstart.database.java.models.User;

import com.senior.DesignApp.R;

public class HomeFragment extends Fragment {

    private DatabaseReference mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        //View v = inflater.inflate(R.layout.fragment_start, container, false);

        //Button button = (Button) View.OnClickListener();
        //FirebaseDatabase database = FirebaseDatabase.getInstance();

        Button b = (Button) root.findViewById(R.id.dataBaseSend);
        EditText key1 = (EditText) root.findViewById(R.id.Key1_text);

        Button b2 = (Button) root.findViewById(R.id.dataBaseSend2);
        EditText key2 = (EditText) root.findViewById(R.id.Key2_text);
        //Button button = (Button) findViewById(R.id.dataBaseSend);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                mDatabase.child("testObj").child("Key1").setValue(key1.getText().toString());
            }
        });


        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                // ...
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }

        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                mDatabase.child("testObj").child("Key2").setValue(key2.getText().toString());
            }
        });

        return root;
    }
}