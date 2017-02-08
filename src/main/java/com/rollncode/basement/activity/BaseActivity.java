package com.rollncode.basement.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.SpiceRequest;
import com.rollncode.basement.fragment.BaseFragment;
import com.rollncode.basement.interfaces.OnBackPressedListener;
import com.rollncode.basement.request.AsyncRequest;
import com.rollncode.basement.service.BaseSpiceService;
import com.rollncode.basement.utility.BaseARequestListener;
import com.rollncode.basement.utility.BaseUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseActivity<S extends BaseSpiceService> extends AppCompatActivity {

    private final SpiceManager mSpiceManager = new SpiceManager(getExecutorClass());

    private boolean mAfterOnSaveInstanceState;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        mAfterOnSaveInstanceState = false;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle b) {
        super.onPostCreate(b);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAfterOnSaveInstanceState = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSpiceManager.start(this);
        mAfterOnSaveInstanceState = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        mAfterOnSaveInstanceState = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
    }

    public final <RESULT, LISTENER extends BaseARequestListener<RESULT>> boolean execute(@NonNull SpiceRequest<RESULT> request, @Nullable LISTENER listener) {
        final boolean isNetworkRequired = !(request instanceof AsyncRequest) || ((AsyncRequest) request).isNetworkRequired();
        if (isNetworkRequired) {
            if (isNetworkAvailable()) {
                mSpiceManager.execute(request, listener);
                return true;

            } else if (listener != null) {
                listener.showNoInternet(this);
            }
            return false;
        }
        mSpiceManager.execute(request, listener);
        return true;
    }

    public boolean startFragment(@NonNull BaseFragment fragment, boolean addToBackStack) {
        return startFragmentSimple(getFragmentContainerId(), fragment, addToBackStack);
    }

    protected final boolean startFragmentSimple(int containerId, @NonNull Fragment fragment, boolean addToBackStack) {
        if (mAfterOnSaveInstanceState) {
            return false;
        }
        final FragmentManager manager = getSupportFragmentManager();
        if (!addToBackStack) {
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        final FragmentTransaction transaction = manager.beginTransaction();

        final String tag = BaseUtils.getTag(fragment);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction
                .replace(containerId, fragment, tag)
                .commit();
        return true;
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getFragmentFromContainer();
        if (!(fragment instanceof OnBackPressedListener) || ((OnBackPressedListener) fragment).onBackPress()) {
            super.onBackPressed();
        }
    }

    @Nullable
    public final Fragment getFragmentFromContainer(@IdRes int containerId) {
        return getSupportFragmentManager().findFragmentById(containerId);
    }

    @Nullable
    public final Fragment getFragmentFromContainer() {
        return getFragmentFromContainer(getFragmentContainerId());
    }

    @NonNull
    protected abstract <S extends BaseSpiceService> Class<S> getExecutorClass();

    protected abstract boolean isNetworkAvailable();

    public abstract int getFragmentContainerId();

    @NonNull
    public abstract String toString();
}