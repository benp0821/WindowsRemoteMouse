package ser421.asu.edu.mousecontrol;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureInterpreter implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
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
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    private float initX = 0;
    private float initY = 0;

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        initX = motionEvent.getX();
        initY = motionEvent.getY();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(motionEvent.getX() - initX) > 20 || Math.abs(motionEvent.getY() - initY) > 20) {
                MainActivity.mouseDrag = true;
                System.out.println("mouse drag");
                return true;
            }
        }
        return false;
    }
}
