package com.rollncode.basement.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 19/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NewFeature {
    boolean isPaidFor() default false;
}