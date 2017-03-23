package com.rollncode.basement.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.rollncode.basement.interfaces.DataEntityRecycler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 07.06.16
 */
public abstract class BaseRecyclerAdapter<DATA, VIEW extends View & DataEntityRecycler<DATA, VIEW>> extends RecyclerView.Adapter<BaseRecyclerAdapter.ViewHolder<DATA, VIEW>> {

    static class ViewHolder<DATA, VIEW extends View & DataEntityRecycler<DATA, VIEW>> extends RecyclerView.ViewHolder {

        private final VIEW mView;

        ViewHolder(@NonNull VIEW view) {
            super(view.getSelf());
            mView = view;
        }

        public final void setData(@NonNull DATA data, @NonNull Object... objects) {
            mView.setData(data, objects);
        }

        public final VIEW getView() {
            return mView;
        }
    }

    private final List<DATA> mData;
//TODO: put here objectsReceiver
    public BaseRecyclerAdapter(@Nullable List<DATA> data) {
        mData = data == null ? new ArrayList<DATA>(0) : new ArrayList<>(data);
    }

    @Nullable
    public DATA getItem(int position) {
        return position < 0 || position >= mData.size() ? null : mData.get(position);
    }

    @NonNull
    public final List<DATA> getData() {
        return mData;
    }

    @Override
    public final ViewHolder<DATA, VIEW> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder<>(obtain(parent, viewType));
    }

    @Override
    public final void onBindViewHolder(ViewHolder<DATA, VIEW> holder, int position) {
        setData(holder, position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(@Nullable Collection<DATA> data) {
        mData.clear();
        if (data != null && data.size() > 0) {
            mData.addAll(data);
        }
    }

    public void setData(@Nullable DATA[] data) {
        mData.clear();
        if (data != null && data.length > 0) {
            mData.addAll(Arrays.asList(data));
        }
    }

    public void addData(@Nullable Collection<DATA> data) {
        if (data != null && data.size() > 0) {
            mData.addAll(data);
        }
    }

    public void addData(@Nullable DATA[] data) {
        if (data != null && data.length > 0) {
            mData.addAll(Arrays.asList(data));
        }
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @NonNull
    protected abstract VIEW obtain(@NonNull View parent, int viewType);

    protected void setData(@NonNull ViewHolder<DATA, VIEW> holder, int position) {
        holder.setData(mData.get(position), position);
    }
}