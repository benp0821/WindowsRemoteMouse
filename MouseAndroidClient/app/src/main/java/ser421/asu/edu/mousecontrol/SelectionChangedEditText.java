package ser421.asu.edu.mousecontrol;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;

public class SelectionChangedEditText extends AppCompatEditText {

    Context context;

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
            MainActivity.keyboardBuf += "\\u";
        }else if (selEnd == getText().toString().length()){
            //System.out.println("down key pressed");
            MainActivity.keyboardBuf += "\\d";
        }else if (selEnd == getText().toString().length()-3){
            //System.out.println("left key pressed");
            if (!MainActivity.ignoreLeftArrow){
                MainActivity.keyboardBuf += "\\l";
            }else {
                MainActivity.ignoreLeftArrow = false;
            }
        }else if (selEnd == getText().toString().length()-1){
            //System.out.println("right key pressed");
            MainActivity.keyboardBuf += "\\r";
        }

        Log.i("MyEditText", "onSelectionChanged: " + selStart + " - " + selEnd);
        if (getText().length() >= 4) {
            setSelection(getText().toString().length() - 2);
        }
    }
}
