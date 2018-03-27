package com.example.administrator.musiceditingpanelproject.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Edited by Administrator on 2018/3/24.
 */

public class MusicEditingPanelApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    /**
     * 获得App的Context
     *
     * @return 上下文
     */
    public static Context getApplication() {
        return mContext;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion() {
        try {
            PackageManager manager = getApplication().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getApplication().getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
