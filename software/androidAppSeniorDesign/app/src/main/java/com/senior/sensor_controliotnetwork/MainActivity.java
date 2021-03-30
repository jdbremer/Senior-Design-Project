package com.senior.sensor_controliotnetwork;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senior.sensor_controliotnetwork.ui.connections.ConnectionsFragment;
import com.senior.sensor_controliotnetwork.ui.connections.ConnectionsViewModel;
import com.senior.sensor_controliotnetwork.ui.light.lightService;
import com.senior.sensor_controliotnetwork.ui.connections.connectionsService;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static kotlin.jvm.internal.Reflection.function;

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
    private static final int RC_SIGN_IN = 123;
    private ConnectionListReceiver connectionListReceiver;
    private String contents;
    private boolean initialize = false;


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

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




        //check to see if the user was already signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent lightIntent = new Intent(this, lightService.class);
            this.startService(lightIntent);
            Intent connectionIntent = new Intent(this, connectionsService.class);
            this.startService(connectionIntent);
        } else {
            createSignInIntent(); //initiate the sign on screen
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user.getUid();
                Intent lightIntent = new Intent(this, lightService.class);
                this.startService(lightIntent);
                Intent connectionIntent = new Intent(this, connectionsService.class);
                this.startService(connectionIntent);
            } else {
                // Sign in failed
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_Out: //if the user clicks the sign out button, do something
                signOutIntent();
                return true;
            case R.id.alexa_Linking:
                doApptoApp();
            case R.id.child_Connect:
                Fragment someFragment = new ChildConnect();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.child_connect, someFragment ); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doApptoApp() {

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

    public void createSignInIntent() { //firebase sign on intent, this will open a new screen and allow the user to sign in

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public void signOutIntent() { //firebase sign out intent, this will open the login screen again since the user signed out
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        createSignInIntent();
                    }
                });
    }


}