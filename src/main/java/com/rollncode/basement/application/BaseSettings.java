package com.rollncode.basement.application;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseSettings {

    protected final SharedPreferences mPreferences;

    protected BaseSettings(@NonNull SharedPreferences preferences) {
        mPreferences = preferences;
    }

    protected final void apply(@NonNull String key, @Nullable Object object) {
        final Editor editor = edit();
        if (object == null) {
            editor.remove(key);

        } else if (object instanceof String) {
            editor.putString(key, object.toString());

        } else if (object instanceof Integer) {
            editor.putInt(key, (int) object);

        } else if (object instanceof Long) {
            editor.putLong(key, (long) object);

        } else if (object instanceof Set) {
            //noinspection unchecked
            final Set<String> set = (Set<String>) object;
            if (set.size() == 0) {
                editor.remove(key);

            } else {
                editor.putStringSet(key, set);
            }

        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (boolean) object);

        } else if (object instanceof Float) {
            editor.putFloat(key, (float) object);

        } else {
            throw new IllegalStateException("Wrong object type: " + object.getClass().getName());
        }
        editor.apply();
    }

    @CheckResult
    protected final Editor edit() {
        return mPreferences.edit();
    }

    public final void clear() {
        edit().clear().apply();
    }
}