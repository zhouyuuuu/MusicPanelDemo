package com.example.administrator.musiceditingpanelproject.common.util;

import android.util.Log;

import com.example.administrator.musiceditingpanelproject.BuildConfig;


/**
 * Edited by Administrator on 2018/3/4.
 */

public class LogUtil {

    private static final String TAG = "default_tag";

    private LogUtil() {
        super();
    }

    public static void e(String TAG, String description) {
        if (BuildConfig.LOG) {
            Log.e(TAG, description);
        }
    }

    public static void e(String description) {
        e(TAG, description);
    }
}
