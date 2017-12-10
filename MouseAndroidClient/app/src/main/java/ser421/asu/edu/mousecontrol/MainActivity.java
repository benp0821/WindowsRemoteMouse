//TODO: Make double click and drag equal click and drag
//TODO: add Pebble functionality
//TODO: auto populate IP Address with auto detected ip based on scan of network
//TODO: add keyboard functionality

package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity  implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    float prevX, prevY;
    float difX = 0, difY = 0;
    boolean mouseClick = false;
    boolean rightClick = false;
    boolean newIP = false;

    final int BTN_SPEED = 25;

    GestureDetector detector;

    private Socket socket;
    ClientThread thread;

    private static final int SERVERPORT = 27015;
    private static String serverip = "192.168.1.10";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detector = new GestureDetector(this, this);


        View up = findViewById(R.id.upBtn);
        up.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difY = -BTN_SPEED;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difY = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difY = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
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
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
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
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    difX = BTN_SPEED;
                    difY = BTN_SPEED;
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP ){
                    difX = 0;
                    difY = 0;
                    return true;
                }
                return false;
            }
        });

        CustomEditText ipTextBox = findViewById(R.id.ipAddrTxt);
        ipTextBox.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    serverip = textView.getText().toString().trim();
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
        mouseClick = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
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
            if (Math.abs(difX) > 100 || Math.abs(difY) > 100){
                prevX = event.getX();
                prevY = event.getY();
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            difX = 0;
            difY = 0;
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
            try {
                newIP = false;

                InetSocketAddress serverAddr = new InetSocketAddress(serverip, SERVERPORT);
                socket = new Socket();
                socket.connect(serverAddr, 5000);

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            while (socket != null && !socket.isClosed() && transmitData()){
                try {
                    System.out.println(socket.getInetAddress());
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public boolean transmitData(){
            try{
                if (socket != null) {
                    socket.getOutputStream().write((Math.round(difX) + " " + Math.round(difY) + ";").getBytes());
                    socket.getOutputStream().flush();

                    if (mouseClick){
                        mouseClick = false;
                        socket.getOutputStream().write(("c;").getBytes());
                        socket.getOutputStream().flush();
                    }else if (rightClick){
                        rightClick = false;
                        socket.getOutputStream().write(("r;").getBytes());
                        socket.getOutputStream().flush();
                    }
                }
            }catch(IOException e){
                thread = new ClientThread();
                new Thread(thread).start();
                return false;
            }

            if (newIP){
                thread = new ClientThread();
                new Thread(thread).start();
            }

            return !newIP;
        }

    }
}
