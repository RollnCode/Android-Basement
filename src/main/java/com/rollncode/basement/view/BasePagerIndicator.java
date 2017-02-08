package com.rollncode.basement.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnAdapterChangeListener;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public abstract class BasePagerIndicator extends LinearLayout
        implements OnPageChangeListener, OnAdapterChangeListener {

    private static final float MIN_SCALE = 0.75F;
    private static final float MIN_ALPHA = 0.5F;

    private final int mIndicatorSize;

    private PagerAdapter mAdapter;
    private int mCurrent;

    public BasePagerIndicator(@NonNull Context context) {
        this(context, null);
    }

    public BasePagerIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePagerIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        {
            super.setOrientation(HORIZONTAL);
            super.setGravity(Gravity.CENTER);
        }
        mIndicatorSize = getIndicatorSize();

        if (isInEditMode()) {
            addIndicator(false);
            addIndicator(true);
            addIndicator(true);
        }
    }

    protected abstract int getIndicatorSize();

    public final void setViewPager(@NonNull ViewPager pager) {
        onAdapterChanged(pager, null, pager.getAdapter());
    }

    private void addIndicator(boolean notCurrent) {
        final View view = new View(getContext());
        view.setBackground(getIndicatorDrawable());

        super.addView(view, mIndicatorSize, mIndicatorSize);

        final LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.leftMargin = params.rightMargin = mIndicatorSize / 2;

        view.requestLayout();

        if (notCurrent) {
            view.setAlpha(MIN_ALPHA);
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        }
    }

    @NonNull
    protected Drawable getIndicatorDrawable() {
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.WHITE);

        return drawable;
    }

    @Override
    public final void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public final void onPageSelected(int position) {
        if (mAdapter == null || mAdapter.getCount() <= 0) {
            return;
        }
        View current = super.getChildAt(mCurrent);
        if (current != null) {
            current.animate().alpha(MIN_ALPHA).scaleX(MIN_SCALE).scaleY(MIN_SCALE).start();
        }
        mCurrent = position;

        current = super.getChildAt(position);
        if (current != null) {
            current.animate().alpha(1).scaleX(1).scaleY(1).start();
        }
    }

    @Override
    public final void onPageScrollStateChanged(int state) {
    }

    @Override
    public final void onAdapterChanged(@NonNull ViewPager pager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
        if ((mAdapter = newAdapter) != null) {
            final int current = pager.getCurrentItem();
            {
                super.removeAllViews();

                final int count = mAdapter.getCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        addIndicator(current != i);
                    }
                }
                pager.removeOnPageChangeListener(this);
                pager.addOnPageChangeListener(this);

                pager.removeOnAdapterChangeListener(this);
                pager.addOnAdapterChangeListener(this);
            }
            mCurrent = -1;
            onPageSelected(current);
        }
    }

    @CallSuper
    @MainThread
    public void notifyDataSetChanged() {
        if (mAdapter != null && mAdapter.getCount() != super.getChildCount()) {
            final int current = mCurrent;

            super.removeAllViews();

            final int count = mAdapter.getCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    addIndicator(current != i);
                }
            }
            mCurrent = -1;
            onPageSelected(current);
        }
    }
}