package com.senior.sensor_controliotnetwork.ui.connections;

import android.graphics.Typeface;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionsFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList = new ArrayList<String>();

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReference;


    private ConnectionsViewModel connectionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        connectionsViewModel =
                new ViewModelProvider(this).get(ConnectionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_connections, container, false);


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


        //CONSTANT LISTENER CODE//
        //EditText receivedkey2 = (EditText) root.findViewById(R.id.receivedKey2);
        ValueEventListener constantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                 Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                while (iter.hasNext()){
                    DataSnapshot snap = iter.next();
                    String nodId = snap.getKey();
                    int onOff = Integer.parseInt((String) snap.getValue());
                    if(onOff == 1){
                        addingToList(nodId);
                    }
                    else{
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
        mPostReference.addValueEventListener(constantListener);  //Uncomment this to start the continous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


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



    public void removeFromList(String s){

        if(arrayList.contains(s)){
            arrayList.remove(s);
            adapter.notifyDataSetChanged();
        }

    }


    public void addingToList(String s){

        if(!arrayList.contains(s)){
            arrayList.add(s);
            adapter.notifyDataSetChanged();
        }


    }
}