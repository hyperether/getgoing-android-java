package com.hyperether.getgoing.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.hyperether.getgoing.R;

//TODO clean up this mess - Ivana
public class ProgressBarBitmap {

    public static Bitmap getWidgetBitmap(Context context, int goal, int width, int height, int stroke, int padding) {

//        int width = 800;
//        int height = 800;
//        int stroke = 30;
//        int padding = 0;
//        float density = context.getResources().getDisplayMetrics().density;


        //Paint for arc stroke.
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        //paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setPathEffect(new CornerPathEffect(10) );

        //Paint for text values.
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.widget_text_large_value) / density));
//        mTextPaint.setColor(context.getResources().getColor(R.color.light_theme_accent));
//        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final RectF arc = new RectF();
        arc.set((stroke/2) + padding, (stroke/2) + padding, width-padding-(stroke/2), height-padding-(stroke/2));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //First draw full arc as background.
//        paint.setColor(Color.argb(75, 255, 255, 255));
        paint.setColor(context.getResources().getColor(R.color.color_button_background));
        canvas.drawArc(arc, 160, 220, false, paint);
        //Then draw arc progress with actual value.
        paint.setColor(context.getResources().getColor(R.color.light_theme_accent));

        int temp = 0;
        if(goal <= 50) {
            if(goal > 0) {
                temp = 220 / (100 / goal);
            } else {
                temp = 0;
            }
        } else {
            if(goal == 100) {
                temp = 220;
            } else {
                temp = 220 - (220 / (100 / (100 - goal)));
            }
        }

        canvas.drawArc(arc, 160, temp, false, paint);
        //Draw text value.
        canvas.drawText(goal + "%", bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent()) / 2, mTextPaint);
        //Draw widget title.
//        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.widget_text_large_title) / density));
//        canvas.drawText(context.getString(R.string.widget_text_arc_battery), bitmap.getWidth() / 2, bitmap.getHeight()-(stroke+padding), mTextPaint);

        return  bitmap;
    }
}
