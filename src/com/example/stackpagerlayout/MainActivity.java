package com.example.stackpagerlayout;

import core.views.StackPagerLayout;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {

    private StackPagerLayout mStackPagerLayout = null;
    private int mCount = 10;
    private TestAdapter mTestAdapter = new TestAdapter();
    private int[] mColors = new int[] {
            Color.RED,
            Color.YELLOW,
            Color.BLUE
    };

    private View mGoPre;
    private View mGoNext;
    private View mGoPosition;
    private View mSmoothAdd;
    private EditText mGoPostionText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStackPagerLayout = (StackPagerLayout)this.findViewById(R.id.stackPagerLayout);
        mStackPagerLayout.setAdapter(mTestAdapter);
        mGoPre = this.findViewById(R.id.moveToPre);
        mGoNext = this.findViewById(R.id.moveToNext);
        mGoPosition = this.findViewById(R.id.gotoPostionBtn);
        mSmoothAdd = this.findViewById(R.id.smoothAdd);
        mGoPostionText = (EditText)this.findViewById(R.id.gotoPostionText);
        this.mGoPre.setOnClickListener(this);
        this.mGoNext.setOnClickListener(this);
        this.mSmoothAdd.setOnClickListener(this);
        this.mGoPosition.setOnClickListener(this);
    }

    private class TestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mCount;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int color = mColors[position%mColors.length];
            TestTextView tv = null;
            if (convertView != null) {
                tv = (TestTextView)convertView;
            } else {
                tv = new TestTextView(getApplicationContext());
            }
            tv.setMessage("TEXT:"+position, color);
            return tv;
        }}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mGoPre) {
            mStackPagerLayout.movePre();
        }
        if (v == mGoNext) {
            mStackPagerLayout.moveNext();
        }
        if (v == mSmoothAdd) {
            mCount ++;
            mStackPagerLayout.reloadAndSmoothMoveNext();
        }
        if (v == mGoPosition) {
            try {
               String text = ""+ mGoPostionText.getText();
               int position = Integer.parseInt(text);
               mStackPagerLayout.smoothMoveToPosition(position);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

}
