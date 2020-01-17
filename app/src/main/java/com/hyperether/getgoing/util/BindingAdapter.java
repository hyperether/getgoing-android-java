package com.hyperether.getgoing.util;

import android.view.View;
import android.widget.TextView;

public class BindingAdapter {

    @androidx.databinding.BindingAdapter("goal")
    public static void displayGoalValue(View view, double pGoal) {
        StringBuilder goal = new StringBuilder();
        if (pGoal < 1000) {
            goal.append((int)pGoal)
                    .append(" m");
        } else {
            goal.append(pGoal/1000)
                    .append(" km");
        }

        ((TextView) view).setText(goal.toString());
    }
}
