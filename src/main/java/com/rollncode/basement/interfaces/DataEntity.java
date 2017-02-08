package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;

public interface DataEntity<DATA> {
    void setData(@NonNull DATA data, @NonNull Object... objects);
}