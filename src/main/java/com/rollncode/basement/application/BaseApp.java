package com.rollncode.basement.application;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.rollncode.basement.utility.BaseUtils;

import java.lang.ref.WeakReference;

import io.fabric.sdk.android.Fabric;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseApp extends Application
        implements ActivityLifecycleCallbacks {

    protected static final int WHAT_START = 0x1;
    protected static final int WHAT_MIDDLE = 0x2;
    protected static final int WHAT_END = 0x3;

    private SparseBooleanArray mCreateDestroy;
    private SparseBooleanArray mResumePause;
    private WorkerHandler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        {
            Fabric.with(this, new Crashlytics(), new Answers());
        }
        mCreateDestroy = new SparseBooleanArray();
        mResumePause = new SparseBooleanArray();
        mHandler = new WorkerHandler(this);

        super.registerActivityLifecycleCallbacks(this);
    }

    protected abstract void handleMessage(int what);

    public final boolean isVisible() {
        return mResumePause.size() > 0;
    }

    @NonNull
    public final Looper getWorkerLooper() {
        return mHandler.getLooper();
    }

    @CallSuper
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (mCreateDestroy.size() == 0) {
            startWorker();
            BaseUtils.threadSleep(50);

            synchronized (BaseConstant.LOCK) {
                //here app wait until WHAT_START
            }
        }
        mCreateDestroy.put(activity.hashCode(), true);
    }

    @CallSuper
    @Override
    public void onActivityStarted(Activity activity) {
    }

    @CallSuper
    @Override
    public void onActivityResumed(Activity activity) {
        mResumePause.put(activity.hashCode(), true);
    }

    @CallSuper

    @Override
    public void onActivityPaused(Activity activity) {
        mResumePause.delete(activity.hashCode());
        mHandler.sendEmptyMessage(WHAT_MIDDLE);
    }

    @CallSuper
    @Override
    public void onActivityStopped(Activity activity) {
    }

    @CallSuper
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @CallSuper
    @Override
    public void onActivityDestroyed(Activity activity) {
        mCreateDestroy.delete(activity.hashCode());
        if (mCreateDestroy.size() == 0) {
            mHandler.sendEmptyMessage(WHAT_END);
        }
    }

    @CallSuper
    public void startWorker() {
        mHandler.sendEmptyMessage(WHAT_START);
    }

    private static final class WorkerHandler extends Handler {

        private final WeakReference<BaseApp> mApp;

        private WorkerHandler(@NonNull BaseApp app) {
            super(BaseUtils.newLooper("AppWorkerThread", Thread.MAX_PRIORITY));
            mApp = new WeakReference<>(app);
        }

        @Override
        public void handleMessage(Message msg) {
            final BaseApp app = mApp.get();
            if (app != null) {
                app.handleMessage(msg.what);
            }
        }
    }
}