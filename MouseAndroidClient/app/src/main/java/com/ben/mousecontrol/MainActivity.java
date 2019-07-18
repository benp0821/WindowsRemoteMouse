package com.ben.mousecontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    SocketClient client;
    Thread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ben.mousecontrol.R.layout.activity_main);

        KeyboardInterpreter.startKeyListener(this);
        GestureInterpreter.startGestureInterpreter(this);
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