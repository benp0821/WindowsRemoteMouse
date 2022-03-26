package com.ben.mousecontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    SocketClient client;
    Thread thread;
    Thread findThread;
    volatile String ip, connectionType = null;
    int scanCounter;
    boolean previewImage;
    AlertDialog scanAlertDialog;
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    boolean bluetoothFailure = false;
    HashMap<String, String> bluetoothDevices = new HashMap<>();
    ArrayList<String> bluetoothDeviceNames = new ArrayList<>();
    boolean bluetoothDiscovery = false;
    boolean wifiScanning = false;
    boolean wifiManualEntry = false;
    BroadcastReceiver receiver = null;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean wifiOrBluetoothChoice = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ben.mousecontrol.R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        KeyboardInterpreter.startKeyListener(this);
        GestureInterpreter.startGestureInterpreter(this);
        ArrowButtonInterpreter.startArrowButtonInterpreter(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        if (connectionType.equals("Bluetooth")){
            MenuItem mi = menu.findItem(R.id.connType);
            mi.setTitle("Switch to WiFi");

            MenuItem scan = menu.findItem(R.id.scan);
            scan.setVisible(false);
            MenuItem manual = menu.findItem(R.id.manual);
            manual.setVisible(false);
        }else if (connectionType.equals("WiFi")){
            MenuItem mi = menu.findItem(R.id.connType);
            mi.setTitle("Switch to Bluetooth");

            MenuItem scan = menu.findItem(R.id.scan);
            scan.setVisible(true);
            MenuItem manual = menu.findItem(R.id.manual);
            manual.setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
        previewImage = pref.getBoolean("previewImage", true);

        MenuItem previewImageBtn = menu.findItem(R.id.show_image);
        previewImageBtn.setChecked(previewImage);

        ImageView cursor = findViewById(R.id.cursor);

        if (previewImageBtn.isChecked()){
            cursor.setVisibility(ImageView.VISIBLE);
        }else{
            cursor.setVisibility(ImageView.INVISIBLE);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void manualIPEntered(EditText input, AlertDialog dialog){
        if (!input.getText().toString().equals("")) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            dialog.dismiss();

            wifiManualEntry = false;

            SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
            SharedPreferences.Editor editor = pref.edit();
            ip = input.getText().toString();
            editor.putString("ipAddr", ip);
            editor.putString("connectionType", "WiFi");
            editor.apply();

            endNetworkingTasks();

            client = new SocketClient(this, ip, 8888); //192.168.1.8

            thread = new Thread(client);
            thread.start();
        }
    }

    public void enterManualIP(){
        wifiManualEntry = true;
        wifiScanning = false;

        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
        CustomKeyboard.keyboardPinned = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter IP: ");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.manual_ip_popup, findViewById(R.id.layout), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        input.setText(ip);
        input.setSelection(ip.length());
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            wifiManualEntry = false;

            dialog.cancel();
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface d) {
                //Hides keyboard when area outside of manual ip entry dialog box is touched (not cancel button).
                //Code doesn't make sense in this context, but it works when the other method of hiding keyboard does not for whatever reason.
                hiddenKeyBuffer.postDelayed(() -> CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false), 100);

                wifiManualEntry = false;
            }
        });

        dialog.setOnShowListener(dialog1 -> {
            Button b = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view -> {
                manualIPEntered(input, dialog);
            });
        });

        if (dialog.getWindow() != null){
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                manualIPEntered(input, dialog);
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        CustomKeyboard.keyboardPinned = false;
        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);

        switch (item.getItemId()) {
            case R.id.connType:
                bluetoothFailure = false;
                bluetoothDiscovery = false;
                if (connectionType.equals("WiFi")){
                    endNetworkingTasks();
                    startBluetoothOnResume();

                    connectionType = "Bluetooth";
                    editor.putString("connectionType", "");
                }else if (connectionType.equals("Bluetooth")){
                    endNetworkingTasks();
                    startWifiOnResume();

                    connectionType = "WiFi";
                    editor.putString("connectionType", "");
                }

                editor.apply();

                invalidateOptionsMenu();
                break;
            case R.id.scan:
                if (findThread == null || !findThread.isAlive()) {
                    endNetworkingTasks();
                    scanCounter = 0;
                    FindIP finderClass = new FindIP(this);
                    findThread = new Thread(finderClass);
                    findThread.start();
                }else{
                    scanCounter = 0;
                }
                break;
            case R.id.manual:
                enterManualIP();
            case R.id.show_image:
                item.setChecked(!item.isChecked());

                editor.putBoolean("previewImage", item.isChecked());
                previewImage = item.isChecked();

                ImageView cursor = findViewById(R.id.cursor);

                if (item.isChecked()){
                    cursor.setVisibility(ImageView.VISIBLE);
                }else{
                    cursor.setVisibility(ImageView.INVISIBLE);
                }

                editor.apply();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            int index = 0;
            HashMap<String, Integer> PermissionsMap = new HashMap<String, Integer>();
            for (String permission : permissions){
                PermissionsMap.put(permission, grantResults[index]);
                index++;
            }

            if((PermissionsMap.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)){
                AlertDialog.Builder failure = new AlertDialog.Builder(this);
                failure.setTitle("Error");
                failure.setMessage("Failed to Find Bluetooth Devices");
                failure.setPositiveButton("OK", null);

                failure.setCancelable(false);
                failure.create().show();

                bluetoothFailure = true;
            }else{
                findBluetoothDevice();
            }
        }
    }

    public void findBluetoothDevice(){
        bluetoothDiscovery = true;

        bluetoothDevices.clear();
        bluetoothDeviceNames.clear();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, bluetoothDeviceNames);

        bluetoothAdapter.startDiscovery();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName() != null && !bluetoothDevices.containsKey(device.getName())) {
                        bluetoothDevices.put(device.getName(), device.getAddress());
                        bluetoothDeviceNames.add(device.getName());
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        findViewById(R.id.layout).post(() -> {
            TextView promptView = new TextView(this);
            promptView.setText("Available Bluetooth Devices");
            promptView.setTextSize(25);
            promptView.setTextColor(Color.BLACK);
            promptView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            ListPopupWindow popup = new ListPopupWindow(this);
            popup.setAdapter(arrayAdapter);
            popup.setAnchorView(findViewById(R.id.layout));
            android.view.Display display = ((android.view.WindowManager) Objects.requireNonNull(getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
            Point p = new Point();
            display.getSize(p);
            popup.setWidth((int)(p.x * 0.88));
            popup.setHeight((int)(p.y * 0.68));
            popup.setModal(true);
            popup.setHorizontalOffset((p.x - popup.getWidth()) / 2);
            popup.setPromptView(promptView);
            popup.show();
            popup.setOnDismissListener(() -> {
                bluetoothAdapter.cancelDiscovery();
            });
        });

        //TODO: Do this after bluetooth device selected
        //bluetoothAdapter.cancelDiscovery();

        /*SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        connectionType = "Bluetooth";
        editor.putString("connectionType", connectionType);
        editor.apply();*/
    }

    public void findBluetoothDeviceIfLocationEnabled(){
        if (!bluetoothDiscovery) {
            bluetoothDiscovery = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                findBluetoothDevice();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                findBluetoothDeviceIfLocationEnabled();
            }else if (resultCode == RESULT_CANCELED){
                bluetoothFailure = true;

                AlertDialog.Builder failure = new AlertDialog.Builder(this);
                failure.setTitle("Error");
                failure.setMessage("There was an error when attempting to enable Bluetooth.");
                failure.setPositiveButton("OK", null);

                failure.setCancelable(false);
                failure.create().show();
            }
        }
    }

    public void startBluetoothOnResume(){
        wifiOrBluetoothChoice = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            AlertDialog.Builder failure = new AlertDialog.Builder(this);
            failure.setTitle("Error");
            failure.setMessage("Bluetooth Not Supported On This Device");
            failure.setPositiveButton("OK", null);

            failure.setCancelable(false);
            failure.create().show();
        }else{
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                findBluetoothDeviceIfLocationEnabled();
            }
        }
    }

    public void startWifiOnResume(){
        wifiOrBluetoothChoice = false;

        SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
        if (ip == null || ip.equals("")) {
            ip = pref.getString("ipAddr", "");
        }
        if (ip.equals("") || ip == null) {
            if (!wifiManualEntry){
                wifiScanning = true;
                scanCounter = 0;
                FindIP finderClass = new FindIP(this);
                findThread = new Thread(finderClass);
                findThread.start();
            }

        } else {
            client = new SocketClient(this, ip, 8888); //192.168.1.8

            thread = new Thread(client);
            thread.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        KeyboardInterpreter.startupUpIgnore = true;
        KeyboardInterpreter.startUpBackspaceIgnore = true;

        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setText("////");
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);

        if (scanAlertDialog != null) {
            scanAlertDialog.dismiss();
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        if (connectionType == null) {
            connectionType = pref.getString("connectionType", "");
        }

        if (connectionType.equals("") && !bluetoothFailure && !bluetoothDiscovery && !wifiScanning && !wifiManualEntry){
            if (!wifiOrBluetoothChoice) {
                AlertDialog.Builder connTypeChoice = new AlertDialog.Builder(this);
                connTypeChoice.setTitle("Connection Type");
                connTypeChoice.setMessage("Do you want to Connect Via Bluetooth or WiFi?");
                connTypeChoice.setPositiveButton("Bluetooth", (dialog, which) -> {
                    connectionType = "Bluetooth";
                    editor.putString("connectionType", "");
                    editor.apply();
                    startBluetoothOnResume();
                });

                connTypeChoice.setNegativeButton("WiFi", (dialog, which) -> {
                    connectionType = "WiFi";
                    editor.putString("connectionType", "");
                    editor.apply();
                    startWifiOnResume();
                });
                connTypeChoice.setCancelable(false);
                connTypeChoice.create().show();
                wifiOrBluetoothChoice = true;
            }
        }else if (connectionType.equals("WiFi")) {
            startWifiOnResume();
        }else if (connectionType.equals("Bluetooth") && !bluetoothFailure)
        {
            startBluetoothOnResume();
        }

        if (CustomKeyboard.keyboardPinned){

            hiddenKeyBuffer.requestFocus();
            //delays so that keyboard will be able to reappear. If timeout is not long enough, visiblity is reverted back to false
            //and keyboardPinned is set to false.
            hiddenKeyBuffer.postDelayed(() -> CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, true), 100);

            Button pinBtn = findViewById(R.id.pinBtn);
            CustomKeyboard.toggleCustomKeyPressed(pinBtn, true);

            if (CustomKeyboard.keyCombo){
                Button comboBtn = findViewById(R.id.comboBtn);
                CustomKeyboard.toggleCustomKeyPressed(comboBtn, true);
            }
        }else{
            CustomKeyboard.keyCombo = false;
            TextView comboTextBox = findViewById(R.id.comboTextBox);
            comboTextBox.setVisibility(CustomKeyboard.keyCombo ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onPause(){
        endNetworkingTasks();

        super.onPause();
    }

    public void endNetworkingTasks(){
        if (!GestureInterpreter.mouseDragging.equals("false")) {
            SocketClient.addCommand("mouseDragEnd " + GestureInterpreter.mouseDragging);
            GestureInterpreter.mouseDragging = "false";

            Button leftClickBtn = findViewById(R.id.leftClickBtn);
            leftClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            Button rightClickBtn = findViewById(R.id.rightClickBtn);
            rightClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            Button midClickBtn = findViewById(R.id.midClickBtn);
            midClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        }


        if (receiver != null){
            try {
                unregisterReceiver(receiver);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        bluetoothAdapter.cancelDiscovery();

        try {
            thread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            findThread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            client.endNetworkingTasks();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abcPressed(View view) {
        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, true);
    }

    class FindIP implements Runnable {

        AppCompatActivity context;
        String ipAddr = null;

        FindIP(AppCompatActivity context){
            this.context = context;
        }

        String findIpAddress() {
            AlertDialog.Builder scanAlert  = new AlertDialog.Builder(context);
            scanAlert.setTitle("Scanning");
            scanAlert.setPositiveButton("Connect Manually", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enterManualIP();
                    scanCounter = 1000;
                }
            });

            scanAlert.setNegativeButton("Try Again", null); //listener added below, as the button should not close the dialog.
            scanAlert.setCancelable(true);

            scanAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    scanCounter = 1000;
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanAlertDialog = scanAlert.create();
                }
            });

            while (scanCounter < 255) {

                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo;
                if (wm != null) {
                    connectionInfo = wm.getConnectionInfo();

                    int ipAddress = connectionInfo.getIpAddress();

                    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                        ipAddress = Integer.reverseBytes(ipAddress);
                    }

                    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

                    try {
                        ipAddr = InetAddress.getByAddress(ipByteArray).getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                String prefix;
                if (ipAddr != null) {
                    prefix = ipAddr.substring(0, ipAddr.lastIndexOf(".") + 1);
                } else {
                    prefix = "192.168.0.";
                }

                if (findThread.isInterrupted()){
                    return "";
                }

                Socket s = null;
                try {
                    ipAddr = prefix + String.valueOf(scanCounter);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (scanAlertDialog != null) {
                                    scanAlertDialog.setMessage("Scanning Network (" + ipAddr + "). Make sure the accompanying software is downloaded and running on the computer you wish to connect to (http:\\\\www.___.com), and ensure both devices are connected to the same WiFi network.");
                                    scanAlertDialog.show();

                                    scanAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            scanCounter = 0;
                                        }
                                    });
                                }
                            }
                        });

                    s = new Socket();
                    s.connect(new InetSocketAddress(ipAddr, 8888), 5000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scanAlertDialog.dismiss();

                            AlertDialog.Builder success  = new AlertDialog.Builder(context);
                            success.setTitle("Success");
                            success.setMessage("Connected Successfully to " + ipAddr);
                            success.setPositiveButton("OK", null);
                            success.create().show();
                        }
                    });
                    return ipAddr;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (s != null) {
                            s.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                scanCounter++;
            }

            if (scanCounter >= 1000){
                return "manual";
            }else{
                return null;
            }
        }

        @Override
        public void run() {
            String temp = findIpAddress();
            if (temp != null && !temp.equals("manual")) {
                if (thread == null || !thread.isAlive()) {
                    ip = temp;
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("ipAddr", ip);
                    editor.putString("connectionType", "WiFi");
                    editor.apply();

                    wifiScanning = false;
                    wifiManualEntry = false;

                    client = new SocketClient(context, ip, 8888);

                    thread = new Thread(client);
                    thread.start();
                }
            } else if (temp == null){
                scanAlertDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scanAlertDialog.dismiss();

                        AlertDialog.Builder failure = new AlertDialog.Builder(context);
                        failure.setTitle("Error");
                        failure.setMessage("Failed to Connect");
                        failure.setPositiveButton("Connect Manually", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                enterManualIP();
                            }
                        });

                        failure.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                scanCounter = 0;
                                FindIP finderClass = new FindIP(context);
                                findThread = new Thread(finderClass);
                                findThread.start();
                            }
                        });
                        failure.setCancelable(true);
                        failure.create().show();
                    }
                });
            }
        }
    }
}