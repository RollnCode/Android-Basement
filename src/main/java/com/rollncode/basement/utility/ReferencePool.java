package com.rollncode.basement.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools.Pool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ReferencePool<T> implements Pool<T> {

    private final List<WeakReference<T>> mPool;

    public ReferencePool(int initialSize) {
        mPool = new ArrayList<>(initialSize);
    }

    @Nullable
    @Override
    public synchronized T acquire() {
        if (mPool.size() > 0) {
            WeakReference<T> reference;
            T t;
            for (Iterator<WeakReference<T>> iterator = mPool.iterator(); iterator.hasNext(); ) {
                reference = iterator.next();
                t = reference == null ? null : reference.get();

                iterator.remove();
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public boolean release(@NonNull T t) {
        boolean release = true;
        if (mPool.size() > 0) {
            WeakReference<T> reference;
            T output;
            for (Iterator<WeakReference<T>> iterator = mPool.iterator(); iterator.hasNext(); ) {
                reference = iterator.next();
                output = reference == null ? null : reference.get();

                if (output == null) {
                    iterator.remove();

                } else if (output == t) {
                    release = false;
                    break;
                }
            }
        }
        if (release) {
            mPool.add(new WeakReference<>(t));
        }
        return release;
    }
}