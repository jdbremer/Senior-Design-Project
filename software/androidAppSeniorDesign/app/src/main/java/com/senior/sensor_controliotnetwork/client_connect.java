package com.senior.sensor_controliotnetwork;

import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.senior.sensor_controliotnetwork.ui.light.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;




//DEPRECATED CLASS//





public class client_connect extends Fragment {

    public WifiManager wifiManager;
    public Spinner ssid_dropdown;

    public static ArrayAdapter<String> adapter;
    public static ArrayList<String> str;

    //bluetooth
    String ssid;
    Button connectBtn;
    TextView ssid_password;
    TextView bluetoothPairFound;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;

    boolean found = false;

    public static client_connect newInstance() {
        return new client_connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        findBT();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //findBT();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.client_connect_fragment, container, false);

        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if(!found){
                    findBT();
                }
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        connectBtn = (Button) root.findViewById(R.id.uartConnect);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button send_client_creds = (Button) root.findViewById(R.id.send_client_creds);

        send_client_creds.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button uart_disc = (Button) root.findViewById(R.id.uartDisconnect);

        uart_disc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bluetoothPairFound = (TextView) root.findViewById(R.id.txt_blueDeviceFound);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getContext().registerReceiver(wifiScanReceiver, intentFilter);

        ssid_dropdown = (Spinner) root.findViewById(R.id.ssid_spinner);
        ssid_password = (TextView) root.findViewById(R.id.editTxtPassword);



        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }

        findBT();

        return root;
    }

    void closeBT() throws IOException
    {
        found = false;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        bluetoothPairFound.setText("Bluetooth Closed");
    }



    void sendData() throws IOException
    {
        FirebaseUser user_loggedIn = FirebaseAuth.getInstance().getCurrentUser();
        String msg = "start";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        msg = "ssid:";
        msg += ssid_dropdown.getSelectedItem().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        msg = "password:";
        msg += ssid_password.getText();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        msg = "uid:";
        msg += user_loggedIn.getUid();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        msg = "stop";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        bluetoothPairFound.setText("Data Sent");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        //beginListenForData();

        bluetoothPairFound.setText("Bluetooth Opened");
        beginListenForData();

    }



    void findBT() {
        //boolean found = true;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            bluetoothPairFound.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        //while(found){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("SERVER")) {
                        mmDevice = device;
                        bluetoothPairFound.setText("Bluetooth Device Found");
                        found = true;
                        return;
                    }
                }
                bluetoothPairFound.setText("No bluetooth adapter available");
            }
       // }
    }



    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Iterator<ScanResult> iter = results.iterator();
        str = new ArrayList<String>();
        while(iter.hasNext()){
            str.add(iter.next().SSID);
        }
        //String[] str = new String[]{results.get(0).SSID,results.get(1).SSID};
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, str);

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, str);
        ssid_dropdown.setAdapter(adapter);
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        Iterator<ScanResult> iter = results.iterator();
        str = new ArrayList<String>();
        while(iter.hasNext()){
            str.add(iter.next().SSID);
        }
        //String[] str = new String[]{results.get(0).SSID,results.get(1).SSID};
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, str);
        ssid_dropdown.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }


    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        found = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !found)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            bluetoothPairFound.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        found = true;
                    }
                }
            }
        });

        workerThread.start();
    }




}