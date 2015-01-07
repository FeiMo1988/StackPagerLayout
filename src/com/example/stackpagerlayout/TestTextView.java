package com.example.stackpagerlayout;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

public class TestTextView extends TextView {

    public TestTextView(Context context) {
        super(context);
        this.setGravity(Gravity.CENTER);
        this.setTextSize(20);
    }

    public void setMessage(String msg,int color) {
        this.setBackgroundColor(color);
        this.setText(msg);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
         super.dispatchTouchEvent(event);
         return true;
    }

}
