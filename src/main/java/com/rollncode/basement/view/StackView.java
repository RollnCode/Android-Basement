package com.rollncode.basement.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.PointF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.rollncode.basement.utility.ALog;
import com.rollncode.basement.utility.ARandom;
import com.rollncode.basement.interfaces.OnStackViewCallback;
import com.rollncode.basement.type.GestureDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 01/02/17
 */
public final class StackView extends FrameLayout {

    private static final TimeInterpolator TIME_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float SCROLLING_CROSS_DP = 100;
    private static final int ANIMATION_DURATION = 250;
    private static final float CLICK_CROSS_DP = 10;
    private static final int VISIBLE_MAX_COUNT = 3;
    private static final float ELEVATION_DP = 6;
    private static final int ROTATION_ANGLE = 7;

    //VIEW's
    private View mFrontView;
    private View mRecycledChild;

    //VALUE's
    private final TimeInterpolator mInterpolator;
    private final float mScrollingCrossMin;
    private final int mAnimationDuration;
    private final float mClickCrossMin;
    private final LayoutParams mParams;
    private final int mVisibleCount;
    private final float mElevation;
    private final int mAngle;

    private final PointF mScrollingCrossTotal;
    private final PointF mScrollingCross;
    private final PointF mLastXY;

    private boolean mAnimation;
    private boolean mScrolling;
    private PointF mBorder;

    private final List<View> mRecycledChildren;
    private int mFrontPosition;
    @GestureDirection
    private int mLastGestureDirection;

    //ADAPTER
    private BaseAdapter mAdapter;

    //CALLBACK's
    private OnScrollListener mScrollListener;
    private OnItemClickListener mItemClickListener;
    private OnStackViewCallback mStackViewCallback;

    public StackView(Context context) {
        this(context, null);
    }

    public StackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScrollingCrossMin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCROLLING_CROSS_DP, getResources().getDisplayMetrics());
        mClickCrossMin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLICK_CROSS_DP, getResources().getDisplayMetrics());
        mElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ELEVATION_DP, getResources().getDisplayMetrics());
        mParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mAnimationDuration = ANIMATION_DURATION;
        mInterpolator = TIME_INTERPOLATOR;
        mVisibleCount = VISIBLE_MAX_COUNT;
        mAngle = ROTATION_ANGLE;

        mScrollingCrossTotal = new PointF();
        mScrollingCross = new PointF();
        mLastXY = new PointF();

        mRecycledChildren = new ArrayList<>();
    }

    public void setAdapter(@NonNull BaseAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        mFrontPosition = 0;

        super.post(mRunNotifyChanges);
    }

    public void setOnScrollListener(@Nullable OnScrollListener listener) {
        mScrollListener = listener;
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnStackViewCallback(@Nullable OnStackViewCallback callback) {
        mStackViewCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Boolean handleMotionEvent = null;
        try {
            handleMotionEvent = handleMotionEvent(event);

        } catch (Exception e) {
            ALog.LOG.toLog(e);
        }
        return handleMotionEvent != null && handleMotionEvent || super.onTouchEvent(event);
    }

    public boolean previousFrontItem() {
        if (mAnimation) {
            return false;
        }
        if (mFrontPosition > 0) {
            mAnimation = true;
            final int count = mAdapter.getCount();
            if (mFrontPosition >= count) {
                mFrontPosition = count;
            }
            final View view = getPreparedChildView(-1, mFrontPosition - 1, false);

            view.setTranslationY(mBorder.y);

            super.addView(view, mParams);

            final float factor = getScaleFactor(-1);
            view.animate().translationX(0).translationY(0).scaleX(factor).scaleY(factor).setListener(mFrontInListener).setStartDelay(0).start();
            return true;
        }
        return false;
    }

    public boolean releaseFrontItem(@GestureDirection int direction) {
        if (mAnimation) {
            return false;
        }
        if ((mFrontView = getChildAt(getChildCount() - 1)) != null) {
            mAnimation = true;
            initBorder();

            releaseViewWithAnimation(mFrontView, direction);
            mFrontView = null;
            return true;
        }
        return false;
    }

    @Nullable
    private Boolean handleMotionEvent(@NonNull MotionEvent event) throws Exception {
        if (mAnimation) {
            return null;
        }
        if (mFrontView == null) {
            mFrontView = getChildAt(getChildCount() - 1);
        }
        if (mFrontView == null) {
            return null;
        }
        final View frontView = mFrontView;
        initBorder();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();

                if (frontView.getTranslationX() == 0 && frontView.getTranslationY() == 0
                        && x >= frontView.getLeft() && x <= frontView.getRight()
                        && y >= frontView.getTop() && y <= frontView.getBottom()) {
                    mLastXY.set(x, y);
                    mScrollingCrossTotal.set(0, 0);
                    mScrollingCross.set(0, 0);

                    mScrolling = true;
                    return true;
                }
            }
            return false;

            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();

                final float differenceX = (x - mLastXY.x) / 2F;
                final float differenceY = (y - mLastXY.y) / 2F;

                mLastXY.set(x - differenceX, y - differenceY);

                mScrollingCrossTotal.offset(Math.abs(differenceX), Math.abs(differenceY));
                mScrollingCross.offset(differenceX, differenceY);

                if (mScrolling) {
                    if (mStackViewCallback != null) {
                        final float path = Math.max(Math.abs(mScrollingCross.x), Math.abs(mScrollingCross.y));
                        final float progress = path / mBorder.y;

                        mStackViewCallback.onFrontViewMove(frontView, getGestureDirection(), progress);
                    }
                    float translationX = Math.min(mBorder.y, frontView.getTranslationX() + differenceX);
                    translationX = Math.max(mBorder.x, translationX);

                    float translationY = Math.min(mBorder.y, frontView.getTranslationY() + differenceY);
                    translationY = Math.max(mBorder.x, translationY);

                    frontView.setTranslationX(translationX);
                    frontView.setTranslationY(translationY);
                }
                return true;

            default:
                @GestureDirection final int direction = getGestureDirection();
                if (direction != GestureDirection.NONE
                        && mStackViewCallback != null && mStackViewCallback.onFrontViewRelease(mFrontPosition, direction)) {
                    releaseViewWithAnimation(frontView, direction);

                } else {
                    if (mItemClickListener != null
                            && mScrollingCrossTotal.x <= mClickCrossMin && mScrollingCrossTotal.y <= mClickCrossMin
                            && Math.abs(mScrollingCross.x) <= mClickCrossMin && Math.abs(mScrollingCross.y) <= mClickCrossMin) {
                        mItemClickListener.onItemClick(null, frontView, mFrontPosition, mAdapter.getItemId(mFrontPosition));
                    }
                    frontView.animate().translationX(0).translationY(0).setListener(null).setStartDelay(0).start();
                    if (mStackViewCallback != null) {
                        mStackViewCallback.onFrontViewMove(frontView, GestureDirection.NONE, 0);
                    }
                }
                mScrollingCross.set(0, 0);
                mScrolling = false;
                mFrontView = null;

                return null;
        }
    }

    private void initBorder() {
        if (mBorder == null) {
            final int size = Math.max(super.getWidth(), super.getHeight());
            mBorder = new PointF(-size, size);
        }
    }

    private void releaseViewWithAnimation(@NonNull View view, @GestureDirection int direction) {
        mAnimation = true;
        mRecycledChild = view;
        final ViewPropertyAnimator animator = view.animate();

        switch (mLastGestureDirection = direction) {
            case GestureDirection.LEFT:
                animator.translationX(mBorder.x);
                break;

            case GestureDirection.TOP:
                animator.translationY(mBorder.x);
                break;

            case GestureDirection.RIGHT:
                animator.translationX(mBorder.y);
                break;

            case GestureDirection.BOTTOM:
                animator.translationY(mBorder.y);
                break;

            case GestureDirection.LEFT_TOP:
                animator.translationX(mBorder.x).translationY(mBorder.x);
                break;

            case GestureDirection.LEFT_BOTTOM:
                animator.translationX(mBorder.x).translationY(mBorder.y);
                break;

            case GestureDirection.RIGHT_TOP:
                animator.translationX(mBorder.y).translationY(mBorder.x);
                break;

            case GestureDirection.RIGHT_BOTTOM:
                animator.translationX(mBorder.y).translationY(mBorder.y);
                break;
            //noinspection ConstantConditions
            case GestureDirection.NONE:
            default:
                throw new IllegalStateException();
        }
        animator.alpha(0).setListener(mFrontOutListener).setStartDelay(0).start();
    }

    @GestureDirection
    private int getGestureDirection() {
        final boolean scrollHorizontal = Math.abs(mScrollingCross.x) > mScrollingCrossMin;
        final boolean scrollVertical = Math.abs(mScrollingCross.y) > mScrollingCrossMin;

        if (scrollHorizontal && scrollVertical) {
            if (mScrollingCross.x < 0 && mScrollingCross.y < 0) {
                return GestureDirection.LEFT_TOP;

            } else if (mScrollingCross.x > 0 && mScrollingCross.y < 0) {
                return GestureDirection.RIGHT_TOP;

            } else if (mScrollingCross.x > 0 && mScrollingCross.y > 0) {
                return GestureDirection.RIGHT_BOTTOM;

            } else if (mScrollingCross.x < 0 && mScrollingCross.y < 0) {
                return GestureDirection.LEFT_BOTTOM;
            }

        } else if (scrollHorizontal) {
            return mScrollingCross.x < 0 ? GestureDirection.LEFT : GestureDirection.RIGHT;

        } else if (scrollVertical) {
            return mScrollingCross.y < 0 ? GestureDirection.TOP : GestureDirection.BOTTOM;
        }
        return GestureDirection.NONE;
    }

    private final AnimatorListenerAdapter mFrontOutListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            post(mRunFrontOut);
        }
    };

    private final AnimatorListenerAdapter mFrontInListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            post(mRunFrontIn);
        }
    };

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            postDelayed(mRunOnChanged, mAnimationDuration);
        }

        @Override
        public void onInvalidated() {
            removeAllViews();
        }
    };

    @Override
    public void removeAllViews() {
        final View[] views = new View[getChildCount()];
        for (int i = 0; i < views.length; i++) {
            views[i] = getChildAt(i);
        }
        super.removeAllViews();

        Collections.addAll(mRecycledChildren, views);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        if (view != null) {
            view.animate().cancel();
            mRecycledChildren.add(view);
        }
    }

    @Override
    public void removeViewAt(int index) {
        final View view = getChildAt(index);
        super.removeViewAt(index);

        if (view != null) {
            view.animate().cancel();
            mRecycledChildren.add(view);
        }
    }

    private void refreshChildView(int index, int position) {
        final View child = super.getChildAt(getChildCount() - 1 - index);
        if (child == null) {
            addChildView(index, position, false);

        } else {
            mAdapter.getView(position, child, this);
        }
    }

    private void addChildView(int index, int position, boolean animate) {
        super.addView(getPreparedChildView(index, position, animate), 0, mParams);
    }

    @CheckResult
    @NonNull
    private View getPreparedChildView(int index, int position, boolean animate) {
        final View recycled = mRecycledChildren.size() > 0 ? mRecycledChildren.remove(0) : null;
        if (recycled != null) {
            recycled.animate().cancel();
        }
        final View view = mAdapter.getView(position, recycled, this);
        {
            if (mStackViewCallback != null) {
                mStackViewCallback.onFrontViewMove(view, GestureDirection.NONE, 0);
            }
//            if (!view.isHardwareAccelerated()) {
//                view.setLayerType(LAYER_TYPE_SOFTWARE, null);
//            }
            view.setRotation(mAngle - ARandom.nextInt(mAngle * 2));
            view.setTranslationX(0);
            view.setTranslationY(0);
            {
                final float factor = getScaleFactor(index);

                view.setScaleX(factor);
                view.setScaleY(factor);
            }
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                view.setTranslationZ(getTranslationFactor(index));
            }
            final ViewPropertyAnimator animator = view.animate().setListener(null).setDuration(mAnimationDuration).setInterpolator(mInterpolator);
            if (animate) {
                view.setAlpha(0);
                animator.alpha(1).setStartDelay(index * (mAnimationDuration / 2)).start();

            } else {
                view.setAlpha(1);
            }
        }
        return view;
    }

    private void reorderChildren() {
        View view;
        float factor;
        ViewPropertyAnimator animator;
        final int duration = mAnimationDuration / 2;

        for (int i = 0, size = getChildCount(), j = size - 1; i < size; i++, j--) {
            view = getChildAt(i);
            animator = view.animate();

            factor = getScaleFactor(j);
            animator
                    .translationX(0).translationY(0)
                    .scaleX(factor).scaleY(factor)
                    .setListener(null).setStartDelay(size * duration - i * duration - duration);

            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                animator.translationZ(getTranslationFactor(j));
            }
            animator.start();
            setAnimationFalse();
        }
        if (getChildCount() > mVisibleCount) {
            removeViewAt(0);
        }
    }

    private float getScaleFactor(float index) {
        return 1F - index / (mVisibleCount * 10F);
    }

    private float getTranslationFactor(float index) {
        return mElevation - mElevation * (index / mVisibleCount);
    }

    private void setAnimationFalse() {
        removeCallbacks(mRunAnimationStop);
        postDelayed(mRunAnimationStop, mAnimationDuration);
    }

    private final Runnable mRunAnimationStop = new Runnable() {
        @Override
        public void run() {
            mAnimation = false;
            if (mScrollListener != null) {
                mScrollListener.onScroll(null, mFrontPosition, getChildCount() * 2, mAdapter.getCount());
            }
        }
    };

    private final Runnable mRunNotifyChanges = new Runnable() {
        @Override
        public void run() {
            removeAllViews();
            mAdapter.notifyDataSetChanged();
        }
    };

    private final Runnable mRunOnChanged = new Runnable() {
        @Override
        public void run() {
            if (mAnimation) {
                postDelayed(this, mAnimationDuration / 5);
                return;
            }
            mAnimation = true;

            if (mAdapter.getCount() < mFrontPosition) {
                if (mStackViewCallback != null) {
                    mStackViewCallback.onFrontViewChanged(0, mFrontPosition, GestureDirection.NONE);
                }
                mFrontPosition = 0;
                removeAllViews();
            }
            int position = mFrontPosition;
            int index = 0;

            for (; position < mAdapter.getCount() && index < mVisibleCount; position++, index++) {
                refreshChildView(index, position);
            }
            if (position < mAdapter.getCount() && mRecycledChildren.size() < 3) {
                mRecycledChildren.add(getPreparedChildView(-1, position, false));
            }
            setAnimationFalse();
        }
    };

    private final Runnable mRunFrontOut = new Runnable() {
        @Override
        public void run() {
            removeView(mRecycledChild);
            mRecycledChild = null;
            reorderChildren();

            final int backPosition = mFrontPosition++ + mVisibleCount;
            if (mStackViewCallback != null) {
                mStackViewCallback.onFrontViewChanged(mFrontPosition, mFrontPosition - 1, mLastGestureDirection);
            }
            if (backPosition < mAdapter.getCount()) {
                addChildView(getChildCount(), backPosition, true);
            }
            setAnimationFalse();
        }
    };

    private final Runnable mRunFrontIn = new Runnable() {
        @Override
        public void run() {
            mFrontPosition--;
            reorderChildren();

            if (mStackViewCallback != null) {
                mStackViewCallback.onFrontViewChanged(mFrontPosition, mFrontPosition + 1, GestureDirection.NONE);
            }
            setAnimationFalse();
        }
    };
}