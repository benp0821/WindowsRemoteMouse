package com.ben.mousecontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

class CustomKeyboard {

    static boolean visible = false;
    static boolean keyboardPinned = false;
    static boolean keyCombo = false;

    static void setKeyboardVisiblity(EditText textBox, boolean isVisible) {
        textBox.requestFocus();
        InputMethodManager imm = (InputMethodManager) textBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        visible = imm != null && isVisible;

        Button comboBtn = ((Activity) textBox.getContext()).findViewById(R.id.comboBtn);
        Button ctrlBtn = ((Activity) textBox.getContext()).findViewById(R.id.ctrlBtn);
        Button altBtn = ((Activity) textBox.getContext()).findViewById(R.id.altBtn);
        Button shiftBtn = ((Activity) textBox.getContext()).findViewById(R.id.shiftBtn);
        Button winBtn = ((Activity) textBox.getContext()).findViewById(R.id.winBtn);
        Button cpyBtn = ((Activity) textBox.getContext()).findViewById(R.id.cpyBtn);
        Button pasteBtn = ((Activity) textBox.getContext()).findViewById(R.id.pasteBtn);
        Button pinBtn = ((Activity) textBox.getContext()).findViewById(R.id.pinBtn);
        CustomKeyButton[] keyboardBtns = {new CustomKeyButton(comboBtn, false),
                                          new CustomKeyButton(ctrlBtn, true),
                                          new CustomKeyButton(altBtn, true),
                                          new CustomKeyButton(shiftBtn, true),
                                          new CustomKeyButton(winBtn, true),
                                          new CustomKeyButton(cpyBtn, false),
                                          new CustomKeyButton(pasteBtn, false),
                                          new CustomKeyButton(pinBtn, false)};

        Button leftBtn = ((Activity) textBox.getContext()).findViewById(R.id.leftClickBtn);
        Button midBtn = ((Activity) textBox.getContext()).findViewById(R.id.midClickBtn);
        Button rightBtn = ((Activity) textBox.getContext()).findViewById(R.id.rightClickBtn);
        Button abcButton = ((Activity) textBox.getContext()).findViewById(R.id.keyboardButton);
        Button[] mouseBtns = {leftBtn, midBtn, rightBtn, abcButton};

        if (visible) {
            if (imm != null) {
                for (Button b : mouseBtns) {
                    b.setVisibility(View.INVISIBLE);
                }

                for (CustomKeyButton b : keyboardBtns) {
                    b.keyboardButton.setVisibility(View.VISIBLE);
                    b.keyboardButton.setOnTouchListener((v, event) -> {
                        int action = event.getActionMasked();

                        if (v.getId() != R.id.pinBtn && v.getId() != R.id.comboBtn) {
                            if (!keyCombo) {
                                if (action == MotionEvent.ACTION_DOWN) {
                                    toggleCustomKeyPressed(b.keyboardButton, true);
                                }

                                if (action == MotionEvent.ACTION_UP) {
                                    toggleCustomKeyPressed(b.keyboardButton, false);
                                }
                            }else{
                                if (action == MotionEvent.ACTION_DOWN && b.isModKey) {
                                    b.modKeyStatus = !b.modKeyStatus;
                                    toggleCustomKeyPressed(b.keyboardButton, b.modKeyStatus);
                                }
                            }
                        }

                        if (v.getId() == R.id.pinBtn){
                            if (action == MotionEvent.ACTION_DOWN) {
                                keyboardPinned = !keyboardPinned;

                                toggleCustomKeyPressed(v, keyboardPinned);
                            }
                        }

                        if (v.getId() == R.id.comboBtn){
                            if (action == MotionEvent.ACTION_DOWN) {
                                keyCombo = !keyCombo;

                                toggleCustomKeyPressed(v, keyCombo);
                                TextView comboTextBox = ((Activity) textBox.getContext()).findViewById(R.id.comboTextBox);
                                comboTextBox.setVisibility(keyCombo ? View.VISIBLE : View.INVISIBLE);

                                for (CustomKeyButton btn : keyboardBtns) {
                                    if (!keyCombo) {
                                        btn.modKeyStatus = false;
                                        if (btn.keyboardButton.getId() != R.id.pinBtn) {
                                            toggleCustomKeyPressed(btn.keyboardButton, false);
                                        }
                                    }else{
                                        if (!btn.isModKey && btn.keyboardButton.getId() != R.id.comboBtn && btn.keyboardButton.getId() != R.id.pinBtn){
                                            btn.keyboardButton.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.argb(25, 0, 0, 0), PorterDuff.Mode.SRC));
                                        }
                                    }
                                }
                            }
                        }

                        return false;
                    });
                }

                imm.showSoftInput(textBox, InputMethodManager.SHOW_IMPLICIT);
                if (!imm.isAcceptingText()){
                    visible = false;
                    keyboardPinned = false;
                    keyCombo = false;
                    TextView comboTextBox = ((Activity) textBox.getContext()).findViewById(R.id.comboTextBox);
                    comboTextBox.setVisibility(keyCombo ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }

        if (!visible){
            for (Button b : mouseBtns) {
                b.setVisibility(View.VISIBLE);
            }

            keyCombo = false;
            TextView comboTextBox = ((Activity) textBox.getContext()).findViewById(R.id.comboTextBox);
            comboTextBox.setVisibility(keyCombo ? View.VISIBLE : View.INVISIBLE);

            for (CustomKeyButton b : keyboardBtns) {
                b.keyboardButton.setVisibility(View.INVISIBLE);
                toggleCustomKeyPressed(b.keyboardButton, false);
            }

            if (imm != null) {
                imm.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
            }

        }
    }

    static void toggleCustomKeyPressed(View view, boolean isPressed){
        if (isPressed){
            view.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC));
        }else{
            view.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC));
        }
    }

}
