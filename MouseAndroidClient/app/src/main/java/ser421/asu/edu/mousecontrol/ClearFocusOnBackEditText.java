package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class ClearFocusOnBackEditText extends AppCompatEditText {

    Context context;

    public ClearFocusOnBackEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EditText ipTextBox = findViewById(R.id.ipAddrTxt);
            ipTextBox.clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}