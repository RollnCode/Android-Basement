package com.rollncode.basement.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.rollncode.basement.interfaces.ObjectsReceiver;
import com.rollncode.basement.interfaces.SharedStrings;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 07.02.17
 */
public abstract class BasePhotoContentHelper {

    private static final String EXTRA_0 = "taphhe.EXTRA_0";//mUri

    private static final int FROM_GALLERY = 1757;
    private static final int FROM_CAMERA = 7571;

    //VALUE's
    private final String mAuthority;
    @IdRes
    private final int mCode;

    private final File mRoot;
    private Uri mUri;

    //CALLBACK
    private final WeakReference<Fragment> mFragment;

    public BasePhotoContentHelper(@NonNull Fragment fragment, @NonNull String authority, @IdRes int code) {
        mFragment = new WeakReference<>(fragment);
        mAuthority = authority;
        mCode = code;

        mRoot = getRootTempDir(fragment.getContext());
        if (!mRoot.exists()) {
            //noinspection ResultOfMethodCallIgnored
            mRoot.mkdirs();
        }
    }

    @NonNull
    protected abstract File getRootTempDir(@NonNull Context context);

    public final void onSelectSource(boolean camera) {
        final Fragment fragment = mFragment.get();
        if (fragment == null) {
            return;
        }
        if (camera) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mUri = createTempFileUri(intent, fragment.getContext());

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                fragment.startActivityForResult(intent, FROM_CAMERA);

            } catch (Exception e) {
                mUri = null;
            }
        } else {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType(SharedStrings.MIME_IMAGE);
            if (intent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
                fragment.startActivityForResult(intent, FROM_GALLERY);
            }
        }
    }

    @CallSuper
    public void onRestoreState(@Nullable Bundle b) {
        if (b != null) {
            mUri = b.getParcelable(EXTRA_0);
        }
    }

    @CallSuper
    public void onSaveState(@NonNull Bundle b) {
        b.putParcelable(EXTRA_0, mUri);
    }

    public final boolean onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (resultCode == Activity.RESULT_OK
                && (requestCode == FROM_GALLERY || requestCode == FROM_CAMERA)) {
            final Uri uri = requestCode == FROM_CAMERA ? mUri : data.getData();
            if (uri != null) {
                final Fragment fragment = mFragment.get();
                if (fragment instanceof ObjectsReceiver) {
                    ((ObjectsReceiver) fragment).onObjectsReceive(mCode, uri);
                }
                mUri = null;
                return true;
            }
        }
        return false;
    }

    public final boolean isLocalCacheUri(@NonNull Uri uri) {
        final String string = uri.toString();
        return string.contains(mAuthority) || string.contains(mRoot.getPath());
    }

    @CheckResult
    public final File toTempFile(@NonNull Uri uri) {
        return new File(mRoot, uri.getLastPathSegment());
    }

    @CheckResult
    private Uri createTempFileUri(@NonNull Intent intent, @NonNull Context context) throws IOException {
        return grantUriPermissions(context, intent, FileProvider.getUriForFile(context, mAuthority, createTempFile()));
    }

    @NonNull
    protected abstract String generateTempFileName();

    @CheckResult
    public final File createTempFile() throws IOException {
        final File file = new File(mRoot, generateTempFileName());
        deleteOnExit(file);

        if (file.createNewFile()) {
            return file;
        }
        throw new IllegalStateException("Can't create file");
    }

    @NonNull
    private static Uri grantUriPermissions(@NonNull Context context, @NonNull Intent intent, @NonNull Uri uri) {
        final List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfo) {
            context.grantUriPermission(info.activityInfo.packageName, uri
                    , Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return uri;
    }

    protected void deleteOnExit(@NonNull File file) {
        file.deleteOnExit();
    }
}