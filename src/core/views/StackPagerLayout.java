package core.views;

import com.example.stackpagerlayout.R;

import android.graphics.Picture;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

/**
 * @author dingwei.chen1988@gmail.com
 * */
public class StackPagerLayout extends AbsAdapterView {

    private int mPreWidthMeasureSpec;
    private int mPreHeightMeasureSpec;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private static final int PRE = 0;
    private static final int CURRENT = 1;
    private static final int NEXT = 2;
    private StackItem mPreStackItem = new StackItem(PRE);
    private StackItem mCurrentStackItem = new StackItem(CURRENT);
    private StackItem mNextStackItem = new StackItem(NEXT);
    private StackItem mShadowStackItem = null;
    private static final int MAX_EDGE_OFFSET = 60;
    private int mStartMotionX;
    private int mStartMotionY;
    private int mMotionX;
    private int mMotionY;
    private EDGE_STATE mEdgeState = null;
    private DRAG_STATE mDragState = null;
    private static final int TIME = 600;
    private DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator(
            1.2f);
    private static final float MIN_SCALE = .9F;
    private static final float MIN_ALPHA = .5F;
    private VelocityTracker mVelocityTracker = null;
    private static final int UNIT = 1000;
    private static final int MAX_VELOCITY = 2000;
    private int mShadowRight = -1;
    private float mShadowAlpha = 0;
    private OnStackPagerLayoutListener mOnStackPagerLayoutListener = null;
    private static final Paint PAINT = new Paint();
    private Drawable mShadowLeft = null;
    private boolean isNotifyAnimatorStart = false;
    private static final int[] ATTRS = new int[] {
        R.attr.stackpagerlayout_leftshadow_src
    };

    private enum DRAG_STATE {
        EDGE_DRAG, NOT_EDGE_DRAG
    }

    private enum EDGE_STATE {
        EDGE_LEFT, EDGE_RIGHT, NOT_EDGE
    }

    private void getMotionFromEvent(MotionEvent event) {
        this.mMotionX = (int) event.getX(0);
        this.mMotionY = (int) event.getY(0);
    }

    private void savePreMotion() {
        //TODO
//        this.mPreMotionX = this.mMotionX;
//        this.mPreMotionY = this.mMotionY;
    }

    private StackItem[] mStackItems = new StackItem[] { mPreStackItem,
            mCurrentStackItem, mNextStackItem };

    private void notifyAnimatorStart() {
        if (!isNotifyAnimatorStart) {
            if (mOnStackPagerLayoutListener != null) {
                mOnStackPagerLayoutListener.onStackPagerAnimationStart();
                isNotifyAnimatorStart = true;
            }
        }
    }

    private void notifyAnimatorEnd() {
        if (isNotifyAnimatorStart) {
            if (mOnStackPagerLayoutListener != null) {
                mOnStackPagerLayoutListener.onStackPagerAnimationEnd();
                isNotifyAnimatorStart = false;
            }
        }
    }

    private class StackItem extends AnimatorListenerAdapter {

        public View view;
        public int index = -1;
        public int type = -1;
        public boolean isReLoad = false;
        public ObjectAnimator mValueAnimator = new ObjectAnimator();
        private OverScroller mOverScroller = null;
        public StackItem(int type) {
            this.type = type;
            mOverScroller = new OverScroller(getContext(),mDecelerateInterpolator);
        }

        public void replaceStackItem(StackItem item) {
            this.view = item.view;
            int destIndex = item.index;
            item.view = null;
            item.index = -1;
            this.movePositionIndexTo(destIndex);
        }

        public void performDragToX(int x) {
            this.setLocX(x);
        }

        public void updateIndex(int index) {
            this.index = index;
        }

        public boolean setPositionIndex(int index) {
            if (this.index != index) {
                this.index = index;
                if (this.index >= 0 && this.index < getDataCount()
                        && this.type == CURRENT) {
                    setSelection(this.index);
                    if (mOnStackPagerLayoutListener != null) {
                        mOnStackPagerLayoutListener.onStackPagerPositionChange(this.index);
                    }
                }
            }
            if (this.view == null) {
                this.attach();
                if (this.view != null) {
                    return true;
                }
            }
            return false;
        }

        public void setPositionIndexAndLayout(int index) {
            setPositionIndex(index);
            this.layout();
        }

        public void cancelAnimator() {
            /*if(mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }*/
            mOverScroller.abortAnimation();
        }

        public boolean isAnimator() {
            return mOverScroller.computeScrollOffset() && this.view!= null && !mOverScroller.isFinished();
        }

        public boolean onAnimator() {
            if (isAnimator()) {
                this.setLocX(mOverScroller.getCurrX());
                if (this.getLocX() == mOverScroller.getFinalX()) {
                    mOverScroller.abortAnimation();
                    this.onEnd();
                    return true;
                }
                return false;
            }
            this.onEnd();
            return true;
        }

        public void startAnimator() {
            if (this.view != null) {
                this.cancelAnimator();
                notifyAnimatorStart();
                /*mValueAnimator.setDuration(TIME)
                              .setInterpolator(mDecelerateInterpolator);
                mValueAnimator.setIntValues(new int[]{getLocX(),getXByType(this.type)});
                mValueAnimator.setTarget(this);
                mValueAnimator.setPropertyName("locX");
                mValueAnimator.addListener(this);
                mValueAnimator.start();
                if (mOnStackPagerLayoutListener != null) {
                    mOnStackPagerLayoutListener.onAnimationStart();
                }*/
                mOverScroller.startScroll(getLocX(), 0, getXByType(this.type) - getLocX(), 0, TIME);
                postInvalidateOnAnimation();
            }
        }

        public int getLocX() {
            if (this.view != null) {
                return (int)this.view.getTranslationX();
            }
            return 0;
        }

        public void setLocX(int x) {
            if (this.view != null) {
                this.view.setTranslationX(x);
            }
            if (mShadowStackItem == this) {
                float fx = x;
                float alpha = (getWidth() - fx) / getWidth() * MIN_ALPHA;
                setShadowRightAndAlpha(this.getLocX(),alpha);
            }
        }

        public void movePositionIndexTo(int index) {
            setPositionIndex(index);
            if (view != null) {
                startAnimator();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onEnd();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            onEnd();
        }

        private void onEnd() {
            if (isReLoad) {
                isReLoad = false;
                this.detach();
                this.attach();
                this.layout();
            }
            if (mShadowStackItem == this) {
                mShadowStackItem = null;
            }
            if (mOnStackPagerLayoutListener != null) {
                mOnStackPagerLayoutListener.onStackPagerAnimationEnd();
            }
        }

        public void attach() {
            if (view == null) {
                view = obtainView(index);
                if (view != null) {
                    attachByType(view, this.type);
                }
            }
        }

        public void layout() {
            this.cancelAnimator();
            if (view != null) {
                view.measure(mViewWidth | MeasureSpec.EXACTLY, mViewHeight
                        | MeasureSpec.EXACTLY);
                view.layout(0, 0, view.getMeasuredWidth(),
                        view.getMeasuredHeight());
                this.setLocX(getXByType(type));
            }
        }

        public void detach() {
            if (view != null) {
                this.isReLoad = false;
                innerRemoveView(view);
                view = null;
            }
        };
    }

    private void attachByType(View view, int type) {
        if (view == null) {
            return;
        }
        switch (type) {
        case PRE:
            this.innerAddViewFromHead(view);
            break;
        case CURRENT:
            this.innerAddViewFromTail(view);
            break;
        case NEXT:
            this.innerAddViewFromTail(view);
            break;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        int animatorNumber = 0;
        for (StackItem item:mStackItems) {
            if (item.isAnimator()) {
                if (!item.onAnimator()) {
                    animatorNumber ++;
                }
            }
        }
        if (animatorNumber > 0) {
            this.postInvalidateOnAnimation();
        } else {
            if (this.mDragState != DRAG_STATE.EDGE_DRAG) {
                notifyAnimatorEnd();
            }
        }
    }

    private int getXByType(int type) {
        int x = 0;
        switch (type) {
        case PRE:
            x = -((mViewWidth << 1) / 3);
            break;
        case CURRENT:
            x = 0;
            break;
        case NEXT:
            x = mViewWidth;
            break;
        }
        return x;
    }

    private float getScaleByType(int type) {
        float x = 0;
        switch (type) {
        case PRE:
            x = .9f;
            break;
        case CURRENT:
            x = 1;
            break;
        case NEXT:
            x = 1;
            break;
        }
        return x;
    }

    private float getAlphaByType(int type) {
        float x = 0;
        switch (type) {
        case PRE:
            x = MIN_ALPHA;
            break;
        case CURRENT:
            x = 1;
            break;
        case NEXT:
            x = 1;
            break;
        }
        return x;
    }

    private void recycleAllStackItems() {
        for (StackItem item : mStackItems) {
            item.detach();
        }
    }

    public StackPagerLayout(Context context, AttributeSet set) {
        super(context, set);
        TypedArray array = context.obtainStyledAttributes(set, ATTRS);
        init(array);
        array.recycle();
    }

    private void init(TypedArray array) {
        mShadowLeft = array.getDrawable(0);
    }

    public StackPagerLayout(Context context) {
        super(context);
        TypedArray array = context.getTheme().obtainStyledAttributes(ATTRS);
        init(array);
        array.recycle();
    }

    @Override
    protected boolean isRecycle(View view) {
        return true;
    }

    @Override
    protected void performDataSetChange() {
        super.performDataSetChange();
        recycleAllStackItems();
    }

    private VelocityTracker obtainVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        return this.mVelocityTracker;
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mViewWidth = width;
        mViewHeight = height;
        boolean isReLayout = this.isDataChange();
        this.setIsDataChange(false);
        if (!isReLayout) {
            isReLayout |= (mPreWidthMeasureSpec != widthMeasureSpec)
                    || (mPreHeightMeasureSpec != heightMeasureSpec);
        }
        mPreWidthMeasureSpec = widthMeasureSpec;
        mPreHeightMeasureSpec = heightMeasureSpec;
        if (isReLayout) {
            mPreStackItem.setPositionIndex(getSelection() - 1);
            mCurrentStackItem.setPositionIndex(getSelection());
            mNextStackItem.setPositionIndex(getSelection() + 1);
            for (StackItem item : mStackItems) {
                item.layout();
            }
        } else {
            for (StackItem item : mStackItems) {
                if (item.view != null && item.view.isLayoutRequested()) {
                    item.layout();
                }
            }
        }
        this.setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {}

    private void performActionDown(MotionEvent ev) {
        this.mEdgeState = null;
        this.mDragState = DRAG_STATE.NOT_EDGE_DRAG;
        this.mStartMotionX = this.mMotionX;
        this.mStartMotionY = this.mMotionY;
        if (this.mStartMotionX <= MAX_EDGE_OFFSET) {
            this.mEdgeState = EDGE_STATE.EDGE_LEFT;
        } else if (this.mStartMotionX >= (mViewWidth - MAX_EDGE_OFFSET)) {
            this.mEdgeState = EDGE_STATE.EDGE_RIGHT;
        } else {
            this.mEdgeState = EDGE_STATE.NOT_EDGE;
        }
        switch (this.mEdgeState) {
        case EDGE_LEFT:
            if (getSelection() == 0) {
                this.mEdgeState = EDGE_STATE.NOT_EDGE;
            }
            break;
        case EDGE_RIGHT:
            if (getSelection() == getDataCount() - 1) {
                this.mEdgeState = EDGE_STATE.NOT_EDGE;
            }
            break;
        default:
            break;
        }
        Picture p = new Picture();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
//        LOG.log("dispatchDraw "+mShadowStackItem+",mShadowRight = "+mShadowRight);
        if (mShadowStackItem != null) {
            canvas.save();
            final Paint paint = PAINT;
            paint.setColor(Color.BLACK);
            paint.setAlpha((int)(mShadowAlpha*255));
            canvas.drawRect(0, 0, mShadowRight, getHeight(), paint);
            canvas.restore();
            if (this.mShadowLeft != null) {
                this.mShadowLeft.setBounds(mShadowRight - mShadowLeft.getIntrinsicWidth(),0,mShadowRight,getHeight());
                this.mShadowLeft.draw(canvas);
            }
        }
    }

    private void performActionMove(MotionEvent ev) {
        if (mDragState == DRAG_STATE.NOT_EDGE_DRAG) {
            if (this.mEdgeState != null
                    && this.mEdgeState != EDGE_STATE.NOT_EDGE) {
                int dx = Math.abs(this.mMotionX - this.mStartMotionX);
                int dy = Math.abs(this.mMotionY - this.mStartMotionY);
                if ((dy + dx) >= (this.getTouchSlop() << 1)) {
                    mDragState = DRAG_STATE.EDGE_DRAG;
                }
            }
        } else {
            this.obtainVelocityTracker().addMovement(ev);
            notifyAnimatorStart();
            switch (mEdgeState) {
            case EDGE_RIGHT:
                float offset = Math.abs(this.mMotionX - getXByType(NEXT));
                mShadowStackItem = mNextStackItem;
                mNextStackItem.performDragToX(this.mMotionX);
                offset = (offset / mViewWidth);
                mCurrentStackItem
                        .performDragToX((int) (offset * getXByType(PRE)));
                break;
            case EDGE_LEFT:
                offset = Math.abs(this.mMotionX);
                mShadowStackItem = mCurrentStackItem;
                mCurrentStackItem.performDragToX(this.mMotionX);
                offset = ((mViewWidth - offset) / mViewWidth);
                mPreStackItem.performDragToX((int) (offset * getXByType(PRE)));
                break;
            }
        }
    }

    private void setShadowRightAndAlpha(int right,float alpha) {
        if (this.mShadowRight != right) {
            this.mShadowRight = right;
            this.mShadowAlpha = alpha;
            if (mShadowRight > 0) {
                invalidate(0, 0, this.mShadowRight, mViewHeight);
            }
        }
    }

    public final void moveNext() {
        if (this.getSelection() == getDataCount() - 1) {
            return;
        }
        mShadowStackItem = mCurrentStackItem;
        mPreStackItem.detach();
        mPreStackItem.replaceStackItem(mCurrentStackItem);
        mCurrentStackItem.replaceStackItem(mNextStackItem);
        mNextStackItem.setPositionIndexAndLayout(mCurrentStackItem.index + 1);
    }

    public boolean isCanMoveForward() {
        return this.getSelection() < this.getDataCount() - 1;
    }

    public boolean isCanMoveBack() {
        return this.getSelection() > 0;
    }

    public final void smoothMoveToPosition(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index > getDataCount() - 1) {
            index = getDataCount() - 1;
        }
        if (index < 0) {
            return;
        }
        if (index == getSelection()) {
            return;
        }
        if (index > getSelection()) {
            if (index - getSelection() == 1) {// move next
                this.moveNext();
            } else {
                mPreStackItem.detach();
                mPreStackItem.replaceStackItem(mCurrentStackItem);
                mPreStackItem.isReLoad = true;
                mPreStackItem.updateIndex(index - 1);
                mNextStackItem.detach();
                mNextStackItem.setPositionIndexAndLayout(index);
                mCurrentStackItem.replaceStackItem(mNextStackItem);
                mNextStackItem.setPositionIndexAndLayout(index + 1);
            }
        } else {
            if (getSelection() - index == 1) {// move pre
                this.movePre();
            } else {
                mNextStackItem.detach();
                mNextStackItem.replaceStackItem(mCurrentStackItem);
                mNextStackItem.isReLoad = true;
                mNextStackItem.updateIndex(index + 1);
                mPreStackItem.detach();
                mPreStackItem.setPositionIndexAndLayout(index);
                mCurrentStackItem.replaceStackItem(mPreStackItem);
                mPreStackItem.setPositionIndexAndLayout(index - 1);
            }
        }

    }

    public void movePre() {
        if (this.getSelection() <= 0) {
            return;
        }
        mShadowStackItem = mNextStackItem;
        mNextStackItem.detach();
        mNextStackItem.replaceStackItem(mCurrentStackItem);
        mCurrentStackItem.replaceStackItem(mPreStackItem);
        mPreStackItem.setPositionIndex(mCurrentStackItem.index - 1);
        mPreStackItem.layout();
    }

    private int getMinOffset() {
        return this.mViewWidth >> 2;
    }

    private void performActionUp(MotionEvent ev) {
        int offset = mMotionX - mStartMotionX;
        if (mDragState == DRAG_STATE.EDGE_DRAG) {
            switch (mEdgeState) {
            case EDGE_RIGHT:
                if (mMotionX < mStartMotionX) {
                    boolean isMoveNext = Math.abs(offset) >= getMinOffset();
                    if (!isMoveNext) {
                        final VelocityTracker tracker = this
                                .obtainVelocityTracker();
                        tracker.computeCurrentVelocity(UNIT);
                        float vx = tracker.getXVelocity(0);
                        if (Math.abs(vx) >= MAX_VELOCITY) {
                            isMoveNext = true;
                        }
                    }
                    if (isMoveNext) {
                        this.moveNext();
                    } else {
                        mNextStackItem
                                .movePositionIndexTo(mNextStackItem.index);
                        mCurrentStackItem
                                .movePositionIndexTo(mCurrentStackItem.index);
                    }
                } else {

                    mNextStackItem.movePositionIndexTo(mNextStackItem.index);
                    mCurrentStackItem
                            .movePositionIndexTo(mCurrentStackItem.index);
                }
                mShadowStackItem = mCurrentStackItem;
                break;
            case EDGE_LEFT:
                if (mMotionX > this.mStartMotionX) {
                    boolean isMovePre = Math.abs(offset) >= getMinOffset();
                    if (!isMovePre) {
                        final VelocityTracker tracker = this
                                .obtainVelocityTracker();
                        tracker.computeCurrentVelocity(UNIT);
                        float vx = tracker.getXVelocity(0);
                        if (Math.abs(vx) >= MAX_VELOCITY) {
                            isMovePre = true;
                        }
                    }
                    if (isMovePre) {
                        this.movePre();
                    } else {
                        mNextStackItem
                                .movePositionIndexTo(mNextStackItem.index);
                        mCurrentStackItem
                                .movePositionIndexTo(mCurrentStackItem.index);
                    }
                } else {
                    mCurrentStackItem
                            .movePositionIndexTo(mCurrentStackItem.index);
                    mPreStackItem.movePositionIndexTo(mPreStackItem.index);
                }
                mShadowStackItem = mCurrentStackItem;
                break;
            }
        }
        this.recycleVelocityTracker();
        mDragState = DRAG_STATE.NOT_EDGE_DRAG;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getMotionFromEvent(ev);
        switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            performActionDown(ev);
            break;
        case MotionEvent.ACTION_MOVE:
            performActionMove(ev);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            this.performActionUp(ev);
            break;
        }
        savePreMotion();
        return mDragState == DRAG_STATE.EDGE_DRAG;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragState != DRAG_STATE.EDGE_DRAG) {
            return true;
        }
        getMotionFromEvent(ev);
        switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            performActionDown(ev);
            break;
        case MotionEvent.ACTION_MOVE:
            performActionMove(ev);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            this.performActionUp(ev);
            break;
        }
        savePreMotion();
        return true;
    }

    /**
     * smooth add next and move to next
     * */
    public final void reloadAndSmoothMoveNext() {
        mNextStackItem.detach();
        int nextIndex = getSelection() + 1;
        mNextStackItem.setPositionIndexAndLayout(nextIndex);
        this.moveNext();
    }

    public static interface OnStackPagerLayoutListener {

        public void onStackPagerAnimationStart();

        public void onStackPagerAnimationEnd();

        public void onStackPagerPositionChange(int position);
    }

    public void setOnStackPagerLayoutListener(
            OnStackPagerLayoutListener listener) {
        this.mOnStackPagerLayoutListener = listener;
    }

}
