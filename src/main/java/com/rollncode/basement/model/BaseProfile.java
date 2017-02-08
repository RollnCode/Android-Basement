package com.rollncode.basement.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.rollncode.basement.utility.BaseUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 23/01/17
 */
public abstract class BaseProfile {

    private static final String KEY_0 = "bapr.KEY_0";
    private static final String KEY_1 = "bapr.KEY_1";
    private static final String KEY_2 = "bapr.KEY_2";
    private static final String KEY_3 = "bapr.KEY_3";

    protected final SharedPreferences mPreferences;

    private String mDeviceId;

    private String mAppToken;

    private String mPushToken;
    private boolean mPushTokenSent;

    protected BaseProfile(@NonNull SharedPreferences preferences) {
        mPreferences = preferences;
        restoreFromPreferences();
    }

    @CallSuper
    protected void restoreFromPreferences() {
        mDeviceId = mPreferences.getString(KEY_0, mDeviceId);
        mAppToken = mPreferences.getString(KEY_1, null);
        mPushToken = mPreferences.getString(KEY_2, null);
        mPushTokenSent = mPreferences.getBoolean(KEY_3, false);
    }

    @WorkerThread
    public final void commit() {
        final Editor editor = mPreferences.edit();
        try {
            editor
                    .putString(KEY_0, mDeviceId)
                    .putString(KEY_1, mAppToken)
                    .putString(KEY_2, mPushToken)
                    .putBoolean(KEY_3, mPushTokenSent)
            ;
            onCommitChanges(editor);

        } finally {
            editor.apply();
        }
    }

    @WorkerThread
    protected abstract void onCommitChanges(@NonNull Editor editor);

    @WorkerThread
    public final void reset() {
        mPreferences.edit().clear().apply();

        onReset();
        restoreFromPreferences();
    }

    protected final void setupDeviceId(@NonNull Context context) {
        if (mDeviceId == null) {
            mDeviceId = BaseUtils.getUniqueDeviceId(context);
        }
    }

    @NonNull
    public final String getDeviceId() {
        return mDeviceId;
    }

    @WorkerThread
    protected abstract void onReset();

    @NonNull
    protected final SharedPreferences getPreferences() {
        return mPreferences;
    }

    @Nullable
    public final String getAppToken() {
        return mAppToken;
    }

    @WorkerThread
    @CallSuper
    public void setAppToken(String appToken) {
        mAppToken = appToken;
        commit();
    }

    @Nullable
    public final String getPushToken() {
        return mPushToken;
    }

    @WorkerThread
    @CallSuper
    public void setPushToken(String pushToken) {
        mPushToken = pushToken;
        commit();
    }

    public final boolean isPushTokenSent() {
        return mPushTokenSent;
    }

    @WorkerThread
    @CallSuper
    public void setPushTokenSent(boolean pushTokenSent) {
        mPushTokenSent = pushTokenSent;
        commit();
    }
}