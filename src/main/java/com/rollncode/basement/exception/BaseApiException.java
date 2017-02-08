package com.rollncode.basement.exception;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 05/09/16
 */
public abstract class BaseApiException extends RuntimeException {

    protected static final int UNKNOWN_ERROR = Integer.MIN_VALUE;

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "Range"})
    public static final BaseApiException SILENT = new BaseApiException(null, null, UNKNOWN_ERROR) {
    };

    private final int mErrorCode;

    protected BaseApiException(@Nullable Throwable throwable, @Nullable String message, @IntRange(from = 1) int errorCode) {
        super(message, throwable);
        mErrorCode = toErrorCode(throwable, errorCode);
    }

    protected int toErrorCode(@Nullable Throwable throwable, int code) {
        return code;
    }

    @SuppressWarnings("unused")
    public final int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public final String getMessage() {
        return toString();
    }

    @NonNull
    @Override
    public String toString() {
        if (mErrorCode > 0 && isKnownError(mErrorCode)) {
            return getMessage(mErrorCode);
        }
        String message = super.getMessage();
        if (!TextUtils.isEmpty(message)) {
            return message;
        }
        Throwable throwable = super.getCause();
        while (throwable != null) {
            message = throwable.getMessage();
            if (!TextUtils.isEmpty(message)) {
                return message;
            }
            throwable = throwable.getCause();
        }
        return getMessage(UNKNOWN_ERROR);
    }

    protected boolean isKnownError(int code) {
        return false;
    }

    @NonNull
    protected String getMessage(int code) {
        return "Unknown";
    }
}