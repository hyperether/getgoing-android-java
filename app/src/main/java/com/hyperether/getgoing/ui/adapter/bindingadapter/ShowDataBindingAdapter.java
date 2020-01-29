package com.hyperether.getgoing.ui.adapter.bindingadapter;

import android.view.View;
import android.widget.TextView;

public class ShowDataBindingAdapter {

    @androidx.databinding.BindingAdapter("goal")
    public static void displayGoalValue(View view, double pGoal) {
        StringBuilder goal = new StringBuilder();
        if (pGoal < 1000) {
            goal.append((int) pGoal)
                    .append(" m");
        } else {
            goal.append(String.format("%.2f", pGoal / 1000))
                    .append(" km");
        }

        ((TextView) view).setText(goal.toString());
    }

    @androidx.databinding.BindingAdapter("displayDistance")
    public static void displayDistance(View view, double pDist) {
        double dist = 0;
        if (pDist > 1000) {
            dist = pDist / 1000;
            ;
        } else {
            dist = pDist;
        }

        ((TextView) view).setText(String.format("%.2f", dist));
    }

    @androidx.databinding.BindingAdapter("displayDistanceUnit")
    public static void displayDistanceUnit(View view, double pDist) {
        StringBuilder sb = new StringBuilder();
        if (pDist > 1000) {
            sb.append("km");
        } else {
            sb.append("m");
        }

        ((TextView) view).setText(sb);
    }

    @androidx.databinding.BindingAdapter("displayKcal")
    public static void displayKcal(View view, double pKcal) {
        long kcal = 0;
        if (pKcal > 1000) {
            kcal = Math.round(pKcal / 1000);
        } else {
            kcal = Math.round(pKcal);
        }

        ((TextView) view).setText(kcal + "");
    }
}
