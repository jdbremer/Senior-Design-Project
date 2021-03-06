//package com.senior.sensor_controliotnetwork.ui.light;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.senior.sensor_controliotnetwork.R;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link DataFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class DataFragment extends Fragment {
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public DataFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment DataFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static DataFragment newInstance(String param1, String param2) {
//        DataFragment fragment = new DataFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_light_data, container, false);
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//





package com.senior.sensor_controliotnetwork.ui.light;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.senior.sensor_controliotnetwork.R;

public class DataFragment extends Fragment {

    public DatabaseReference mDatabase;
    private DatabaseReference mPostReferenceLightConnectionStatus;
    private DatabaseReference mPostReferenceLightSampleInterval;

    boolean onOrOff = false;    //status of weather or not the light sensor is connected
    public int sensorGrabTime = 5;  //time in seconds for how often light sensor grabs new data
    public EditText intervalTxt;
    //ConnectionsFragment connectFrag = new ConnectionsFragment();

    private LightViewModel lightViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lightViewModel =
                new ViewModelProvider(this).get(LightViewModel.class);
        View root = inflater.inflate(R.layout.fragment_light_data, container, false);


        mPostReferenceLightConnectionStatus = FirebaseDatabase.getInstance().getReference().child("Connections").child("LightSensor");  //LISTENER OBJECT
        mPostReferenceLightSampleInterval = FirebaseDatabase.getInstance().getReference().child("Connections").child("LightSensor").child("LightSampleInterval");  //LISTENER OBJECT
        mDatabase = FirebaseDatabase.getInstance().getReference();  //DATABASE OBJECT


        TextView connectionLightText = (TextView) root.findViewById(R.id.textLightConnectionStatus);
        TextView controlLightText = (TextView) root.findViewById(R.id.textLightDataSampleInterval);
        Button lightSampleIntervalButton = (Button) root.findViewById(R.id.buttonLightSampleInterval);
        //CONSTANT LISTENER CODE//
        ValueEventListener controlSwitchConnectionStatusConstantListener = new ValueEventListener(){
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
                if(onOff == 1){ //on
                    connectionLightText.setText("Connected");   //change text so show light sensor is connected
                    onOrOff = true; //light sensor is on
                    lightSampleIntervalButton.setEnabled(true); //allow user to press button to change sample rate
                }
                else if(onOff == 0) {   //off
                    connectionLightText.setText("Not Connected");   //change text to show light sensor is not connected
                    onOrOff = false;    //light sensor is off
                    lightSampleIntervalButton.setEnabled(false);    //do NOT allow user to press button to change sample rate
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                System.out.println("The read failed: " + error.getMessage());
            }
        };
        mPostReferenceLightConnectionStatus.addValueEventListener(controlSwitchConnectionStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//


//        TextView controlSwitchStatusText = (TextView) root.findViewById(R.id.textControlSwitchStatus);
//        //CONSTANT LISTENER CODE//
//        ValueEventListener controlSwitchStatusConstantListener = new ValueEventListener(){
//            @Override
//            public void onDataChange (DataSnapshot dataSnapshot){
//                int onOff = Integer.parseInt((String) dataSnapshot.getValue());
//                if(onOff == 1){
//                    controlSwitchStatusText.setText("ON");
//                }
//                else if(onOff == 0) {
//                    controlSwitchStatusText.setText("OFF");
//                }
//            }
//
//            @Override
//            public void onCancelled (@NonNull DatabaseError error){
//                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//                System.out.println("The read failed: " + error.getMessage());
//            }
//        };
//        mPostReferenceControlSwitchStatus.addValueEventListener(controlSwitchStatusConstantListener);  //Uncomment this to start the continuous grab of updated data (runs code above, constant listener code)
        //END CONSTANT LISTENER CODE//

        intervalTxt = (EditText) root.findViewById(R.id.textLightDataSampleInterval);
        intervalTxt.setText(String.valueOf(sensorGrabTime));        //sets the value in the txt box to the initial value

        Button lightSampleButton = (Button) root.findViewById(R.id.buttonLightSampleInterval);

        lightSampleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorGrabTime = Integer.parseInt(intervalTxt.getText().toString());        //grabs the value in char form and converts it to an int if button was pressed
                mDatabase.child("dataFromApp").child("LightSensor").child("LightSampleInterval").setValue(sensorGrabTime);    //set sample interval in database
//                Utils.hideKeyboard(Activity DataFragment.this);
                Utils.hideKeyboard(getActivity());
            }
        });

        return root;
    }

//    private void closeKeyboard(){
//        View view = this.getCurrentFocus();
//        if(view != null){   //if a view is open
//            InputMethodManager inputMM = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//    }
}