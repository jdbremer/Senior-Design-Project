package com.senior.sensor_controliotnetwork.ui.connections;

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
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.sensor_controliotnetwork.R;
import com.senior.sensor_controliotnetwork.ui.light.GraphFragment;
import com.senior.sensor_controliotnetwork.ui.light.lightService;
import com.senior.sensor_controliotnetwork.ui.microphone.microphoneService;
import com.senior.sensor_controliotnetwork.ui.temp.tempService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class connectionsService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private DatabaseReference mPostReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();  //assign userId the token for the user

    public ArrayList<String> arrayList = new ArrayList<String>();
    public ArrayList<String> allServices = new ArrayList<String>();
    public ArrayList<String> temp = new ArrayList<String>();

    Intent lightIntent;
    Intent micIntent;
//    Intent controlSwitchIntent;
    Intent tempIntent;



    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public void talk() {
            Intent i = new Intent();
            i.putExtra("connectionsArray", arrayList);
            i.setAction("CONNECTIONS");
            sendBroadcast(i);
        }

        public void talkToMainActivity() {
            Intent i = new Intent();
            i.putExtra("connectionsArrayForMain", allServices);
            i.setAction("CONNECTIONS2MAIN");
            sendBroadcast(i);
        }



        public void turnOnService(String nodeId) {
            if(nodeId.contains("LightSensor")){
                getBaseContext().startService(lightIntent);
            }
            else if(nodeId.contains("dBMeter")){
                getBaseContext().startService(micIntent);
            }
//            else if(nodeId.contains("ControlSwitch")){
//                getBaseContext().startService(controlSwitchIntent);
//            }
            else if(nodeId.contains("TempSensor")){
                getBaseContext().startService(tempIntent);
            }
        }

        public void turnOffService(String nodeId) {
            if(nodeId.contains("LightSensor")){
                getBaseContext().stopService(lightIntent);
            }
            else if(nodeId.contains("dBMeter")){
                getBaseContext().stopService(micIntent);
            }
//            else if(nodeId.contains("ControlSwitch")){
//                getBaseContext().stopService(controlSwitchIntent);
//            }
            else if(nodeId.contains("TempSensor")){
                getBaseContext().stopService(tempIntent);
            }
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
                        allServices.clear();
                        Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                        while (iter.hasNext()){
                            DataSnapshot snap = iter.next();
                            String nodId = snap.getKey();
                            allServices.add(nodId);
                            int onOff = Integer.parseInt((String) snap.getValue());
                            if(onOff == 1 && !arrayList.contains(nodId)){
                                turnOnService(nodId);
                                addingToList(nodId);
                            }
                            else if(onOff == 0 && arrayList.contains((nodId))){
                                turnOffService(nodId);
                                removeFromList(nodId);
                                //mDatabase.child(userId).child("dataFromApp").child(nodId).setValue("0");
                            }
                            //justEntered = false;
                        }
                        talkToMainActivity();
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                        //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        System.out.println("The read failed: " + error.getMessage());
                    }
                };
                //END CONSTANT LISTENER CODE//

                //SINGLE LISTENER CODE//
                ValueEventListener singleListener = new ValueEventListener(){
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot){
                        allServices.clear();
                        Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                        while (iter.hasNext()){
                            DataSnapshot snap = iter.next();
                            String nodId = snap.getKey();
                            allServices.add(nodId);
                            int onOff = Integer.parseInt((String) snap.getValue());
                            if(onOff == 1 && !arrayList.contains(nodId)){
                                turnOnService(nodId);
                                initializeList(nodId);
                            }
                            else if(onOff == 0 && arrayList.contains(nodId)) {
                                turnOffService(nodId);
                                removeFromList(nodId);
                                //mDatabase.child(userId).child("dataFromApp").child(nodId).setValue("0");
                            }
                        }
                        talkToMainActivity();
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error){
                        //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        System.out.println("The read failed: " + error.getMessage());
                    }
                };
                mPostReference.addListenerForSingleValueEvent(singleListener); //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
                mPostReference.addValueEventListener(constantListener);
                //END SINGLE LISTENER CODE//


                // Normally we would do some work here, like download a file.
                // For our sample, we just sleep for 5 seconds.

                while(true) {
                    Thread.sleep(1000);
                    if (ConnectionsFragment.active) {
                        //DO STUFF
                        talk();
                    }
                    else {
                        //Whatever

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
        //mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("dataFromChild").child("LightSensor");  //LISTENER OBJECT
        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections");  //LISTENER OBJECT
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("NotificationCH", "NotificationCH", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        lightIntent = new Intent(getBaseContext(), lightService.class);
        micIntent = new Intent(getBaseContext(), microphoneService.class);
        tempIntent = new Intent(getBaseContext(), tempService.class);

        //getBaseContext().startService(lightIntent);

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "connections service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);



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
        Toast.makeText(this, "connections service done", Toast.LENGTH_SHORT).show();
    }

    public void removeFromList(String s){

        if(this.arrayList.contains(s)){
            this.arrayList.remove(s);
            //Collections.sort(arrayList);

           // adapter.notifyDataSetChanged();

            //send notification that device has disconnected
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NotificationCH");
            builder.setContentTitle("Network Disconnection");
            builder.setContentText(s + " has disconnected");
            builder.setSmallIcon(R.drawable.ic_menu_send);
            builder.setAutoCancel(true);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1,builder.build());

        }


    }


    public void addingToList(String s){

        if(!this.arrayList.contains(s)){
            this.arrayList.add(s);
            //Collections.sort(arrayList);
           // adapter.notifyDataSetChanged();

            //send notification that device was added
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NotificationCH");
            builder.setContentTitle("Network Connection");
            builder.setContentText(s + " has connected");
            builder.setSmallIcon(R.drawable.ic_menu_send);
            builder.setAutoCancel(true);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(1,builder.build());
        }
    }

    public void initializeList(String s){
        if(!this.arrayList.contains(s)) {
            this.arrayList.add(s);
            //Collections.sort(arrayList);
            //adapter.notifyDataSetChanged();
        }
    }

}
