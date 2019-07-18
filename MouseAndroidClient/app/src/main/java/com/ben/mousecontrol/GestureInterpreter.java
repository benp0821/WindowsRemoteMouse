package com.ben.mousecontrol;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class GestureInterpreter implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static GestureDetector detector;
    private static int startX, startY, startX2, startY2;
    private static boolean mouseMove = false;
    private static boolean mouseDragging = false;
    private static int multiTouch = 0;
    private static long touchStartTime = 0;
    private static boolean scroll = false;
    private static boolean keyboardPinned = false; //TODO: Implement this functionality

    static void startGestureInterpreter(AppCompatActivity context){
        View view = context.findViewById(com.ben.mousecontrol.R.id.layout);

        detector = new GestureDetector(context, new GestureInterpreter());
        @SuppressLint("ClickableViewAccessibility") View.OnTouchListener touchListener = (v, event) -> {
            int action = event.getActionMasked();

            if (!keyboardPinned) {
                EditText hiddenKeyBuffer = context.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
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
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        SocketClient.addCommand("mouseClick");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
       // SocketClient.addCommand("mouseClick");
       // return true;
        return false;
    }

    private float initX = 0;
    private float initY = 0;

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        initX = motionEvent.getX();
        initY = motionEvent.getY();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE &&
            (Math.abs(motionEvent.getX() - initX) > 5 || Math.abs(motionEvent.getY() - initY) > 5)) {
                if (!mouseDragging) {
                    SocketClient.addCommand("mouseDrag");
                    mouseDragging = true;
                }
        }else if (motionEvent.getAction() == MotionEvent.ACTION_UP && !mouseDragging){
            SocketClient.addCommand("mouseClick");
        }
        return true;
    }
}
