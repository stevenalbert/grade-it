package io.github.stevenalbert.gradeit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Steven Albert on 6/27/2018.
 */
public final class BitmapProcess {

    public static Bitmap getBitmap(Context context, Uri uri) {
        InputStream imageStream = getInputStream(context, uri);

        if(imageStream == null)
            return null;

        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

        try {
            imageStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getExifRotatedBitmap(Context context, Uri uri) {
        InputStream imageStream = getInputStream(context, uri);

        if(imageStream == null)
            return null;

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imageStream);
            imageStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(exifInterface == null)
            return null;

        int rotation = 0;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        Bitmap bitmap = getBitmap(context, uri);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return bitmap;
    }

    public static Bitmap getScaledFitBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        double scale = Math.min(((double) maxHeight / (double) bitmap.getHeight()),
                ((double) maxWidth / (double) bitmap.getWidth()));

        Log.d("Process", "width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight() + ", scale = " + scale);

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * scale),
                (int) Math.floor(bitmap.getHeight() * scale), true);

        return bitmap;
    }

    private static InputStream getInputStream(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return inputStream;
    }
}
