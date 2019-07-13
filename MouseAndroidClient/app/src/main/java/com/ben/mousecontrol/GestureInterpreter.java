package com.ben.mousecontrol;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureInterpreter implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

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
                if (!MainActivity.mouseDragging) {
                    SocketClient.addCommand("mouseDrag");
                    MainActivity.mouseDragging = true;
                }
        }else if (motionEvent.getAction() == MotionEvent.ACTION_UP && !MainActivity.mouseDragging){
            SocketClient.addCommand("mouseClick");
        }
        return true;
    }
}
