package com.senior.sensor_controliotnetwork.ui.connections;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.senior.sensor_controliotnetwork.ui.light.GraphFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class ConnectionsFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList = new ArrayList<String>();

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReference;

    private static Context context;

    private ConnectionsViewModel connectionsViewModel;


    ConnectionLevelReceiver receiver;
    public  static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        receiver = new ConnectionLevelReceiver();
        getActivity().registerReceiver(receiver, new IntentFilter("CONNECTIONS"));  //<----Register

    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
        getActivity().unregisterReceiver(receiver);           //<-- Unregister to avoid memoryleak
    }


    class ConnectionLevelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("CONNECTIONS"))
            {
                ArrayList<String> connectionList = (ArrayList<String>)intent.getSerializableExtra("connectionsArray");
                populateUI(connectionList);
            }

        }


    }

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


        connections.setAdapter(adapter);

        return root;
    }



    public void populateUI(ArrayList connections){
        Iterator<String> connects = connections.iterator();
        while(connects.hasNext()) {
            String currentVal = connects.next();
            if(arrayList.contains(currentVal)){

            }
            else{
                arrayList.add(currentVal);
            }
        }

        Iterator<String> currentConnects = arrayList.iterator();
        ArrayList<String> found = new ArrayList<>();
        while(currentConnects.hasNext()){
            String currentVal = currentConnects.next();
            if(connections.contains(currentVal)){

            }
            else{
                found.add(currentVal);
            }
        }

        Iterator<String> foundElements = found.iterator();
        while(foundElements.hasNext()){
            String currentVal = foundElements.next();
            if(arrayList.contains(currentVal)){
                arrayList.remove(currentVal);
            }
        }
        adapter.notifyDataSetChanged();

    }
}