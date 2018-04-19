package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class ClearFocusOnBackEditText extends AppCompatEditText {

    public ClearFocusOnBackEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}