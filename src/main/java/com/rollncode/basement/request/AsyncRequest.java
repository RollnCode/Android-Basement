package com.rollncode.basement.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import com.rollncode.basement.utility.ALog;

public abstract class AsyncRequest<RESULT> extends SpiceRequest<RESULT> {

    protected static final ALog LOG = new ALog("requestLog");

    private static final DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(1, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public AsyncRequest(@NonNull Class<RESULT> result) {
        super(result);
        super.setRetryPolicy(RETRY_POLICY);
    }

    @Nullable
    @Override
    public abstract RESULT loadDataFromNetwork() throws Exception;

    public abstract boolean isNetworkRequired();

    @WorkerThread
    @Nullable
    public final RESULT execute() {
        try {
            return loadDataFromNetwork();

        } catch (Throwable e) {
            LOG.toLog(e);
        }
        return null;
    }

    /**
     * {@link Thread#MAX_PRIORITY}, {@link Thread#NORM_PRIORITY}, {@link Thread#MIN_PRIORITY}
     */
    @SuppressWarnings("unused")
    public final void oneShot(int priority) {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                execute();
            }
        };
        thread.setPriority(priority);
        thread.start();
    }
}