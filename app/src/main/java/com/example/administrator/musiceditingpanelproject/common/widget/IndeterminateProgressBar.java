package com.example.administrator.musiceditingpanelproject.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.musiceditingpanelproject.common.util.DimensionUtil;

/**
 * 无限循环的进度条
 * Edited by Administrator on 2018/3/15.
 */

public class IndeterminateProgressBar extends View {
    // 默认进度条大小为20dp
    private static final int SIZE_DEFAULT = (int) DimensionUtil.dip2px(20);
    // 默认进度条的条宽度为2dp
    private static final int STROKE_WIDTH_DEFAULT = (int) DimensionUtil.dip2px(2);
    // 每次刷新变化角度
    private static final int ANGLE_MOVE = 5;
    // 颜色进度占用角度
    private static final int ANGLE_SWEEP = 90;
    // 动画效果刷新延时
    private static final int INVALIDATE_DELAY = 0;
    // 进度条底色
    private static final int COLOR_BACKGROUND = 0xff404040;
    // 进度条进度颜色
    private static final int COLOR_PROGRESS = 0xffff464b;
    // 画笔，作为成员变量避免多次创建影响性能
    private Paint mPaint;
    // 矩形区域，作用同画笔
    private RectF mRectF = null;
    // 颜色进度角度位置,默认一开始是-90度
    private int mStartAngle = -90;

    public IndeterminateProgressBar(Context context) {
        this(context, null);
    }

    public IndeterminateProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndeterminateProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 画笔
        mPaint = new Paint();
        // 抗锯齿
        mPaint.setAntiAlias(true);
        // 中空模式
        mPaint.setStyle(Paint.Style.STROKE);
        // 线条宽度
        mPaint.setStrokeWidth(STROKE_WIDTH_DEFAULT);
        // 线条尾部形状
        mPaint.setStrokeCap(Paint.Cap.ROUND);
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
        // 进度条灰色底
        mPaint.setColor(COLOR_BACKGROUND);
        // 画圆
        // 取宽和高中比较短的那个的一半作为半径，要减去进度条的条宽度否则进度条过大，显示不全
        int radio = width > height ? (height - STROKE_WIDTH_DEFAULT) / 2 : (width - STROKE_WIDTH_DEFAULT) / 2;
        int centerX = width / 2;
        int centerY = height / 2;
        canvas.drawCircle(centerX, centerY, radio, mPaint);
        // 进度条的颜色
        mPaint.setColor(COLOR_PROGRESS);
        // 画扇形，LOLLIPOP以上版本调用该方法可以不用new一个RectF，性能比较好一点
        // 这个rect的意思是限定扇形最大圆的正方形
        int rectLeft = width / 2 - radio;
        int rectTop = height / 2 - radio;
        int rectRight = width / 2 + radio;
        int rectBottom = height / 2 + radio;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawArc(rectLeft, rectTop, rectRight, rectBottom, mStartAngle, ANGLE_SWEEP, false, mPaint);
        } else {
            if (mRectF == null)
                mRectF = new RectF(rectLeft, rectTop, rectRight, rectBottom);
            canvas.drawArc(mRectF, mStartAngle, ANGLE_SWEEP, false, mPaint);
        }
        // 更新起始角度
        mStartAngle = (mStartAngle + ANGLE_MOVE) % 360;
        // 重复刷新
        postInvalidateDelayed(INVALIDATE_DELAY);
    }
}
