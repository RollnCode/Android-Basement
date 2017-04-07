package com.rollncode.basement.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.rollncode.basement.exception.BaseApiException;
import com.rollncode.basement.utility.BaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 23/01/17
 */
public abstract class AsyncServerRequest<RESULT> extends AsyncNetworkRequest<RESULT> {

    protected static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    protected static final SimpleArrayMap<String, Object> EMPTY_PARAMETERS = new SimpleArrayMap<>(0);
    protected static final String ARRAY_TYPE = "ARRAY_TYPE";

    protected AsyncServerRequest(@NonNull Class<RESULT> answerClass) {
        super(answerClass);
    }

    @NonNull
    @Override
    protected Request prepareRequest() throws Exception {
        final SimpleArrayMap<String, Object> parameters = getParameters();
        final String token = getToken(isTokenRequired());
        final Builder builder = new Builder();

        if (isTokenRequired()) {
            if (TextUtils.isEmpty(token)) {
                throw new IllegalStateException("Token is not available");
            }
            attachToken(builder, token);
        }
        final String url = attachParameters(builder, getFullUrl(), parameters);

        try {
            return builder.build();

        } finally {
            if (showLog()) {
                //noinspection ThrowFromFinallyBlock
                LOG.toLog(">>> Q\tcode: " + getCode()
                        + "\tclass: " + getClass().getSimpleName()
                        + "\n\turl: " + url
                        + "\n\ttype: " + getRequestType()
                        + (token == null ? "" : "\n\ttoken: " + token)
                        + (parameters == null || parameters == EMPTY_PARAMETERS ? "" : "\n\tparameters: " + toString(parameters))
                        + "\n>>>");
            }
        }
    }

    @Nullable
    protected abstract String getToken(boolean isTokenRequired);

    protected abstract boolean isTokenRequired();

    protected abstract void attachToken(@NonNull Builder builder, @NonNull String token);

    @NonNull
    protected String getFullUrl() {
        return getServerUrl() + getUrl();
    }

    @NonNull
    protected abstract String getServerUrl();

    @NonNull
    protected abstract String getUrl();

    @NonNull
    protected String attachParameters(@NonNull Builder builder, @NonNull String url, @Nullable SimpleArrayMap<String, Object> parameters) throws Exception {
        switch (getRequestType()) {
            case RequestType.GET:
                if (parameters != null) {
                    final HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
                    for (int i = 0, size = parameters.size(); i < size; i++) {
                        urlBuilder.addQueryParameter(parameters.keyAt(i), parameters.valueAt(i).toString());
                    }
                    url = urlBuilder.toString();
                }
                builder.get();
                break;

            case RequestType.HEAD:
                if (parameters != null) {
                    throw new IllegalStateException("HEAD is not require any parameters");
                }
                builder.head();
                break;

            case RequestType.PUT:
                builder.put(toRequestBody(parameters));
                break;

            case RequestType.POST:
                builder.post(toRequestBody(parameters));
                break;

            case RequestType.DELETE:
                builder.delete(toRequestBody(parameters));
                break;

            default:
                throw new IllegalStateException("Unknown RequestType");
        }
        builder.url(url);

        return url;
    }

    @RequestType
    @NonNull
    protected abstract String getRequestType();

    @Nullable
    protected abstract SimpleArrayMap<String, Object> getParameters() throws Exception;

    @Nullable
    protected RequestBody toRequestBody(@Nullable SimpleArrayMap<String, Object> parameters) throws JSONException {
        return parameters == null ? null : RequestBody.create(getMediaType(), toString(parameters));
    }

    @NonNull
    protected MediaType getMediaType() {
        return TYPE_JSON;
    }

    @NonNull
    protected final String toString(@NonNull SimpleArrayMap<String, Object> parameters) throws JSONException {
        final MediaType type = getMediaType();
        if (TYPE_JSON.equals(type)) {
            return toStringLikeJson(parameters);
        }
        throw new IllegalStateException();
    }

    @NonNull
    private String toStringLikeJson(@NonNull SimpleArrayMap<String, Object> parameters) throws JSONException {
        if (parameters.size() == 1 && parameters.containsKey(ARRAY_TYPE)) {
            return parameters.get(ARRAY_TYPE).toString();
        }
        final JSONObject object = new JSONObject();
        Object value;
        for (int i = 0, size = parameters.size(); i < size; i++) {
            value = parameters.valueAt(i);
            object.put(parameters.keyAt(i), value == null ? JSONObject.NULL : value);
        }
        return object.toString();
    }

    @Nullable
    @Override
    protected final RESULT handleResponse(@NonNull Response response) throws Exception {
        final int responseCode = response.code();
        InputStream is = null;
        String raw = null;

        try {
            is = response.body().byteStream();

            raw = BaseUtils.toString(is, false);

            checkResponseCode(raw, responseCode);

            return parseResult(raw);

        } finally {
            BaseUtils.closeSilently(is);
            if (showLog()) {
                LOG.toLog("<<< A\tcode: " + getCode()
                        + "\tclass: " + getClass().getSimpleName()
                        + "\n\turl: " + getFullUrl()
                        + "\n\tresponseCode: " + responseCode
                        + "\n\traw: " + raw
                        + "\n>>>");
            }
        }
    }

    protected abstract void checkResponseCode(@Nullable String string, int responseCode) throws Exception;

    @Nullable
    protected abstract RESULT parseResult(@Nullable String string) throws Exception;

    @Nullable
    @Override
    protected RESULT handleException(@NonNull Throwable t) {
        if (t == BaseApiException.SILENT) {
            if (showLog()) {
                LOG.toLog("<<< E silent\tcode: " + getCode()
                        + "\tclass: " + getClass().getSimpleName()
                        + "\t>>>");
            }
            return null;
        }
        if (showLog()) {
            LOG.toLog("<<< E\tcode: " + getCode()
                    + "\tclass: " + getClass().getSimpleName()
                    + "\n" + LOG.toString(t, false)
                    + "\n>>>");
        }
        //noinspection ThrowableResultOfMethodCallIgnored
        throw t instanceof BaseApiException ? (BaseApiException) t : toApiException(t);
    }

    @NonNull
    protected abstract BaseApiException toApiException(@NonNull Throwable t);

    protected abstract boolean showLog();

    @StringDef({RequestType.GET, RequestType.PUT, RequestType.POST, RequestType.HEAD, RequestType.DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestType {

        String GET = "GET";
        String PUT = "PUT";
        String POST = "POST";
        String HEAD = "HEAD";
        String DELETE = "DELETE";
    }
}