package com.example.administrator.musiceditingpanelproject.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.musiceditingpanelproject.common.util.DimensionUtil;

/**
 * Edited by Administrator on 2018/3/30.
 */

public class DownloadPausedIconView extends View {
    // 默认控件大小为20dp
    private static final int SIZE_DEFAULT = (int) DimensionUtil.dip2px(20);
    // 线条宽度为2dp
    private static final int STROKE_WIDTH_DEFAULT = (int) DimensionUtil.dip2px(2);
    // 下载图标颜色
    private static final int COLOR_ICON = 0xff404040;
    // 画笔，作为成员变量避免多次创建影响性能
    private Paint mPaint;
    // 三角形轨迹
    private Path mPath;
    // 轨迹是否绘画过，防止重复构建三角形，没必要
    private boolean mPathDrawing = false;

    public DownloadPausedIconView(Context context) {
        this(context, null);
    }

    public DownloadPausedIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadPausedIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 画笔
        mPaint = new Paint();
        // 抗锯齿
        mPaint.setAntiAlias(true);
        // 边缘宽度
        mPaint.setStrokeWidth(STROKE_WIDTH_DEFAULT);
        // 线条尾部形状
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 画笔颜色
        mPaint.setColor(COLOR_ICON);
        // 三角形轨迹
        mPath = new Path();
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
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width / 2, height / 2, radio, mPaint);
        //根据Path,绘制三角形，如果绘画过，就不用再画了
        if (!mPathDrawing) {
            // 左上角的点开始
            int leftTopPointX = width * 5 / 16;
            int leftTopPointY = height * 4 / 16;
            mPath.moveTo(leftTopPointX, leftTopPointY);
            // 连到左下角的点
            int leftBottomPointX = width * 5 / 16;
            int leftBottomPointY = height * 12 / 16;
            mPath.lineTo(leftBottomPointX, leftBottomPointY);
            // 连到右边的点
            int rightMiddlePointX = width * 13 / 16;
            int rightMiddlePointY = height / 2;
            mPath.lineTo(rightMiddlePointX, rightMiddlePointY);
            // 封闭组成一个三角形
            mPath.close();
            // 轨迹已经绘画完成
            mPathDrawing = true;
        }
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mPath, mPaint);
    }
}
