package com.rollncode.basement.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 13/01/17
 */
public final class FileSharedPreferences
        implements SharedPreferences {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Object LOCK = new Object();

    static final Object REMOVE = new Object();
    static final String REMOVE_ALL = "REMOVE_ALL";

    private final File mFile;

    private final SparseArray<WeakReference<OnSharedPreferenceChangeListener>> mListeners;

    private JSONObject mObject;

    @NonNull
    public static FileSharedPreferences create(@NonNull Context context, @NonNull String fileName) {
        final File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            return new FileSharedPreferences(file);

        } else {
            try {
                if (file.createNewFile()) {
                    return new FileSharedPreferences(file);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new IllegalStateException("File can't be created with some reason");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public FileSharedPreferences(@NonNull File file) {
        mFile = file;
        if (!mFile.canRead()) {
            throw new IllegalStateException("Don't waste time with this file!");
        }
        mListeners = new SparseArray<>();
    }

    @CheckResult
    @Override
    public Map<String, ?> getAll() {
        final JSONObject object = getJsonFromFile();
        final Map<String, Object> map = new HashMap<>(object.length());

        String key;
        Object value;

        for (Iterator<String> iterator = object.keys(); iterator.hasNext(); ) {
            key = iterator.next();
            value = object.opt(key);

            if (value instanceof JSONArray) {
                value = toSet((JSONArray) value);
            }
            map.put(key, value);
        }
        return map;
    }

    @CheckResult
    @Override
    public String getString(String key, String defValue) {
        return getJsonFromFile().optString(key, defValue);
    }

    @CheckResult
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        final Object optArray = getJsonFromFile().opt(key);
        return optArray instanceof JSONArray ? toSet((JSONArray) optArray) : defValues;
    }

    @CheckResult
    @NonNull
    private Set<String> toSet(@NonNull JSONArray array) {
        final Set<String> set = new HashSet<>(array.length());

        for (int i = 0, length = array.length(); i < length; i++) {
            set.add(String.valueOf(array.opt(i)));
        }
        return set;
    }

    @CheckResult
    @Override
    public int getInt(String key, int defValue) {
        return getJsonFromFile().optInt(key, defValue);
    }

    @CheckResult
    @Override
    public long getLong(String key, long defValue) {
        return getJsonFromFile().optLong(key, defValue);
    }

    @CheckResult
    @Override
    public float getFloat(String key, float defValue) {
        return (float) getJsonFromFile().optDouble(key, defValue);
    }

    @CheckResult
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return getJsonFromFile().optBoolean(key, defValue);
    }

    @CheckResult
    @Override
    public boolean contains(String key) {
        return getJsonFromFile().has(key);
    }

    @CheckResult
    @Override
    public Editor edit() {
        return new FileEditor(this);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(@NonNull OnSharedPreferenceChangeListener listener) {
        synchronized (LOCK) {
            mListeners.put(listener.hashCode(), new WeakReference<>(listener));
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(@NonNull OnSharedPreferenceChangeListener listener) {
        synchronized (LOCK) {
            mListeners.remove(listener.hashCode());
        }
    }

    @NonNull
    private JSONObject getJsonFromFile() {
        if (mObject == null) {
            final StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
                String string;

                while ((string = reader.readLine()) != null) {
                    sb.append(string);
                }

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                close(reader);
            }
            if (sb.length() > 0) {
                try {
                    mObject = new JSONObject(sb.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (mObject == null) {
                mObject = new JSONObject();
            }
        }
        return mObject;
    }

    @MainThread
    private void notifyListeners(@NonNull Iterable<String> keys) {
        synchronized (LOCK) {
            OnSharedPreferenceChangeListener listener;
            for (int i = 0, size = mListeners.size(); i < size; i++) {
                listener = mListeners.valueAt(i).get();
                if (listener != null) {
                    for (String key : keys) {
                        listener.onSharedPreferenceChanged(this, key);
                    }
                }
            }
        }
    }

    synchronized boolean commit(@NonNull JSONObject in) throws JSONException {
        if (in.length() == 0) {
            return false;//nothing to commit
        }
        final JSONObject out = new JSONObject();
        if (!in.has(REMOVE_ALL)) {//not commit changes fom current mObject
            copy(getJsonFromFile(), out);
        }
        copy(in, out);
        final List<String> keys = new ArrayList<>();//to notify changes
        {
            String key;
            final List<String> remove = new ArrayList<>();
            for (Iterator<String> iterator = in.keys(); iterator.hasNext(); ) {
                key = iterator.next();
                keys.add(key);

                if (out.opt(key) == REMOVE) {
                    remove.add(key);
                }
            }
            for (String k : remove) {
                out.remove(k);
            }
            out.remove(REMOVE_ALL);
        }
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(mFile, false));

            final String string = out.toString();
            writer.write(string, 0, string.length());

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        } finally {
            close(writer);
        }
        mObject = null;
        notifyListeners(keys);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getJsonFromFile();
        }
        return true;
    }

    void post(@NonNull FileEditor editor) {
        EXECUTOR.submit(editor);
    }

    private static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copy(@NonNull JSONObject from, @NonNull JSONObject to) throws JSONException {
        String key;
        for (Iterator<String> iterator = from.keys(); iterator.hasNext(); ) {
            key = iterator.next();
            to.put(key, from.opt(key));
        }
    }
}