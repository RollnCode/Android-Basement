package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonEntity {

    void fromJson(@NonNull JSONObject object) throws JSONException;

    @NonNull
    JSONObject toJson() throws JSONException;
}