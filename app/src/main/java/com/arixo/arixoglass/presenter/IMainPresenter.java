package com.arixo.arixoglass.presenter;

import com.arixo.arixoglass.base.Presenter;
import com.arixo.arixoglass.model.IMainModel;
import com.arixo.arixoglass.view.IMainView;

/**
 * Created by lovart on 2019/1/29
 */
public interface IMainPresenter extends Presenter<IMainModel, IMainView> {

    void initCameraService();

    void initBluetoothService();

    void unInitCameraService();

    void unInitBluetoothService();

    void handleRecording();

    void handleCapture();

    void handleLongClick();

    String getTime();

    void closeCamera();

    void openCamera();

    int[] getPreviewResolution();

    void setOffActivity(boolean offActivity);
}
