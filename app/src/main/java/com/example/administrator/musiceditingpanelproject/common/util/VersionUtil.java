package com.example.administrator.musiceditingpanelproject.common.util;

import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;

/**
 * 版本工具类，用来比对音频当前版本是否符合条件
 * Edited by Administrator on 2018/3/25.
 */

public class VersionUtil {

    // 分隔符
    private static final String DELIMITER = "\\.";

    /**
     * 当前版本要高于最低版本且低于最高版本
     *
     * @param minVersion 最低版本
     * @param maxVersion 最高版本
     * @return 当前版本是否符合条件
     */
    public static boolean versionIsMatch(String minVersion, String maxVersion) {
        String version = MusicEditingPanelApplication.getVersion();
        if (version == null) return false;
        String[] versionNumbers = version.split(DELIMITER);
        String[] minVersionNumbers = minVersion.split(DELIMITER);
        String[] maxVersionNumbers = maxVersion.split(DELIMITER);
        return isHigherThanMinVersion(versionNumbers, minVersionNumbers) && isLowerThanMaxVersion(versionNumbers, maxVersionNumbers);
    }

    /**
     * 返回当前版本是否高于最低版本
     *
     * @param versionNumbers    当前版本
     * @param minVersionNumbers 最低版本
     * @return 当前版本是否高于最低版本
     */
    private static boolean isHigherThanMinVersion(String[] versionNumbers, String[] minVersionNumbers) {
        if (versionNumbers.length != minVersionNumbers.length) return false;
        // 从最前面的数字开始比较，如果高了则返回true，如果低了则放回false，如果相同，比较下一个数字，如果所有数字相同，返回true
        for (int i = 0; i < versionNumbers.length; i++) {
            int versionNumberInt = Integer.parseInt(versionNumbers[i]);
            int minVersionNumberInt = Integer.parseInt(minVersionNumbers[i]);
            if (versionNumberInt > minVersionNumberInt) return true;
            if (versionNumberInt < minVersionNumberInt) return false;
        }
        return true;
    }

    /**
     * 返回当前版本是否低于最高版本
     *
     * @param versionNumbers    当前版本
     * @param maxVersionNumbers 最高版本
     * @return 当前版本是否低于最高版本
     */
    private static boolean isLowerThanMaxVersion(String[] versionNumbers, String[] maxVersionNumbers) {
        if (versionNumbers.length != maxVersionNumbers.length) return false;
        // 从最前面的数字开始比较，如果低了则返回true，如果高了则放回false，如果相同，比较下一个数字，如果所有数字相同，返回true
        for (int i = 0; i < versionNumbers.length; i++) {
            int versionNumberInt = Integer.parseInt(versionNumbers[i]);
            int maxVersionNumberInt = Integer.parseInt(maxVersionNumbers[i]);
            if (versionNumberInt > maxVersionNumberInt) return false;
            if (versionNumberInt < maxVersionNumberInt) return true;
        }
        return true;
    }
}
