package com.hyperether.getgoing.ui.formatter;

import android.annotation.SuppressLint;

import com.dinuscxj.progressbar.CircleProgressBar;

public class TimeProgressFormatterInvisible implements CircleProgressBar.ProgressFormatter {
    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {
        return "";
    }
}