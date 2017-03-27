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

    private static final String KEY_0 = "rocobapr.KEY_0";//mDeviceId
    private static final String KEY_1 = "rocobapr.KEY_1";//mAppToken
    private static final String KEY_2 = "rocobapr.KEY_2";//mPushToken
    private static final String KEY_3 = "rocobapr.KEY_3";//mPushTokenSent
    private static final String KEY_4 = "rocobapr.KEY_4";//mAppVersion

    protected final SharedPreferences mPreferences;

    private String mDeviceId;

    private String mAppToken;

    private String mPushToken;
    private boolean mPushTokenSent;

    private int mAppVersion;

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
        {
            mAppVersion = mPreferences.getInt(KEY_4, Integer.MIN_VALUE);
            final int currentAppVersion = getAppVersionCode();

            if (currentAppVersion != mAppVersion) {
                onAppVersionUpdate(mAppVersion, currentAppVersion);
                mPreferences.edit().putInt(KEY_4, mAppVersion = currentAppVersion).apply();
            }
        }
    }

    @WorkerThread
    protected final void commit() {
        final Editor editor = mPreferences.edit();
        try {
            editor
                    .putString(KEY_0, mDeviceId)
                    .putString(KEY_1, mAppToken)
                    .putString(KEY_2, mPushToken)
                    .putBoolean(KEY_3, mPushTokenSent)
                    .putInt(KEY_4, mAppVersion)
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
        mPreferences.edit().putInt(KEY_4, mAppVersion).apply();

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
        mPushTokenSent = false;

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

    protected int getAppVersionCode() {
        return Integer.MIN_VALUE;
    }

    protected void onAppVersionUpdate(int oldVersion, int newVersion) {
    }
}