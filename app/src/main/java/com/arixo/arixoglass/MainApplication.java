package com.arixo.arixoglass;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
