package com.rollncode.basement.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rollncode.basement.interfaces.BaseAdapterInterface;
import com.rollncode.basement.interfaces.DataEntity;
import com.rollncode.basement.interfaces.ObjectsReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 26/01/17
 */
public abstract class BaseListAdapter<DATA, VIEW extends View & DataEntity<DATA>> extends BaseAdapter
        implements BaseAdapterInterface<DATA, VIEW> {

    private final WeakReference<ObjectsReceiver> mReceiver;
    private final List<DATA> mData;

    public BaseListAdapter(@Nullable ObjectsReceiver receiver, @Nullable DATA[] data) {
        mReceiver = receiver == null ? null : new WeakReference<>(receiver);

        mData = new ArrayList<>();
        addData(data);
    }

    @Override
    public void setData(@Nullable DATA[] data) {
        mData.clear();
        addData(data);
    }

    @Override
    public final void addData(@Nullable DATA[] data) {
        if (data != null && data.length > 0) {
            Collections.addAll(mData, data);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onViewSetData(@NonNull VIEW view, int position) {
        view.setData(getItem(position), position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public DATA getItem(int position) {
        return mData.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @NonNull
    protected final List<DATA> getData() {
        return mData;
    }

    public void setData(@NonNull List<DATA> data) {
        mData.clear();
        mData.addAll(data);
    }

    @Override
    public final View getView(int position, View cV, ViewGroup parent) {
        //noinspection unchecked
        final VIEW view = cV != null ? (VIEW) cV : newInstance(parent.getContext(), position, mReceiver);

        onViewSetData(view, position);

        return view;
    }
}