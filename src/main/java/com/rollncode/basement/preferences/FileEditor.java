package com.rollncode.basement.preferences;

import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 13/01/17
 */
final class FileEditor
        implements Editor, Runnable {

    private final FileSharedPreferences mPreferences;
    private final JSONObject mObject;

    FileEditor(@NonNull FileSharedPreferences preferences) {
        mPreferences = preferences;
        mObject = new JSONObject();
    }

    @Override
    public Editor putString(@NonNull String key, @Nullable String value) {
        try {
            if (value == null) {
                mObject.put(key, FileSharedPreferences.REMOVE);

            } else {
                mObject.put(key, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor putStringSet(@NonNull String key, @Nullable Set<String> values) {
        try {
            if (values == null) {
                mObject.put(key, FileSharedPreferences.REMOVE);

            } else {
                final JSONArray array = new JSONArray();
                for (String string : values) {
                    array.put(string);
                }
                mObject.put(key, array);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor putInt(@NonNull String key, int value) {
        try {
            mObject.put(key, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor putLong(@NonNull String key, long value) {
        try {
            mObject.put(key, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor putFloat(@NonNull String key, float value) {
        try {
            mObject.put(key, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor putBoolean(@NonNull String key, boolean value) {
        try {
            mObject.put(key, value);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor remove(String key) {
        try {
            mObject.put(key, FileSharedPreferences.REMOVE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Editor clear() {
        try {
            mObject.putOpt(FileSharedPreferences.REMOVE_ALL, FileSharedPreferences.REMOVE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean commit() {
        try {
            final boolean commit = mPreferences.commit(mObject);
            {
                final List<String> remove = new ArrayList<>();
                for (Iterator<String> iterator = mObject.keys(); iterator.hasNext(); ) {
                    remove.add(iterator.next());
                }
                for (String key : remove) {
                    mObject.remove(key);
                }
            }
            return commit;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void apply() {
        mPreferences.post(this);
    }

    @Override
    public void run() {
        commit();
    }
}