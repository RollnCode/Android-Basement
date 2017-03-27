package com.rollncode.basement.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.rollncode.basement.interfaces.BaseAdapterInterface;
import com.rollncode.basement.interfaces.DataEntity;
import com.rollncode.basement.interfaces.ObjectsReceiver;
import com.rollncode.basement.utility.ReferencePool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 23/01/17
 */
public abstract class BasePagerViewAdapter<DATA, VIEW extends View & DataEntity<DATA>> extends PagerAdapter
        implements BaseAdapterInterface<DATA, VIEW> {

    private final List<DATA> mData;
    private final ReferencePool<VIEW> mPool;
    private final WeakReference<ObjectsReceiver> mReceiver;

    public <R extends ObjectsReceiver> BasePagerViewAdapter(@Nullable R receiver, @Nullable DATA[] data) {
        mReceiver = receiver == null ? null : new WeakReference<ObjectsReceiver>(receiver);
        mData = new ArrayList<>();

        final int poolSize;
        if (data == null || data.length == 0) {
            poolSize = 4;

        } else {
            poolSize = data.length;
            Collections.addAll(mData, data);
        }
        mPool = new ReferencePool<>(poolSize);
    }

    @Override
    public final void setData(@Nullable DATA[] data) {
        mData.clear();
        addData(data);
    }

    @Override
    public final void addData(@Nullable DATA[] data) {
        if (data != null && data.length > 0) {
            Collections.addAll(mData, data);
        }
    }

    @Override
    public final DATA getItem(int position) {
        return mData.get(position);
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        VIEW view = mPool.acquire();
        if (view == null) {
            view = newInstance(container.getContext(), position, mReceiver);
        }
        onViewSetData(view, position);

        container.addView(view);
        return view;
    }

    @Override
    public void onViewSetData(@NonNull VIEW view, int position) {
        view.setData(getItem(position), position);
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        //noinspection unchecked
        final VIEW view = (VIEW) object;

        container.removeView(view);
        mPool.release(view);
    }

    @Override
    public final int getCount() {
        return mData.size();
    }

    @Override
    public final boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}