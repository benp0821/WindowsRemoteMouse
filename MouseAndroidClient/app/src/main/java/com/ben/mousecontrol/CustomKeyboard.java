package com.ben.mousecontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class CustomKeyboard {

    public static boolean visible = false;

    public static void setKeyboardVisiblity(EditText textBox, boolean isVisible){
        visible = isVisible;

        Button abcButton = ((Activity)textBox.getContext()).findViewById(R.id.keyboardButton);
        Button ctrlBtn = ((Activity)textBox.getContext()).findViewById(R.id.ctrlBtn);
        Button altBtn = ((Activity)textBox.getContext()).findViewById(R.id.altBtn);
        Button shiftBtn = ((Activity)textBox.getContext()).findViewById(R.id.shiftBtn);
        Button winBtn = ((Activity)textBox.getContext()).findViewById(R.id.winBtn);
        Button pinBtn = ((Activity)textBox.getContext()).findViewById(R.id.pinBtn);

        Button leftBtn = ((Activity)textBox.getContext()).findViewById(R.id.leftClickBtn);
        Button midBtn = ((Activity)textBox.getContext()).findViewById(R.id.midClickBtn);
        Button rightBtn = ((Activity)textBox.getContext()).findViewById(R.id.rightClickBtn);

        if (isVisible){
            abcButton.setVisibility(View.INVISIBLE);
            leftBtn.setVisibility(View.INVISIBLE);
            rightBtn.setVisibility(View.INVISIBLE);
            midBtn.setVisibility(View.INVISIBLE);

            ctrlBtn.setVisibility(View.VISIBLE);
            altBtn.setVisibility(View.VISIBLE);
            shiftBtn.setVisibility(View.VISIBLE);
            winBtn.setVisibility(View.VISIBLE);
            pinBtn.setVisibility(View.VISIBLE);

            textBox.requestFocus();
            InputMethodManager imm = (InputMethodManager) textBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(textBox, InputMethodManager.SHOW_IMPLICIT);
            }
            //hiddenKeyBuffer.setSelection(hiddenKeyBuffer.getText().length()-2);

        }else{
            abcButton.setVisibility(View.VISIBLE);
            leftBtn.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.VISIBLE);
            midBtn.setVisibility(View.VISIBLE);

            ctrlBtn.setVisibility(View.INVISIBLE);
            altBtn.setVisibility(View.INVISIBLE);
            shiftBtn.setVisibility(View.INVISIBLE);
            winBtn.setVisibility(View.INVISIBLE);
            pinBtn.setVisibility(View.INVISIBLE);

            ctrlBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            altBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            shiftBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            winBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
            pinBtn.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));

            InputMethodManager inputMethodManager = (InputMethodManager)textBox.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
            }

        }
    }

}
