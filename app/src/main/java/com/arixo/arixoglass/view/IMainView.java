package com.arixo.arixoglass.view;

import android.content.Context;

import com.arixo.arixoglass.base.View;
import com.arixo.glasssdk.widget.AspectRatioSurfaceView;

/**
 * Created by lovart on 2019/1/24
 */
public interface IMainView extends View {

    AspectRatioSurfaceView getCameraVew();

    Context getContext();

    void showSelectionDialog();

    void dismissSelectionDialog();

    void showConnectionDialog();

    void dismissConnectionDialog();

    void setToRecord();

    void setToCapture();

    void updateBluetoothItemView();
}
