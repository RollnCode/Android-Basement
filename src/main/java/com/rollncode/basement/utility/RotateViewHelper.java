package com.rollncode.basement.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 06/02/17
 */
public final class RotateViewHelper
        implements Runnable {

    private final Interpolator mInterpolator;
    private float mRotation;
    private long mDuration;

    private WeakReference<View> mView;
    private float mFullRotation;
    private boolean mRefreshing;

    public RotateViewHelper() {
        mInterpolator = new LinearInterpolator();
        setView(null, true);
    }

    public void setView(@Nullable View view, boolean clockwise) {
        mView = new WeakReference<>(view);
        mFullRotation = 360F * (clockwise ? 1 : -1);

        if (view != null) {
            view.animate().setInterpolator(mInterpolator).setListener(null);
        }
    }

    public void cleanUp() {
        final View view = mView.get();
        if (view != null) {
            view.removeCallbacks(this);
            view.animate().cancel();
            view.setTag(null);
        }
        mView.clear();
    }

    public void setRotate(boolean rotate) {
        final View view = mView.get();
        if (view != null && view.getParent() != null) {
            final Object tag = view.getTag();
            if (tag == null || rotate != (boolean) tag) {
                view.setTag(rotate);

                view.removeCallbacks(this);
                view.postDelayed(this, 50);
            }
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    @Override
    public void run() {
        final View view = mView.get();
        if (view != null && view.getParent() != null) {
            final Object tag = view.getTag();
            if (tag instanceof Boolean) {
                final ViewPropertyAnimator animator = view.animate();
                animator.cancel();

                calculateAngleAndDuration(view, mRefreshing = (boolean) tag);
                animator.rotation(mRotation).setDuration(mDuration).start();
            }
        }
    }

    private void calculateAngleAndDuration(@NonNull View view, boolean rotate) {
        final float rotation;
        final long duration;

        if (rotate) {
            view.setRotation(0F);
            final float cycles = 2_000_000F;

            rotation = cycles * mFullRotation;
            duration = (long) (cycles * 1_000L);

        } else {
            final float currentRotation = view.getRotation() % mFullRotation;
            view.setRotation(currentRotation);

            rotation = mFullRotation;
            duration = (long) ((360F - Math.abs(currentRotation)) / 360F * 1_000L);
        }
        mRotation = rotation;
        mDuration = duration;
    }
}