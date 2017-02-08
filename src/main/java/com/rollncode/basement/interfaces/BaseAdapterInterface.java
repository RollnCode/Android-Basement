package com.rollncode.basement.interfaces;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 23/01/17
 */
public interface BaseAdapterInterface<DATA, VIEW extends View & DataEntity<DATA>> {

    void setData(@Nullable DATA[] data);

    void addData(@Nullable DATA[] data);

    DATA getItem(int position);

    @NonNull
    VIEW newInstance(@NonNull Context context, int position, @Nullable WeakReference<ObjectsReceiver> receiver);

    void onViewSetData(@NonNull VIEW view, int position);
}