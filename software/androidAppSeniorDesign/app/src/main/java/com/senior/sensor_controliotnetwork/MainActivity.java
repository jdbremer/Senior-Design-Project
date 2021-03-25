package com.senior.sensor_controliotnetwork;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.senior.sensor_controliotnetwork.ui.connections.ConnectionsFragment;
import com.senior.sensor_controliotnetwork.ui.connections.ConnectionsViewModel;
import com.senior.sensor_controliotnetwork.ui.light.lightService;
import com.senior.sensor_controliotnetwork.ui.connections.connectionsService;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    //public Menu navMenuLogIn;
    public MenuItem conn;
    public MenuItem light;
    public MenuItem control;
    public MenuItem mic;
    public NavigationView navigationView;
    public ArrayList<String> connectionList = new ArrayList<String>();
    public ArrayList<String> arrayList = new ArrayList<String>();
    public ArrayList<String> currentConnectionList = new ArrayList<String>();

    private ConnectionListReceiver connectionListReceiver;

    private boolean initialize = false;

    class ConnectionListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("CONNECTIONS2MAIN"))
            {
                connectionList = (ArrayList<String>)intent.getSerializableExtra("connectionsArrayForMain");
                initialize = true;
                invalidateOptionsMenu();
            }

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel = new NotificationChannel("NotificationCH", "NotificationCH", NotificationManager.IMPORTANCE_DEFAULT);
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        connectionListReceiver = new ConnectionListReceiver();
        registerReceiver(connectionListReceiver, new IntentFilter("CONNECTIONS2MAIN"));  //<----Register

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_connections, R.id.nav_light, R.id.nav_microphone, R.id.nav_controlSwitch)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        Intent lightIntent = new Intent(this, lightService.class);
        this.startService(lightIntent);
        Intent connectionIntent = new Intent(this, connectionsService.class);
        this.startService(connectionIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        // Have the connections item always active
        nav_Menu.findItem(R.id.nav_connections).setVisible(true);

        ///////ADD NEW CONNECTIONS HERE
        nav_Menu.findItem(R.id.nav_light).setVisible(false);
        nav_Menu.findItem(R.id.nav_controlSwitch).setVisible(false);
        nav_Menu.findItem(R.id.nav_microphone).setVisible(false);

        ///////

        if(initialize == true) {

            Iterator<String> connects = connectionList.iterator();
            while (connects.hasNext()) {
                String currentVal = connects.next();
                ///////ADD NEW CONNECTIONS HERE
                if (currentVal.equals("LightSensor")) {
                    nav_Menu.findItem(R.id.nav_light).setVisible(true);
                }
                else if (currentVal.equals("ControlSwitch")) {
                    nav_Menu.findItem(R.id.nav_controlSwitch).setVisible(true);
                }
                else if (currentVal.equals("dBMeter")) {
                    nav_Menu.findItem(R.id.nav_microphone).setVisible(true);
                }

                ///////
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        closeKeyboard();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){   //if a view is open
            InputMethodManager inputMM = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}