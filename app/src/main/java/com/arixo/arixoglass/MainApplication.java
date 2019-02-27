package com.arixo.arixoglass;

import android.app.Application;

import com.arixo.arixoglass.utils.CrashHandler;

/**
 * Created by lovart on 2019/1/30
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }

}
