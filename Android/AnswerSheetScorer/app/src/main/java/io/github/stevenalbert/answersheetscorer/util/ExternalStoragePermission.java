package io.github.stevenalbert.answersheetscorer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Steven Albert on 6/25/2018.
 */
public final class ExternalStoragePermission {

    private static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static void requestWrite(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[] {WRITE_PERMISSION},
                requestCode);
    }

    public static void requestRead(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[] {READ_PERMISSION},
                requestCode);
    }

    public static boolean isWriteGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, WRITE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isReadGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, READ_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
