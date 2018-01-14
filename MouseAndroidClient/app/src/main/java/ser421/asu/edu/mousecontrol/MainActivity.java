//TODO: add Pebble functionality
//TODO: redo mouse movement code
//TODO: scroll doesn't work on non-standard windows
//TODO: add option to exit server to system tray
//TODO: fix logic for detecting backspace keyboard event
//TODO: voice control on keyboard causes problems (might be caused by backspace problem)
//TODO: scroll left and right (two finger swipe)
//TODO: switch from constraintlayout and make it work on all device sizes

package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    float prevX, prevY;
    float prevX2, prevY2;
    float difX = 0, difY = 0;
    boolean mouseClick = false;
    boolean rightClick = false;
    boolean mouseDragStart = false, mouseDrag = false, mouseDragEnd = false;
    boolean doubleClick = false;
    float doubleClickInitX = 0, doubleClickInitY = 0;
    int scroll = 0;
    boolean newIP = false;
    boolean transmitMovement = false;
    String keyboardBuf = "";
    volatile boolean scan = true, initialScan = true;
    int initScanCounter = 0;
    boolean multiTouch = false;
    TextView connectionStatusText;
    int xSpeed = 0, ySpeed = 0;
    final int MOVEMENT_MIN = 100;
    final int SPEED = 20;
    int previousBufLength = 0;

    EditText hiddenKeyBuffer;

    GestureDetector detector;

    boolean sendPing = false;
    Timer pingServer = new Timer();
    class PingServerTask extends TimerTask {
        public void run(){
            sendPing = true;
        }
    }

    boolean amTyping = false;
    Timer t = new Timer();
    class StopTypingTask extends TimerTask {
        public void run() {
            amTyping = false;
        }
    }
    StopTypingTask stt = new StopTypingTask();

    private Socket socket;
    ClientThread thread;

    private static final int SERVERPORT = 27015;
    private static String serverip = "1.1.1.1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pingServer.schedule(new PingServerTask(), 5000, 5000);

        try {
            FileInputStream in = openFileInput("savedIP.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            serverip = line.trim();
            initScanCounter = Integer.parseInt(serverip.substring(serverip.lastIndexOf(".") + 1, serverip.length())) + 1;
        } catch (IOException e) {
            e.printStackTrace();
            initialScan = false;
        }

        detector = new GestureDetector(this, this);

        connectionStatusText = findViewById(R.id.connectionStatusTxt);

        View up = findViewById(R.id.upBtn);
        up.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                ySpeed = -SPEED;
                xSpeed = 0;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                ySpeed = 0;
                xSpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });

        View down = findViewById(R.id.downBtn);
        down.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                ySpeed = SPEED;
                xSpeed = 0;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                ySpeed = 0;
                xSpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View left = findViewById(R.id.leftBtn);
        left.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                xSpeed = -SPEED;
                ySpeed = 0;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View right = findViewById(R.id.rightBtn);
        right.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                xSpeed = SPEED;
                ySpeed = 0;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View leftUp = findViewById(R.id.leftUpBtn);
        leftUp.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                xSpeed = -SPEED;
                ySpeed = -SPEED;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View rightUp = findViewById(R.id.rightUpBtn);
        rightUp.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                xSpeed = SPEED;
                ySpeed = -SPEED;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View leftDown = findViewById(R.id.leftDownBtn);
        leftDown.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                xSpeed = -SPEED;
                ySpeed = SPEED;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });


        View rightDown = findViewById(R.id.rightDownBtn);
        rightDown.setOnTouchListener((v, event) -> {
            v.performClick();

            hideIPKeyboard();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                xSpeed = SPEED;
                ySpeed = SPEED;
                transmitMovement = true;
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP ){
                xSpeed = 0;
                ySpeed = 0;
                transmitMovement = false;
                return true;
            }
            return false;
        });

        hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setBackgroundColor(Color.TRANSPARENT);
        hiddenKeyBuffer.setTextColor(Color.TRANSPARENT);
        hiddenKeyBuffer.setCursorVisible(false);
        hiddenKeyBuffer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (previousBufLength > hiddenKeyBuffer.getText().toString().length()){
                    keyboardBuf += "\\b";
                }else {
                    keyboardBuf += hiddenKeyBuffer.getText().toString().substring(previousBufLength);
                }
                previousBufLength = hiddenKeyBuffer.getText().toString().length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                amTyping = true;
                stt.cancel();
                stt = new StopTypingTask();
                t.schedule(stt, 100);
            }
        });
        hiddenKeyBuffer.setOnEditorActionListener((textView, keyCode, event) -> {
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                keyboardBuf = "\\n";
            }
            return true;
        });

        View rescanButton = findViewById(R.id.rescanBtn);
        rescanButton.setOnClickListener(v -> {
            scan = true;
            initScanCounter = 0;
        });

        View continueButton = findViewById(R.id.continueBtn);
        continueButton.setOnClickListener(v -> scan = true);

        View abcButton = findViewById(R.id.keyboardButton);
        abcButton.setOnClickListener(v -> showKeyBufferKeyboard());

        ClearFocusOnBackEditText ipTextBox = findViewById(R.id.ipAddrTxt);
        ipTextBox.setRawInputType(Configuration.KEYBOARD_12KEY);
        ipTextBox.setOnEditorActionListener((textView, keyCode, event) -> {
            if (keyCode == EditorInfo.IME_ACTION_DONE) {
                scan = false;
                String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
                String temp = textView.getText().toString().trim();
                if (temp.matches(PATTERN)) {
                    serverip = temp;
                    initScanCounter = Integer.parseInt(serverip.substring(serverip.lastIndexOf(".") + 1, serverip.length())) + 1;
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid IP Syntax", Toast.LENGTH_LONG).show();
                }
                newIP = true;
                hideIPKeyboard();
                return true;
            }
            return false;
        });

        thread = new ClientThread();
        new Thread(thread).start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (!multiTouch) {
            if (!mouseDrag) {
                mouseClick = true;
            }else{
                mouseDragEnd = true;
                mouseDrag = false;
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!multiTouch) {
            doubleClickInitX = e.getX();
            doubleClickInitY = e.getY();
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (!multiTouch) {
            if (e.getAction() == MotionEvent.ACTION_MOVE && !mouseDrag && !mouseDragEnd && !mouseDragStart) {
                if (Math.abs(e.getX() - doubleClickInitX) > 15 || Math.abs(e.getY() - doubleClickInitY) > 15) {
                    mouseDragStart = true;
                }
            } else if (e.getAction() == MotionEvent.ACTION_UP && !mouseDragStart && !mouseDrag && !mouseDragEnd) {
                doubleClick = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void hideIPKeyboard(){
        EditText ipTextBox = findViewById(R.id.ipAddrTxt);
        ipTextBox.clearFocus();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr != null) {
            mgr.hideSoftInputFromWindow(ipTextBox.getWindowToken(), 0);
        }
    }

    public void showKeyBufferKeyboard(){
        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(hiddenKeyBuffer, InputMethodManager.SHOW_IMPLICIT);
        }
        hiddenKeyBuffer.setSelection(hiddenKeyBuffer.getText().length());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        detector.onTouchEvent(event);
        hideIPKeyboard();

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                difX = 0;
                difY = 0;
                transmitMovement = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!multiTouch) {
                    difX = event.getX() - prevX;
                    difY = event.getY() - prevY;
                    if (Math.abs(difX) > MOVEMENT_MIN || Math.abs(difY) > MOVEMENT_MIN) {
                        if (difX > MOVEMENT_MIN){
                            prevX = event.getX();
                            xSpeed = SPEED;
                        }else if (difX < -MOVEMENT_MIN){
                            xSpeed = -SPEED;
                            prevX = event.getX();
                        }else if (difX < 30 && difX > -30){
                            xSpeed = 0;
                        }
                        if (difY > MOVEMENT_MIN){
                            prevY = event.getY();
                            ySpeed = SPEED;
                        }else if (difY < -MOVEMENT_MIN){
                            ySpeed = -SPEED;
                            prevY = event.getY();
                        }else if (difY < 30 && difY > -30){
                            ySpeed = 0;
                        }
                        transmitMovement = true;
                    }
                }else if (event.getPointerCount() > 1 && event.getY(0) - prevY > 40 && event.getY(1) - prevY2 > 40) {
                    System.out.println("two finger scroll up");
                    scroll = 2;
                    prevX = event.getX(0);
                    prevY = event.getY(0);
                    prevX2 = event.getX(1);
                    prevY2 = event.getY(1);
                }else if (event.getPointerCount() > 1 && event.getY(0) - prevY < -40 && event.getY(1) - prevY2 < -40) {
                    System.out.println("two finger scroll down");
                    scroll = 1;
                    prevX = event.getX(0);
                    prevY = event.getY(0);
                    prevX2 = event.getX(1);
                    prevY2 = event.getY(1);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                multiTouch = true;
                prevX = event.getX(0);
                prevY = event.getY(0);
                prevX2 = event.getX(1);
                prevY2 = event.getY(1);
                break;
            case MotionEvent.ACTION_DOWN:
                prevX = event.getX();
                prevY = event.getY();
                if (multiTouch){
                    multiTouch = false;
                }
                break;
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (!multiTouch) {
            rightClick = true;
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            if (scan) {
                if (initialScan) {
                    try {
                        InetSocketAddress serverAddr = new InetSocketAddress(serverip, SERVERPORT);
                        socket = new Socket();
                        try {
                            connectionStatusText.setText(String.format("%s %s", getString(R.string.scanningText), serverip));
                            connectionStatusText.invalidate();
                            Thread.sleep(100);
                            socket.connect(serverAddr, 2000);
                            scan = false;
                        } catch (ConnectException e) {
                            e.printStackTrace();
                            socket.close();
                            scan = true;
                            initScanCounter = 0;
                        }finally {
                            initialScan = false;
                        }
                    } catch (Exception e) {
                        scan = true;
                        initScanCounter = 0;
                        e.printStackTrace();
                    } finally {
                        initialScan = false;
                    }
                }

                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo;
                String ipString = null;
                if (wm != null) {
                    connectionInfo = wm.getConnectionInfo();

                    int ipAddress = connectionInfo.getIpAddress();

                    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)){
                        ipAddress = Integer.reverseBytes(ipAddress);
                    }

                    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

                    try {
                        ipString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                //String ipString = Formatter.formatIpAddress(ipAddress);
                String prefix;
                if (ipString != null) {
                    prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                }else{
                    prefix = "0.0.0.";
                    initScanCounter = 255;
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (initScanCounter < 255 && scan) {
                    try {
                        String ip = prefix + String.valueOf(initScanCounter);
                        InetSocketAddress serverAddr = new InetSocketAddress(ip, SERVERPORT);
                        socket = new Socket();
                        try {
                            connectionStatusText.setText(String.format("%s %s", getString(R.string.scanningText), ip));
                            connectionStatusText.invalidate();
                            Thread.sleep(200);
                            socket.connect(serverAddr, 500);
                            scan = false;
                            serverip = ip;

                            FileOutputStream outputStream;
                            try {
                                outputStream = openFileOutput("savedIP.txt", Context.MODE_PRIVATE);
                                outputStream.write(serverip.getBytes());
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }catch (ConnectException e){
                            e.printStackTrace();
                            socket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    initScanCounter++;
                }
                scan = false;
            }else{
                try {
                    InetSocketAddress serverAddr = new InetSocketAddress(serverip, SERVERPORT);
                    socket = new Socket();
                    socket.connect(serverAddr, 2000);

                } catch (Exception e1) {
                    e1.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (socket != null && socket.isConnected()){
                connectionStatusText.setText(String.format("%s %s", getString(R.string.ConnectedToText), serverip));
                ClearFocusOnBackEditText ipTextBox = findViewById(R.id.ipAddrTxt);
                ipTextBox.setText(serverip);
            }

            rightClick = false;
            mouseClick = false;
            doubleClick = false;
            mouseDrag = false;
            mouseDragStart = false;
            mouseDragEnd = false;
            scroll = 0;

            boolean keepLooping = true;
            while (keepLooping){
                if (socket == null || !socket.isConnected() || !transmitData() || scan){
                    keepLooping = false;
                }
            }

            try {
                socket.close();
                thread = new ClientThread();
                new Thread(thread).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!scan) {
                connectionStatusText.setText(R.string.failedConnectionText);
            }
        }

        boolean transmitData(){
            try{
                if (socket != null) {
                    if (transmitMovement) {
                        socket.getOutputStream().write((Math.round(xSpeed) + " " + Math.round(ySpeed) + ";").getBytes());
                        socket.getOutputStream().flush();
                        Thread.sleep(30);
                    }

                    if (sendPing) {
                        socket.getOutputStream().write(("g").getBytes());
                        socket.getOutputStream().flush();
                        sendPing = false;
                    }else if (!Objects.equals(keyboardBuf, "") && !amTyping){
                        socket.getOutputStream().write(("k" + keyboardBuf).getBytes());
                        socket.getOutputStream().flush();
                        keyboardBuf = "";
                    }else if (scroll != 0){
                        if (scroll == 1){
                            socket.getOutputStream().write(("sd").getBytes());
                            socket.getOutputStream().flush();
                        }else if (scroll == 2){
                            socket.getOutputStream().write(("su").getBytes());
                            socket.getOutputStream().flush();
                        }
                        scroll = 0;
                    }else if (mouseDragStart){
                        mouseDragStart = false;
                        mouseDrag = true;
                        socket.getOutputStream().write(("d").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("drag start");
                    }else if (mouseDragEnd){
                        mouseDragEnd = false;
                        mouseDrag = false;
                        mouseClick = false;
                        rightClick = false;
                        socket.getOutputStream().write(("e").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("drag end");
                    }else if (doubleClick && !mouseDrag && !mouseClick){
                        doubleClick = false;
                        socket.getOutputStream().write(("x").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("double click");
                    }else if (mouseClick && !mouseDrag && !rightClick){
                        mouseClick = false;
                        socket.getOutputStream().write(("c").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("mouse click");
                    }else if (rightClick && !mouseDrag){
                        rightClick = false;
                        socket.getOutputStream().write(("r").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("right click");
                    }
                }
            }catch(IOException | InterruptedException e){
                e.printStackTrace();
                return false;
            }

            if (newIP){
                newIP = false;
                return false;
            }

            return true;
        }

    }
}
