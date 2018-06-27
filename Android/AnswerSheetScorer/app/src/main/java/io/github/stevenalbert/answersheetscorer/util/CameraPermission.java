package io.github.stevenalbert.answersheetscorer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Steven Albert on 6/26/2018.
 */
public final class CameraPermission {

    public static void request(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[] {Manifest.permission.CAMERA},
                requestCode);
    }

    public static boolean isGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
