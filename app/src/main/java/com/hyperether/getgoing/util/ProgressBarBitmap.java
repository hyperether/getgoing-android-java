package com.hyperether.getgoing.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.hyperether.getgoing.R;

public class ProgressBarBitmap {

    public static Bitmap getWidgetBitmap(Context context, long goal, double length, int width, int height, float startAngle, float sweepAngle, int stroke, int padding) {
        float scale = 0;
        if (goal > 0 && length >= 0) {
            scale = (float) (length / goal);
        }

        //Paint for arc stroke.
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        final RectF arc = new RectF();
        arc.set((stroke / 2) + padding, (stroke / 2) + padding, width - padding - (stroke / 2), height - padding - (stroke / 2));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //draw full arc as background.
        paint.setColor(context.getResources().getColor(R.color.color_button_background));
        canvas.drawArc(arc, startAngle, sweepAngle, false, paint);

        //draw arc progress with actual value.
        paint.setColor(context.getResources().getColor(R.color.light_theme_accent));

        float temp = 0;
        if (scale <= 1 && scale >= 0) {
            temp = sweepAngle * scale;
        } else if (scale < 0) {
            temp = 0;
        } else {
            temp = sweepAngle;
        }

        canvas.drawArc(arc, startAngle, temp, false, paint);

        return bitmap;
    }
}
