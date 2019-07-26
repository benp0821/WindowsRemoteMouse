package com.ben.mousecontrol;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GestureInterpreter implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static GestureDetector detector;
    private static int startX, startY, startX2, startY2;
    private static float initX = 0;
    private static float initY = 0;
    private static boolean mouseMove = false;
    private static String mouseDragging = "false";
    private static int multiTouch = 0;
    private static long touchStartTime = 0;
    private static boolean scroll = false;

    static void startGestureInterpreter(AppCompatActivity context){
        View view = context.findViewById(com.ben.mousecontrol.R.id.layout);

        Button leftClickBtn = context.findViewById(R.id.leftClickBtn);
        leftClickBtn.setOnTouchListener((view1, motionEvent) -> {
            mouseButtonAction(view1, motionEvent, "left");
            return false;
        });

        Button rightClickBtn = context.findViewById(R.id.rightClickBtn);
        rightClickBtn.setOnTouchListener((view1, motionEvent) -> {
            mouseButtonAction(view1, motionEvent, "right");
            return false;
        });

        Button midClickBtn = context.findViewById(R.id.midClickBtn);
        midClickBtn.setOnTouchListener((view1, motionEvent) -> {
            mouseButtonAction(view1, motionEvent, "middle");
            return false;
        });

        detector = new GestureDetector(context, new GestureInterpreter());
        @SuppressLint("ClickableViewAccessibility") View.OnTouchListener touchListener = (v, event) -> {
            int action = event.getActionMasked();

            if (!KeyboardInterpreter.keyboardPinned) {
                EditText hiddenKeyBuffer = context.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
                CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
            }

            detector.onTouchEvent(event);

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
                onMouseDown(event);

            }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
                onMouseUp(context, event);
            }

            if (action == MotionEvent.ACTION_MOVE || !mouseDragging.equals("false")){
                onMouseMove(event);
                if (mouseDragging.equals("left")){
                    leftClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
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
        if (!mouseDragging.equals("false")) {
            SocketClient.addCommand("mouseDragEnd " + mouseDragging);
            mouseDragging = "false";
        }
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
                startDragging("left");
        }else if (motionEvent.getAction() == MotionEvent.ACTION_UP && mouseDragging.equals("false")){
            SocketClient.addCommand("mouseClick");
        }
        return true;
    }

    private static void startDragging(String button){
        if (mouseDragging.equals("false")) {
            SocketClient.addCommand("mouseDrag " + button);
            mouseDragging = button;
        }
    }

    private static void onMouseDown(MotionEvent event){
        multiTouch = event.getPointerCount();

        if (multiTouch == 1){
            touchStartTime = System.currentTimeMillis();
            mouseMove = true;
        }else {
            mouseMove = false;
        }

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            startX = (int) event.getX();
            startY = (int) event.getY();
            if (multiTouch > 1) {
                startX2 = (int) event.getX(1);
                startY2 = (int) event.getY(1);
            }
        }
    }

    private static void onMouseUp(AppCompatActivity context, MotionEvent event) {
        int action = event.getActionMasked();
        if (multiTouch == 2 && action == MotionEvent.ACTION_UP && System.currentTimeMillis() - touchStartTime < 600 && !scroll){
            SocketClient.addCommand("mouseClick btn=right");
            multiTouch = 0;
        }

        if (multiTouch == 2 && !mouseDragging.equals("false") && !scroll){
            SocketClient.addCommand("mouseDragEnd " + mouseDragging);
            mouseDragging = "false";
        }

        mouseMove = false;
        if (event.getPointerCount() < 2) {
            scroll = false;
        }

        if (mouseDragging.equals("false")){
            Button leftClickBtn = context.findViewById(R.id.leftClickBtn);
            leftClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            Button rightClickBtn = context.findViewById(R.id.rightClickBtn);
            rightClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            Button midClickBtn = context.findViewById(R.id.midClickBtn);
            midClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        }
    }

    private static void onMouseMove(MotionEvent event) {
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

    private static void mouseButtonAction(View view1, MotionEvent motionEvent, String button){
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            onMouseDown(motionEvent);

            if (button.equals("left") && mouseDragging.equals("false")){
                Button leftClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.leftClickBtn);
                leftClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
            }else if (button.equals("right") && mouseDragging.equals("false")){
                Button rightClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.rightClickBtn);
                rightClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
            }else if (button.equals("middle") && mouseDragging.equals("false")){
                Button midClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.midClickBtn);
                midClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
            if (mouseDragging.equals("false")){
                SocketClient.addCommand("mouseClick btn=" + button);

                if (button.equals("left")){
                    Button leftClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.leftClickBtn);
                    leftClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
                }else if (button.equals("right")){
                    Button rightClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.rightClickBtn);
                    rightClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
                }else if (button.equals("middle")){
                    Button midClickBtn = ((AppCompatActivity)view1.getContext()).findViewById(R.id.midClickBtn);
                    midClickBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
                }
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE){
            if (Math.abs(motionEvent.getX() - startX) > 5 || Math.abs(motionEvent.getY() - startY) > 5){
                startDragging(button);
            }

            onMouseMove(motionEvent);
        }
        view1.onTouchEvent(motionEvent);
    }
}
