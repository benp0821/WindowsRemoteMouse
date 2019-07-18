package com.ben.mousecontrol;

import android.annotation.SuppressLint;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private GestureDetector detector;
    SocketClient client;
    Thread thread;
    int startX, startY, startX2, startY2;
    static boolean mouseMove = false;
    static boolean mouseDragging = false;
    int multiTouch = 0;
    long touchStartTime = 0;
    boolean scroll = false;
    boolean keyboardPinned = false; //TODO: Implement this functionality

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ben.mousecontrol.R.layout.activity_main);

        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hiddenKeyBuffer.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
            }
        });

        KeyboardInterpreter.startKeyListener(this);

        View view = findViewById(com.ben.mousecontrol.R.id.layout);

        detector = new GestureDetector(this, new GestureInterpreter());
        @SuppressLint("ClickableViewAccessibility") View.OnTouchListener touchListener = (v, event) -> {
            int action = event.getActionMasked();

            if (!keyboardPinned) {
                CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
            }

            detector.onTouchEvent(event);

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
                multiTouch = event.getPointerCount();

                if (multiTouch == 1){
                    touchStartTime = System.currentTimeMillis();
                    mouseMove = true;
                }else {
                    mouseMove = false;
                }

                if (action == MotionEvent.ACTION_DOWN) {
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    if (multiTouch > 1) {
                        startX2 = (int) event.getX(1);
                        startY2 = (int) event.getY(1);
                    }
                }

            }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
                if (multiTouch == 2 && action == MotionEvent.ACTION_UP && System.currentTimeMillis() - touchStartTime < 600 && !scroll){
                    SocketClient.addCommand("mouseClick btn=right");
                    multiTouch = 0;
                }

                mouseMove = false;
                mouseDragging = false;
                if (event.getPointerCount() < 2) {
                    scroll = false;
                }
            }

            if (action == MotionEvent.ACTION_MOVE || mouseDragging){
                int difX = (int)event.getX() - startX;
                int difY = (int)event.getY() - startY;
                startX = (int)event.getX();
                startY = (int)event.getY();
                if (mouseMove) {
                    SocketClient.addCommand("mouseMove " + (difX * 5) + " " + (difY * 5));
                }

                if (event.getPointerCount() == 2){
                    int difX2 = (int)event.getX(1) - startX2;
                    int difY2 = (int)event.getY(1) - startY2;
                    startX2 = (int)event.getX(1);
                    startY2 = (int)event.getY(1);

                    if (difY > 5 && difY2 > 5) {
                        SocketClient.addCommand("vscroll 40");
                        scroll = true;
                    } else if (difY < -5 && difY2 < -5) {
                        SocketClient.addCommand("vscroll -40");
                        scroll = true;
                    } else if (difX > 15 && difX2 > 15){
                        SocketClient.addCommand("hscroll -40");
                        scroll = true;
                    }else if (difX < -15 && difX2 < -15){
                        SocketClient.addCommand("hscroll 40");
                        scroll = true;
                    }

                }
            }

            return true;
        };

        view.setOnTouchListener(touchListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);

        client = new SocketClient(this, "192.168.1.8", 8888);
        thread = new Thread(client);
        thread.start();
    }

    @Override
    public void onPause(){
        client.endNetworkingTasks();
        thread.interrupt();
        super.onPause();
    }

    public void abcPressed(View view) {
        EditText hiddenKeyBuffer = findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, true);
    }

    public void leftClickBtnPressed(View view) {
        SocketClient.addCommand("mouseClick");
    }

    public void midClickBtnPressed(View view) {
        SocketClient.addCommand("mouseClick btn=middle");
    }

    public void rightClickBtnPressed(View view) {
        SocketClient.addCommand("mouseClick btn=right");
    }
}