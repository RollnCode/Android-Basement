package com.rollncode.basement.utility;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;

import com.rollncode.basement.BuildConfig;
import com.rollncode.basement.interfaces.Log;
import com.rollncode.basement.interfaces.SharedStrings;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ALog implements Log {

    /**
     * Default log instance: "adb logcat -s tLog"
     */
    public static final ALog LOG = new ALog("aLog").setShowLogs(true);
    private static final int MAX_MESSAGE_LENGTH = 3 * 1024;

    //BASE
    private final String mLogTag;
    protected boolean mShowLogs;

    @SuppressWarnings("WeakerAccess")
    public ALog(@NonNull String logTag) {
        mLogTag = logTag;
        mShowLogs = BuildConfig.DEBUG;
    }

    @NonNull
    public ALog setShowLogs(boolean showLogs) {
        mShowLogs = showLogs;
        return this;
    }

    @Override
    public void toLog(@Nullable String s) {
        if (mShowLogs) {
            if (s == null) {
                android.util.Log.d(mLogTag, SharedStrings.NULL);

            } else {
                final int length = s.length();
                if (length > MAX_MESSAGE_LENGTH) {
                    toLog("\ts\t-\t-\t-\t-\tpart start\t-\t-\t-\t-\ts");
                    for (int start = 0, end = MAX_MESSAGE_LENGTH;
                         start < length;
                         start += MAX_MESSAGE_LENGTH, end += MAX_MESSAGE_LENGTH) {
                        if (end > length) {
                            end = length;
                        }
                        toLog(s.substring(start, end));
                    }
                    toLog("\te\t-\t-\t-\t-\tpart end  \t-\t-\t-\t-\te");

                } else {
                    android.util.Log.d(mLogTag, s);
                }
            }
        }
    }

    public void toLog(@Nullable Object o) {
        toLog(o == null ? null : o.toString());
    }

    public void toLog(@NonNull PointF p) {
        toLog("PointF\tx = " + p.x + "\ty = " + p.y);
    }

    @Override
    @NonNull
    public StringBuilder toString(@NonNull Throwable e, boolean fullStack) {
        return toString(new StringBuilder(), e, fullStack);
    }

    @NonNull
    private StringBuilder toString(@NonNull StringBuilder sb, @NonNull Throwable e, boolean fullStack) {
        e.printStackTrace();//TODO: maybe remove in future

        if (!BaseUtils.isEmpty(e.getMessage())) {
            sb.append(e.getMessage()).append(SharedStrings.NEW_LINE_C);
        }
        sb.append(e.getClass().getName());

        for (StackTraceElement s : e.getStackTrace()) {
            sb.append(SharedStrings.NEW_LINE_C).append(s.toString());
        }
        if (fullStack) {
            final Throwable cause = e.getCause();
            if (cause != null) {
                //noinspection ResultOfMethodCallIgnored
                toString(sb.append(SharedStrings.NEW_LINE_C), e, true);
            }
        }
        return sb;
    }

    @Override
    public void toLog(@NonNull Throwable e) {
        toLog(toString(e, false));
    }

    public void toLogFullStack(@NonNull Throwable e) {
        toLog(toString(e, true).toString());
    }

    public void toLog(@NonNull Cursor c) {
        final StringBuilder sb = new StringBuilder();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            BaseUtils.clear(sb);

            sb.append("< < < < < < <\n");
            for (int i = 0; i < c.getColumnCount(); i++) {
                BaseUtils.clear(sb);

                final String columnName = c.getColumnName(i);

                sb.append(columnName);
                sb.append(SharedStrings.TAB_C);
                sb.append(c.getString(c.getColumnIndex(columnName)));
                sb.append(SharedStrings.NEW_LINE_C);

                toLog(sb);
            }
            sb.append(" > > >\n \n");
        }
    }

    public void toLog(@Nullable Intent intent) {
        final StringBuilder sb = new StringBuilder().append("Intent");
        if (intent == null) {
            sb.append(" is null");

        } else {
            sb.append("\taction: ").append(intent.getAction());
            if (intent.getData() != null) {
                sb.append("\n\tdata: ").append(intent.getData());
            }
            final Bundle b = intent.getExtras();
            if (b != null) {
                sb.append("\tBundle:");
                for (String key : b.keySet()) {
                    sb.append(SharedStrings.NEW_LINE_C).append(key).append(SharedStrings.TAB_C).append(SharedStrings.TAB_C).append(b.get(key));
                }
            }
        }
        toLog(sb);

    }

    public void toLog(@Nullable Bundle b) {
        final StringBuilder sb = new StringBuilder("Bundle:");
        if (b != null) {
            for (String key : b.keySet()) {
                sb.append(SharedStrings.NEW_LINE_C).append(key).append(SharedStrings.TAB_C).append(b.get(key));
            }
        }
        toLog(sb);
    }

    public void toLog(@NonNull MotionEvent event) {
        final String action;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MASK:
                action = "ACTION_MASK";
                break;

            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                break;

            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                break;

            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;

            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;

            case MotionEvent.ACTION_OUTSIDE:
                action = "ACTION_OUTSIDE";
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";
                break;

            case MotionEvent.ACTION_POINTER_UP:
                action = "ACTION_POINTER_UP";
                break;

            case MotionEvent.ACTION_HOVER_MOVE:
                action = "ACTION_HOVER_MOVE";
                break;

            case MotionEvent.ACTION_HOVER_ENTER:
                action = "ACTION_HOVER_ENTER";
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                action = "ACTION_HOVER_EXIT";
                break;

            case MotionEvent.ACTION_POINTER_INDEX_MASK:
                action = "ACTION_POINTER_INDEX_MASK";
                break;

            case MotionEvent.ACTION_POINTER_INDEX_SHIFT:
                action = "ACTION_POINTER_INDEX_SHIFT";
                break;

            default:
                action = event.toString();
                break;
        }
        toLog("MotionEvent: " + action + "\t x: " + event.getX() + "\t y: " + event.getY());
    }

    public <T> void toLog(@Nullable Set<T> set) {
        if (set != null) {
            final StringBuilder sb = new StringBuilder();
            for (T value : set) {
                sb.append(value).append(SharedStrings.NEW_LINE_C);
            }
            toLog(sb);
        }
    }

    public void toLogScrollState(int scrollState) {
        final String string;
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_FLING://завершает
                string = "SCROLL_STATE_FLING";
                break;

            case OnScrollListener.SCROLL_STATE_IDLE://не скролится
                string = "SCROLL_STATE_IDLE";
                break;

            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://скролится
                string = "SCROLL_STATE_TOUCH_SCROLL";
                break;

            default:
                string = String.valueOf(scrollState);
                break;
        }
        toLog("scrollState: " + string);
    }

    public void toLogStackTrace(int traceSize) {
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < traceSize) {
            traceSize = elements.length;
        }
        if (elements.length < 4) {
            return;
        }
        final StringBuilder sb = new StringBuilder("StackTrace:");
        for (int i = 3; i < traceSize; i++) {
            sb.append("\n\t").append(elements[i].toString());
        }
        toLog(sb);
    }

    public <VALUE, LIST extends List<VALUE>> void toLog(@NonNull LIST list) {
        final StringBuilder sb = new StringBuilder();
        sb.append(list.getClass().getName()).append(SharedStrings.TAB_C).append("size: ").append(list.size()).append(SharedStrings.NEW_LINE_C);
        int count = 0;

        for (VALUE value : list) {
            sb.append(count++).append(SharedStrings.TAB_C).append(value).append(SharedStrings.NEW_LINE_C);
        }
        sb.deleteCharAt(sb.length() - 1);

        toLog(sb.toString());
    }

    public void toLog(@NonNull View v) {
        toLog(v.getClass().getSimpleName()
                + SharedStrings.NEW_LINE_C + SharedStrings.TAB_C
                + "x: " + v.getX() + SharedStrings.TAB_C
                + "y: " + v.getY() + SharedStrings.TAB_C
                + "width: " + v.getWidth() + SharedStrings.TAB_C
                + "height: " + v.getHeight());
    }

    public <T> T throughLog(T value) {
        toLog(value);
        return value;
    }
}