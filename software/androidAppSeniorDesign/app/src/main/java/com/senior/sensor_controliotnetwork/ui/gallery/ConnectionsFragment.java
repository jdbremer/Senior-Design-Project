package com.senior.sensor_controliotnetwork.ui.gallery;

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

import com.senior.sensor_controliotnetwork.MainActivity;
import com.senior.sensor_controliotnetwork.R;

import java.util.ArrayList;

public class ConnectionsFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList = new ArrayList<String>();



    private ConnectionsViewModel connectionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        connectionsViewModel =
                new ViewModelProvider(this).get(ConnectionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        ListView connections = root.findViewById(R.id.connectionsList);

        EditText connecitonsFilter = (EditText) root.findViewById(R.id.searchConnections);

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

        connecitonsFilter.addTextChangedListener(new TextWatcher() {
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

        addingToList();

        return root;
    }



    public void addingToList(){

        arrayList.add("I");
        arrayList.add("Just");
        arrayList.add("Created");
        arrayList.add("A");
        arrayList.add("List");
        arrayList.add("LETS GOOO");
        arrayList.add("TEST CHILD NODE CONNECTION \nTime Connected: 15h:32m");

        arrayList.add("Can you SCROLL???");
        arrayList.add("Lets find out....");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("Adding entry");
        arrayList.add("YES YOU CAN SCROLL!!!!");
    }
}