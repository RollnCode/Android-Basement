package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 20/01/17
 */
public interface AnalyticsInterface {

    void screenView(@NonNull Object object);

    void requestExecuted(@NonNull String url, @NonNull SimpleArrayMap<String, Object> parameters);
}