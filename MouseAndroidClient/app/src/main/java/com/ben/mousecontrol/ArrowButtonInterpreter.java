package com.ben.mousecontrol;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

class ArrowButtonInterpreter {

    private static String arrowToggle = "keyboard";
    private static volatile boolean buttonPressed = false;

    @SuppressLint("ClickableViewAccessibility")
    static void startArrowButtonInterpreter(AppCompatActivity context){
        Button arrowToggleBtn = context.findViewById(R.id.arrowToggleBtn);
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("sharedPref", 0);
        arrowToggle = pref.getString("arrowToggle", "keyboard");
        if (arrowToggle.equals("keyboard")){
            KeyboardInterpreter.toggleCustomKeyPressed(arrowToggleBtn, true);
        }else if (arrowToggle.equals("mouse")){
            KeyboardInterpreter.toggleCustomKeyPressed(arrowToggleBtn, false);
        }

        arrowToggleBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = pref.edit();
            String currentValue = pref.getString("arrowToggle", "keyboard");
            if (currentValue.equals("keyboard")){
                arrowToggle = "mouse";
                KeyboardInterpreter.toggleCustomKeyPressed(arrowToggleBtn, false);
            }else if (currentValue.equals("mouse")){
                arrowToggle = "keyboard";
                KeyboardInterpreter.toggleCustomKeyPressed(arrowToggleBtn, true);
            }
            editor.putString("arrowToggle", arrowToggle);
            editor.apply();
        });

        Button leftBtn = context.findViewById(R.id.leftBtn);
        leftBtn.setOnTouchListener((view, event) -> {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\l"});
            } else {
                arrowButtonPressed(view, event, new String[]{"mouseMove -10 0"});
            }
            return true;
        });

        Button rightBtn = context.findViewById(R.id.rightBtn);
        rightBtn.setOnTouchListener((view, event) -> {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\r"});
            } else {
                arrowButtonPressed(view, event, new String[]{"mouseMove 10 0"});
            }
            return true;
        });

        Button upBtn = context.findViewById(R.id.upBtn);
        upBtn.setOnTouchListener((view, event) -> {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\u"});
            } else {
                arrowButtonPressed(view, event, new String[]{"mouseMove 0 -10"});
            }
            return true;
        });

        Button downBtn = context.findViewById(R.id.downBtn);
        downBtn.setOnTouchListener((view, event) ->  {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\d"});
            } else {
                arrowButtonPressed(view, event, new String[]{"mouseMove 0 10"});
            }
            return true;
        });

        Button rightDownBtn = context.findViewById(R.id.rightDownBtn);
        rightDownBtn.setOnTouchListener((view, event) ->  {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\r", "k \\d"});
            } else {
                arrowButtonPressed(view, event, new String[]{"mouseMove 10 10"});
            }
            return true;
        });

        Button rightUpBtn = context.findViewById(R.id.rightUpBtn);
        rightUpBtn.setOnTouchListener((view, event) ->   {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\r", "k \\u"});
            }else{
                arrowButtonPressed(view, event, new String[]{"mouseMove 10 -10"});
            }
            return true;
        });

        Button leftDownBtn = context.findViewById(R.id.leftDownBtn);
        leftDownBtn.setOnTouchListener((view, event) ->   {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\l", "k \\d"});
            }else{
                arrowButtonPressed(view, event, new String[]{"mouseMove -10 10"});
            }
            return true;
        });

        Button leftUpBtn = context.findViewById(R.id.leftUpBtn);
        leftUpBtn.setOnTouchListener((view, event) ->   {
            if (arrowToggle.equals("keyboard")) {
                arrowButtonPressed(view, event, new String[]{"k \\l", "k \\u"});
            }else{
                arrowButtonPressed(view, event, new String[]{"mouseMove -10 -10"});
            }
            return true;
        });

    }

    private static void arrowButtonPressed(View view, MotionEvent event, String command[]){
        view.performClick();

        Handler handler = new Handler();
        Runnable runnable;
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            if (!buttonPressed) {
                buttonPressed = true;
                runnable = new Runnable() {
                    public void run() {
                        if (buttonPressed) {
                            SocketClient.addCommand(command[0]);
                            if (command.length > 1){
                                for (int i = 1; i < command.length; i++){
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    SocketClient.addCommand(command[i]);
                                }
                            }
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(runnable);
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            buttonPressed = false;
        }
    }

}
