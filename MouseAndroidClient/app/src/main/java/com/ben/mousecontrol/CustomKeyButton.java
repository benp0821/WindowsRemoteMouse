package com.ben.mousecontrol;

import android.widget.Button;

public class CustomKeyButton {

    public Button keyboardButton = null;
    public boolean isModKey = false;
    public boolean modKeyStatus = false;

    public CustomKeyButton(Button keyboardButton, boolean isModKey){
        this.keyboardButton = keyboardButton;
        this.isModKey = isModKey;
        this.modKeyStatus = false;
    }

}
