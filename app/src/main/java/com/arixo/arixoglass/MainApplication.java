package com.arixo.arixoglass;

import android.app.Application;
import android.content.Context;

import com.arixo.arixoglass.utils.CrashHandler;

/**
 * Created by lovart on 2019/1/30
 */
public class MainApplication extends Application {

    private static MainApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler.getInstance().init(this);
    }

    public static Context getMyApplication() {
        return instance;
    }

}
