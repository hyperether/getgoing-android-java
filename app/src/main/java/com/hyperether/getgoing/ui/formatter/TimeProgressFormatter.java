package com.hyperether.getgoing.ui.formatter;

import android.annotation.SuppressLint;

import com.dinuscxj.progressbar.CircleProgressBar;

    /*
     mData time in ms
     return time in minutes
     */

public final class TimeProgressFormatter implements CircleProgressBar.ProgressFormatter {

    private long mData;

    public TimeProgressFormatter(long pData) {
        mData = pData;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {

        return String.valueOf(mData / 60000);
    }
}
