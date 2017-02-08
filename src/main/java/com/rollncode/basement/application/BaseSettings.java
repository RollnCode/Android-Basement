package com.rollncode.basement.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseSettings {

    protected final SharedPreferences mPreferences;

    protected BaseSettings(@NonNull Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected final void quickApply(@NonNull String key, @Nullable Object object) {
        if (object == null) {
            mPreferences.edit().remove(key).apply();
            return;
        }
        final Editor editor = mPreferences.edit();
        if (object instanceof String) {
            editor.putString(key, object.toString());

        } else if (object instanceof Integer) {
            editor.putInt(key, (int) object);

        } else if (object instanceof Long) {
            editor.putLong(key, (long) object);

        } else if (object instanceof Set) {
            //noinspection unchecked
            editor.putStringSet(key, (Set<String>) object);

        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (boolean) object);

        } else if (object instanceof Float) {
            editor.putFloat(key, (float) object);

        } else {
            throw new IllegalStateException("Wrong object type: " + object.getClass().getName());
        }
        editor.apply();
    }
}