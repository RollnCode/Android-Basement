package com.rollncode.basement.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.rollncode.basement.utility.BaseUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

abstract class AsyncNetworkRequest<RESULT> extends AsyncRequest<RESULT> {

    private static long sCode;

    private final long mCode;
    private final long mStartTime;

    protected AsyncNetworkRequest(@NonNull Class<RESULT> result) {
        super(result);

        mCode = sCode++;
        mStartTime = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public RESULT loadDataFromNetwork() throws Exception {
        boolean errorOccurred = true;
        try {
            final Request request = prepareRequest();

            final Response response = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build().newCall(request).execute();

            final RESULT rawResult = handleResponse(response);

            final RESULT checkedResult = checkResult(rawResult);

            errorOccurred = false;
            return checkedResult;

        } catch (Throwable t) {
            return handleException(t);

        } finally {
            postLoad(errorOccurred);
        }
    }

    @Override
    public final boolean isNetworkRequired() {
        return true;
    }

    public final long getCode() {
        return mCode;
    }

    @SuppressWarnings("unused")
    @WorkerThread
    protected final void waitUntilTimeout(long timeout) {
        BaseUtils.waitUntilTimeout(mStartTime, timeout);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
    protected void postLoad(boolean errorOccurred) {
    }

    @NonNull
    protected abstract Request prepareRequest() throws Exception;

    @Nullable
    protected abstract RESULT handleResponse(@NonNull Response response) throws Exception;

    @Nullable
    protected abstract RESULT checkResult(@Nullable RESULT result) throws Exception;

    @Nullable
    protected abstract RESULT handleException(@NonNull Throwable t);
}