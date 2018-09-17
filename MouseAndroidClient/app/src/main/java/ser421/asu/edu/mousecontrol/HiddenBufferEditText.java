package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class HiddenBufferEditText extends android.support.v7.widget.AppCompatEditText{
    public HiddenBufferEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (CustomKeyboard.visible) {
                CustomKeyboard.setKeyboardVisiblity(this, false);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
