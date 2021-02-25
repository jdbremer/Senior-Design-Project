package com.senior.sensor_controliotnetwork.ui.connections;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.senior.sensor_controliotnetwork.MainActivity;
import com.senior.sensor_controliotnetwork.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ConnectionsFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList = new ArrayList<String>();

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReference;

    private static Context context;

    private ConnectionsViewModel connectionsViewModel;


    boolean justEntered = false;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        connectionsViewModel =
                new ViewModelProvider(this).get(ConnectionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_connections, container, false);

        context = getContext();


        mPostReference = FirebaseDatabase.getInstance().getReference().child("Connections");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT




        ListView connections = root.findViewById(R.id.connectionsList);

        EditText connectionsFilter = (EditText) root.findViewById(R.id.searchConnections);

        adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,arrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position, convertView, parent);


                // Set the item text style to bold
                item.setTypeface(item.getTypeface(), Typeface.BOLD);

                // Change the item text size
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);





                // return the view
                return item;
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("NotificationCH", "NotificationCH", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }


        connectionsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


//        Collections.sort(arrayList, new Comparator<AppDetail>() {
//
//            /* This comparator will sort AppDetail objects alphabetically. */
//
//            @Override
//            public int compare(AppDetail a1, AppDetail a2) {
//
//                // String implements Comparable
//                return (a1.label.toString()).compareTo(a2.label.toString());
//            }
//        });


        //CONSTANT LISTENER CODE//
        ValueEventListener constantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                while (iter.hasNext()){
                    DataSnapshot snap = iter.next();
                    String nodId = snap.getKey();
                    int onOff = Integer.parseInt((String) snap.getValue());
                    if(onOff == 1 && !arrayList.contains(nodId)){
                        addingToList(nodId);
                    }
                    else if(onOff == 0 && arrayList.contains((nodId))){
                        removeFromList(nodId);
                    }
                    justEntered = false;
                }
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
                Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                while (iter.hasNext()){
                    DataSnapshot snap = iter.next();
                    String nodId = snap.getKey();
                    int onOff = Integer.parseInt((String) snap.getValue());
                    if(onOff == 1 && !arrayList.contains(nodId)){
                        initializeList(nodId);
                    }
                    else if(onOff == 0 && arrayList.contains(nodId)) {
                        removeFromList(nodId);
                    }
                }


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




        connections.setAdapter(adapter);

        return root;
    }



    public void removeFromList(String s){

        if(arrayList.contains(s)){
            arrayList.remove(s);
            //Collections.sort(arrayList);

            adapter.notifyDataSetChanged();

            //send notification that device has disconnected
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NotificationCH");
            builder.setContentTitle("Network Disconnection");
            builder.setContentText(s + " has disconnected");
            builder.setSmallIcon(R.drawable.ic_menu_send);
            builder.setAutoCancel(true);
//            if (isAdded()) {
//                getContext().getSystemService(Context.ALARM_SERVICE);
//            }
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            managerCompat.notify(1,builder.build());

        }


    }


    public void addingToList(String s){

        if(!arrayList.contains(s)){
            arrayList.add(s);
            //Collections.sort(arrayList);
            adapter.notifyDataSetChanged();

            //send notification that device was added
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NotificationCH");
            builder.setContentTitle("Network Connection");
            builder.setContentText(s + " has connected");
            builder.setSmallIcon(R.drawable.ic_menu_send);
            builder.setAutoCancel(true);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            managerCompat.notify(1,builder.build());
        }
    }

    public void initializeList(String s){
        if(!arrayList.contains(s)) {
            arrayList.add(s);
            //Collections.sort(arrayList);
            adapter.notifyDataSetChanged();
        }
    }
}