package com.senior.DesignApp.ui.home;
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


public class modifyUIText {
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