package com.arixo.arixoglass.model.impl;

import android.os.SystemClock;

import com.arixo.arixoglass.model.IMainModel;

import java.text.DecimalFormat;

/**
 * Created by lovart on 2019/1/24
 */
public class MainModelImpl implements IMainModel {

    @Override
    public String getTimeFrom(long startTime) {
        int time = (int) ((SystemClock.elapsedRealtime() - startTime) / 1000);
        String hh = new DecimalFormat("00").format(time / 3600);
        String mm = new DecimalFormat("00").format(time % 3600 / 60);
        String ss = new DecimalFormat("00").format(time % 60);
        return hh + ":" + mm + ":" + ss;
    }
}
