package com.hyperether.getgoing.ui.dynamicview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class CurvedBottomView extends View {

    private Path mPath;
    private Paint mPaint;

    private int mNavBarWidth;
    private int mNavBarHeight;

    private final int BUTTON_CURVE_RADIUS = 110;

    private Point mFirstCurveStart = new Point();
    private Point mFirstCurveEnd = new Point();
    private Point mFirstCurveControl1 = new Point();
    private Point mFirstCurveControl2 = new Point();

    private Point mSecondCurveStart = new Point();
    private Point mSecondCurveEnd = new Point();
    private Point mSecondCurveControl1 = new Point();
    private Point mSecondCurveControl2 = new Point();

    public CurvedBottomView(Context context) {
        super(context);
        init();
    }
    public CurvedBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CurvedBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        mPath = new Path();
        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.WHITE);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mNavBarHeight = getHeight();
        mNavBarWidth = getWidth();

        mFirstCurveStart.set((mNavBarWidth / 2) - (BUTTON_CURVE_RADIUS * 2) - (BUTTON_CURVE_RADIUS / 3), 0);
        mFirstCurveEnd.set((mNavBarWidth / 2), BUTTON_CURVE_RADIUS - BUTTON_CURVE_RADIUS / 4);

        mSecondCurveStart = mFirstCurveEnd;
        mSecondCurveEnd.set((mNavBarWidth / 2) + (BUTTON_CURVE_RADIUS * 2) + (BUTTON_CURVE_RADIUS / 3), 0);

        mFirstCurveControl1.set(mFirstCurveStart.x + BUTTON_CURVE_RADIUS + (BUTTON_CURVE_RADIUS / 4), mFirstCurveStart.y);
        mFirstCurveControl2.set(mFirstCurveEnd.x - (BUTTON_CURVE_RADIUS * 2) + BUTTON_CURVE_RADIUS, mFirstCurveEnd.y);

        mSecondCurveControl1.set(mSecondCurveStart.x + (BUTTON_CURVE_RADIUS * 2) - BUTTON_CURVE_RADIUS, mSecondCurveStart.y);
        mSecondCurveControl2.set(mSecondCurveEnd.x - (BUTTON_CURVE_RADIUS + (BUTTON_CURVE_RADIUS / 4)), mSecondCurveEnd.y);

        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(mFirstCurveStart.x, mFirstCurveStart.y);

        mPath.cubicTo(mFirstCurveControl1.x, mFirstCurveControl1.y, mFirstCurveControl2.x, mFirstCurveControl2.y, mFirstCurveEnd.x, mFirstCurveEnd.y);
        mPath.cubicTo(mSecondCurveControl1.x, mSecondCurveControl1.y, mSecondCurveControl2.x, mSecondCurveControl2.y, mSecondCurveEnd.x, mSecondCurveEnd.y);

        mPath.lineTo(mNavBarWidth, 0);
        mPath.lineTo(mNavBarWidth, mNavBarHeight);
        mPath.lineTo(0, mNavBarHeight);
        mPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }
}
