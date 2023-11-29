package com.hyperether.getgoing.ui.formatter;

import android.annotation.SuppressLint;

import com.hyperether.getgoing.ui.dynamicview.CircleProgressBar;

public final class TimeProgressFormatter implements CircleProgressBar.ProgressFormatter {
    private long mData;

    public TimeProgressFormatter(long pData) {
        mData = pData;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {
        if (mData < 60) {
            return String.valueOf(mData).concat(" sec");
        } else {
            return String.valueOf(mData / 60).concat(" min");
        }
    }
}
