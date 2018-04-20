package ser421.asu.edu.mousecontrol;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

public class SelectionChangedEditText extends AppCompatEditText {

    private final Context context;

    public SelectionChangedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selStart, selEnd);

        if (selEnd == 0){
            //System.out.println("up key pressed");
            new MainActivity.CommandSender().execute("keyboard", "\\u");
        }else if (selEnd == getText().toString().length()){
            //System.out.println("down key pressed");
            new MainActivity.CommandSender().execute("keyboard", "\\d");
        }else if (selEnd == getText().toString().length()-3){
            //System.out.println("left key pressed");
            if (!MainActivity.ignoreLeftArrow){
                new MainActivity.CommandSender().execute("keyboard", "\\l");
            }else {
                MainActivity.ignoreLeftArrow = false;
            }
        }else if (selEnd == getText().toString().length()-1){
            //System.out.println("right key pressed");
            new MainActivity.CommandSender().execute("keyboard", "\\r");
        }

        Log.i("MyEditText", "onSelectionChanged: " + selStart + " - " + selEnd);
        if (getText().length() >= 4) {
            setSelection(getText().toString().length() - 2);
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MainActivity.setKeyboardToolbarVisiblity(context, false);
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
