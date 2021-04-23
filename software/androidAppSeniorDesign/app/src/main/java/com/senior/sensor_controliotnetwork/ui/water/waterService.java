package com.senior.sensor_controliotnetwork.ui.water;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.sensor_controliotnetwork.R;

import java.util.HashMap;

public class waterService extends Service {
    private Looper serviceLooper;
    private String threshold;
    private Float thresholdFloat = 0.0f;
    private ServiceHandler serviceHandler;
    private DatabaseReference mPostReference;
    private DatabaseReference mPostReferenceTempThreshold;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user
    private HashMap<Integer, String> sensorValues = new HashMap<Integer, String>();;
    public int inc = 0;
    public String value = "";
    public String thresholdMetValue = "";
    public boolean graphActive = false;





    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public void talkToGraph() {
//            Intent i = new Intent();
//            i.putExtra("MAPS", sensorValues);
//            i.setAction("SensorMap");
//            sendBroadcast(i);
        }

        public void talkToData() {
//            Intent i = new Intent();
//            i.putExtra("SENSOR", value);
//            i.setAction("sensorVal");
//            sendBroadcast(i);
        }



        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            //sensorValues = new HashMap<Integer, String>();
            sensorValues.clear();
            inc = 0;


            try {
                //mPostReferenceTempThreshold.setValue("0");
                //CONSTANT LISTENER CODE//
                ValueEventListener constantListener = new ValueEventListener(){
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot){
                        //LineGraphSeries<DataPoint> mSeries3 = new LineGraphSeries<>();

                        int maxGraphPoints = 26;
                        value = (String) dataSnapshot.getValue();

                        //send notification that the temp data is over the threshold value
                        float valueFloat = Float.parseFloat(value);
                        if(thresholdFloat < valueFloat && thresholdFloat != 0.0){
                            thresholdMetValue = value;
                            sendNotification(thresholdMetValue);
                        }
                        if(valueFloat > 0.0) {
                            //add data to a hash table
                            sensorValues.put(inc, value);


                            //if the max number of points was reached
                            if (inc >= maxGraphPoints) {

                                //shift all the data within the hash table
                                for (int i = 0; i < maxGraphPoints + 1; i++) {
                                    if (i == 0) {

                                        sensorValues.remove(i);
                                    } else {
                                        String key = sensorValues.get(i);
                                        sensorValues.remove(i);
                                        sensorValues.put(i - 1, key);
                                    }
                                }
                            } else {

                                inc++;

                            }
                            if (WaterGraphFragment.active) {
                                //DO STUFF
                                talkToGraph();
                            }
                            if (WaterDataFragment.active) {
                                //DO STUFF
                                talkToData();
                            }
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


                //CONSTANT LISTENER CODE//
                ValueEventListener thresholdListener = new ValueEventListener(){
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot){

                        threshold = (String) dataSnapshot.getValue();
                        thresholdFloat = Float.parseFloat(threshold);

                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                        //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        System.out.println("The read failed: " + error.getMessage());
                    }
                };
                //END CONSTANT LISTENER CODE//
                mPostReferenceTempThreshold.addValueEventListener(thresholdListener);


                // Normally we would do some work here, like download a file.
                // For our sample, we just sleep for 5 seconds.

                while(true) {
                    Thread.sleep(1000);
                    if (WaterGraphFragment.active && graphActive == false) {
                        //DO STUFF
                        // talkToGraph();
                        graphActive = true;
                    }
                    else if(!WaterGraphFragment.active) {
                        graphActive = false;
                    }
                    if (WaterDataFragment.active) {
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
        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("TempSensor");  //LISTENER OBJECT
        mPostReferenceTempThreshold = FirebaseDatabase.getInstance().getReference().child(userId).child("internalAppData").child("thresholds").child("TempSensor");  //LISTENER OBJECT
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("NotificationTempThreshold", "NotificationTempThreshold", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "temp service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);


        //mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("TempSensor");  //LISTENER OBJECT
        //GraphFragment test = (GraphFragment) getSupportFragmentManager().findFragmentByTag("testID");


        // If we get killed, after returning from here, don't restart
        // START_STICKY means that it restarts after being force quit
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "temp service done", Toast.LENGTH_SHORT).show();
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

    public void sendNotification(String tempData){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NotificationTempThreshold");
        builder.setContentTitle("Temp Threshold Met");
        String temp = String.format("%.2f", Float.parseFloat(thresholdMetValue));
        builder.setContentText("Threshold Value: " + thresholdFloat + " Temp Value: " + temp);
        builder.setSmallIcon(R.drawable.ic_menu_send);
        builder.setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1,builder.build());
    }
}