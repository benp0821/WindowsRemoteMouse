package com.ben.mousecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    SocketClient client;
    Thread thread;
    Thread findThread;
    volatile String ip;
    Handler scanTextHandler;
    Runnable scanTextRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ben.mousecontrol.R.layout.activity_main);

        KeyboardInterpreter.startKeyListener(this);
        GestureInterpreter.startGestureInterpreter(this);
        ArrowButtonInterpreter.startArrowButtonInterpreter(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);

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

        if (KeyboardInterpreter.keyboardPinned){

            hiddenKeyBuffer.requestFocus();
            //delays so that keyboard will be able to reappear. If timeout is not long enough, visiblity is reverted back to false
            //and keyboardPinned is set to false.
            hiddenKeyBuffer.postDelayed(() -> CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, true), 100);

            Button pinBtn = findViewById(R.id.pinBtn);
            KeyboardInterpreter.toggleCustomKeyPressed(pinBtn, true);
        }
    }

    @Override
    public void onPause(){
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
        try {
            scanTextHandler.removeCallbacks(scanTextRunnable);
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onPause();
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

            runOnUiThread(() -> scanTextHandler = new Handler());

            scanTextRunnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Scanning...", Toast.LENGTH_LONG).show();
                    if (ip != null && ip.equals("") && findThread.isAlive()) {
                        scanTextHandler.postDelayed(this, 1000);
                    }
                }
            };
            runOnUiThread(scanTextRunnable);

            int counter = 0;
            while (counter < 255) {
                if (findThread.isInterrupted()){
                    return "";
                }

                Socket s = null;
                try {
                    ipAddr = prefix + String.valueOf(counter);
                    s = new Socket();
                    s.connect(new InetSocketAddress(ipAddr, 8888), 5000);
                    scanTextHandler.removeCallbacks(scanTextRunnable);
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
                counter++;
            }


            return null;
        }

        @Override
        public void run() {
            ip = findIpAddress();

            if (ip != null) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedPref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("ipAddr", ip);
                editor.apply();

                client = new SocketClient(context, ip, 8888);

                thread = new Thread(client);
                thread.start();
            } else {
                try {
                    scanTextHandler.removeCallbacks(scanTextRunnable);
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(() -> Toast.makeText(context, "Can't Find Device", Toast.LENGTH_LONG).show());
            }
        }
    }
}