package com.rollncode.basement.interfaces;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

public interface CursorEntity {

    void fromCursor(@NonNull Cursor cursor);

    @NonNull
    ContentValues toContentValues();
}