package com.rollncode.basement.interfaces;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

public interface ObjectsReceiver {
    void onObjectsReceive(@IdRes int code, @NonNull Object... objects);
}