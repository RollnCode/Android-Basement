package com.rollncode.basement.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.rollncode.basement.interfaces.ObjectsReceiver;

import java.lang.ref.WeakReference;

/**
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 20/02/17
 */
public abstract class BaseAlertDialog {

    protected final AlertDialog mDialog;
    protected final WeakReference<ObjectsReceiver> mReceiver;

    protected BaseAlertDialog(@NonNull Context context, @Nullable ObjectsReceiver receiver) {
        mReceiver = receiver == null ? null : new WeakReference<>(receiver);
        mDialog = createDialog(context);
    }

    @NonNull
    protected abstract AlertDialog createDialog(@NonNull Context context);

    public abstract void show(@NonNull Object... objects);

    @NonNull
    protected final Context getContext() {
        return mDialog.getContext();
    }
}