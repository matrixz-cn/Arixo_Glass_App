package com.arixo.arixoglass.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.arixo.arixoglass.R;

/**
 * Created by lovart on 2019/1/28
 */
public class BluetoothConnectingDialog extends Dialog {

    public BluetoothConnectingDialog(@NonNull Context context, boolean inSetting) {
        super(context, inSetting ? R.style.Setting_Dialog_Msg : R.style.Dialog_Msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connecting_dialog);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setAttributes(layoutParams);
        }
    }

}
