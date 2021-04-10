package com.senior.sensor_controliotnetwork.ui.connections;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.service.controls.Control;
import android.view.Menu;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
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


import com.senior.sensor_controliotnetwork.MainActivity;
import com.senior.sensor_controliotnetwork.R;
import com.senior.sensor_controliotnetwork.ui.controlSwitch.ControlSwitchFragment;
import com.senior.sensor_controliotnetwork.ui.light.GraphFragment;
import com.senior.sensor_controliotnetwork.ui.light.LightFragment;
import com.senior.sensor_controliotnetwork.ui.light.lightService;
import com.senior.sensor_controliotnetwork.ui.microphone.microphoneService;
import com.senior.sensor_controliotnetwork.ui.temp.tempService;
import com.senior.sensor_controliotnetwork.ui.microphone.MicrophoneDataFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class ConnectionsFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList = new ArrayList<String>();

    FirebaseUser user;
    String userId;

    public MainActivity main = new MainActivity();
    public NavigationView navigationView;
    public DatabaseReference mDatabase;
    private DatabaseReference mPostReference;
    public DrawerLayout mDrawer;
    private static Context context;

    private ConnectionsViewModel connectionsViewModel;

    Intent lightIntent;
    Intent micIntent;
    Intent tempIntent;

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
        //check to see if the user was already signed in
        FirebaseUser user_loggedIn = FirebaseAuth.getInstance().getCurrentUser();
        if (user_loggedIn != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            userId = user.getUid();  //assign userId the token for the user
            mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections");  //LISTENER OBJECT
            mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT
        } else {
        }
//        mPostReference = FirebaseDatabase.getInstance().getReference().child(userId).child("Connections");  //LISTENER OBJECT
//        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT

        ListView connections = root.findViewById(R.id.connectionsList);

        EditText connectionsFilter = (EditText) root.findViewById(R.id.searchConnections);

        lightIntent = new Intent(getContext(), lightService.class);
        micIntent = new Intent(getContext(), microphoneService.class);
        tempIntent = new Intent(getContext(), tempService.class);

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


//code for using the connections list to change the fragment
//        connections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                position = position;
//                String newtest = arrayList.get(position);
////               // Menu menu = navigationView.getMenu();
////                navigationView.setCheckedItem(R.id.nav_microphone);
////                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_microphone));
////                //navigationView.getMenu().getItem(0).setChecked(false);
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                LightFragment newGamefragment = new LightFragment();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.connectionFrag, newGamefragment).commit();
//            }
//        });

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