package com.example.administrator.musiceditingpanelproject.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.administrator.musiceditingpanelproject.common.util.DimensionUtil;

/**
 * ViewPager的页码指示器
 * Edited by Administrator on 2018/3/24.
 */

public class PageIndicator extends View {
    // 默认控件大小为400dp
    private static final int SIZE_WIDTH_DEFAULT = (int) DimensionUtil.dip2px(400);
    // 默认控件大小为22dp
    private static final int SIZE_HEIGHT_DEFAULT = (int) DimensionUtil.dip2px(22);
    // 指示点宽度为3dp
    private static final int STROKE_WIDTH_DEFAULT = (int) DimensionUtil.dip2px(3);
    // 指示点间距
    private static final int POINT_SPACING = (int) DimensionUtil.dip2px(10);
    // 非当前页指示点颜色
    private static final int COLOR_POINT_NON_CURRENT = 0xff848484;
    // 当前页指示点颜色
    private static final int COLOR_POINT_CURRENT = 0xffffffff;
    // 画笔，作为成员变量避免多次创建影响性能
    private Paint mPaint;
    // 当前页码
    private int mCurrentPageIndex = 1;
    // 总页数
    private int mTotalPages = 1;

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureWidthLength(widthMeasureSpec), getMeasureHeightLength(heightMeasureSpec));
    }

    /**
     * 计算控件宽度
     */
    private int getMeasureWidthLength(int measureSpec) {
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return MeasureSpec.getSize(measureSpec) < SIZE_WIDTH_DEFAULT ? MeasureSpec.getSize(measureSpec) : SIZE_WIDTH_DEFAULT;
            case MeasureSpec.UNSPECIFIED:
                return SIZE_WIDTH_DEFAULT;
            default:
                return 0;
        }
    }

    /**
     * 计算控件高度
     */
    private int getMeasureHeightLength(int measureSpec) {
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return MeasureSpec.getSize(measureSpec) < SIZE_HEIGHT_DEFAULT ? MeasureSpec.getSize(measureSpec) : SIZE_HEIGHT_DEFAULT;
            case MeasureSpec.UNSPECIFIED:
                return SIZE_HEIGHT_DEFAULT;
            default:
                return 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 起始点坐标，为中点减去（totalPage-1）段间距的一半
        int startX = (getWidth() - (mTotalPages - 1) * POINT_SPACING) / 2;
        // 垂直居中Y值
        int middleY = getHeight() / 2;
        // 从起始点开始画mTotalPages个点
        for (int i = 1; i <= mTotalPages; i++) {
            if (i == mCurrentPageIndex) {
                mPaint.setColor(COLOR_POINT_CURRENT);
            } else {
                mPaint.setColor(COLOR_POINT_NON_CURRENT);
            }
            // 如第二个点的X坐标为 ，startPoint + （2-1）段间距
            canvas.drawPoint(startX + (i - 1) * POINT_SPACING, middleY, mPaint);
        }
    }

    /**
     * 设置当前页码
     *
     * @param currentPageIndex 页码
     */
    public void setCurrentPageIndex(int currentPageIndex) {
        if (currentPageIndex < 1 || currentPageIndex > mTotalPages) return;
        this.mCurrentPageIndex = currentPageIndex;
        invalidate();
    }

    /**
     * 设置总页数
     *
     * @param totalPages 页数
     */
    public void setTotalPages(int totalPages) {
        if (totalPages < 1) return;
        this.mTotalPages = totalPages;
        setCurrentPageIndex(1);
    }
}
