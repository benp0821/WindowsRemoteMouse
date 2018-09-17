package ser421.asu.edu.mousecontrol;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private GestureDetector detector;
    SocketClient client;
    Thread thread;
    int startX, startY;
    static boolean mouseMove = false;
    static boolean mouseDrag = false;
    int multiTouch = 0;
    long touchStartTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
            }
        });

        View view = findViewById(R.id.layout);

        detector = new GestureDetector(this, new GestureInterpreter());
        @SuppressLint("ClickableViewAccessibility") View.OnTouchListener touchListener = (v, event) -> {
            int action = event.getActionMasked();
            if(detector.onTouchEvent(event)){
                return true;
            }else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
                multiTouch = event.getPointerCount();

                if (multiTouch == 1){
                    touchStartTime = System.currentTimeMillis();
                    mouseMove = true;
                }else {
                    mouseMove = false;
                }
                mouseDrag = false;

                if (action == MotionEvent.ACTION_DOWN) {
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                }

                return true;
            }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
                if (multiTouch == 2 && action == MotionEvent.ACTION_UP && System.currentTimeMillis() - touchStartTime < 600){
                    SocketClient.addCommand("mouseClick btn=right");
                    multiTouch = 0;
                }

                mouseMove = false;
                mouseDrag = false;
                return true;
            }else if (action == MotionEvent.ACTION_MOVE){
                int difX = (int)event.getX() - startX;
                int difY = (int)event.getY() - startY;
                startX = (int)event.getX();
                startY = (int)event.getY();
                if (mouseMove && !mouseDrag) {
                    SocketClient.addCommand("mouseMove " + (difX * 5) + " " + (difY * 5));
                }else if (mouseMove){
                    SocketClient.addCommand("mouseDrag " + (difX * 5) + " " + (difY * 5));
                }
                return true;
            }

            return true;
        };

        view.setOnTouchListener(touchListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
        CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);

        client = new SocketClient(this, "192.168.1.111", 8888);
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
        EditText hiddenKeyBuffer = findViewById(R.id.hiddenKeyBuffer);
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