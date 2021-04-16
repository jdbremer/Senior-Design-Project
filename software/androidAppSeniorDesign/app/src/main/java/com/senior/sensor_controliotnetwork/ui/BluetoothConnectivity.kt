package com.senior.sensor_controliotnetwork.ui

import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.multidex.MultiDex
import com.google.firebase.auth.FirebaseAuth
import com.senior.sensor_controliotnetwork.R
import com.senior.sensor_controliotnetwork.client_connect
import java.lang.Thread.sleep
import java.util.*

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val WRITE_READ_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb"

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothConnectivity : AppCompatActivity()  {

    //private var isScanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()
    private val handler = Handler(Looper.getMainLooper())

    lateinit var bluetoothGatt: BluetoothGatt
    lateinit var characteristic: BluetoothGattCharacteristic
    lateinit var characteristicss: BluetoothGattCharacteristic
    private var scanning = false
    private var ble_name_string = ""
    private var GattTableComplete = false
    private var mSending = true

    var wifiManager: WifiManager? = null


    //private val list = listOf<ScanResult>()


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connectivity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val bluetoothManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getSystemService(BluetoothManager::class.java)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter


        findViewById<Button>(R.id.backButton).setOnClickListener { view ->
           // close();
            finish();
        }

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }

        scanLeDevice();

        findViewById<Button>(R.id.startBLEScan).setOnClickListener {
            val loadingCircle = findViewById<View>(R.id.loadingPanel)
            loadingCircle.isVisible = true
            scanLeDevice();
        }

        findViewById<Button>(R.id.send_cred).setOnClickListener {
            val loadingCircle = findViewById<View>(R.id.loadingPanel)
            loadingCircle.isVisible = true
            //scanLeDevice();
            val ble_name = findViewById<View>(R.id.ble_spinner) as Spinner? //this is the spinner that houses the ble names
            ble_name_string = ble_name?.selectedItem.toString() //grab the selected name from the spinner
            //find the ble scan result from the name
            //this will also connect to the ble module
            //after connection a callback will take place which will tell the program whether or not it successfully
            //connected to the ble device
            findResultViaName(ble_name_string)
//            sendSequence(ble_name_string)

        }



        wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)



        val success = wifiManager!!.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
    }



    private fun scanSuccess() {
        val ssid_dropdown = findViewById<View>(R.id.ssid_spinner) as Spinner?
        var results = wifiManager!!.scanResults
        var str = arrayListOf<String>()
        for (item in results) {
            str.add(item.SSID.toString())
        }
        var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, str)
        ssid_dropdown?.adapter = adapter
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val ssid_dropdown = findViewById<View>(R.id.ssid_spinner) as Spinner?
        var results = wifiManager!!.scanResults
        var str = arrayListOf<String>()
        for (item in results) {
            str.add(item.SSID.toString())
        }
        var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, str)
        ssid_dropdown?.adapter = adapter
    }


    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 5000

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanLeDevice() {
        val bluetoothManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getSystemService(BluetoothManager::class.java)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
        val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
        bluetoothLeScanner?.let { scanner ->
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    scanner.stopScan(leScanCallback)
//                    finding()
//                    loadingPanel
                    val loadingCircle = findViewById<View>(R.id.loadingPanel)
                    loadingCircle.isVisible = false
                }, SCAN_PERIOD)
                scanning = true
                scanResults.clear()
                scanner.startScan(leScanCallback)
            } else {
                scanning = false
                scanner.stopScan(leScanCallback)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun finding(){
        var str = arrayListOf<String>()
        val ble_dropdown = findViewById<View>(R.id.ble_spinner) as Spinner?
//        val textView = findViewById(R.id.device_name) as TextView
        for(item in scanResults){
            if(item.name != null){
                if(item.name.toString().contains("_IoT")) {  //make sure to change this to "_IoT"
                    str.add(item.name.toString())
                }
            }
        }
        if(str.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, str)
            ble_dropdown?.adapter = adapter
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun findResultViaName(name: String){
        for(item in scanResults){
            if(item.name != null){
                if(item.name.toString().contains(name)) {  //make sure to change this to "_IoT"
                    bluetoothGatt = item.connectGatt(this, false, gattCallback)
                    bluetoothGatt.requestMtu(512)
                }
            }
        }
    }


    //if the connection state changed, this callback will be called
    private val gattCallback = object : BluetoothGattCallback() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            close()
            scanLeDevice()
        }


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                    Log.w("BluetoothGattCallback", "Successfully connected to $")
                } else if (false) { /* Thought we might want to put something here, not sure yet */  }
            } else {  /* Thought we might want to put something here, not sure yet */ }
        }



        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                printGattTable()
            }
        }
    }


    /*

    Example of the Gatt Table for the HM-10 BLE Module


    I/printGattTable: Service 00001800-0000-1000-8000-00805f9b34fb
    Characteristics:
    |--00002a00-0000-1000-8000-00805f9b34fb
    |--00002a01-0000-1000-8000-00805f9b34fb
    |--00002a02-0000-1000-8000-00805f9b34fb
    |--00002a03-0000-1000-8000-00805f9b34fb
    |--00002a04-0000-1000-8000-00805f9b34fb
    Service 00001801-0000-1000-8000-00805f9b34fb
    Characteristics:
    |--00002a05-0000-1000-8000-00805f9b34fb
    Service 0000ffe0-0000-1000-8000-00805f9b34fb     <-- This is the Write/Read/Notify UUID SERVICE
    Characteristics:
    |--0000ffe1-0000-1000-8000-00805f9b34fb      <-- This is the Write/Read/Notify UUID CHARACTERISTIC


     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                    separator = "\n|--",
                    prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
        }
        findCharacteristic(WRITE_READ_UUID)?.let { characteristic = it }

        GattTableComplete = true
        //sendSequence()
        sendingSequence()
    }


    fun BluetoothGatt.findCharacteristic(uuid: String): BluetoothGattCharacteristic? {
        services?.forEach { service ->
            service.characteristics?.firstOrNull { characteristic ->
                characteristic.uuid.toString() == uuid
            }?.let { matchingCharacteristic ->
                Log.i("findcharacteristics", "\nService ${service.uuid}\nCharacteristics:\n$matchingCharacteristic")
                return matchingCharacteristic
            }
        }
        Log.i("findcharacteristics", "\nCould not find Characteristics")
        return null
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun sendingSequence(){
        val ssid = findViewById<View>(R.id.ssid_spinner) as Spinner? //this is the spinner that houses the ble names
        var ssid_string = ssid?.selectedItem.toString() //grab the selected name from the spinner


        val password = findViewById<View>(R.id.editTxtPassword) as TextView? //this is the spinner that houses the ble names
        var password_string = password?.text.toString() //grab the selected name from the spinner

        val user_loggedIn = FirebaseAuth.getInstance().currentUser

        val byteArray = ("start\nssid:" + ssid_string + "\npassword:" + password_string + "\nuid:" + user_loggedIn?.uid.toString() + "\nstop\n").toByteArray()
        send(characteristic, byteArray)

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun send(charac: BluetoothGattCharacteristic, payload: ByteArray): Boolean {
        bluetoothGatt?.let { gatt ->
            if (charac != null) {
                charac.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                charac.value = payload//byteArrayOf(0x2E, 0x38)
                bluetoothGatt.writeCharacteristic(charac)
                Log.i("Bluetooth", "Sent DATA")
            }
        }
        return true
    }

    fun close() {
        bluetoothGatt.close()
    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if(!scanResults.contains(result.device)) {
                scanResults.add(result.device);
                finding() //load the ble spinner with new values
            }
        }
    }

}

