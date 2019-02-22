package com.arixo.arixoglass.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.utils.SystemParams;
import com.arixo.bluetooth.library.connection.BluetoothServiceConnection;

/**
 * Created by lovart on 2019/2/13
 */
public class ClearCacheDialog extends Dialog {

    public ClearCacheDialog(@NonNull Context context) {
        super(context, R.style.Setting_Dialog_Msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clear_cache_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        Button cancelButton = findViewById(R.id.b_cancel_button);
        Button confirmButton = findViewById(R.id.b_confirm_button);
        cancelButton.setOnClickListener(view -> dismiss());
        confirmButton.setOnClickListener(view -> {
            SystemParams.getInstance().clear();
            BluetoothServiceConnection.getInstance().clearCache();
            dismiss();
        });
    }
}
