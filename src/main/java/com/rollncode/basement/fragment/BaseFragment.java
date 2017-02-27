package com.rollncode.basement.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.request.SpiceRequest;
import com.rollncode.basement.activity.BaseActivity;
import com.rollncode.basement.interfaces.ObjectsReceiver;
import com.rollncode.basement.utility.BaseARequestListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class BaseFragment extends Fragment
        implements ObjectsReceiver {

    private static final SimpleArrayMap<String, Boolean> PERMISSIONS_ASKED_BEFORE = new SimpleArrayMap<>();
    private static final int REQUEST_PERMISSION = 1303;

    protected boolean mAfterOnCreate;

    private BroadcastReceiver mInternetReceiver;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        super.setHasOptionsMenu(true);

        mAfterOnCreate = true;
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context context = getContext();
        final ActionBar bar = context instanceof AppCompatActivity ? ((AppCompatActivity) context).getSupportActionBar() : null;

        if (bar != null) {
            onActionBarReady(bar);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mInternetReceiver == null) {
            getContext().registerReceiver(mInternetReceiver = newInternetReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mInternetReceiver != null) {
            getContext().unregisterReceiver(mInternetReceiver);
            mInternetReceiver = null;
        }
    }

    protected final void askPermissions(boolean force) {
        final String[] permissions2Check = getPermissions2Check();
        final String key = getClass().getName();
        final String[] permissions;
        {
            final List<String> list = new ArrayList<>(permissions2Check.length);
            for (String permission : permissions2Check) {
                if (ActivityCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
                    list.add(permission);
                }
            }
            if (!force && PERMISSIONS_ASKED_BEFORE.get(key) != null) {
                onRequiredPermissionsAnswer(list);
                return;
            }
            permissions = list.toArray(new String[list.size()]);
        }
        if (permissions.length > 0) {
            PERMISSIONS_ASKED_BEFORE.put(key, Boolean.TRUE);
            setRefreshing(true);

            requestPermissions(permissions, REQUEST_PERMISSION);

        } else {
            //noinspection unchecked
            onRequiredPermissionsAnswer(Collections.EMPTY_LIST);
        }
    }

    @NonNull
    protected String[] getPermissions2Check() {
        return new String[0];
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                setRefreshing(false);

                final List<String> denied = new ArrayList<>(permissions.length);
                int count = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        denied.add(permissions[count]);
                    }
                    count++;
                }
                onRequiredPermissionsAnswer(denied);
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    protected void onRequiredPermissionsAnswer(@NonNull Collection<String> denied) {
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            onCleanUp();

        } catch (Exception ignore) {

        } finally {
            System.gc();
        }
    }

    protected final View findViewВyId(@NonNull View v, @IdRes int id) {
        final View view = v.findViewById(id);
        //TODO: handle view
        return view;
    }

    @CallSuper
    protected void onActionBarReady(@NonNull ActionBar bar) {
        bar.setTitle(getToolbarTitle());

        @DrawableRes final int indicator = getToolbarIndicator();
        if (indicator == 0) {
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowHomeEnabled(false);

        } else {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);

            bar.setHomeAsUpIndicator(indicator);
        }
    }

    @DrawableRes
    protected int getToolbarIndicator() {
        return 0;
    }

    @Nullable
    protected CharSequence getToolbarTitle() {
        @StringRes final int stringRes = getToolbarTitleRes();
        return stringRes != 0 ? super.getText(stringRes) : null;
    }

    @StringRes
    protected int getToolbarTitleRes() {
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startFragment(@NonNull BaseFragment fragment, boolean addToBackStack) {
        final Context context = getContext();
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).startFragment(fragment, addToBackStack);
        }
    }

    protected final void onBackPressed() {
        final Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).onBackPressed();
        }
    }

    @SuppressWarnings("unused")
    public final void finishActivity() {
        final Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public abstract void setRefreshing(boolean block);

    public <RESULT, LISTENER extends BaseARequestListener<RESULT>> boolean execute(@NonNull SpiceRequest<RESULT> request, @Nullable LISTENER listener) {
        final Context context = getContext();
        return context instanceof BaseActivity && ((BaseActivity) context).execute(request, listener);
    }

    @CheckResult
    private BroadcastReceiver newInternetReceiver() {
        return new BroadcastReceiver() {

            private boolean mSticky = true;

            @Override
            public void onReceive(Context context, Intent intent) {
                if (mSticky) {
                    mSticky = false;
                    return;
                }
                final View view = getView();
                if (view != null && view.getParent() != null) {
                    view.removeCallbacks(mInternetRun);
                    view.postDelayed(mInternetRun, 500);
                }
            }
        };
    }

    private final Runnable mInternetRun = new Runnable() {
        @Override
        public void run() {
            onInternetConnectionChanged(isNetworkAvailable());
        }
    };

    @LayoutRes
    protected abstract int getLayoutResId();

    protected abstract void onCleanUp() throws Exception;

    protected abstract boolean isNetworkAvailable();

    protected abstract void onInternetConnectionChanged(boolean internetAvailable);

    @NonNull
    public abstract String toString();
}