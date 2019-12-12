package com.hyperether.getgoing.ui.adapter.formatter;

import android.annotation.SuppressLint;

import com.dinuscxj.progressbar.CircleProgressBar;

public final class MyProgressFormatter2 implements CircleProgressBar.ProgressFormatter {
    private int mData;

    public MyProgressFormatter2(int pData) { mData = pData; }

    @SuppressLint("DefaultLocale")
    @Override
    public CharSequence format(int progress, int max) {
        return String.valueOf(mData);
    }
}
