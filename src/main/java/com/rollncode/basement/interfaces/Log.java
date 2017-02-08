package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/01/17
 */
public interface Log {

    void toLog(@Nullable String string);

    void toLog(@NonNull Throwable e);

    @NonNull
    StringBuilder toString(@NonNull Throwable e, boolean fullStack);
}