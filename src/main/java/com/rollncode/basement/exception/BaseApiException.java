package com.rollncode.basement.exception;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 05/09/16
 */
public abstract class BaseApiException extends RuntimeException {

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "Range"})
    public static final BaseApiException SILENT = new BaseApiException(null, null, 0) {
        @Override
        protected int toErrorCode(@NonNull Throwable throwable, int code) {
            return code;
        }

        @Override
        protected boolean isKnownError(int code) {
            return false;
        }

        @NonNull
        @Override
        protected String getMessage(int code) {
            return "silent";
        }
    };

    private final int mErrorCode;

    protected BaseApiException(@Nullable Throwable throwable, @Nullable String message, int code) {
        super(message, throwable);
        mErrorCode = throwable == null ? code : toErrorCode(throwable, code);
    }

    protected abstract int toErrorCode(@NonNull Throwable throwable, int code);

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
        if (mErrorCode != 0 && isKnownError(mErrorCode)) {
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
        return getMessage(mErrorCode);
    }

    protected abstract boolean isKnownError(int code);

    @NonNull
    protected abstract String getMessage(int code);
}