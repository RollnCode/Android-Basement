package com.rollncode.basement.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.rollncode.basement.interfaces.BaseAdapterInterface;
import com.rollncode.basement.interfaces.DataEntity;
import com.rollncode.basement.interfaces.ObjectsReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 07.06.16
 */
public abstract class BaseRecyclerAdapter<DATA, VIEW extends View & DataEntity<DATA>> extends RecyclerView.Adapter<BaseRecyclerAdapter.ViewHolder<DATA, VIEW>>
        implements BaseAdapterInterface<DATA, VIEW> {

    private final WeakReference<ObjectsReceiver> mReceiver;
    private final List<DATA> mData;

    public <R extends ObjectsReceiver> BaseRecyclerAdapter(@Nullable R receiver, @Nullable List<DATA> data) {
        mReceiver = receiver == null ? null : new WeakReference<ObjectsReceiver>(receiver);
        mData = data == null ? new ArrayList<DATA>(0) : new ArrayList<>(data);
    }

    @Override
    @NonNull
    public final DATA getItem(int position) {
        return mData.get(position);
    }

    @Override
    public final ViewHolder<DATA, VIEW> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder<>(newInstance(parent.getContext(), viewType, mReceiver));
    }

    @NonNull
    public abstract VIEW newInstance(@NonNull Context context, int viewType, @Nullable WeakReference<ObjectsReceiver> receiver);

    @Override
    public final void onBindViewHolder(ViewHolder<DATA, VIEW> holder, int position) {
        onViewSetData(holder.getItemView(), position);
    }

    @Override
    public void onViewSetData(@NonNull VIEW view, int position) {
        view.setData(getItem(position), position);
    }

    @Override
    public final int getItemCount() {
        return mData.size();
    }

    @Override
    public final void setData(@Nullable DATA[] data) {
        mData.clear();
        if (data != null && data.length > 0) {
            Collections.addAll(mData, data);
        }
    }

    @Override
    public final void addData(@Nullable DATA[] data) {
        if (data != null && data.length > 0) {
            Collections.addAll(mData, data);
        }
    }

    static class ViewHolder<DATA, VIEW extends View & DataEntity<DATA>> extends RecyclerView.ViewHolder {

        private final Class<VIEW> mClass;

        ViewHolder(@NonNull VIEW view) {
            super(view);
            //noinspection unchecked
            mClass = (Class<VIEW>) view.getClass();
        }

        @NonNull
        VIEW getItemView() {
            return mClass.cast(itemView);
        }
    }
}