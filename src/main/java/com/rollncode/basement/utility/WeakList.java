package com.rollncode.basement.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 27/03/17
 */
public final class WeakList<DATA> {

    private final ArrayList<WeakReference<DATA>> mList;

    public WeakList() {
        mList = new ArrayList<>();
    }

    public synchronized void add(@Nullable DATA data) {
        if (data != null) {
            mList.add(new WeakReference<>(data));
        }
    }

    @NonNull
    public synchronized List<DATA> getStrong() {
        final List<DATA> list = new ArrayList<>(mList.size());

        DATA data;
        for (WeakReference<DATA> reference : mList) {
            data = reference.get();
            if (data != null) {
                list.add(data);
            }
        }
        return list;
    }

    public synchronized void clear() {
        mList.clear();
    }

    @Override
    public synchronized int hashCode() {
        return mList.hashCode();
    }
}