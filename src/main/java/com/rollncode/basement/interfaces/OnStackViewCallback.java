package com.rollncode.basement.interfaces;

import android.support.annotation.NonNull;
import android.view.View;

import com.rollncode.basement.type.GestureDirection;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 06/02/17
 */
public interface OnStackViewCallback {

    boolean onFrontViewRelease(int position, @GestureDirection int direction);

    void onFrontViewChanged(int newPosition, int oldPosition, @GestureDirection int direction);

    void onFrontViewMove(@NonNull View view, @GestureDirection int direction, float progress);
}