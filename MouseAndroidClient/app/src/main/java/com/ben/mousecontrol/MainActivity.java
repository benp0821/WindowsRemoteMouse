package com.ben.mousecontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    SocketClient client;
    Thread thread;
    Thread findThread;
    volatile String ip;
    int scanCounter;
    boolean previewImage;
    AlertDialog scanAlertDialog;

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

            SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
            SharedPreferences.Editor editor = pref.edit();
            ip = input.getText().toString();
            editor.putString("ipAddr", ip);
            editor.apply();

            endNetworkingTasks();

            client = new SocketClient(this, ip, 8888); //192.168.1.8

            thread = new Thread(client);
            thread.start();
        }
    }

    public void enterManualIP(){
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

            dialog.cancel();
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

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
        switch (item.getItemId()) {
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

                SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
                SharedPreferences.Editor editor = pref.edit();
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

        ip = pref.getString("ipAddr", "");

        if (ip.equals("")) {
            FindIP finderClass = new FindIP(this);
            findThread = new Thread(finderClass);
            findThread.start();
        }else{
            client = new SocketClient(this, ip, 8888); //192.168.1.8

            thread = new Thread(client);
            thread.start();
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
                    scanCounter = 256;
                }
            });

            scanAlert.setNegativeButton("Try Again", null); //listener added below, as the button should not close the dialog.
            scanAlert.setCancelable(false);

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


            return null;
        }

        @Override
        public void run() {
            String temp = findIpAddress();
            if (temp != null) {
                if (thread == null || !thread.isAlive()) {
                    ip = temp;
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("ipAddr", ip);
                    editor.apply();

                    client = new SocketClient(context, ip, 8888);

                    thread = new Thread(client);
                    thread.start();
                }
            } else {
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
                        failure.setCancelable(false);
                        failure.create().show();
                    }
                });
            }
        }
    }
}