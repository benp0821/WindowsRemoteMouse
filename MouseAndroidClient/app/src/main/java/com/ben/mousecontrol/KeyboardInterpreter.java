package com.ben.mousecontrol;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class KeyboardInterpreter {

    private static int ignoreArrowPress = 0;

    public static void startKeyListener(AppCompatActivity context){

        EditText hiddenKeyBuffer = context.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);
        hiddenKeyBuffer.setSelection(2);

        TextWatcher hiddenTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = hiddenKeyBuffer.getText().toString();
                String value = "";
                if (text.length() > 4) {
                    value = text.substring(2, text.length() - 2);
                }

                if (value.equals("\t")) {
                    value = "\\t";
                }

                if (hiddenKeyBuffer.getSelectionStart() == 1 && text.length() == 3){
                    value = "\\b";
                }

                if (value.equals(" ")){
                    value = "\\s";
                }

                if (!value.equals("")) {
                    SocketClient.addCommand("k " + value);
                }
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

    public static void selectionChanged(AppCompatActivity activity, int selStart, int selEnd){
        if (activity == null){
            return;
        }

        if ((selStart == 1 && selEnd == 2) || selStart == 2 && selEnd == 1){
            SocketClient.addCommand("k \\hl"); //highlight left
        }

        if ((selStart == 3 && selEnd == 2) || selStart == 2 && selEnd == 3){
            SocketClient.addCommand("k \\hr"); //hightlight right
        }

        if ((selStart <= 1 && selEnd >= 3) || selStart >= 3 && selEnd <= 1){
            SocketClient.addCommand("k \\ha"); //hightlight all
        }

        if ((selStart == 0 && selEnd == 2) || (selStart == 2 && selEnd == 0)){
            SocketClient.addCommand("k \\hu"); //hightlight up
        }

        if ((selStart == 4 && selEnd == 2) || (selStart == 2 && selEnd == 4)){
            SocketClient.addCommand("k \\hd"); //hightlight down
        }

        EditText hiddenKeyBuffer = activity.findViewById(com.ben.mousecontrol.R.id.hiddenKeyBuffer);


        if (ignoreArrowPress == 0) {
            if (selStart == 1 && selEnd == 1 && hiddenKeyBuffer.getText().toString().length() == 4) {
                SocketClient.addCommand("k \\l"); //left
            }

            if (selStart == 3 && selEnd == 3 && hiddenKeyBuffer.getText().toString().length() == 4) {
                SocketClient.addCommand("k \\r"); //right
            }

            if (selStart == 0 && selEnd == 0 && hiddenKeyBuffer.getText().toString().length() == 4) {
                SocketClient.addCommand("k \\u"); //up
            }

            if (selStart == 4 && selEnd == 4 && hiddenKeyBuffer.getText().toString().length() == 4) {
                SocketClient.addCommand("k \\d"); //down
            }
        }else{
            ignoreArrowPress--;
        }

        if (!hiddenKeyBuffer.getText().toString().equals("////")){
            hiddenKeyBuffer.getText().replace(0, hiddenKeyBuffer.getText().length(), "////");
            ignoreArrowPress = 4;
        }

        hiddenKeyBuffer.setSelection(2, 2);
    }

}
