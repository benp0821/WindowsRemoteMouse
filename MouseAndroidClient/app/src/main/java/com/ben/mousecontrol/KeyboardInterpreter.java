package com.ben.mousecontrol;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

class KeyboardInterpreter {

    private static boolean ignoreLeftOrUpPress = false;
    static boolean startupDownIgnore = true;
    static boolean startupUpIgnore = true;
    static boolean startUpBackspaceIgnore = true;

    static void startKeyListener(AppCompatActivity context){

        EditText hiddenKeyBuffer = context.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setSelection(2);
        hiddenKeyBuffer.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CustomKeyboard.setKeyboardVisiblity(hiddenKeyBuffer, false);
            }
        });

        TextWatcher hiddenTextWatcher = new TextWatcher() {
            String previousText = hiddenKeyBuffer.getText().toString();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = hiddenKeyBuffer.getText().toString();

                boolean diff = false;
                int diffStartIndex = previousText.length() - 2;
                String value = "";

                if (hiddenKeyBuffer.getText().toString().equals("//")) {
                    hiddenKeyBuffer.append("/");
                    value += "\\b";
                    ignoreLeftOrUpPress = true;
                }

                if (hiddenKeyBuffer.getText().toString().equals("///")) {
                    hiddenKeyBuffer.append("/");
                    value += "\\b";
                    ignoreLeftOrUpPress = true;
                }

                for (int i = 2; i < previousText.length() - 2; ) {
                    if (i < text.length() - 2) {
                        if (!diff && previousText.codePointAt(i) != (text.codePointAt(i))) {
                            diff = true;
                            diffStartIndex = i;
                        }

                        if (diff) {
                            value += "\\b";
                        }
                    } else {
                        value += "\\b";
                    }

                    int codePoint = previousText.codePointAt(i);
                    i += Character.charCount(codePoint);
                }

                for (int i = diffStartIndex; i < text.length() - 2; ) {
                    int codePoint = text.codePointAt(i);
                    if (Character.isBmpCodePoint(codePoint)) {
                        value += (char) codePoint;
                    } else if (Character.isValidCodePoint(codePoint)) {
                        value += Character.highSurrogate(codePoint);
                        value += Character.lowSurrogate(codePoint);
                    } else {
                        value += '?';
                    }

                    i += Character.charCount(codePoint);
                }

                value = value.replace("\t", "\\t");
                value = value.replace(" ", "\\s");

                previousText = hiddenKeyBuffer.getText().toString();

                if (startUpBackspaceIgnore){
                    value = "";
                    startUpBackspaceIgnore = false;
                }

                if (!value.equals("")) {
                    SocketClient.addCommand("k " + value);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        hiddenKeyBuffer.addTextChangedListener(hiddenTextWatcher);

        hiddenKeyBuffer.setOnEditorActionListener((textView, keyCode, event) -> {
            if (keyCode == EditorInfo.IME_ACTION_NEXT) {
                SocketClient.addCommand("k \\n");
            }
            return true;
        });
    }

    static void selectionChanged(AppCompatActivity activity, int selStart, int selEnd){
        if (activity == null){
            return;
        }

        EditText hiddenKeyBuffer = activity.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        int sel = hiddenKeyBuffer.getText().toString().length()-2;

        if ((selStart == sel - 1 && selEnd == sel) || selStart == sel && selEnd == sel - 1){
            SocketClient.addCommand("k \\hl"); //highlight left
        }

        if ((selStart == sel + 1 && selEnd == sel) || selStart == sel && selEnd == sel + 1){
            SocketClient.addCommand("k \\hr"); //hightlight right
        }

        if ((selStart == 0 && selEnd == sel + 2) || selStart == sel + 2 && selEnd == 0){
            SocketClient.addCommand("k \\ha"); //hightlight all
        }

        if ((selStart == 0 && selEnd == sel) || (selStart == sel && selEnd == 0)){
            SocketClient.addCommand("k \\hu"); //hightlight up
        }

        if ((selStart == sel + 2 && selEnd == sel) || (selStart == sel && selEnd == sel + 2)){
            SocketClient.addCommand("k \\hd"); //hightlight down
        }

        if (selStart == sel - 1 && selEnd == sel - 1) {
            if (!ignoreLeftOrUpPress) {
                SocketClient.addCommand("k \\l"); //left
            }else{
                ignoreLeftOrUpPress = false;
            }
        }

        if (selStart == sel + 1 && selEnd == sel + 1) {
            SocketClient.addCommand("k \\r"); //right
        }

        if (selStart == 0 && selEnd == 0) {
            if (!startupUpIgnore) {
                if (!ignoreLeftOrUpPress) {
                    SocketClient.addCommand("k \\u"); //up
                } else {
                    ignoreLeftOrUpPress = false;
                }
            }else{
                startupUpIgnore = false;
            }
        }

        if (selStart == sel + 2 && selEnd == sel + 2) {
            if (!startupDownIgnore) {
                SocketClient.addCommand("k \\d"); //down
            }else{
                startupDownIgnore = false;
            }
        }

        hiddenKeyBuffer.setSelection(hiddenKeyBuffer.getText().length() - 2);
    }

}
