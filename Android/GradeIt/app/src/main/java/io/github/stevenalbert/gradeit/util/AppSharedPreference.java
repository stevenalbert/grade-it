package io.github.stevenalbert.gradeit.util;

import android.content.Context;

import io.github.stevenalbert.gradeit.BuildConfig;

/**
 * Created by Steven Albert on 10/21/2018.
 */
public class AppSharedPreference {

    private static final String PREF_NAME = BuildConfig.APPLICATION_ID;
    private static final String METADATA_NAME = "metadata";
    private static final String INIT = "init";

    public static String getSavedMetadataString(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(METADATA_NAME, "");
    }

    public static void saveMetadataString(Context context, String metadataString) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(METADATA_NAME, metadataString).apply();
    }

    public static boolean isInit(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(INIT, false);
    }

    public static void setInit(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(INIT, true).apply();
    }
}
