package com.ben.mousecontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class HiddenBufferEditText extends android.support.v7.widget.AppCompatEditText{

    private final AppCompatActivity activity;

    public HiddenBufferEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        activity = (AppCompatActivity)context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (CustomKeyboard.visible) {
                CustomKeyboard.setKeyboardVisiblity(this, false);
                CustomKeyboard.keyboardPinned = false;
                CustomKeyboard.keyCombo = false;
                TextView comboTextBox = activity.findViewById(R.id.comboTextBox);
                comboTextBox.setVisibility(CustomKeyboard.keyCombo ? View.VISIBLE : View.INVISIBLE);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd){
        super.onSelectionChanged(selStart, selEnd);

        KeyboardInterpreter.selectionChanged(activity, selStart, selEnd);
    }
}
