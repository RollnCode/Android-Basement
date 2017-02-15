package com.rollncode.basement.utility;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 15/02/17
 */
public abstract class CalendarDateHelper {

    private final Calendar mCalendar;
    private final Date mDate;

    protected CalendarDateHelper() {
        mCalendar = Calendar.getInstance();
        mDate = new Date();
    }

    public boolean isSameDay(long first, long second) {
        mCalendar.setTimeInMillis(first);
        final int fYear = mCalendar.get(Calendar.YEAR);
        final int fDayOfYear = mCalendar.get(Calendar.DAY_OF_YEAR);

        mCalendar.setTimeInMillis(second);
        final int sYear = mCalendar.get(Calendar.YEAR);
        final int sDayOfYear = mCalendar.get(Calendar.DAY_OF_YEAR);

        return fYear == sYear && fDayOfYear == sDayOfYear;
    }

    public boolean isCurrentYear(long time) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        final int cYear = mCalendar.get(Calendar.YEAR);

        mCalendar.setTimeInMillis(time);
        final int tYear = mCalendar.get(Calendar.YEAR);

        return cYear == tYear;
    }

    @NonNull
    public CharSequence format(long time, @NonNull DateFormat format) {
        mDate.setTime(time);
        return format.format(mDate);
    }
}