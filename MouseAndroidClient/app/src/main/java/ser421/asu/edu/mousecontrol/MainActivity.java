//TODO: add Pebble functionality
//TODO: add zoom and scroll events
//TODO: problems with double click/ double click and drag
//TODO: add option to autoscan starting from after currently connected ip (in case multiple servers are on network)
//TODO: add keyboard functionality (k command followed by the text in the buffer)

package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity  implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    float prevX, prevY;
    float difX = 0, difY = 0;
    boolean mouseClick = false;
    boolean rightClick = false;
    boolean mouseDragStart = false, mouseDrag = false, mouseDragEnd = false;
    boolean doubleClick = false;
    float doubleClickInitX = 0, doubleClickInitY = 0;
    boolean newIP = false;
    boolean transmitMovement = false;
    boolean buttonDown = false;
    volatile boolean initialScan = true;
    int initScanCounter = 0;
    TextView connectionStatusText;

    final int BTN_SPEED = 20;

    GestureDetector detector;

    private Socket socket;
    ClientThread thread;

    private static final int SERVERPORT = 27015;
    private static String serverip = "1.1.1.1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detector = new GestureDetector(this, this);

        connectionStatusText = findViewById(R.id.connectionStatusTxt);

        View up = findViewById(R.id.upBtn);
        up.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difY = -BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });

        View down = findViewById(R.id.downBtn);
        down.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difY = BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View left = findViewById(R.id.leftBtn);
        left.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = -BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View right = findViewById(R.id.rightBtn);
        right.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View leftUp = findViewById(R.id.leftUpBtn);
        leftUp.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = -BTN_SPEED;
                    difY = -BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View rightUp = findViewById(R.id.rightUpBtn);
        rightUp.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = BTN_SPEED;
                    difY = -BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View leftDown = findViewById(R.id.leftDownBtn);
        leftDown.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = -BTN_SPEED;
                    difY = BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });


        View rightDown = findViewById(R.id.rightDownBtn);
        rightDown.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    difX = BTN_SPEED;
                    difY = BTN_SPEED;
                    transmitMovement = true;
                    buttonDown = true;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
                    buttonDown = false;
                    return true;
                }
                return false;
            }
        });

        View rescanButton = findViewById(R.id.rescanBtn);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialScan = true;
                initScanCounter = 0;
            }
        });

        CustomEditText ipTextBox = findViewById(R.id.ipAddrTxt);
        ipTextBox.setRawInputType(Configuration.KEYBOARD_12KEY);
        ipTextBox.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    initialScan = false;
                    String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
                    String temp = textView.getText().toString().trim();
                    if (temp.matches(PATTERN)) {
                        serverip = temp;
                    }else{
                        Toast.makeText(getApplicationContext(), "Invalid IP Syntax", Toast.LENGTH_LONG).show();
                    }
                    newIP = true;
                    hideKeyboard();
                    return true;
                }
                return false;
            }
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
        if (!mouseDrag) {
            mouseClick = true;
        }
        return true;
    }
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        doubleClickInitX = e.getX();
        doubleClickInitY = e.getY();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_MOVE && !mouseDrag && !mouseDragEnd && !mouseDragStart){
            if (Math.abs(e.getX() - doubleClickInitX) > 15 || Math.abs(e.getY() - doubleClickInitY) > 15){
                mouseDragStart = true;
            }else{
                doubleClick = true;
            }
        }else if (e.getAction() == MotionEvent.ACTION_UP && !mouseDragStart && !mouseDrag && !mouseDragEnd){
            doubleClick = true;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        prevX = e.getX();
        prevY = e.getY();
        return true;
    }

    public void hideKeyboard(){
        EditText ipTextBox = findViewById(R.id.ipAddrTxt);
        ipTextBox.clearFocus();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(ipTextBox.getWindowToken(), 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        detector.onTouchEvent(event);

        hideKeyboard();

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            difX = event.getX() - prevX;
            difY = event.getY() - prevY;
            if (Math.abs(difX) > BTN_SPEED || Math.abs(difY) > BTN_SPEED){
                prevX = event.getX();
                prevY = event.getY();
                transmitMovement = true;
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            difX = 0;
            difY = 0;
            if (mouseDrag){
                mouseDragStart = false;
                mouseDrag = false;
                mouseDragEnd = true;
            }
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
        rightClick = true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            if (initialScan) {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);
                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                System.out.println(ipString);
                while (initScanCounter < 255 && initialScan) {
                    try {
                        String ip = prefix + String.valueOf(initScanCounter);
                        InetSocketAddress serverAddr = new InetSocketAddress(ip, SERVERPORT);
                        socket = new Socket();
                        try {
                            connectionStatusText.setText("Scanning " + ip);
                            socket.connect(serverAddr, 500);
                            initialScan = false;
                            serverip = ip;
                        }catch (ConnectException e){
                            e.printStackTrace();
                            socket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    initScanCounter++;
                }
                initialScan = false;
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
                connectionStatusText.setText("Connected to " + serverip);
                CustomEditText ipTextBox = findViewById(R.id.ipAddrTxt);
                ipTextBox.setText(serverip);
            }

            while (socket != null && socket.isConnected() && transmitData() && !initialScan);

            try {
                socket.close();
                thread = new ClientThread();
                new Thread(thread).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            connectionStatusText.setText("Connection Failed");
        }

        public boolean transmitData(){
            try{
                if (socket != null) {
                    if (transmitMovement) {
                        socket.getOutputStream().write((Math.round(difX) + " " + Math.round(difY) + ";").getBytes());
                        socket.getOutputStream().flush();
                        if (!buttonDown) {
                            transmitMovement = false;
                        }
                        if (buttonDown){
                            Thread.sleep(15);
                        }
                    }

                    if (mouseDragStart){
                        mouseDragStart = false;
                        mouseDragEnd = false;
                        mouseDrag = true;
                        socket.getOutputStream().write(("d;").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("drag start");
                    }else if (mouseDragEnd){
                        mouseDragEnd = false;
                        mouseDrag = false;
                        mouseClick = false;
                        rightClick = false;
                        socket.getOutputStream().write(("e;").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("drag end");
                    }else if (doubleClick && !mouseDrag && !mouseClick){
                        doubleClick = false;
                        socket.getOutputStream().write(("x;").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("double click");
                    }else if (mouseClick && !mouseDrag && !rightClick){
                        mouseClick = false;
                        socket.getOutputStream().write(("c;").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("mouse click");
                    }else if (rightClick && !mouseDrag){
                        rightClick = false;
                        socket.getOutputStream().write(("r;").getBytes());
                        socket.getOutputStream().flush();
                        System.out.println("right click");
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
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
