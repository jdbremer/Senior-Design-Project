package com.senior.sensor_controliotnetwork.ui.light;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class lightService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private DatabaseReference mPostReference;
    private HashMap<String, String> sensorValues = new HashMap<String, String>();
    public int inc = 0;
    public String value = "";



    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public void talkToGraph() {
            Intent i = new Intent();
            i.putExtra("MAPS", sensorValues);
            i.setAction("SensorMap");
            sendBroadcast(i);
        }

        public void talkToData() {
            Intent i = new Intent();
            i.putExtra("SENSOR", value);
            i.setAction("sensorVal");
            sendBroadcast(i);
        }



        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {



            try {
            //CONSTANT LISTENER CODE//
            ValueEventListener constantListener = new ValueEventListener(){
                @Override
                public void onDataChange (DataSnapshot dataSnapshot){
                    //LineGraphSeries<DataPoint> mSeries3 = new LineGraphSeries<>();

                    int maxGraphPoints = 11;
                    value = (String) dataSnapshot.getValue();
                    //add data to a hash table
                    sensorValues.put(String.valueOf(inc), value );


                    //if the max number of points was reached
                    if(inc >= maxGraphPoints){

                        //shift all the data within the hash table
                        for ( int i = 0; i < maxGraphPoints+1 ; i++){
                            if(i == 0){

                                sensorValues.remove(String.valueOf(i));
                            }
                            else{
                                String key = sensorValues.get(String.valueOf(i));
                                sensorValues.remove(String.valueOf(i));
                                sensorValues.put(String.valueOf(i-1), key);
                            }
                        }

                        if (GraphFragment.active) {
                            //DO STUFF
                            talkToGraph();
                        }
                        if (DataFragment.active) {
                            //DO STUFF
                            talkToData();
                        }
                    }
                    else{
                        inc++;
                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError error){
                    //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    System.out.println("The read failed: " + error.getMessage());
                }
            };
            //END CONSTANT LISTENER CODE//
            mPostReference.addValueEventListener(constantListener);


            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

                while(true) {
                    Thread.sleep(1000);
                    if (GraphFragment.active) {
                        //DO STUFF
                        talkToGraph();
                    }
                    if (DataFragment.active) {
                        //DO STUFF
                        talkToData();
                    }
                }

            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        mPostReference = FirebaseDatabase.getInstance().getReference().child("dataFromChild").child("LightSensor");  //LISTENER OBJECT
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, " light service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        //mPostReference = FirebaseDatabase.getInstance().getReference().child("dataFromChild").child("LightSensor");  //LISTENER OBJECT
        //GraphFragment test = (GraphFragment) getSupportFragmentManager().findFragmentByTag("testID");


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "light service done", Toast.LENGTH_SHORT).show();
    }

    public static boolean isNumeric(String strNum) {    //check if a string is a number
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}


