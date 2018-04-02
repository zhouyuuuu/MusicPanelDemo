package com.example.administrator.musiceditingpanelproject.common.util;

import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;

/**
 * 尺寸类型转换工具类
 * Edited by Administrator on 2018/3/24.
 */

public class DimensionUtil {

    /**
     * de转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static float dip2px(float dpValue) {
        float scale = MusicEditingPanelApplication.getApplication().getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    /**
     * px转dp
     *
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dip(float pxValue) {
        final float scale = MusicEditingPanelApplication.getApplication().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
