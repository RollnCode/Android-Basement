package com.rollncode.basement.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 30/01/17
 */
public abstract class BaseOffsetLimitHelper<RESULT, LISTENER extends BaseARequestListener<RESULT[]>>
        implements OnScrollListener {

    private final LISTENER mRequestListener;

    private int mLimit;
    private int mAttempts;

    private boolean mWaitForResponse;
    private boolean mErrorInResponse;

    private int mOffset;
    private int mAttemptCount;

    private int mOldTop;
    private int mOldFirstVisibleItem;

    protected BaseOffsetLimitHelper(int limit, int attempts) {
        mRequestListener = newListenerInstance();

        mLimit = limit;
        mAttempts = attempts;
    }

    @NonNull
    protected abstract LISTENER newListenerInstance();

    protected abstract boolean executeRequest(int offset, int limit, @NonNull LISTENER listener);

    protected abstract void setRefreshing(boolean refreshing);

    protected abstract void showError(@NonNull Throwable e, @NonNull String errorMessage);

    protected abstract void onData(@Nullable RESULT[] results, boolean add);

    @Override
    public final void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public final boolean queryAll() {
        return queryAll(true);
    }

    @SuppressWarnings("WeakerAccess")
    public final boolean queryAll(boolean force) {
        if (!mWaitForResponse
                && (force || mOffset > 0)
                && executeRequest(0, Math.max(mLimit, mOffset), mRequestListener)) {
            mWaitForResponse = true;

            mOffset = 0;
            mAttemptCount = 0;

            setRefreshing(true);
            return true;
        }
        return false;
    }

    public final boolean reset() {
        if (mWaitForResponse) {
            return false;
        }
        mWaitForResponse = false;
        mErrorInResponse = false;

        mOffset = 0;
        mAttemptCount = 0;

        return true;
    }

    @Override
    public final void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final View view = absListView == null ? null : absListView.getChildAt(0);
        final int top = (view == null) ? 0 : view.getTop();

        if (firstVisibleItem == mOldFirstVisibleItem) {
            if (top > mOldTop) {
                onScrollDown();

            } else if (top < mOldTop) {
                onScrollUp();
            }

        } else {
            if (firstVisibleItem < mOldFirstVisibleItem) {
                onScrollDown();

            } else {
                onScrollUp();
            }
        }

        mOldTop = top;
        mOldFirstVisibleItem = firstVisibleItem;

        if (mWaitForResponse) {
            return;
        }

        if (mAttemptCount == mAttempts && firstVisibleItem == totalItemCount) {
            lastItemThrown();

        } else if (totalItemCount == 0 || firstVisibleItem + visibleItemCount == totalItemCount) {
            if (!mErrorInResponse && mAttemptCount < mAttempts && executeRequest(mOffset, mLimit, mRequestListener)) {
                mWaitForResponse = true;
                setRefreshing(true);
            }

        } else if (mErrorInResponse) {
            mErrorInResponse = false;
        }
    }

    protected final void onErrorInner(@NonNull Throwable e, @NonNull String errorMessage) {
        setRefreshing(false);
        showError(e, errorMessage);

        mWaitForResponse = false;
        mErrorInResponse = true;
    }

    protected final void onResultInner(@Nullable RESULT[] results) {
        setRefreshing(false);
        {
            final int length = results == null ? 0 : results.length;
            if (length == 0) {
                onData(null, mOffset != 0);
                mAttemptCount++;

            } else {
                onData(results, mOffset > 0);
                if (length < mLimit) {
                    mAttemptCount = mAttempts;
                }
            }
            mOffset += length;
        }
        mWaitForResponse = mErrorInResponse = false;
    }

    protected void lastItemThrown() {
    }

    protected void onScrollUp() {
    }

    protected void onScrollDown() {
    }
}