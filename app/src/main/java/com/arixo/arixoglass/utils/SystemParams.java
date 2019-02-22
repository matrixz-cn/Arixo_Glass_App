package com.arixo.arixoglass.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Set;

public class SystemParams {

    private static SystemParams instance;
    private static SharedPreferences sSharedPreferences = null;

    private SystemParams() {
    }

    //在Application初始化
    public static void init(Context context, String spName) {
        sSharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public static SystemParams getInstance() {

        if (instance == null) {
            synchronized (SystemParams.class) {
                if (instance == null) {
                    instance = new SystemParams();
                }
            }
        }
        return instance;
    }

    /**
     * get
     **/
    public int getInt(String key) {
        return sSharedPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        return sSharedPreferences.getInt(key, defValue);
    }

    public float getFloat(String key) {
        return sSharedPreferences.getFloat(key, 0);
    }

    public float getFloat(String key, float defValue) {
        return sSharedPreferences.getFloat(key, defValue);
    }

    public long getLong(String key) {
        return sSharedPreferences.getLong(key, 0);
    }

    public long getLong(String key, long defValue) {
        return sSharedPreferences.getLong(key, defValue);
    }

    public String getString(String key) {
        return sSharedPreferences.getString(key, null);
    }

    public String getString(String key, String defValue) {
        return sSharedPreferences.getString(key, defValue);
    }

    public boolean getBoolean(String key) {
        return sSharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sSharedPreferences.getBoolean(key, defValue);
    }

    /**
     * set
     **/
    public void setInt(String key, int value) {
        Editor editor = sSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setFloat(String key, float value) {
        Editor editor = sSharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void setLong(String key, long value) {
        Editor editor = sSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void setString(String key, String value) {
        Editor editor = sSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setBoolean(String key, boolean value) {
        Editor editor = sSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setSetString(String key, Set<String> values) {
        Editor editor = sSharedPreferences.edit();
        editor.putStringSet(key, values);
        editor.apply();
    }

    public void remove(String key) {
        Editor editor = sSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        Editor editor = sSharedPreferences.edit();
        editor.clear().apply();
    }
}
