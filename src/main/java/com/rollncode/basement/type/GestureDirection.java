package com.rollncode.basement.type;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 06/02/17
 */
@IntDef({GestureDirection.NONE
        , GestureDirection.LEFT, GestureDirection.TOP, GestureDirection.RIGHT, GestureDirection.BOTTOM
        , GestureDirection.LEFT_TOP, GestureDirection.RIGHT_TOP, GestureDirection.LEFT_BOTTOM, GestureDirection.RIGHT_BOTTOM})
@Retention(RetentionPolicy.RUNTIME)
public @interface GestureDirection {

    int NONE = 0x0F;
    int LEFT = 0x010F;
    int TOP = 0x020F;
    int RIGHT = 0x040F;
    int BOTTOM = 0x080F;
    int LEFT_TOP = LEFT | TOP;
    int RIGHT_TOP = RIGHT | TOP;
    int LEFT_BOTTOM = LEFT | BOTTOM;
    int RIGHT_BOTTOM = RIGHT | BOTTOM;
}