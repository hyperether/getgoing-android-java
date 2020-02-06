package com.hyperether.getgoing.ui.bindingadapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.formatter.DistanceProgressFormatter;
import com.hyperether.getgoing.ui.formatter.TimeProgressFormatter;

import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;

public class LastExerciseBindingAdapter {

    @androidx.databinding.BindingAdapter("progress_activity_icon")
    public static void displayActivityProgressIcon(View view, int pAcId) {

        Drawable drawable;
        view.setVisibility(View.VISIBLE);

        switch (pAcId) {
            case ACTIVITY_WALK_ID:
                drawable = view.getResources().getDrawable(R.drawable.ic_light_walking_icon_white);
                ((ImageView) view).setImageDrawable(drawable);
                break;
            case ACTIVITY_RUN_ID:
                drawable = view.getResources().getDrawable(R.drawable.ic_light_running_icon_white);
                ((ImageView) view).setImageDrawable(drawable);
                break;
            case ACTIVITY_RIDE_ID:
                drawable = view.getResources().getDrawable(R.drawable.ic_light_bicycling_icon);
                ((ImageView) view).setImageDrawable(drawable);
                break;
            default:
                view.setVisibility(View.INVISIBLE);
        }
    }

    @androidx.databinding.BindingAdapter("progress_activity_name")
    public static void displayActivityProgressName(View view, int pAcId) {

        String acName = "";

        switch (pAcId) {
            case ACTIVITY_WALK_ID:
                acName = view.getResources().getString(R.string.walking);
                break;
            case ACTIVITY_RUN_ID:
                acName = view.getResources().getString(R.string.running);
                break;
            case ACTIVITY_RIDE_ID:
                acName = view.getResources().getString(R.string.cycling);
        }

        ((TextView) view).setText(acName);
    }

    @androidx.databinding.BindingAdapter("progress_activity")
    public static void displayActivityProgress(View view, DbRoute lastRoute) {

        int progress = 0;
        double distance = 0;

        if (lastRoute != null) {
            if (lastRoute.getLength() >= 0) {
                distance = lastRoute.getLength();
            }

            if (lastRoute.getGoal() > 0) {
                progress = (int) (distance / lastRoute.getGoal() * 100);
            } else {
                // goal will never be 0 with current implementation, this check is not necessary
                progress = 100;
            }
        }

        ((CircleProgressBar) view).setProgressFormatter(new DistanceProgressFormatter(distance));
        ((CircleProgressBar) view).setProgress(progress);
    }

    @androidx.databinding.BindingAdapter("progress_time")
    public static void displayTimeProgress(View view, long pDuration) {
        ((CircleProgressBar) view).setProgressFormatter(new TimeProgressFormatter(pDuration));
    }

}
