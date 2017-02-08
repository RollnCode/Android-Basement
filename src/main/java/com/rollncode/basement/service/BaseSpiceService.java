package com.rollncode.basement.service;

import android.app.Application;
import android.app.Notification;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.memory.LruCacheStringObjectPersister;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Collection;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public abstract class BaseSpiceService extends SpiceService {

    private static final int THREAD_COUNT = Math.min(2, Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

    @Override
    public Notification createDefaultNotification() {
        return null;
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        final CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new LruCacheStringObjectPersister(1_024 * 1_024));

        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return THREAD_COUNT;
    }

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return CHECKER;
    }

    private static final NetworkStateChecker CHECKER = new NetworkStateChecker() {
        @Override
        public boolean isNetworkAvailable(Context context) {
            return true;
        }

        @Override
        public void checkPermissions(Context context) {
        }
    };

    @Override
    public void dontNotifyRequestListenersForRequest(CachedSpiceRequest<?> request, Collection<RequestListener<?>> listRequestListener) {
    }
}