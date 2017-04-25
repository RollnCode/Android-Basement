package com.rollncode.basement.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
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
import com.rollncode.basement.utility.BaseARequestListener;
import com.rollncode.basement.utility.BaseUtils;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseActivity extends AppCompatActivity {

    private final SpiceManager mSpiceManager = newSpiceManager();

    private boolean mAfterOnSaveInstanceState;
    private Handler mHandler;
    private boolean mSticky;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);

        mAfterOnSaveInstanceState = false;
        mHandler = new Handler();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle b) {
        super.onPostCreate(b);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSpiceManager != null) {
            mSpiceManager.start(this);
        }
        mAfterOnSaveInstanceState = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        {
            mAfterOnSaveInstanceState = false;
        }
        mSticky = true;
        super.registerReceiver(mReceiver, BaseUtils.newIntentFilter(getGlobalBroadcastActions(), ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        mAfterOnSaveInstanceState = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSpiceManager != null && mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
    }

    public final <RESULT, LISTENER extends BaseARequestListener<RESULT>> boolean execute(@NonNull SpiceRequest<RESULT> request, @Nullable LISTENER listener) {
        if (mSpiceManager == null) {
            throw new IllegalStateException();
        }
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

    @Nullable
    protected abstract SpiceManager newSpiceManager();

    protected abstract boolean isNetworkAvailable();

    public abstract int getFragmentContainerId();

    @Nullable
    protected abstract String[] getGlobalBroadcastActions();

    protected abstract void onGlobalBroadcastReceive(@NonNull Context context, @NonNull Intent intent);

    @NonNull
    public abstract String toString();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (mSticky) {
                        mSticky = false;
                        return;
                    }
                    mHandler.removeCallbacks(mInternetRun);
                    mHandler.postDelayed(mInternetRun, 500);
                    break;

                default:
                    BaseActivity.this.onGlobalBroadcastReceive(context, intent);
                    break;
            }

        }
    };

    private final Runnable mInternetRun = new Runnable() {
        @Override
        public void run() {
            onInternetConnectionChanged(isNetworkAvailable());
        }
    };

    @CallSuper
    protected void onInternetConnectionChanged(boolean internetAvailable) {
        final Fragment fragment = getFragmentFromContainer();
        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).onInternetConnectionChanged(internetAvailable);
        }
    }
}