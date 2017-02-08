package com.rollncode.basement.application;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.rollncode.basement.R;
import com.rollncode.basement.utility.BaseUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public class BaseAContext<A extends BaseApp> {

    protected final A mApp;

    protected final Resources mResources;
    protected final ContentResolver mContentResolver;
    private final InputMethodManager mInputMethodManager;
    private final ConnectivityManager mConnectivityManager;

    protected final Point mScreenSize;
    protected final int mStatusBarHeight;
    protected final int mActionBarHeight;

    @SuppressLint({"HardwareIds", "PrivateResource"})
    protected BaseAContext(@NonNull A app) {
        mApp = app;

        mResources = mApp.getResources();
        mContentResolver = mApp.getContentResolver();
        mInputMethodManager = (InputMethodManager) mApp.getSystemService(Context.INPUT_METHOD_SERVICE);
        mConnectivityManager = (ConnectivityManager) mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            final WindowManager windowManager = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
            mScreenSize = new Point();
            windowManager.getDefaultDisplay().getSize(mScreenSize);
        }
        {
            final int resourceId = mResources.getIdentifier("status_bar_height", "dimen", "android");
            mStatusBarHeight = resourceId > 0 ? mResources.getDimensionPixelSize(resourceId) : 0;
        }

        final TypedValue value = new TypedValue();
        if (app.getTheme().resolveAttribute(R.attr.actionBarSize, value, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(value.data, mResources.getDisplayMetrics());

        } else {
            mActionBarHeight = mResources.getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
        }
    }

    @CheckResult
    @Nullable
    protected final String getStringInner(@StringRes int stringRes, @NonNull Object... objects) {
        return mResources.getString(stringRes, objects);
    }

    protected final void hideInputKeyboardInner(@Nullable View view) {
        if (view != null) {
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @CheckResult
    @Nullable
    protected final Drawable getDrawableInner(@DrawableRes int drawableRes) {
        return ContextCompat.getDrawable(mApp, drawableRes);
    }

    protected final int getColorInner(@ColorRes int colorRes) {
        return ContextCompat.getColor(mApp, colorRes);
    }

    protected final int getDimensionPixelSizeInner(@DimenRes int id) {
        return id == 0 ? 0 : mResources.getDimensionPixelSize(id);
    }

    @CheckResult
    protected final boolean isNetworkAvailableInner() {
        return BaseUtils.isNetworkAvailable(mConnectivityManager);
    }
}