package com.rollncode.basement.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 06/03/17
 */
public abstract class BaseDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        super.setStyle(DialogFragment.STYLE_NORMAL, getThemeRes());
    }

    @StyleRes
    protected abstract int getThemeRes();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        final Window window = getDialog().getWindow();
        if (isFullScreen() && window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        final View v = onCreateView(inflater, container);
        if (isFullScreen()) {
            final Point screenSize = new Point();
            {
                final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getSize(screenSize);
            }
            v.setMinimumWidth(screenSize.x);
            v.setMinimumHeight(screenSize.y);

            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return v;
    }

    protected View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return super.onCreateView(inflater, container, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle b) {
        super.onViewCreated(view, b);
    }

    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        try {
            onCleanUp();

        } catch (Exception ignore) {
        } finally {
            System.gc();
        }
    }

    protected abstract void onCleanUp() throws Exception;

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    protected boolean isFullScreen() {
        return false;
    }

    @Override
    public final void show(FragmentManager manager, String tag) {
        throw new NoSuchMethodError();
    }

    @Override
    public final int show(FragmentTransaction transaction, String tag) {
        throw new NoSuchMethodError();
    }

    @CallSuper
    public boolean show(@NonNull FragmentManager manager) {
        final String tag = getClass().getName();

        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
            return true;
        }
        return false;
    }
}