package com.hyperether.getgoing.ui.adapter.formatter;

import android.annotation.SuppressLint;

import com.dinuscxj.progressbar.CircleProgressBar;

import java.text.DecimalFormat;

public final class MyProgressFormatter implements CircleProgressBar.ProgressFormatter {
    private double mData;
    private DecimalFormat df = new DecimalFormat("#.##");

    public MyProgressFormatter(double pData) { mData = pData / 1000; }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {
        return df.format(mData).concat("km");
    }
}
