package com.rollncode.basement.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.rollncode.basement.interfaces.Log;

public abstract class BaseARequestListener<RESULT> implements RequestListener<RESULT> {

    private final Log mLog;
    protected final boolean mShowNoInternet;

    protected BaseARequestListener(@NonNull Log log, boolean showNoInternet) {
        mLog = log;
        mShowNoInternet = showNoInternet;
    }

    @Override
    public final void onRequestFailure(@NonNull SpiceException e) {
        if (e instanceof RequestCancelledException) {
            onCancelled();
            return;
        }
        final Throwable cause = e.getCause();
        final Throwable error = cause == null ? e : cause;

        mLog.toLog(error);

        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            errorMessage = getUnknownErrorString();
        }
        onError(error, errorMessage);
    }

    @NonNull
    protected abstract String getUnknownErrorString();

    @Override
    public final void onRequestSuccess(@Nullable RESULT result) {
        try {
            onResult(result);

        } catch (Throwable e) {
            mLog.toLog(e);
        }
    }

    public abstract void showNoInternet(@NonNull Context context);

    public void onCancelled() {
    }

    public abstract void onError(@NonNull Throwable e, @NonNull String errorMessage);

    public abstract void onResult(@Nullable RESULT result);
}