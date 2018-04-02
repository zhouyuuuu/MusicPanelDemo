package com.example.administrator.musiceditingpanelproject.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.musiceditingpanelproject.common.util.DimensionUtil;

/**
 * 下载图标
 * Edited by Administrator on 2018/3/24.
 */

public class DownloadIconView extends View {
    // 默认控件大小为20dp
    private static final int SIZE_DEFAULT = (int) DimensionUtil.dip2px(20);
    // 线条宽度为2dp
    private static final int STROKE_WIDTH_DEFAULT = (int) DimensionUtil.dip2px(2);
    // 下载图标颜色
    private static final int COLOR_ICON = 0xff404040;
    // 画笔，作为成员变量避免多次创建影响性能
    private Paint mPaint;

    public DownloadIconView(Context context) {
        this(context, null);
    }

    public DownloadIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 画笔
        mPaint = new Paint();
        // 抗锯齿
        mPaint.setAntiAlias(true);
        // 中空模式
        mPaint.setStyle(Paint.Style.STROKE);
        // 边缘宽度
        mPaint.setStrokeWidth(STROKE_WIDTH_DEFAULT);
        // 线条尾部形状
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 画笔颜色
        mPaint.setColor(COLOR_ICON);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureLength(widthMeasureSpec), getMeasureLength(heightMeasureSpec));
    }

    private int getMeasureLength(int measureSpec) {
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return MeasureSpec.getSize(measureSpec) < SIZE_DEFAULT ? MeasureSpec.getSize(measureSpec) : SIZE_DEFAULT;
            case MeasureSpec.UNSPECIFIED:
                return SIZE_DEFAULT;
            default:
                return 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radio = width > height ? (height - STROKE_WIDTH_DEFAULT) / 2 : (width - STROKE_WIDTH_DEFAULT) / 2;
        // 画圆
        canvas.drawCircle(width / 2, height / 2, radio, mPaint);
        // 画中间线
        canvas.drawLine(width / 2, height / 4, width / 2, height * 3 / 4, mPaint);
        // 画左边斜线
        canvas.drawLine(width / 4 + STROKE_WIDTH_DEFAULT / 2, height / 2 + STROKE_WIDTH_DEFAULT / 2, width / 2, height * 3 / 4, mPaint);
        // 画右边斜线
        canvas.drawLine(width * 3 / 4 - STROKE_WIDTH_DEFAULT / 2, height / 2 + STROKE_WIDTH_DEFAULT / 2, width / 2, height * 3 / 4, mPaint);
    }

}
