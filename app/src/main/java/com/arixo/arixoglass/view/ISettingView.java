package com.arixo.arixoglass.view;

import android.content.Context;

import com.arixo.arixoglass.base.View;

/**
 * Created by lovart on 2019/1/29
 */
public interface ISettingView extends View {

    void showSelectionDialog();

    void dismissSelectionDialog();

    void showConnectionDialog();

    void dismissConnectionDialog();

    void updateBluetoothItemView();

    Context getContext();

    void setOSDSwitchStatus(boolean isOSD);

    void setLCDSwitchStatus(boolean isOpen);

    void showBurstShotSettingBox();

    void hideBurstShotSettingBox();

    void showLCDBrightnessSettingBox();

    void hideLCDBrightnessSettingBox();

    void setLCDLevelChecked(int level);

}
