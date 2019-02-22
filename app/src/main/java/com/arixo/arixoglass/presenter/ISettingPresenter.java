package com.arixo.arixoglass.presenter;

import com.arixo.arixoglass.base.Presenter;
import com.arixo.arixoglass.model.ISettingModel;
import com.arixo.arixoglass.view.ISettingView;

/**
 * Created by lovart on 2019/1/29
 */
public interface ISettingPresenter extends Presenter<ISettingModel, ISettingView> {

    void initDevice();

    void unInitDevice();

    void initBluetoothService();

    void unInitBluetoothService();

    void handleBluetoothSetting();

    void handleBurstShotSetting(boolean checked);

    void handleOSDSetting(boolean isOSD);

    void handleLCDSetting(boolean checked);

    boolean isOSD();

    boolean supportOSD();

    void setLCDBrightnessLevel(int level);

}
