package com.hyperether.getgoing.ui.formatter;

import android.annotation.SuppressLint;

import com.hyperether.getgoing.ui.dynamicview.CircleProgressBar;

import java.text.DecimalFormat;

public final class DistanceProgressFormatter implements CircleProgressBar.ProgressFormatter {
    private double mData;
    private DecimalFormat df = new DecimalFormat("#.##");

    public DistanceProgressFormatter(double pData) {
        mData = pData;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {
        if (mData > 1000) {
            return df.format(mData / 1000).concat(" km");
        } else {
            return df.format((int) mData).concat(" m");
        }
    }
}
