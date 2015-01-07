package core.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * @author dingwei.chen1988@gmail.com
 * */
public class AbsAdapterView extends AdapterView<BaseAdapter> {

    protected BaseAdapter mBaseAdapter = null;
    private DataSetObserver mDataSetObserver = new ObserverImpl();
    protected static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private RecycleBin<View> mRecycleBin = new RecycleBin<View>(10);
    protected static final LayoutParams PARAMS = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private boolean mIsDataChange = false;
    private int mSelectionPosition = 0;
    private int mLongPressTimeOut = 0;
    private int mTouchSlop;
    protected LongPressAction mLongPressAction = new LongPressAction();
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private boolean mIsInit = false;
    public AbsAdapterView(Context context) {
        super(context);
        this.init(context);
    }
    public AbsAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }
    public AbsAdapterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context);
    }
    protected final boolean isDataChange() {
        return this.mIsDataChange;
    }

    protected void init(Context context) {
        if (mIsInit) {
            return ;
        }
        mIsInit = true;
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mLongPressTimeOut = ViewConfiguration.getLongPressTimeout();
        mMaximumVelocity = ViewConfiguration.getMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.getMinimumFlingVelocity();
    }


    protected final int getLongPressTime() {
        return this.mLongPressTimeOut;
    }

    protected final int getMaximumVelocity() {
        return this.mMaximumVelocity;
    }

    protected final int getMinimumVelocity() {
        return this.mMinimumVelocity;
    }

    protected final View obtainView(int position,View convertView) {
        if (position < 0 || position > this.getDataCount() - 1) {
            return null;
        }
        return this.mBaseAdapter.getView(position, convertView, this);
    }

    protected final int getTouchSlop() {
        return this.mTouchSlop;
    }

    protected final void setIsDataChange(boolean isDataChange) {
        this.mIsDataChange = isDataChange;
    }

    @Override
    public final BaseAdapter getAdapter() {
        // TODO Auto-generated method stub
        return mBaseAdapter;
    }

    @Override
    public final View getSelectedView() {
        // TODO Auto-generated method stub
        return null;
    }

    protected final void innerAddViewFromTail(View view) {
        this.addViewInLayout(view, -1, PARAMS);
    }

    protected final void innerAddViewFromHead(View view) {
        this.addViewInLayout(view, 0, PARAMS);
    }

    protected final void innerRemoveView(View view) {
        this.removeViewInLayout(view);
    }

    @Override
    public final void setAdapter(BaseAdapter adapter) {
        if (mBaseAdapter != adapter) {
            if (mBaseAdapter != null) {
                mBaseAdapter.unregisterDataSetObserver(mDataSetObserver);
            }
            mBaseAdapter = adapter;
            if (mBaseAdapter != null) {
                mBaseAdapter.registerDataSetObserver(mDataSetObserver);
                mBaseAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public final void setSelection(int position) {
        int prePosition = this.mSelectionPosition;
        this.mSelectionPosition = position;
        this.onSelectionChange(prePosition,position);
    }

    protected void onSelectionChange(int prePostion,int position) {}

    public final int getSelection() {
        return this.mSelectionPosition;
    }

    protected void performDataSetChange() {
        recycleAllViews();
        mIsDataChange = true;
        this.requestLayout();
    }

    protected final View obtainView(int position) {
        if (position < 0 || position > this.getDataCount() - 1) {
            return null;
        }
        return this.mBaseAdapter.getView(position, mRecycleBin.obtain(), this);
    }

    protected final int getDataCount() {
        if (this.mBaseAdapter != null) {
            return this.mBaseAdapter.getCount();
        }
        return 0;
    }

    protected final void recycleAllViews() {
        final int count = this.getChildCount();
        int index = 0;
        View view = null;
        List<View> views = new ArrayList<View>(this.getChildCount());
        while (index < count) {
            view = this.getChildAt(index);
            if (isRecycle(view)) {
                mRecycleBin.recycle(view);
                views.add(view);
            }
            index++;
        }
        for (View v:views) {
            this.removeViewInLayout(v);
        }
    }

    protected boolean isRecycle(View view) {
        return true;
    }

    private class ObserverImpl extends DataSetObserver {
        @Override
        public void onChanged() {
            performDataSetChange();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }

    protected final void recycleView(View view) {
        if (view == null) {
            return;
        }
        this.removeViewInLayout(view);
        mRecycleBin.recycle(view);
    }

    protected void performLongPress() {}

    protected class LongPressAction implements Runnable {
        public void start() {
            this.cancel();
            HANDLER.postDelayed(this, mLongPressTimeOut);
        }

        public void cancel() {
            HANDLER.removeCallbacks(this);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            performLongPress();
        }
    }

}
