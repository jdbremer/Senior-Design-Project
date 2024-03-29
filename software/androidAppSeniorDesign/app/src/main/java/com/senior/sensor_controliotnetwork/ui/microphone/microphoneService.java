package com.senior.sensor_controliotnetwork.ui.microphone;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.senior.sensor_controliotnetwork.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.senior.sensor_controliotnetwork.ui.microphone.MicrophoneDataFragment.active;

public class microphoneService extends Service {
    private Looper serviceLooper;
    private String threshold;
    private Float thresholdFloat = 0.0f;
    private ServiceHandler serviceHandler;
    private DatabaseReference mPostReference;
    private DatabaseReference mPostReferenceMicThreshold;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user
    private HashMap<Integer, String> sensorValues = new HashMap<Integer, String>();
    public int inc = 0;
    public String value = "";
    public String thresholdMetValue = "";




    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public void talkToGraph() {
            Intent i = new Intent();
            i.putExtra("micMAPS", sensorValues);
            i.setAction("micSensorMap");
            sendBroadcast(i);
        }

        public void talkToData() {
            Intent i = new Intent();
            i.putExtra("micSENSOR", value);
            i.setAction("micSensorVal");
            sendBroadcast(i);
        }



        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {



            try {
                mPostReferenceMicThreshold.setValue("0");
                //CONSTANT LISTENER CODE//
                ValueEventListener constantListener = new ValueEventListener(){
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot){
                        //LineGraphSeries<DataPoint> mSeries3 = new LineGraphSeries<>();

                        int maxGraphPoints = 26;
                        value = (String) dataSnapshot.getValue();
                        try {
                            //send notification that the light data is over the threshold value
                            float valueFloat = Float.parseFloat(value);
                            if (thresholdFloat < valueFloat && thresholdFloat != 0.0) {
                                thresholdMetValue = value;
                                sendNotification(thresholdMetValue);
                            }

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

                                if (MicrophoneGraphFragment.active) {
                                    //DO STUFF
                                    talkToGraph();
                                }
                                if (active) {
                                    //DO STUFF
                                    talkToData();
                                }
                            } else {
                                inc++;
                            }
                        }
                        catch (Exception e){
                            System.out.println("Microphone Service: The incoming data failed");
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
                        try {
                            threshold = (String) dataSnapshot.getValue();
                            thresholdFloat = Float.parseFloat(threshold);
                        }
                        catch (Exception e){
                            System.out.println("The incoming data failed");
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                        //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        System.out.println("The read failed: " + error.getMessage());
                    }
                };
                //END CONSTANT LISTENER CODE//
                mPostReferenceMicThreshold.addValueEventListener(thresholdListener);


                // Normally we would do some work here, like download a file.
                // For our sample, we just sleep for 5 seconds.

                while(true) {
                    Thread.sleep(1000);
                    if (MicrophoneGraphFragment.active) {
                        //DO STUFF
                        talkToGraph();
                    }
                    if (active) {
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
        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("dBMeter");  //LISTENER OBJECT
        mPostReferenceMicThreshold = FirebaseDatabase.getInstance().getReference().child(userId).child("internalAppData").child("thresholds").child("dBMeter");  //LISTENER OBJECT
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("NotificationMicThreshold", "NotificationMicThreshold", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "dbMeter service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        //mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("LightSensor");  //LISTENER OBJECT
        //GraphFragment test = (GraphFragment) getSupportFragmentManager().findFragmentByTag("testID");


        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "dBMeter service done", Toast.LENGTH_SHORT).show();
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

    public void sendNotification(String lightData){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NotificationMicThreshold");
        builder.setContentTitle("dBMeter Threshold Met");
        String temp = String.format("%.2f", Float.parseFloat(thresholdMetValue));
        builder.setContentText("Threshold Value: " + thresholdFloat + " dBMeter Value: " + temp);
        builder.setSmallIcon(R.drawable.ic_menu_send);
        builder.setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1,builder.build());
    }
}