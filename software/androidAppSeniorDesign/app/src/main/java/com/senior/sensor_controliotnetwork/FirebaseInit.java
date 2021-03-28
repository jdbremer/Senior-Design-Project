package com.senior.sensor_controliotnetwork;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseInit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
