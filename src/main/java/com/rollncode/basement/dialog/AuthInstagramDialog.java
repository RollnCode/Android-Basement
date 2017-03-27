package com.rollncode.basement.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rollncode.basement.interfaces.ObjectsReceiver;
import com.rollncode.basement.utility.ALog;
import com.rollncode.basement.utility.BaseUtils;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 01.02.17
 */

public final class AuthInstagramDialog {

    public final static int CODE = 12461353;

    private static final String AUTHORIZE = "https://api.instagram.com/oauth/authorize/";
    private static final String ACCESS_TOKEN = "https://api.instagram.com/oauth/access_token";

    //VIEW
    private final Dialog mDialog;

    //VALUE's
    private final String mClientId;
    private final String mClientSecret;
    private final String mRedirectUrl;

    //CALLBACK
    private final WeakReference<ObjectsReceiver> mReceiver;

    public static void show(@NonNull Context context, @NonNull ObjectsReceiver receiver
            , @NonNull String clientId, @NonNull String clientSecret, @NonNull String redirectUrl) {
        new AuthInstagramDialog(context, receiver, clientId, clientSecret, redirectUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private AuthInstagramDialog(@NonNull Context context, @NonNull ObjectsReceiver receiver
            , @NonNull String clientId, @NonNull String clientSecret, @NonNull String redirectUrl) {
        final CookieManager manager = CookieManager.getInstance();
        if (manager.hasCookies()) {
            //noinspection deprecation
            manager.removeSessionCookie();
            //noinspection deprecation
            manager.removeAllCookie();
        }
        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUrl = redirectUrl;

        final WebView view = new WebView(context);
        view.getSettings().setJavaScriptEnabled(true);
        view.setWebViewClient(newWebViewClient());
        view.clearCache(false);

        view.loadUrl(authorizeUrl());
        {
            mDialog = new Dialog(context);
            mDialog.setCancelable(true);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            mDialog.show();
        }
        mReceiver = new WeakReference<>(receiver);
    }

    private void dismiss() {
        mDialog.dismiss();
    }

    @CheckResult
    private String authorizeUrl() {
        return AUTHORIZE
                + "?client_id=" + mClientId
                + "&redirect_uri=" + mRedirectUrl
                + "&response_type=code&display=touch&scope=likes+comments+relationships+follower_list";
    }

    @CheckResult
    private WebViewClient newWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final OnClickListener listener = new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            handler.proceed();

                        } else {
                            handler.cancel();
                            dismiss();
                        }
                    }
                };
                new AlertDialog.Builder(view.getContext())
                        .setMessage("SSL certificate not valid")
                        .setPositiveButton("Proceed", listener)
                        .setNegativeButton("Cancel", listener)
                        .show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                final LayoutParams params = view.getLayoutParams();
                if (params.height == LayoutParams.WRAP_CONTENT) {
                    params.height = view.getHeight();
                    view.requestLayout();
                }
                if (url.contains("code=")) {
                    final Uri uri = Uri.parse(url);
                    //noinspection ResourceType
                    new TokenRequest(AuthInstagramDialog.this, uri.getQueryParameter("code")).execute();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                dismiss();
            }
        };
    }

    private static final class TokenRequest extends AsyncTask<Void, Void, Object> {

        private final AuthInstagramDialog mDialog;
        private final String mCode;

        private ProgressDialog mProgressDialog;

        private TokenRequest(@NonNull AuthInstagramDialog dialog, @NonNull String code) {
            mDialog = dialog;
            mCode = code;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mDialog.mDialog.getContext());
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setTitle("Please, wait a moment...");
            mProgressDialog.setIndeterminate(true);

            mProgressDialog.show();
        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                final Request.Builder builder = new Request.Builder();
                builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")
                        , "client_id=" + mDialog.mClientId +
                                "&client_secret=" + mDialog.mClientSecret +
                                "&grant_type=authorization_code" +
                                "&redirect_uri=" + mDialog.mRedirectUrl +
                                "&code=" + mCode));

                builder.url(ACCESS_TOKEN);
                final Request request = builder.build();

                final Response response = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build().newCall(request).execute();

                final String raw = BaseUtils.toString(response.body().byteStream(), true);
                ALog.LOG.toLog(raw);

                final JSONObject object = (JSONObject) new JSONTokener(raw).nextValue();

                final String token = object.getString("access_token");
                final JSONObject user = object.getJSONObject("user");
                final String id = user.getString("id");
                final String username = user.getString("username");
                final String bio = user.optString("bio");

                return new String[]{token, id, username, bio};

            } catch (Exception e) {
                ALog.LOG.toLog(e);

                return e;
            }
        }

        @Override
        protected void onPostExecute(@NonNull Object object) {
            //noinspection ResourceType
            BaseUtils.receiveObjects(mDialog.mReceiver, CODE, object);

            try {
                mProgressDialog.dismiss();
                mDialog.dismiss();

            } catch (Exception ignore) {
            } finally {
                mProgressDialog = null;
            }
        }
    }
}