package com.ben.mousecontrol;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;

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
                KeyboardInterpreter.keyboardPinned = false;
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
