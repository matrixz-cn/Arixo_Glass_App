package com.arixo.arixoglass.model.impl;

import com.arixo.arixoglass.model.IMainModel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by lovart on 2019/1/24
 */
public class MainModelImpl implements IMainModel {

    @Override
    public String getTimeFrom(long startTime) {
        long diff = System.currentTimeMillis() - startTime;
        long hour = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, minutes % 60, seconds % 60);
    }
}
