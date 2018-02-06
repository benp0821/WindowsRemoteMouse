//TODO: add Pebble functionality
//TODO: voice control on keyboard causes problems (might be caused by backspace problem)
//TODO: add support for emojis and non-ascii characters
//TODO: add EditText to specify port, add option to server window to specify port
//TODO: add picture to system tray icon, add icon for android app
//TODO: make server start on computer startup (option in settings window)
//TODO: add keyboard buttons for tab, esc, f1-f12, delete, volume up/down, pgup, pgdn, home, end, insert, prtscr keys toggle buttons
//TODO: add left, right, and middle click button
//TODO: add bluetooth support
//TODO: closing keyboard sometimes crashes app
//TODO: make mod keys work with mouse inputs as well
//TODO: make arrow buttons for keyboard work when held down again

package ser421.asu.edu.mousecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    float prevX, prevY;
    float prevX2, prevY2;
    float difX = 0, difY = 0;
    int xSpeed = 0, ySpeed = 0;
    int scroll = 0;
    int previousBufLength = 2;
    final int MOVEMENT_MIN = 10;
    final int SPEED = 20;
    boolean mouseClick = false;
    boolean rightClick = false;
    boolean mouseDragStart = false, mouseDrag = false, mouseDragEnd = false;
    boolean doubleClick = false;
    boolean newIP = false;
    boolean transmitMovement = false;
    boolean multiTouch = false;
    boolean arrowsControlMouse = true;
    boolean mouseKeyPressed = false;
    static boolean ctrlPressed = false, altPressed = false, shiftPressed = false, winPressed = false, keyboardPinned = false;
    static boolean ignoreLeftArrow = false;
    float doubleClickInitX = 0, doubleClickInitY = 0;
    static String keyboardBuf = "";

    volatile boolean scan = true, initialScan = true;
    int initScanCounter = 1;
    private Socket socket;
    ClientThread thread;
    private static final int SERVERPORT = 27015;
    private static String serverip = "1.1.1.1";

    TextView connectionStatusText;
    Button arrowToggleButton;
    SelectionChangedEditText hiddenKeyBuffer;
    TextWatcher hiddenTextWatcher;
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

        arrowToggleButton = findViewById(R.id.arrowToggleBtn);
        arrowToggleButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        arrowToggleButton.setOnClickListener(v -> {
            FileOutputStream outputStream;
            if (arrowsControlMouse){
                try {
                    outputStream = openFileOutput("savedArrowPreference.txt", Context.MODE_PRIVATE);
                    outputStream.write("keyboard".getBytes());
                    arrowToggleButton.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
                    arrowsControlMouse = false;
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                try {
                    outputStream = openFileOutput("savedArrowPreference.txt", Context.MODE_PRIVATE);
                    outputStream.write("mouse".getBytes());
                    arrowToggleButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                    arrowsControlMouse = true;
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            FileInputStream in = openFileInput("savedArrowPreference.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine().trim();
            if (line.equals("mouse")){
                arrowToggleButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                arrowsControlMouse = true;
            }else{
                arrowToggleButton.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
                arrowsControlMouse = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        detector = new GestureDetector(this, this);

        connectionStatusText = findViewById(R.id.connectionStatusTxt);

        View up = findViewById(R.id.upBtn);
        up.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, 0, -SPEED/2, 1);
            return true;
        });
        View down = findViewById(R.id.downBtn);
        down.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, 0, SPEED/2, 2);
            return true;
        });
        View left = findViewById(R.id.leftBtn);
        left.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, -SPEED/2, 0, 3);
            return true;
        });
        View right = findViewById(R.id.rightBtn);
        right.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, SPEED/2, 0, 4);
            return true;
        });
        View leftUp = findViewById(R.id.leftUpBtn);
        leftUp.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, -SPEED/2, -SPEED/2, 5);
            return true;
        });
        View rightUp = findViewById(R.id.rightUpBtn);
        rightUp.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, SPEED/2, -SPEED/2, 6);
            return true;
        });
        View leftDown = findViewById(R.id.leftDownBtn);
        leftDown.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, -SPEED/2, SPEED/2, 7);
            return true;
        });
        View rightDown = findViewById(R.id.rightDownBtn);
        rightDown.setOnTouchListener((v, event) -> {
            v.performClick();
            arrowButtonPressed(event, SPEED/2, SPEED/2, 8);
            return true;
        });

        Button ctrlBtn = findViewById(R.id.ctrlBtn);
        Button altBtn = findViewById(R.id.altBtn);
        Button shiftBtn = findViewById(R.id.shiftBtn);
        Button winBtn = findViewById(R.id.winBtn);
        Button pinBtn = findViewById(R.id.pinBtn);
        ctrlBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        altBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        shiftBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        winBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        pinBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

        ctrlBtn.setOnClickListener(v -> ctrlPressed = toggleKeyPressed(v, ctrlPressed));
        altBtn.setOnClickListener(v -> altPressed = toggleKeyPressed(v, altPressed));
        shiftBtn.setOnClickListener(v -> shiftPressed = toggleKeyPressed(v, shiftPressed));
        winBtn.setOnClickListener(v -> {
            winPressed = toggleKeyPressed(v, winPressed);
            if (!winPressed){
                keyboardBuf = "\\w";
            }
        });
        pinBtn.setOnClickListener(v -> keyboardPinned = toggleKeyPressed(v, keyboardPinned));


        hiddenTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (previousBufLength + 2 > hiddenKeyBuffer.getText().toString().length()){
                    keyboardBuf += "\\b";
                    if (hiddenKeyBuffer.getText().toString().length() == 3){
                        hiddenKeyBuffer.append("/");
                        ignoreLeftArrow = true;
                    }
                }else {
                    keyboardBuf += hiddenKeyBuffer.getText().toString().substring(previousBufLength, hiddenKeyBuffer.getText().toString().length()-2);
                }
                previousBufLength = hiddenKeyBuffer.getText().toString().length()-2;
            }

            @Override
            public void afterTextChanged(Editable s) {
                amTyping = true;
                stt.cancel();
                stt = new StopTypingTask();
                t.schedule(stt, 100);
            }
        };

        hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setBackgroundColor(Color.TRANSPARENT);
        hiddenKeyBuffer.setTextColor(Color.TRANSPARENT);
        hiddenKeyBuffer.setCursorVisible(false);
        hiddenKeyBuffer.addTextChangedListener(hiddenTextWatcher);
        hiddenKeyBuffer.setOnEditorActionListener((textView, keyCode, event) -> {
            if (keyCode == EditorInfo.IME_ACTION_NEXT) {
                keyboardBuf = "\\n";
            }
            return true;
        });

        View rescanButton = findViewById(R.id.rescanBtn);
        rescanButton.setOnClickListener(v -> {
            if (scan){
                initScanCounter = 0;
            }else{
                initScanCounter = 1;
            }
            scan = true;
            hideIPKeyboard();
        });

        View continueButton = findViewById(R.id.continueBtn);
        continueButton.setOnClickListener(v -> {
            scan = true;
            hideIPKeyboard();
        });

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
                    initScanCounter = Integer.parseInt(serverip.substring(serverip.lastIndexOf(".") + 1, serverip.length()));
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid IP Syntax", Toast.LENGTH_LONG).show();
                }
                newIP = true;
                hideIPKeyboard();
                return true;
            }
            return false;
        });
        ipTextBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                setKeyboardToolbarVisiblity(this, false);
            }
        });

        setKeyboardToolbarVisiblity(this, false);

        System.out.println("onCreate() called");
        thread = new ClientThread();
        new Thread(thread).start();
    }

    public boolean toggleKeyPressed(View v, boolean isPressed){
        if (!isPressed){
            v.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
        }else{
            v.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        }
        return !isPressed;
    }

    public void arrowButtonPressed(MotionEvent event, int xSpeed, int ySpeed, int keyDir){
        hideIPKeyboard();
        if (arrowsControlMouse) {
            arrowMouseButtonEvent(event, xSpeed, ySpeed);
        }else{
            arrowKeyboardButtonEvent(event, keyDir);
        }
    }

    public void arrowMouseButtonEvent(MotionEvent event, int xSpeed, int ySpeed){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            transmitMovement = true;
            mouseKeyPressed = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            this.xSpeed = 0;
            this.ySpeed = 0;
            transmitMovement = false;
            mouseKeyPressed = false;
        }
    }

    public void arrowKeyboardButtonEvent(MotionEvent event, int keyDir){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (keyDir == 1){
                keyboardBuf = "\\u";
            }else if (keyDir == 2){
                keyboardBuf = "\\d";
            }else if (keyDir == 3){
                keyboardBuf = "\\l";
            }else if (keyDir == 4){
                keyboardBuf = "\\r";
            }else if (keyDir == 5){
                keyboardBuf = "\\l\\u";
            }else if (keyDir == 6){
                keyboardBuf = "\\r\\u";
            }else if (keyDir == 7){
                keyboardBuf = "\\l\\d";
            }else if (keyDir == 8){
                keyboardBuf = "\\r\\d";
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("onResume() called");

        setKeyboardToolbarVisiblity(this, false);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("onDestroy() called");
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

    public static void setKeyboardToolbarVisiblity(Context context, boolean isVisible){
        Button abcButton = ((Activity)context).findViewById(R.id.keyboardButton);
        Button ctrlBtn = ((Activity)context).findViewById(R.id.ctrlBtn);
        Button altBtn = ((Activity)context).findViewById(R.id.altBtn);
        Button shiftBtn = ((Activity)context).findViewById(R.id.shiftBtn);
        Button winBtn = ((Activity)context).findViewById(R.id.winBtn);
        Button pinBtn = ((Activity)context).findViewById(R.id.pinBtn);

        if (isVisible){
            abcButton.setVisibility(View.INVISIBLE);
            ctrlBtn.setVisibility(View.VISIBLE);
            altBtn.setVisibility(View.VISIBLE);
            shiftBtn.setVisibility(View.VISIBLE);
            winBtn.setVisibility(View.VISIBLE);
            pinBtn.setVisibility(View.VISIBLE);
        }else{
            abcButton.setVisibility(View.VISIBLE);
            ctrlBtn.setVisibility(View.INVISIBLE);
            altBtn.setVisibility(View.INVISIBLE);
            shiftBtn.setVisibility(View.INVISIBLE);
            winBtn.setVisibility(View.INVISIBLE);
            pinBtn.setVisibility(View.INVISIBLE);

            ctrlBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            altBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            shiftBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            winBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            pinBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            ctrlPressed = false;
            altPressed = false;
            shiftPressed = false;
            winPressed = false;
            keyboardPinned = false;

        }
    }

    public void hideIPKeyboard(){
        if (!keyboardPinned) {
            EditText ipTextBox = findViewById(R.id.ipAddrTxt);
            ipTextBox.clearFocus();
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mgr != null) {
                mgr.hideSoftInputFromWindow(ipTextBox.getWindowToken(), 0);
            }

            setKeyboardToolbarVisiblity(this, false);
        }
    }

    public void showKeyBufferKeyboard(){
        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(hiddenKeyBuffer, InputMethodManager.SHOW_IMPLICIT);
        }
        hiddenKeyBuffer.setSelection(hiddenKeyBuffer.getText().length()-2);

        setKeyboardToolbarVisiblity(this, true);
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
                xSpeed = 0;
                ySpeed = 0;
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
                }else if (event.getPointerCount() > 1 && event.getX(0) - prevX > 40 && event.getX(1) - prevX2 > 40 &&
                        Math.abs(event.getY(0) - prevY) <= 40 && Math.abs(event.getY(1) - prevY2) <= 40){
                    System.out.println("two finger scroll left");
                    scroll = 4;
                    prevX = event.getX(0);
                    prevY = event.getY(0);
                    prevX2 = event.getX(1);
                    prevY2 = event.getY(1);
                }else if (event.getPointerCount() > 1 && event.getX(0) - prevX < -40 && event.getX(1) - prevX2 < -40 &&
                        Math.abs(event.getY(0) - prevY) <= 40 && Math.abs(event.getY(1) - prevY2) <= 40){
                    System.out.println("two finger scroll right");
                    scroll = 3;
                    prevX = event.getX(0);
                    prevY = event.getY(0);
                    prevX2 = event.getX(1);
                    prevY2 = event.getY(1);
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

    public void writeIpToFile(){
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("savedIP.txt", Context.MODE_PRIVATE);
            outputStream.write(serverip.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            initScanCounter = 1;
                        }finally {
                            initialScan = false;
                        }
                    } catch (Exception e) {
                        scan = true;
                        initScanCounter = 1;
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
                            writeIpToFile();
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
                    writeIpToFile();

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
                runOnUiThread(() -> {
                    connectionStatusText.setText(String.format("%s %s", getString(R.string.ConnectedToText), serverip));
                    ClearFocusOnBackEditText ipTextBox = findViewById(R.id.ipAddrTxt);
                    if (!ipTextBox.hasFocus()) {
                        ipTextBox.setText(serverip);
                    }
                });
            }

            runOnUiThread(() -> {
                hiddenKeyBuffer.removeTextChangedListener(hiddenTextWatcher);
                hiddenKeyBuffer.setText(getString(R.string.initialHiddenBufferText));
                previousBufLength = 2;
                hiddenKeyBuffer.addTextChangedListener(hiddenTextWatcher);
                keyboardBuf = "";
            });

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if (!isDestroyed()) {
                    thread = new ClientThread();
                    new Thread(thread).start();
                }
            }
            if (!scan) {
                runOnUiThread(() -> connectionStatusText.setText(R.string.failedConnectionText));
            }
        }

        String applyModifiers(String initString){
            String writeParams = initString;
            if (ctrlPressed){
                writeParams += "a";
                if (!keyboardBuf.contains("\\l") && !keyboardBuf.contains("\\r") && !keyboardBuf.contains("\\u") && !keyboardBuf.contains("\\d")) {
                    ctrlPressed = false;
                    Button ctrlBtn = findViewById(R.id.ctrlBtn);
                    ctrlBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
                }else if (altPressed || shiftPressed || winPressed){
                    ctrlPressed = false;
                    Button ctrlBtn = findViewById(R.id.ctrlBtn);
                    ctrlBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
                }
            }
            if (altPressed){
                writeParams += "b";
                altPressed = false;
                Button altBtn = findViewById(R.id.altBtn);
                altBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            }
            if (shiftPressed){
                writeParams += "c";
                shiftPressed = false;
                Button shiftBtn = findViewById(R.id.shiftBtn);
                shiftBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            }
            if (winPressed){
                writeParams += "d";
                winPressed = false;
                Button winBtn = findViewById(R.id.winBtn);
                winBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            }
            return writeParams;
        }

        boolean transmitData(){
            try{
                if (socket != null) {
                    if (transmitMovement) {
                        socket.getOutputStream().write((Math.round(xSpeed) + " " + Math.round(ySpeed) + ";").getBytes());
                        socket.getOutputStream().flush();
                        if (!mouseKeyPressed){
                            xSpeed = 0;
                            ySpeed = 0;
                        }
                        Thread.sleep(30);
                    }

                    if (sendPing) {
                        socket.getOutputStream().write(("g").getBytes());
                        socket.getOutputStream().flush();
                        sendPing = false;
                    }else if (!Objects.equals(keyboardBuf, "") && !amTyping){
                        if (keyboardBuf.contains("\t")) {
                            socket.getOutputStream().write((applyModifiers("k") + ":" + "\\t").getBytes());
                            socket.getOutputStream().flush();
                        }else {
                            socket.getOutputStream().write((applyModifiers("k") + ":" + keyboardBuf).getBytes());
                            socket.getOutputStream().flush();
                        }
                        keyboardBuf = "";
                    }else if (scroll != 0){
                        if (scroll == 1){
                            socket.getOutputStream().write(("sd").getBytes());
                            socket.getOutputStream().flush();
                        }else if (scroll == 2){
                            socket.getOutputStream().write(("su").getBytes());
                            socket.getOutputStream().flush();
                        }else if (scroll == 3){
                            socket.getOutputStream().write(("sr").getBytes());
                            socket.getOutputStream().flush();
                        }else if (scroll == 4){
                            socket.getOutputStream().write(("sl").getBytes());
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
