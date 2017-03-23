package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;
import android.view.View;

public interface DataEntityRecycler<DATA, SELF extends View> extends DataEntity<DATA> {
    @NonNull
    SELF getSelf();
}